const brevo = require('@getbrevo/brevo');
const config = require('../../config');

class BrevoEmailService {
  getApiInstance() {
    const apiInstance = new brevo.TransactionalEmailsApi();
    const apiKey = config.brevo.apiKey;
    if (apiKey) {
      if (typeof apiInstance.setApiKey === 'function' && brevo.TransactionalEmailsApiApiKeys) {
        apiInstance.setApiKey(brevo.TransactionalEmailsApiApiKeys.apiKey, apiKey);
      }
      if (apiInstance.authentications && apiInstance.authentications['apiKey']) {
        apiInstance.authentications['apiKey'].apiKey = apiKey;
      }
    }
    return apiInstance;
  }

  async verifyConnection() {
    const apiKey = config.brevo.apiKey;
    if (!apiKey) {
      return { success: false, message: 'Brevo API key is not configured in .env' };
    }
    try {
      const accountApi = new brevo.AccountApi();
      if (typeof accountApi.setApiKey === 'function' && brevo.AccountApiApiKeys) {
        accountApi.setApiKey(brevo.AccountApiApiKeys.apiKey, apiKey);
      }
      if (accountApi.authentications && accountApi.authentications['apiKey']) {
        accountApi.authentications['apiKey'].apiKey = apiKey;
      }
      const accountData = await accountApi.getAccount();
      return {
        success: true,
        email: accountData.body?.email,
        companyName: accountData.body?.companyName,
        plan: accountData.body?.plan
      };
    } catch (err) {
      return {
        success: false,
        error: err.response?.body?.message || err.message
      };
    }
  }

  async sendPasswordResetEmail({ toEmail, displayName, resetToken }) {
    const apiKey = config.brevo.apiKey;
    if (!apiKey) {
      console.warn('[EMAIL] Brevo API key is not configured in .env. Skipping email dispatch in dev mode.');
      return { success: true, simulated: true };
    }

    const resetUrl = `${config.resetPasswordUrl}?token=${resetToken}`;
    const apiInstance = this.getApiInstance();

    const sendSmtpEmail = new brevo.SendSmtpEmail();
    sendSmtpEmail.subject = 'Seva Diary — Password Recovery Request';
    sendSmtpEmail.sender = {
      name: config.brevo.senderName,
      email: config.brevo.senderEmail
    };
    sendSmtpEmail.to = [{ email: toEmail, name: displayName || 'Devotee' }];
    sendSmtpEmail.htmlContent = `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #E3DED4; border-radius: 12px; background-color: #FDFBF7;">
        <h2 style="color: #1E4D2B; margin-bottom: 8px;">Sat Sahib ${displayName || 'Devotee'},</h2>
        <p style="color: #1F2421; font-size: 15px; line-height: 1.5;">
          We received a request to reset your password for your Seva Diary account.
        </p>
        <p style="margin: 24px 0;">
          <a href="${resetUrl}" style="background-color: #1E4D2B; color: #FFFFFF; padding: 12px 24px; text-decoration: none; border-radius: 8px; font-weight: bold; display: inline-block;">
            Reset Your Password
          </a>
        </p>
        <p style="color: #5C645E; font-size: 13px;">
          This link will expire in 1 hour. If you did not request this, please disregard this email.
        </p>
        <hr style="border: none; border-top: 1px solid #E3DED4; margin: 24px 0;" />
        <p style="color: #5C645E; font-size: 12px; text-align: center;">
          Seva Diary — Personal Devotional Diary & Naam Jaap Tracker
        </p>
      </div>
    `;

    try {
      const response = await this.apiInstance.sendTransacEmail(sendSmtpEmail);
      console.log(`[EMAIL] Password reset email queued for recipient successfully.`);
      return { success: true, messageId: response.body?.messageId };
    } catch (error) {
      console.error('[EMAIL] Failed to send transactional email via Brevo:', error.message);
      // Return safe failure without leaking API details
      throw new Error('Email delivery failed. Please try again later.');
    }
  }
}

module.exports = new BrevoEmailService();
