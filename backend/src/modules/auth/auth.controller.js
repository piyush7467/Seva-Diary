const { validationResult } = require('express-validator');
const authService = require('./auth.service');

class AuthController {
  async register(req, res, next) {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          success: false,
          error: { code: 'VALIDATION_ERROR', message: errors.array()[0].msg, details: errors.array() }
        });
      }

      const result = await authService.register(req.body);
      res.status(201).json({
        success: true,
        data: result
      });
    } catch (err) {
      next(err);
    }
  }

  async login(req, res, next) {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          success: false,
          error: { code: 'VALIDATION_ERROR', message: errors.array()[0].msg }
        });
      }

      const { usernameOrEmail, password } = req.body;
      const userAgent = req.headers['user-agent'] || '';
      const ipAddress = req.ip || req.connection.remoteAddress || '';

      const result = await authService.login({
        usernameOrEmail,
        password,
        userAgent,
        ipAddress
      });

      res.status(200).json({
        success: true,
        data: result
      });
    } catch (err) {
      next(err);
    }
  }

  async refresh(req, res, next) {
    try {
      const { refreshToken } = req.body;
      const userAgent = req.headers['user-agent'] || '';
      const ipAddress = req.ip || req.connection.remoteAddress || '';

      const result = await authService.refresh({
        refreshToken,
        userAgent,
        ipAddress
      });

      res.status(200).json({
        success: true,
        data: result
      });
    } catch (err) {
      next(err);
    }
  }

  async logout(req, res, next) {
    try {
      const { refreshToken } = req.body;
      await authService.logout({ refreshToken });
      res.status(200).json({
        success: true,
        data: { message: 'Logged out successfully.' }
      });
    } catch (err) {
      next(err);
    }
  }

  async forgotPassword(req, res, next) {
    try {
      const { email } = req.body;
      const result = await authService.forgotPassword({ email });
      res.status(200).json({
        success: true,
        data: result
      });
    } catch (err) {
      next(err);
    }
  }

  async resetPassword(req, res, next) {
    try {
      const { token, newPassword } = req.body;
      const result = await authService.resetPassword({ token, newPassword });
      res.status(200).json({
        success: true,
        data: result
      });
    } catch (err) {
      next(err);
    }
  }

  async requestChangePasswordOtp(req, res, next) {
    try {
      const result = await authService.requestChangePasswordOtp({ userId: req.user.userId });
      res.status(200).json({
        success: true,
        data: result
      });
    } catch (err) {
      next(err);
    }
  }

  async verifyChangePasswordOtp(req, res, next) {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          success: false,
          error: { code: 'VALIDATION_ERROR', message: errors.array()[0].msg }
        });
      }

      const { otp, newPassword } = req.body;
      const result = await authService.verifyChangePasswordOtp({
        userId: req.user.userId,
        otp,
        newPassword
      });

      res.status(200).json({
        success: true,
        data: result
      });
    } catch (err) {
      next(err);
    }
  }
}

module.exports = new AuthController();
