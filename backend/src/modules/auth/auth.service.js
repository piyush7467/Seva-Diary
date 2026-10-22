const { v4: uuidv4 } = require('uuid') || { v4: () => require('crypto').randomUUID() };
const crypto = require('crypto');
const { User, RefreshSession, PasswordResetToken } = require('./auth.model');
const {
  hashPassword,
  verifyPassword,
  generateRandomToken,
  hashToken,
  signAccessToken
} = require('../../common/utils/token.utils');
const brevoService = require('../../integrations/email/brevo.service');
const config = require('../../config');

class AuthService {
  async register({
    firstName,
    lastName = '',
    gender = 'MALE',
    displayName,
    username,
    email,
    phone = '',
    address = {},
    mantraStage = 'Pratham Nam',
    mantraDetails = '',
    password
  }) {
    const normalizedUsername = username.trim().toLowerCase();
    const normalizedEmail = email.trim().toLowerCase();

    // Check uniqueness
    const existingUser = await User.findOne({
      $or: [
        { normalizedUsername },
        { normalizedEmail }
      ]
    });

    if (existingUser) {
      if (existingUser.normalizedUsername === normalizedUsername) {
        const error = new Error('Username is already taken.');
        error.statusCode = 409;
        error.code = 'USERNAME_EXISTS';
        throw error;
      }
      if (existingUser.normalizedEmail === normalizedEmail) {
        const error = new Error('An account with this email already exists.');
        error.statusCode = 409;
        error.code = 'EMAIL_EXISTS';
        throw error;
      }
    }

    const passwordHash = await hashPassword(password);
    const resolvedDisplayName = displayName?.trim() || `${firstName.trim()} ${gender === 'FEMALE' ? 'Dasi' : 'Das'}`;

    const user = await User.create({
      firstName: firstName.trim(),
      lastName: lastName.trim(),
      gender,
      displayName: resolvedDisplayName,
      normalizedUsername,
      normalizedEmail,
      phone: phone.trim(),
      address: {
        addressLine: address.addressLine || '',
        cityVillage: address.cityVillage || '',
        tehsil: address.tehsil || '',
        district: address.district || '',
        state: address.state || '',
        pinCode: address.pinCode || ''
      },
      mantraStage: mantraStage || 'Pratham Nam',
      mantraDetails: mantraDetails || '',
      passwordHash,
      isEmailVerified: !config.enableEmailVerification
    });

    return {
      userId: user._id.toString(),
      displayName: user.displayName,
      username: user.normalizedUsername,
      email: user.normalizedEmail,
      mantraStage: user.mantraStage,
      isEmailVerified: user.isEmailVerified
    };
  }

  async login({ usernameOrEmail, password, userAgent = '', ipAddress = '' }) {
    const query = usernameOrEmail.trim().toLowerCase();
    const user = await User.findOne({
      $or: [
        { normalizedUsername: query },
        { normalizedEmail: query }
      ]
    });

    if (!user) {
      const error = new Error('Invalid username/email or password.');
      error.statusCode = 401;
      error.code = 'INVALID_CREDENTIALS';
      throw error;
    }

    const isMatch = await verifyPassword(password, user.passwordHash);
    if (!isMatch) {
      const error = new Error('Invalid username/email or password.');
      error.statusCode = 401;
      error.code = 'INVALID_CREDENTIALS';
      throw error;
    }

    // Generate tokens
    const accessToken = signAccessToken({
      sub: user._id.toString(),
      username: user.normalizedUsername,
      displayName: user.displayName
    });

    const rawRefreshToken = generateRandomToken(40);
    const tokenHash = hashToken(rawRefreshToken);
    const tokenFamily = crypto.randomUUID ? crypto.randomUUID() : generateRandomToken(16);
    const expiresAt = new Date(Date.now() + config.jwt.refreshExpiresInDays * 24 * 60 * 60 * 1000);

    await RefreshSession.create({
      userId: user._id,
      tokenHash,
      tokenFamily,
      expiresAt,
      userAgent,
      ipAddress
    });

    return {
      accessToken,
      refreshToken: rawRefreshToken,
      user: {
        userId: user._id.toString(),
        displayName: user.displayName,
        username: user.normalizedUsername,
        email: user.normalizedEmail,
        preferences: user.preferences
      }
    };
  }

