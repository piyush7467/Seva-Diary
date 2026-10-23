const mongoose = require('mongoose');

const ChantingEntrySchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  localDate: { type: String, required: true }, // Format: YYYY-MM-DD
  count: { type: Number, required: true, min: 0, default: 0 },
  target: { type: Number, default: 108 }
}, { timestamps: true });

ChantingEntrySchema.index({ userId: 1, localDate: 1 }, { unique: true });
ChantingEntrySchema.index({ userId: 1, localDate: -1 });

const ChantingEntry = mongoose.models.ChantingEntry || mongoose.model('ChantingEntry', ChantingEntrySchema);

module.exports = { ChantingEntry };
