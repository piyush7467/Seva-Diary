require('dotenv').config();

module.exports = {
  env: process.env.NODE_ENV || 'development',
  port: parseInt(process.env.PORT, 10) || 5000,
  mongodbUri: process.env.MONGODB_URI || 'mongodb://localhost:27017/seva_diary',
  jwt: {
    accessSecret: process.env.JWT_ACCESS_SECRET || 'dev_jwt_access_secret_key_change_in_prod_123',
    refreshSecret: process.env.JWT_REFRESH_SECRET || 'dev_jwt_refresh_secret_key_change_in_prod_456',
    accessExpiresIn: process.env.JWT_ACCESS_EXPIRES_IN || '15m',
    refreshExpiresInDays: 30
  },
  brevo: {
    apiKey: process.env.BREVO_API_KEY || '',
    senderName: process.env.BREVO_SENDER_NAME || 'Seva Diary',
    senderEmail: process.env.BREVO_SENDER_EMAIL || 'no-reply@sevadiary.app'
  },
  resetPasswordUrl: process.env.APP_RESET_PASSWORD_URL || 'https://sevadiary.app/reset-password',
  enableEmailVerification: process.env.ENABLE_EMAIL_VERIFICATION === 'true',
  allowedOrigins: process.env.ALLOWED_ORIGINS ? process.env.ALLOWED_ORIGINS.split(',') : ['http://localhost:3000', 'https://sevadiary.app']
};
