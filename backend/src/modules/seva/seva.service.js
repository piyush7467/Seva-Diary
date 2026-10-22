const { SevaRecord } = require('./seva.model');

class SevaService {
  calculateDuration(startDateStr, endDateStr) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(startDateStr) || !/^\d{4}-\d{2}-\d{2}$/.test(endDateStr)) {
      const error = new Error("Dates must be in 'YYYY-MM-DD' format.");
      error.statusCode = 400;
      throw error;
    }

    const start = new Date(startDateStr);
    const end = new Date(endDateStr);

    if (end < start) {
      const error = new Error('End date cannot precede start date.');
      error.statusCode = 400;
      throw error;
    }

    // Inclusive duration calculation
    const diffTime = end.getTime() - start.getTime();
    const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24)) + 1;
    return diffDays;
  }

  async createRecord(userId, data) {
    const {
      ashramCode,
      customAshramName,
      startDate,
      endDate,
      purpose,
      sevaType,
      customPurposeOrSeva,
      notes,
      locationDetails
    } = data;

    if (!ashramCode) {
      const error = new Error('Ashram selection is required.');
      error.statusCode = 400;
      throw error;
    }

    if (ashramCode === 'OTHER_ASHRAM' && (!customAshramName || !customAshramName.trim())) {
      const error = new Error("Custom ashram name is required when 'Other' is selected.");
      error.statusCode = 400;
      throw error;
    }

    if (!purpose) {
      const error = new Error('Purpose of visit is required.');
      error.statusCode = 400;
      throw error;
    }

    // Conditionality rule: clean or validate sevaType
    let resolvedSevaType = '';
    if (purpose === 'SEVA') {
      resolvedSevaType = sevaType || '';
    }

    const durationDays = this.calculateDuration(startDate, endDate);

    const record = await SevaRecord.create({
      userId,
      ashramCode,
      customAshramName: ashramCode === 'OTHER_ASHRAM' ? customAshramName.trim() : '',
      startDate,
      endDate,
      durationDays,
      purpose,
      sevaType: resolvedSevaType,
      customPurposeOrSeva: customPurposeOrSeva?.trim() || '',
      notes: notes?.trim() || '',
      locationDetails: locationDetails?.trim() || ''
    });

    return record;
  }

  async getRecords(userId, query = {}) {
    const {
      ashramCode,
      purpose,
      sevaType,
      startDate,
      endDate,
      search,
      page = 1,
      limit = 20,
      sortBy = 'newest'
    } = query;

    const filter = { userId };

    if (ashramCode) {
      filter.ashramCode = ashramCode;
    }

    if (purpose) {
      filter.purpose = purpose;
    }

    if (sevaType) {
      filter.sevaType = sevaType;
    }

    // Overlapping interval filtering
    if (startDate && endDate) {
      filter.startDate = { $lte: endDate };
      filter.endDate = { $gte: startDate };
    } else if (startDate) {
      filter.endDate = { $gte: startDate };
    } else if (endDate) {
      filter.startDate = { $lte: endDate };
    }

    if (search && search.trim()) {
      const regex = new RegExp(search.trim(), 'i');
      filter.$or = [
        { customAshramName: regex },
        { notes: regex },
        { locationDetails: regex },
        { customPurposeOrSeva: regex }
      ];
    }

    const sortOrder = sortBy === 'oldest' ? { startDate: 1, _id: 1 } : { startDate: -1, _id: -1 };
    const skip = (Math.max(1, parseInt(page, 10)) - 1) * parseInt(limit, 10);
    const parsedLimit = Math.min(100, Math.max(1, parseInt(limit, 10)));

    const [records, total] = await Promise.all([
      SevaRecord.find(filter)
        .sort(sortOrder)
        .skip(skip)
        .limit(parsedLimit)
        .lean(),
      SevaRecord.countDocuments(filter)
    ]);

    return {
      records: records.map(r => ({
        id: r._id.toString(),
        ashramCode: r.ashramCode,
        customAshramName: r.customAshramName,
        startDate: r.startDate,
        endDate: r.endDate,
        durationDays: r.durationDays,
        purpose: r.purpose,
        sevaType: r.sevaType,
        customPurposeOrSeva: r.customPurposeOrSeva,
        notes: r.notes,
        locationDetails: r.locationDetails,
        createdAt: r.createdAt
      })),
      pagination: {
        total,
        page: parseInt(page, 10),
        limit: parsedLimit,
        pages: Math.ceil(total / parsedLimit)
      }
    };
  }

  async getRecordById(userId, recordId) {
    const record = await SevaRecord.findOne({ _id: recordId, userId }).lean();
    if (!record) {
      const error = new Error('Seva record not found or unauthorized.');
      error.statusCode = 404;
      throw error;
    }
    return {
      id: record._id.toString(),
      ashramCode: record.ashramCode,
      customAshramName: record.customAshramName,
      startDate: record.startDate,
      endDate: record.endDate,
      durationDays: record.durationDays,
      purpose: record.purpose,
      sevaType: record.sevaType,
      customPurposeOrSeva: record.customPurposeOrSeva,
      notes: record.notes,
      locationDetails: record.locationDetails,
      createdAt: record.createdAt,
      updatedAt: record.updatedAt
    };
  }

  async updateRecord(userId, recordId, updates) {
    const record = await SevaRecord.findOne({ _id: recordId, userId });
    if (!record) {
      const error = new Error('Seva record not found or unauthorized.');
      error.statusCode = 404;
      throw error;
    }

    if (updates.ashramCode !== undefined) record.ashramCode = updates.ashramCode;
    if (updates.customAshramName !== undefined) record.customAshramName = updates.customAshramName;
    if (updates.purpose !== undefined) record.purpose = updates.purpose;
    if (updates.sevaType !== undefined) record.sevaType = updates.sevaType;
    if (updates.customPurposeOrSeva !== undefined) record.customPurposeOrSeva = updates.customPurposeOrSeva;
    if (updates.notes !== undefined) record.notes = updates.notes;
    if (updates.locationDetails !== undefined) record.locationDetails = updates.locationDetails;

    // Recalculate duration if dates changed
    const startDate = updates.startDate || record.startDate;
    const endDate = updates.endDate || record.endDate;
    record.startDate = startDate;
    record.endDate = endDate;
    record.durationDays = this.calculateDuration(startDate, endDate);

    // Clean sevaType if purpose was switched away from SEVA
    if (record.purpose !== 'SEVA') {
      record.sevaType = '';
    }

    await record.save();
    return this.getRecordById(userId, record._id);
  }

  async deleteRecord(userId, recordId) {
    const result = await SevaRecord.findOneAndDelete({ _id: recordId, userId });
    if (!result) {
      const error = new Error('Seva record not found or unauthorized.');
      error.statusCode = 404;
      throw error;
    }
    return { success: true };
  }
}

module.exports = new SevaService();
