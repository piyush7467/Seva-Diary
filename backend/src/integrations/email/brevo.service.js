const brevo = require('@getbrevo/brevo');
const config = require('../../config');

class BrevoEmailService {
  getApiInstance() {
    const apiInstance = new brevo.TransactionalEmailsApi();
    const apiKey = config.brevo.apiKey;
    if (apiKey) {
      if (typeof apiInstance.setApiKey === 'function' && brevo.TransactionalEmailsApiApiKeys) {
        apiInstance.setApiKey(brevo.TransactionalEmailsApiApiKeys.apiKey, apiKey);
      }
      if (apiInstance.authentications && apiInstance.authentications['apiKey']) {
        apiInstance.authentications['apiKey'].apiKey = apiKey;
      }
    }
    return apiInstance;
  }

  async verifyConnection() {
    const apiKey = config.brevo.apiKey;
    if (!apiKey) {
      return { success: false, message: 'Brevo API key is not configured in .env' };
    }
    try {
      const accountApi = new brevo.AccountApi();
      if (typeof accountApi.setApiKey === 'function' && brevo.AccountApiApiKeys) {
        accountApi.setApiKey(brevo.AccountApiApiKeys.apiKey, apiKey);
      }
      if (accountApi.authentications && accountApi.authentications['apiKey']) {
        accountApi.authentications['apiKey'].apiKey = apiKey;
      }
      const accountData = await accountApi.getAccount();
      return {
        success: true,
        email: accountData.body?.email,
        companyName: accountData.body?.companyName,
        plan: accountData.body?.plan
      };
    } catch (err) {
      return {
        success: false,
        error: err.response?.body?.message || err.message
      };
    }
  }

  async sendPasswordResetEmail({ toEmail, displayName, resetToken }) {
    const apiKey = config.brevo.apiKey;
    if (!apiKey) {
      console.warn('[EMAIL] Brevo API key is not configured in .env. Skipping email dispatch in dev mode.');
      return { success: true, simulated: true };
    }

    const resetUrl = `${config.resetPasswordUrl}?token=${resetToken}`;
    const apiInstance = this.getApiInstance();

    const sendSmtpEmail = new brevo.SendSmtpEmail();
    sendSmtpEmail.subject = 'Seva Diary — Password Recovery Request';
    sendSmtpEmail.sender = {
      name: config.brevo.senderName,
      email: config.brevo.senderEmail
    };
    sendSmtpEmail.to = [{ email: toEmail, name: displayName || 'Devotee' }];
    sendSmtpEmail.htmlContent = `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #E3DED4; border-radius: 12px; background-color: #FDFBF7;">
        <h2 style="color: #1E4D2B; margin-bottom: 8px;">Sat Sahib ${displayName || 'Devotee'},</h2>
        <p style="color: #1F2421; font-size: 15px; line-height: 1.5;">
          We received a request to reset your password for your Seva Diary account.
        </p>
        <p style="margin: 24px 0;">
          <a href="${resetUrl}" style="background-color: #1E4D2B; color: #FFFFFF; padding: 12px 24px; text-decoration: none; border-radius: 8px; font-weight: bold; display: inline-block;">
            Reset Your Password
          </a>
        </p>
        <p style="color: #5C645E; font-size: 13px;">
          This link will expire in 1 hour. If you did not request this, please disregard this email.
        </p>
        <hr style="border: none; border-top: 1px solid #E3DED4; margin: 24px 0;" />
        <p style="color: #5C645E; font-size: 12px; text-align: center;">
          Seva Diary — Personal Devotional Diary & Naam Jaap Tracker
        </p>
      </div>
    `;

    try {
      const response = await apiInstance.sendTransacEmail(sendSmtpEmail);
      console.log(`[EMAIL] Password reset email queued for recipient successfully.`);
      return { success: true, messageId: response.body?.messageId };
    } catch (error) {
      console.error('[EMAIL] Failed to send transactional email via Brevo:', error.message);
      // Return safe failure without leaking API details
      throw new Error('Email delivery failed. Please try again later.');
    }
  }

  async sendChangePasswordOtpEmail({ toEmail, displayName, otpCode }) {
    const apiKey = config.brevo.apiKey;
    if (!apiKey) {
      console.warn(`[EMAIL] Brevo API key is not configured in .env. OTP for ${toEmail}: ${otpCode}`);
      return { success: true, simulated: true, otp: otpCode };
    }

    const apiInstance = this.getApiInstance();
    const sendSmtpEmail = new brevo.SendSmtpEmail();
    sendSmtpEmail.subject = `Seva Diary — Your Password Change Verification OTP is ${otpCode}`;
    sendSmtpEmail.sender = {
      name: config.brevo.senderName,
      email: config.brevo.senderEmail
    };
    sendSmtpEmail.to = [{ email: toEmail, name: displayName || 'Devotee' }];
    sendSmtpEmail.htmlContent = `
      <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 580px; margin: 0 auto; padding: 28px; border: 1px solid #E5DFD3; border-radius: 16px; background-color: #FAF8F2;">
        <div style="text-align: center; margin-bottom: 20px;">
          <h2 style="color: #174C38; margin: 0; font-size: 24px; font-weight: bold;">Seva Diary</h2>
          <p style="color: #D99A32; font-size: 14px; margin-top: 4px; font-weight: 600;">Sat Saheb</p>
        </div>
        <p style="color: #252B27; font-size: 16px; line-height: 1.5;">
          Hello <strong>${displayName || 'Devotee'}</strong>,
        </p>
        <p style="color: #646B65; font-size: 14px; line-height: 1.6;">
          You requested to change your password for your Seva Diary account. Please use the following 6-digit OTP code to verify your request:
        </p>
        <div style="text-align: center; margin: 28px 0;">
          <div style="display: inline-block; background-color: #174C38; color: #FFFFFF; font-size: 32px; font-weight: bold; letter-spacing: 8px; padding: 14px 32px; border-radius: 12px; box-shadow: 0 4px 12px rgba(23, 76, 56, 0.18);">
            ${otpCode}
          </div>
        </div>
        <p style="color: #8E9690; font-size: 13px; text-align: center;">
          This OTP code is valid for <strong>10 minutes</strong>. Never share this code with anyone.
        </p>
        <hr style="border: none; border-top: 1px solid #E5DFD3; margin: 24px 0;" />
        <p style="color: #8E9690; font-size: 12px; text-align: center; margin: 0;">
          If you did not make this request, please secure your account immediately.
        </p>
      </div>
    `;

    try {
      const response = await apiInstance.sendTransacEmail(sendSmtpEmail);
      console.log(`[EMAIL] Change password OTP queued successfully for ${toEmail}`);
      return { success: true, messageId: response.body?.messageId };
    } catch (error) {
      console.error('[EMAIL] Failed to send OTP email via Brevo:', error.message);
      throw new Error('Email delivery failed. Please try again later.');
    }
  }
  }
}

module.exports = new BrevoEmailService();
