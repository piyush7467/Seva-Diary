const sevaService = require('./seva.service');

class SevaController {
  async create(req, res, next) {
    try {
      const record = await sevaService.createRecord(req.user.userId, req.body);
      res.status(201).json({
        success: true,
        data: record
      });
    } catch (err) {
      next(err);
    }
  }

  async getAll(req, res, next) {
    try {
      const result = await sevaService.getRecords(req.user.userId, req.query);
      res.status(200).json({
        success: true,
        data: result.records,
        pagination: result.pagination
      });
    } catch (err) {
      next(err);
    }
  }

  async getById(req, res, next) {
    try {
      const record = await sevaService.getRecordById(req.user.userId, req.params.id);
      res.status(200).json({
        success: true,
        data: record
      });
    } catch (err) {
      next(err);
    }
  }

  async update(req, res, next) {
    try {
      const record = await sevaService.updateRecord(req.user.userId, req.params.id, req.body);
      res.status(200).json({
        success: true,
        data: record
      });
    } catch (err) {
      next(err);
    }
  }

  async delete(req, res, next) {
    try {
      await sevaService.deleteRecord(req.user.userId, req.params.id);
      res.status(200).json({
        success: true,
        data: { message: 'Seva record deleted successfully.' }
      });
    } catch (err) {
      next(err);
    }
  }
}

module.exports = new SevaController();
