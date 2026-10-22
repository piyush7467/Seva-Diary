const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const config = require('./config');
const { connectToDatabase } = require('./config/db');
const { errorHandler, notFoundHandler } = require('./common/middleware/error.middleware');

// Routes
const authRoutes = require('./modules/auth/auth.routes');
const userRoutes = require('./modules/users/user.routes');
const chantingRoutes = require('./modules/chanting/chanting.routes');
const sevaRoutes = require('./modules/seva/seva.routes');
const masterDataRoutes = require('./modules/master-data/master-data.routes');

const app = express();

// Security & Body Parsers
app.use(helmet());
app.use(cors({
  origin: (origin, callback) => {
    // Allow requests with no origin (like mobile apps, curl) or allowed origins
    if (!origin || config.allowedOrigins.includes(origin) || config.env === 'development') {
      return callback(null, true);
    }
    return callback(new Error('CORS policy: Not allowed by Access-Control-Allow-Origin'));
  },
  credentials: true
}));
app.use(express.json({ limit: '1mb' }));
app.use(express.urlencoded({ extended: true, limit: '1mb' }));

// Database connection middleware for serverless invocations
app.use(async (req, res, next) => {
  try {
    if (config.env !== 'test') {
      await connectToDatabase();
    }
    next();
  } catch (err) {
    console.error('[DB] Connection error:', err.message);
    res.status(503).json({
      success: false,
      error: { code: 'DATABASE_UNAVAILABLE', message: 'Database connection failed. Please try again.' }
    });
  }
});

// Root & Health Endpoints
const healthPayload = (req, res) => {
  res.status(200).json({
    status: 'healthy',
    message: 'Seva Diary API is running',
    timestamp: new Date().toISOString(),
    environment: config.env,
    version: '1.0.0'
  });
};

app.get(['/', '/health', '/api/health', '/api/v1/health'], healthPayload);

app.use('/api/v1/auth', authRoutes);
app.use('/api/v1/users', userRoutes);
app.use('/api/v1/chanting', chantingRoutes);
app.use('/api/v1/seva', sevaRoutes);
app.use('/api/v1/master-data', masterDataRoutes);

// Error Handling
app.use(notFoundHandler);
app.use(errorHandler);

module.exports = app;
