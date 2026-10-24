const mongoose = require('mongoose');

const AppUpdateSchema = new mongoose.Schema({
  versionCode: { type: Number, required: true },
  versionName: { type: String, required: true, trim: true },
  title: { type: String, default: 'New Update Available', trim: true },
  releaseNotes: { type: String, default: '', trim: true },
  downloadUrl: { type: String, required: true, trim: true },
  isForceUpdate: { type: Boolean, default: false }
}, { timestamps: true });

const AppUpdate = mongoose.models.AppUpdate || mongoose.model('AppUpdate', AppUpdateSchema);

module.exports = AppUpdate;
