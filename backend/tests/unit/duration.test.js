const sevaService = require('../../src/modules/seva/seva.service');

describe('SevaService - Inclusive Duration Calculation', () => {
  it('should calculate 1 day for a same-day visit', () => {
    const days = sevaService.calculateDuration('2026-10-15', '2026-10-15');
    expect(days).toBe(1);
  });

  it('should calculate 3 days inclusive for a 3-day visit', () => {
    const days = sevaService.calculateDuration('2026-10-01', '2026-10-03');
    expect(days).toBe(3);
  });

  it('should calculate 8 days spanning across a week', () => {
    const days = sevaService.calculateDuration('2026-10-01', '2026-10-08');
    expect(days).toBe(8);
  });

  it('should throw an error if end date precedes start date', () => {
    expect(() => {
      sevaService.calculateDuration('2026-10-05', '2026-10-02');
    }).toThrow('End date cannot precede start date.');
  });

  it('should throw an error for malformed dates', () => {
    expect(() => {
      sevaService.calculateDuration('invalid', '2026-10-02');
    }).toThrow("Dates must be in 'YYYY-MM-DD' format.");
  });
});
