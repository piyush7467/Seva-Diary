const express = require('express');
const chantingController = require('./chanting.controller');
const { authenticate } = require('../../common/middleware/auth.middleware');

const router = express.Router();

router.use(authenticate);

router.get('/', chantingController.getEntries);
router.get('/summary', chantingController.getSummary);
router.put('/:date', chantingController.updateDaily);

module.exports = router;
