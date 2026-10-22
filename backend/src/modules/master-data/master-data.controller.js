const masterDataService = require('./master-data.service');

class MasterDataController {
  async getMasterData(req, res, next) {
    try {
      const data = await masterDataService.getAllMasterData();
      res.status(200).json({
        success: true,
        data
      });
    } catch (err) {
      next(err);
    }
  }
}

module.exports = new MasterDataController();
