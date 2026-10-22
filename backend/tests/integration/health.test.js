const request = require('supertest');
const app = require('../../src/app');

describe('API Health & Routing Integration Tests', () => {
  it('GET / should return 200 and healthy status', async () => {
    const res = await request(app).get('/');
    expect(res.statusCode).toBe(200);
    expect(res.body.status).toBe('healthy');
    expect(res.body.message).toBe('Seva Diary API is running');
    expect(res.body.version).toBe('1.0.0');
  });

  it('GET /health should return 200 and healthy status', async () => {
    const res = await request(app).get('/health');
    expect(res.statusCode).toBe(200);
    expect(res.body.status).toBe('healthy');
  });

  it('GET /api/v1/health should return status healthy', async () => {
    const res = await request(app).get('/api/v1/health');
    expect(res.statusCode).toBe(200);
    expect(res.body.status).toBe('healthy');
    expect(res.body.version).toBe('1.0.0');
  });

  it('GET non-existent route should return 404 with structured JSON', async () => {
    const res = await request(app).get('/api/v1/non-existent-endpoint');
    expect(res.statusCode).toBe(404);
    expect(res.body.success).toBe(false);
    expect(res.body.error.code).toBe('NOT_FOUND');
  });
});
