const express = require('express');
const { body } = require('express-validator');
const authController = require('./auth.controller');
const { authLimiter, passwordResetLimiter } = require('../../common/middleware/rate-limiter.middleware');

const router = express.Router();

router.post(
  '/register',
  authLimiter,
  [
    body('firstName').trim().notEmpty().withMessage('First name is required.'),
    body('username')
      .trim()
      .isLength({ min: 3, max: 20 })
      .withMessage('Username must be between 3 and 20 characters.')
      .matches(/^[a-zA-Z0-9]+$/)
      .withMessage('Username must contain only English letters and digits.'),
    body('email').trim().isEmail().withMessage('Valid email address is required.'),
    body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters.')
  ],
  authController.register
);

router.post(
  '/login',
  authLimiter,
  [
    body('usernameOrEmail').trim().notEmpty().withMessage('Username or email is required.'),
    body('password').notEmpty().withMessage('Password is required.')
  ],
  authController.login
);

router.post(
  '/refresh',
  [
    body('refreshToken').notEmpty().withMessage('Refresh token is required.')
  ],
  authController.refresh
);

router.post('/logout', authController.logout);

router.post(
  '/forgot-password',
  passwordResetLimiter,
  [
    body('email').trim().isEmail().withMessage('A valid email address is required.')
  ],
  authController.forgotPassword
);

router.post(
  '/reset-password',
  [
    body('token').notEmpty().withMessage('Reset token is required.'),
    body('newPassword').isLength({ min: 6 }).withMessage('New password must be at least 6 characters.')
  ],
  authController.resetPassword
);

module.exports = router;
