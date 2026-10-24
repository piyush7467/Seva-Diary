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

  async sendPasswordResetEmail({ toEmail, displayName, resetToken, otpCode }) {
    const code = otpCode || resetToken;
    const apiKey = config.brevo.apiKey;
    if (!apiKey) {
      console.warn(`[EMAIL] Brevo API key is not configured in .env. Password reset OTP for ${toEmail}: ${code}`);
      return { success: true, simulated: true, otp: code };
    }

    const apiInstance = this.getApiInstance();

    const sendSmtpEmail = new brevo.SendSmtpEmail();
    sendSmtpEmail.subject = `Seva Diary — Your Password Reset Verification Code is ${code}`;
    sendSmtpEmail.sender = {
      name: config.brevo.senderName,
      email: config.brevo.senderEmail
    };
    sendSmtpEmail.to = [{ email: toEmail, name: displayName || 'Devotee' }];
    sendSmtpEmail.htmlContent = `
      <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 580px; margin: 0 auto; padding: 28px; border: 1px solid #E5DFD3; border-radius: 16px; background-color: #FAF8F2;">
        <div style="text-align: center; margin-bottom: 20px;">
          <h2 style="color: #174C38; margin: 0; font-size: 24px; font-weight: bold;">Seva Diary</h2>
          <p style="color: #D99A32; font-size: 14px; margin-top: 4px; font-weight: 600;">Sat Saheb</p>
        </div>
        <p style="color: #252B27; font-size: 16px; line-height: 1.5;">
          Hello <strong>${displayName || 'Devotee'}</strong>,
        </p>
        <p style="color: #646B65; font-size: 14px; line-height: 1.6;">
          We received a request to reset your password for your Seva Diary account. Please use the following 6-digit verification code:
        </p>
        <div style="text-align: center; margin: 28px 0;">
          <div style="display: inline-block; background-color: #174C38; color: #FFFFFF; font-size: 32px; font-weight: bold; letter-spacing: 8px; padding: 14px 32px; border-radius: 12px; box-shadow: 0 4px 12px rgba(23, 76, 56, 0.18);">
            ${code}
          </div>
        </div>
        <p style="color: #8E9690; font-size: 13px; text-align: center;">
          This code is valid for <strong>15 minutes</strong>. Enter this code in the Seva Diary app to reset your password.
        </p>
        <hr style="border: none; border-top: 1px solid #E5DFD3; margin: 24px 0;" />
        <p style="color: #8E9690; font-size: 12px; text-align: center; margin: 0;">
          If you did not request this, please ignore this email. Your account remains secure.
        </p>
      </div>
    `;

    try {
      const response = await apiInstance.sendTransacEmail(sendSmtpEmail);
      console.log(`[EMAIL] Password reset email queued successfully for ${toEmail}`);
      return { success: true, messageId: response.body?.messageId };
    } catch (error) {
      const errMsg = error.response?.body?.message || error.message;
      console.error('[EMAIL] Failed to send transactional email via Brevo:', errMsg);
      console.log(`[DEV/FALLBACK] Password reset OTP for ${toEmail} is: ${code}`);
      throw new Error(`Email delivery failed: ${errMsg}`);
    }
  }

  async sendChangePasswordOtpEmail({ toEmail, displayName, otpCode }) {
    const apiKey = config.brevo.apiKey;
    if (!apiKey) {
      console.warn(`[EMAIL] Brevo API key is not configured in .env. OTP for ${toEmail}: ${otpCode}`);
      return { success: true, simulated: true, otp: otpCode };
    }

    const apiInstance = this.getApiInstance();
    const sendSmtpEmail = new brevo.SendSmtpEmail();
    sendSmtpEmail.subject = `Seva Diary — Your Password Change Verification OTP is ${otpCode}`;
    sendSmtpEmail.sender = {
      name: config.brevo.senderName,
      email: config.brevo.senderEmail
    };
    sendSmtpEmail.to = [{ email: toEmail, name: displayName || 'Devotee' }];
    sendSmtpEmail.htmlContent = `
      <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 580px; margin: 0 auto; padding: 28px; border: 1px solid #E5DFD3; border-radius: 16px; background-color: #FAF8F2;">
        <div style="text-align: center; margin-bottom: 20px;">
          <h2 style="color: #174C38; margin: 0; font-size: 24px; font-weight: bold;">Seva Diary</h2>
          <p style="color: #D99A32; font-size: 14px; margin-top: 4px; font-weight: 600;">Sat Saheb</p>
        </div>
        <p style="color: #252B27; font-size: 16px; line-height: 1.5;">
          Hello <strong>${displayName || 'Devotee'}</strong>,
        </p>
        <p style="color: #646B65; font-size: 14px; line-height: 1.6;">
          You requested to change your password for your Seva Diary account. Please use the following 6-digit OTP code to verify your request:
        </p>
        <div style="text-align: center; margin: 28px 0;">
          <div style="display: inline-block; background-color: #174C38; color: #FFFFFF; font-size: 32px; font-weight: bold; letter-spacing: 8px; padding: 14px 32px; border-radius: 12px; box-shadow: 0 4px 12px rgba(23, 76, 56, 0.18);">
            ${otpCode}
          </div>
        </div>
        <p style="color: #8E9690; font-size: 13px; text-align: center;">
          This OTP code is valid for <strong>10 minutes</strong>. Never share this code with anyone.
        </p>
        <hr style="border: none; border-top: 1px solid #E5DFD3; margin: 24px 0;" />
        <p style="color: #8E9690; font-size: 12px; text-align: center; margin: 0;">
          If you did not make this request, please secure your account immediately.
        </p>
      </div>
    `;

    try {
      const response = await apiInstance.sendTransacEmail(sendSmtpEmail);
      console.log(`[EMAIL] Change password OTP queued successfully for ${toEmail}`);
      return { success: true, messageId: response.body?.messageId };
    } catch (error) {
      const errMsg = error.response?.body?.message || error.message;
      console.error('[EMAIL] Failed to send OTP email via Brevo:', errMsg);
      console.log(`[DEV/FALLBACK] Change password OTP for ${toEmail} is: ${otpCode}`);
      throw new Error(`Email delivery failed: ${errMsg}`);
    }
  }
}

module.exports = new BrevoEmailService();