  async refresh({ refreshToken, userAgent = '', ipAddress = '' }) {
    if (!refreshToken) {
      const error = new Error('Refresh token is required.');
      error.statusCode = 400;
      error.code = 'MISSING_TOKEN';
      throw error;
    }

    const incomingHash = hashToken(refreshToken);
    const session = await RefreshSession.findOne({ tokenHash: incomingHash });

    if (!session) {
      const error = new Error('Invalid refresh token.');
      error.statusCode = 401;
      error.code = 'INVALID_TOKEN';
      throw error;
    }

    // Reuse detection
    if (session.revokedAt) {
      // Token reuse detected! Invalidate entire token family immediately
      await RefreshSession.updateMany(
        { tokenFamily: session.tokenFamily },
        { revokedAt: new Date() }
      );
      const error = new Error('Session compromised. Token reuse detected. Please log in again.');
      error.statusCode = 401;
      error.code = 'TOKEN_REUSE_DETECTED';
      throw error;
    }

    if (new Date() > session.expiresAt) {
      const error = new Error('Refresh token has expired.');
      error.statusCode = 401;
      error.code = 'TOKEN_EXPIRED';
      throw error;
    }

    // Revoke old token
    session.revokedAt = new Date();
    await session.save();

    // Look up user
    const user = await User.findById(session.userId);
    if (!user) {
      const error = new Error('User account not found.');
      error.statusCode = 404;
      throw error;
    }

    // Issue rotated tokens within the same family
    const newAccessToken = signAccessToken({
      sub: user._id.toString(),
      username: user.normalizedUsername,
      displayName: user.displayName
    });

    const newRawRefreshToken = generateRandomToken(40);
    const newTokenHash = hashToken(newRawRefreshToken);
    const expiresAt = new Date(Date.now() + config.jwt.refreshExpiresInDays * 24 * 60 * 60 * 1000);

    await RefreshSession.create({
      userId: user._id,
      tokenHash: newTokenHash,
      tokenFamily: session.tokenFamily,
      expiresAt,
      userAgent,
      ipAddress
    });

    return {
      accessToken: newAccessToken,
      refreshToken: newRawRefreshToken
    };
  }

  async logout({ refreshToken }) {
    if (refreshToken) {
      const tokenHash = hashToken(refreshToken);
      await RefreshSession.updateOne(
        { tokenHash },
        { revokedAt: new Date() }
      );
    }
    return { success: true };
  }

  async forgotPassword({ email }) {
    const normalizedEmail = email?.trim().toLowerCase();
    if (!normalizedEmail) return { success: true };

    const user = await User.findOne({ normalizedEmail });
    if (user) {
      const rawToken = generateRandomToken(32);
      const tokenHash = hashToken(rawToken);
      const expiresAt = new Date(Date.now() + 60 * 60 * 1000); // 1 hour

      await PasswordResetToken.create({
        userId: user._id,
        tokenHash,
        expiresAt
      });

      // Send transactional reset email via Brevo
      try {
        await brevoService.sendPasswordResetEmail({
          toEmail: user.normalizedEmail,
          displayName: user.displayName,
          resetToken: rawToken
        });
      } catch (err) {
        console.error('[AUTH] Brevo delivery failure:', err.message);
      }
    }

    // Generic response to prevent account enumeration
    return {
      success: true,
      message: 'If the email is registered, a password recovery link has been sent.'
    };
  }

  async resetPassword({ token, newPassword }) {
    if (!token || !newPassword || newPassword.length < 6) {
      const error = new Error('Valid token and password (min 6 characters) are required.');
      error.statusCode = 400;
      throw error;
    }

    const tokenHash = hashToken(token);
    const resetEntry = await PasswordResetToken.findOne({
      tokenHash,
      usedAt: null,
      expiresAt: { $gt: new Date() }
    });

    if (!resetEntry) {
      const error = new Error('Reset link is invalid or has expired.');
      error.statusCode = 400;
      error.code = 'INVALID_RESET_TOKEN';
      throw error;
    }

    const user = await User.findById(resetEntry.userId);
    if (!user) {
      const error = new Error('User not found.');
      error.statusCode = 404;
      throw error;
    }

    // Update password
    user.passwordHash = await hashPassword(newPassword);
    await user.save();

    // Mark token as used
    resetEntry.usedAt = new Date();
    await resetEntry.save();

    // Invalidate all refresh sessions for this user for security
    await RefreshSession.updateMany(
      { userId: user._id, revokedAt: null },
      { revokedAt: new Date() }
    );

    return { success: true, message: 'Password has been successfully updated. Please log in.' };
  }
}

module.exports = new AuthService();
