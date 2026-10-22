const express = require('express');
const masterDataController = require('./master-data.controller');

const router = express.Router();

router.get('/', masterDataController.getMasterData);

module.exports = router;
