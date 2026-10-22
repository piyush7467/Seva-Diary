const app = require('../src/app');
const config = require('../src/config');
const { connectToDatabase } = require('../src/config/db');

// If running locally (not in serverless environment)
if (process.env.NODE_ENV !== 'production' && require.main === module) {
  connectToDatabase()
    .then(() => {
      app.listen(config.port, () => {
        console.log(`[SERVER] Seva Diary backend running on http://localhost:${config.port}`);
      });
    })
    .catch((err) => {
      console.error('[SERVER] Startup failed:', err.message);
      process.exit(1);
    });
}

// Export for Vercel Serverless
module.exports = app;
