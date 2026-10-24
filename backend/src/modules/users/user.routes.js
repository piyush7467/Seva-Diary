const express = require('express');
const userController = require('./user.controller');
const { authenticate } = require('../../common/middleware/auth.middleware');

const router = express.Router();

router.use(authenticate);

router.get('/me', userController.getProfile);
router.patch('/me', userController.updateProfile);
router.put('/me', userController.updateProfile);

module.exports = router;
