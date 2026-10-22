const config = require('../../config');

function errorHandler(err, req, res, next) {
  const statusCode = err.statusCode || err.status || 500;
  const message = err.message || 'Internal Server Error';

  // Log error without exposing sensitive headers or passwords
  if (config.env !== 'test') {
    console.error(`[ERROR] ${req.method} ${req.url} -> ${statusCode}: ${message}`);
    if (config.env === 'development' && err.stack) {
      console.error(err.stack);
    }
  }

  res.status(statusCode).json({
    success: false,
    error: {
      code: err.code || 'SERVER_ERROR',
      message: statusCode === 500 && config.env === 'production' 
        ? 'An unexpected error occurred. Please try again later.' 
        : message,
      ...(config.env === 'development' && { details: err.details || null })
    }
  });
}

function notFoundHandler(req, res) {
  res.status(404).json({
    success: false,
    error: {
      code: 'NOT_FOUND',
      message: `The requested resource '${req.originalUrl}' was not found.`
    }
  });
}

module.exports = { errorHandler, notFoundHandler };
