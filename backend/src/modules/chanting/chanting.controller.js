const chantingService = require('./chanting.service');

class ChantingController {
  async updateDaily(req, res, next) {
    try {
      const { date } = req.params;
      const { count, increment, target } = req.body;
      const userId = req.user.userId;

      const result = await chantingService.setOrIncrementCount({
        userId,
        date,
        count,
        increment,
        target
      });

      res.status(200).json({
        success: true,
        data: result
      });
    } catch (err) {
      next(err);
    }
  }

  async getEntries(req, res, next) {
    try {
      const { startDate, endDate } = req.query;
      const userId = req.user.userId;

      const result = await chantingService.getEntriesByRange({
        userId,
        startDate,
        endDate
      });

      res.status(200).json({
        success: true,
        data: result
      });
    } catch (err) {
      next(err);
    }
  }

  async getSummary(req, res, next) {
    try {
      const userId = req.user.userId;
      const result = await chantingService.getSummary(userId);

      res.status(200).json({
        success: true,
        data: result
      });
    } catch (err) {
      next(err);
    }
  }
}

module.exports = new ChantingController();
