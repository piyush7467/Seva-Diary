const { ChantingEntry } = require('./chanting.model');

class ChantingService {
  async setOrIncrementCount({ userId, date, count, increment, target }) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) {
      const error = new Error("Invalid date format. Expected 'YYYY-MM-DD'.");
      error.statusCode = 400;
      throw error;
    }

    if (count !== undefined && count < 0) {
      const error = new Error('Chanting count cannot be negative.');
      error.statusCode = 400;
      throw error;
    }

    let updateQuery = {};
    if (count !== undefined) {
      updateQuery.$set = { count };
      if (target !== undefined) {
        updateQuery.$set.target = target;
      }
    } else if (increment !== undefined) {
      updateQuery.$inc = { count: increment };
      if (target !== undefined) {
        updateQuery.$set = { target };
      }
    } else {
      const error = new Error("Either 'count' or 'increment' must be provided.");
      error.statusCode = 400;
      throw error;
    }

    // Atomic upsert
    let entry = await ChantingEntry.findOneAndUpdate(
      { userId, localDate: date },
      updateQuery,
      { new: true, upsert: true, setDefaultsOnInsert: true }
    );

    // Safeguard: If decrement pushed count below 0, atomically reset to 0
    if (entry.count < 0) {
      entry = await ChantingEntry.findOneAndUpdate(
        { userId, localDate: date },
        { $set: { count: 0 } },
        { new: true }
      );
    }

    return {
      date: entry.localDate,
      count: entry.count,
      target: entry.target,
      updatedAt: entry.updatedAt
    };
  }

  async getEntriesByRange({ userId, startDate, endDate }) {
    const filter = { userId };
    if (startDate && endDate) {
      filter.localDate = { $gte: startDate, $lte: endDate };
    } else if (startDate) {
      filter.localDate = { $gte: startDate };
    } else if (endDate) {
      filter.localDate = { $lte: endDate };
    }

    const entries = await ChantingEntry.find(filter)
      .sort({ localDate: -1 })
      .lean();

    return entries.map(e => ({
      date: e.localDate,
      count: e.count,
      target: e.target,
      updatedAt: e.updatedAt
    }));
  }

  async getSummary(userId) {
    const now = new Date();
    const todayStr = now.toISOString().slice(0, 10);

    // Calculate start of current week (Monday)
    const dayOfWeek = (now.getDay() + 6) % 7; // Monday = 0
    const monday = new Date(now);
    monday.setDate(now.getDate() - dayOfWeek);
    const weekStartStr = monday.toISOString().slice(0, 10);

    // Calculate start of current month
    const monthStartStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`;

    const allEntries = await ChantingEntry.find({ userId }).lean();

    let todayCount = 0;
    let weekTotal = 0;
    let monthTotal = 0;
    let allTimeTotal = 0;

    for (const entry of allEntries) {
      allTimeTotal += entry.count;
      if (entry.localDate === todayStr) {
        todayCount = entry.count;
      }
      if (entry.localDate >= weekStartStr && entry.localDate <= todayStr) {
        weekTotal += entry.count;
      }
      if (entry.localDate >= monthStartStr && entry.localDate <= todayStr) {
        monthTotal += entry.count;
      }
    }

    return {
      today: todayCount,
      thisWeek: weekTotal,
      thisMonth: monthTotal,
      allTimeTotal,
      totalActiveDays: allEntries.filter(e => e.count > 0).length
    };
  }
}

module.exports = new ChantingService();
