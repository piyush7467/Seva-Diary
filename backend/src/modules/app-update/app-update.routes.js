const express = require('express');
const appUpdateController = require('./app-update.controller');

const router = express.Router();

// Public endpoint for mobile apps to check for updates
router.get('/version', appUpdateController.getLatestVersion);

// Endpoint to publish or update the latest version
router.post('/version', appUpdateController.setLatestVersion);

module.exports = router;
