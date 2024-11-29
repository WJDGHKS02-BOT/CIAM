import {Redirect} from '../../../_pages/redirect.js';

async function accounts_login({email, password, captchaToken}) {
  const API_END_POINT = '/accounts.login';
  const API_PARAMS = {
    loginID: email,
    password: password,
    apiKey: GIGYA_API_KEY,
    ...(captchaToken && {
      captchaToken: captchaToken,
      captchaType: 'reCaptchaV2',
    }),
  }
  const ERROR_CODES = {
    LOGIN_SUCCESS: 0,
    PENDING_REGISTRATION: 206001,
    PENDING_TFA_VERIFICATION: 403101,
    PENDING_TFA_REGISTRATION: 403102,
    IS_REQUIRED_CHANGE_PASSWORD: 403100,
    IS_REQUIRED_CAPTCHA: 401020,
    IS_WRONG_CAPTCHA: 401021,
    INVALID_PARAMETER_VALUE: 400006,
    ACCOUNT_LOCKED: 403120,
    EMAIL_OR_PASSWORD_INCORRECT: 403042,
  };

  async function callAPI() {
    try {
      return await gigyaInstance.post(API_END_POINT, new URLSearchParams(API_PARAMS));
    } catch (error) {
      console.error('Login API call failed:', error);
      throw new Error('Network error during login');
    }
  }

  async function handleResponse(res) {
    try {
      switch (res.errorCode) {
        case ERROR_CODES.LOGIN_SUCCESS:
          return await gigya.socialize.notifyLogin({
            dontHandleScreenSet: true,
            siteUID: res.UID,
            UIDSig: res.UIDSignature,
            UIDTimestamp: res.signatureTimestamp,
          });
        case ERROR_CODES.PENDING_REGISTRATION:
          return Redirect.approvalStatusError('pending');
        case ERROR_CODES.PENDING_TFA_VERIFICATION:
        case ERROR_CODES.PENDING_TFA_REGISTRATION:
          try {
            signInSession.regToken = res.regToken;
            const {data: userTfaSettings} = await samsung.getUserTfaSettings();
            return userTfaSettings.tfaMethods.gigyaTotp
                ? Redirect.tfaOtp()
                : Redirect.tfaEmail();
          } catch (error) {
            console.error('Failed to get TFA settings:', error);
            return showLoginPageResponseMessages('tfa.SETTINGS_ERROR');
          }
        case ERROR_CODES.IS_REQUIRED_CHANGE_PASSWORD:
          return showLoginPageResponseMessages('accounts.REQUIRED_PASSWORD_CHANGE');
        case ERROR_CODES.INVALID_PARAMETER_VALUE:
          return showLoginPageResponseMessages('field.EMAIL_OR_PASSWORD_INCORRECT');
        case ERROR_CODES.EMAIL_OR_PASSWORD_INCORRECT:
          return showLoginPageResponseMessages('field.EMAIL_OR_PASSWORD_INCORRECT');
        case ERROR_CODES.IS_WRONG_CAPTCHA:
          showLoginPageResponseMessages('captcha.IS_WRONG');
          return resetCaptcha();
        case ERROR_CODES.IS_REQUIRED_CAPTCHA:
          showLoginPageResponseMessages('captcha.IS_REQUIRED');
          return showCaptcha();
        case ERROR_CODES.ACCOUNT_LOCKED:
          return showLoginPageResponseMessages('accounts.ACCOUNT_LOCKED');
        default:
          return showLoginPageResponseMessages('all.INVALID_LOGIN_REQUEST');
      }
    } catch (error) {
      console.error('Error handling login response:', error);
      return showLoginPageResponseMessages('all.UNEXPECTED_ERROR');
    }
  }

  const response = await callAPI();
  return handleResponse(response.data);
}

export default accounts_login;