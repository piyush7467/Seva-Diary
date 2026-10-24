const AppUpdate = require('./app-update.model');

class AppUpdateController {
  async getLatestVersion(req, res, next) {
    try {
      // Find the most recently created or highest versionCode
      const latest = await AppUpdate.findOne().sort({ versionCode: -1, createdAt: -1 });

      if (!latest) {
        return res.status(200).json({
          success: true,
          data: {
            versionCode: 1,
            versionName: '1.0.0',
            title: 'Seva Diary is up to date',
            releaseNotes: 'Devotional diary for spiritual journey.',
            downloadUrl: 'https://github.com/piyush7467/Seva-Diary/releases/latest',
            isForceUpdate: false
          }
        });
      }

      res.status(200).json({
        success: true,
        data: {
          versionCode: latest.versionCode,
          versionName: latest.versionName,
          title: latest.title,
          releaseNotes: latest.releaseNotes,
          downloadUrl: latest.downloadUrl,
          isForceUpdate: latest.isForceUpdate,
          updatedAt: latest.updatedAt
        }
      });
    } catch (err) {
      next(err);
    }
  }

  async setLatestVersion(req, res, next) {
    try {
      const { versionCode, versionName, title, releaseNotes, downloadUrl, isForceUpdate } = req.body;

      if (!versionCode || !versionName || !downloadUrl) {
        return res.status(400).json({
          success: false,
          error: { code: 'VALIDATION_ERROR', message: 'versionCode, versionName, and downloadUrl are required.' }
        });
      }

      // Upsert by versionCode
      const updated = await AppUpdate.findOneAndUpdate(
        { versionCode: parseInt(versionCode, 10) },
        {
          $set: {
            versionCode: parseInt(versionCode, 10),
            versionName: versionName.trim(),
            title: title?.trim() || 'New Update Available',
            releaseNotes: releaseNotes?.trim() || '',
            downloadUrl: downloadUrl.trim(),
            isForceUpdate: Boolean(isForceUpdate)
          }
        },
        { upsert: true, new: true, setDefaultsOnInsert: true }
      );

      res.status(200).json({
        success: true,
        message: `Version ${versionName} (code ${versionCode}) published successfully!`,
        data: updated
      });
    } catch (err) {
      next(err);
    }
  }
}

module.exports = new AppUpdateController();
