const { User } = require('../auth/auth.model');

class UserController {
  async getProfile(req, res, next) {
    try {
      const user = await User.findById(req.user.userId).select('-passwordHash');
      if (!user) {
        return res.status(404).json({
          success: false,
          error: { code: 'USER_NOT_FOUND', message: 'User not found.' }
        });
      }

      res.status(200).json({
        success: true,
        data: {
          id: user._id.toString(),
          userId: user._id.toString(),
          firstName: user.firstName,
          lastName: user.lastName,
          gender: user.gender,
          displayName: user.displayName,
          username: user.normalizedUsername,
          email: user.normalizedEmail,
          phone: user.phone,
          address: user.address,
          mantraStage: user.mantraStage || 'Pratham Nam',
          mantraDetails: user.mantraDetails,
          preferences: user.preferences,
          createdAt: user.createdAt
        }
      });
    } catch (err) {
      next(err);
    }
  }

  async updateProfile(req, res, next) {
    try {
      const allowedUpdates = [
        'firstName',
        'lastName',
        'gender',
        'displayName',
        'phone',
        'address',
        'mantraStage',
        'mantraDetails',
        'preferences'
      ];

      const updates = {};
      for (const key of allowedUpdates) {
        if (req.body[key] !== undefined) {
          updates[key] = req.body[key];
        }
      }

      // If displayName not explicitly supplied but firstName is updated, sync displayName
      if (updates.firstName && !updates.displayName) {
        updates.displayName = `${updates.firstName} ${updates.lastName || ''}`.trim();
      }

      const user = await User.findByIdAndUpdate(
        req.user.userId,
        { $set: updates },
        { new: true, runValidators: true }
      ).select('-passwordHash');

      if (!user) {
        return res.status(404).json({
          success: false,
          error: { code: 'USER_NOT_FOUND', message: 'User not found.' }
        });
      }

      res.status(200).json({
        success: true,
        data: {
          id: user._id.toString(),
          userId: user._id.toString(),
          firstName: user.firstName,
          lastName: user.lastName,
          gender: user.gender,
          displayName: user.displayName,
          username: user.normalizedUsername,
          email: user.normalizedEmail,
          phone: user.phone,
          address: user.address,
          mantraStage: user.mantraStage || 'Pratham Nam',
          mantraDetails: user.mantraDetails,
          preferences: user.preferences,
          updatedAt: user.updatedAt
        }
      });
    } catch (err) {
      next(err);
    }
  }
}

module.exports = new UserController();
