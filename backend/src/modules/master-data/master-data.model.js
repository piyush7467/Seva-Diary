const mongoose = require('mongoose');

const MasterDataSchema = new mongoose.Schema({
  code: { type: String, required: true, unique: true },
  category: { type: String, required: true, enum: ['ASHRAM', 'PURPOSE', 'SEVA_TYPE'] },
  labels: {
    en: { type: String, required: true },
    hi: { type: String, required: true }
  },
  location: {
    city: { type: String, default: '' },
    state: { type: String, default: '' }
  },
  order: { type: Number, default: 0 },
  isActive: { type: Boolean, default: true }
}, { timestamps: true });

MasterDataSchema.index({ category: 1, isActive: 1, order: 1 });

const MasterData = mongoose.models.MasterData || mongoose.model('MasterData', MasterDataSchema);

module.exports = { MasterData };
