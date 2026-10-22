require('dotenv').config();
const mongoose = require('mongoose');
const brevoService = require('../src/integrations/email/brevo.service');

async function testConnections() {
  console.log('==============================================');
  console.log('   Seva Diary — Integration Verification      ');
  console.log('==============================================\n');

  // 1. Test MongoDB Atlas Connection
  const mongoUri = process.env.MONGODB_URI;
  if (!mongoUri || mongoUri.includes('<username>')) {
    console.log('❌ MongoDB Atlas: MONGODB_URI not set or still contains placeholder values.');
  } else {
    try {
      console.log('⏳ Testing MongoDB Atlas connection...');
      await mongoose.connect(mongoUri, { serverSelectionTimeoutMS: 5000 });
      console.log('✅ MongoDB Atlas: Successfully connected!');
      await mongoose.disconnect();
    } catch (err) {
      console.log(`❌ MongoDB Atlas: Connection failed -> ${err.message}`);
    }
  }

  console.log('');

  // 2. Test Brevo API Connection
  const brevoKey = process.env.BREVO_API_KEY;
  if (!brevoKey || brevoKey === 'xkeysib-your-brevo-api-key-here') {
    console.log('❌ Brevo API: BREVO_API_KEY not configured in .env.');
  } else {
    console.log('⏳ Testing Brevo API authentication...');
    const result = await brevoService.verifyConnection();
    if (result.success) {
      console.log(`✅ Brevo API: Authentication successful! Account: ${result.email || 'Verified'}`);
      console.log(`   Sender: ${process.env.BREVO_SENDER_NAME || 'Seva Diary'} <${process.env.BREVO_SENDER_EMAIL || 'Not configured'}>`);
    } else {
      console.log(`❌ Brevo API: Authentication failed -> ${result.error || result.message}`);
    }
  }

  console.log('\n==============================================');
}

testConnections();
