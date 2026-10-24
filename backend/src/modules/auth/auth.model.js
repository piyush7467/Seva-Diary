const mongoose = require('mongoose');

const UserSchema = new mongoose.Schema({
  firstName: { type: String, required: true, trim: true },
  lastName: { type: String, default: '', trim: true },
  gender: { type: String, enum: ['MALE', 'FEMALE', 'OTHER'], default: 'MALE' },
  displayName: { type: String, required: true, trim: true },
  normalizedUsername: { type: String, required: true, unique: true, lowercase: true, trim: true, minlength: 3, maxlength: 20 },
  normalizedEmail: { type: String, required: true, unique: true, lowercase: true, trim: true },
  phone: { type: String, default: '', trim: true },
  address: {
    addressLine: { type: String, default: '' },
    cityVillage: { type: String, default: '' },
    tehsil: { type: String, default: '' },
    district: { type: String, default: '' },
    state: { type: String, default: '' },
    pinCode: { type: String, default: '' }
  },
  mantraStage: { type: String, default: 'Pratham Nam' },
  mantraDetails: { type: String, default: '' },
  passwordHash: { type: String, required: true },
  isEmailVerified: { type: Boolean, default: false },
  preferences: {
    theme: { type: String, enum: ['system', 'light', 'dark'], default: 'system' },
    language: { type: String, enum: ['en', 'hi'], default: 'en' }
  }
}, { timestamps: true });

const RefreshSessionSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  tokenHash: { type: String, required: true, unique: true },
  tokenFamily: { type: String, required: true, index: true },
  expiresAt: { type: Date, required: true, index: { expires: 0 } },
  revokedAt: { type: Date, default: null },
  userAgent: { type: String, default: '' },
  ipAddress: { type: String, default: '' }
}, { timestamps: true });

RefreshSessionSchema.index({ userId: 1, tokenFamily: 1 });

const PasswordResetTokenSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  tokenHash: { type: String, required: true, unique: true },
  expiresAt: { type: Date, required: true, index: { expires: 0 } },
  usedAt: { type: Date, default: null }
}, { timestamps: true });

const ChangePasswordOtpSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  otpHash: { type: String, required: true },
  expiresAt: { type: Date, required: true, index: { expires: 0 } },
  usedAt: { type: Date, default: null }
}, { timestamps: true });

const User = mongoose.models.User || mongoose.model('User', UserSchema);
const RefreshSession = mongoose.models.RefreshSession || mongoose.model('RefreshSession', RefreshSessionSchema);
const PasswordResetToken = mongoose.models.PasswordResetToken || mongoose.model('PasswordResetToken', PasswordResetTokenSchema);
const ChangePasswordOtp = mongoose.models.ChangePasswordOtp || mongoose.model('ChangePasswordOtp', ChangePasswordOtpSchema);

module.exports = { User, RefreshSession, PasswordResetToken, ChangePasswordOtp };
