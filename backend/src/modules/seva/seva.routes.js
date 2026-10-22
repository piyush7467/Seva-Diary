const express = require('express');
const { body } = require('express-validator');
const sevaController = require('./seva.controller');
const { authenticate } = require('../../common/middleware/auth.middleware');

const router = express.Router();

router.use(authenticate);

router.post(
  '/',
  [
    body('ashramCode').notEmpty().withMessage('Ashram selection is required.'),
    body('startDate').matches(/^\d{4}-\d{2}-\d{2}$/).withMessage('Valid start date (YYYY-MM-DD) is required.'),
    body('endDate').matches(/^\d{4}-\d{2}-\d{2}$/).withMessage('Valid end date (YYYY-MM-DD) is required.'),
    body('purpose').notEmpty().withMessage('Visit purpose is required.')
  ],
  sevaController.create
);

router.get('/', sevaController.getAll);
router.get('/:id', sevaController.getById);
router.patch('/:id', sevaController.update);
router.delete('/:id', sevaController.delete);

module.exports = router;
