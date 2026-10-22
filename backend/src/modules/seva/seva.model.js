const mongoose = require('mongoose');

const SevaRecordSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  ashramCode: { type: String, required: true },
  customAshramName: { type: String, default: '', trim: true },
  startDate: { type: String, required: true }, // Format: YYYY-MM-DD
  endDate: { type: String, required: true },   // Format: YYYY-MM-DD
  durationDays: { type: Number, required: true, min: 1 },
  purpose: { type: String, required: true },
  sevaType: { type: String, default: '' },
  customPurposeOrSeva: { type: String, default: '', trim: true },
  notes: { type: String, default: '', trim: true },
  locationDetails: { type: String, default: '', trim: true }
}, { timestamps: true });

SevaRecordSchema.index({ userId: 1, startDate: -1 });
SevaRecordSchema.index({ userId: 1, ashramCode: 1 });
SevaRecordSchema.index({ userId: 1, purpose: 1 });

const SevaRecord = mongoose.models.SevaRecord || mongoose.model('SevaRecord', SevaRecordSchema);

module.exports = { SevaRecord };
