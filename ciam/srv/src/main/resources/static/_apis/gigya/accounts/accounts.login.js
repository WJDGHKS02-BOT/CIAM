async function accounts_login({email, password, captchaToken}) {
  function callAPI() {
    return gigyaInstance.post('/accounts.login',
        new URLSearchParams({
          loginID: email,
          password: password,
          apiKey: GIGYA_API_KEY,
          ...(captchaToken && {
            captchaToken: captchaToken,
            captchaType: 'reCaptchaV2',
          }),
        }));
  }

  async function handleResponse(response) {
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        debugger;
        setItemWithExpiry('user_uid', response.UID);
        return gigya.socialize.notifyLogin({
          dontHandleScreenSet: true,
          siteUID: response.UID,
          UIDSig: response.UIDSignature,
          UIDTimestamp: response.signatureTimestamp,
        });
      case ERROR_CODES.accounts.PENDING:
        return location.assign(`/approval-status-error?approvalStatus=pending`);
      case ERROR_CODES.captcha.IS_REQUIRED:
        showLoginPageResponseMessages('captcha.IS_REQUIRED');
        return showCaptcha();
      case ERROR_CODES.accounts.REQUIRED_PASSWORD_CHANGE:
        return showLoginPageResponseMessages('accounts.REQUIRED_PASSWORD_CHANGE');
      case ERROR_CODES.captcha.IS_WRONG:
        showLoginPageResponseMessages('captcha.IS_WRONG');
        return resetCaptcha();
      case ERROR_CODES.field.EMAIL_OR_PASSWORD_INCORRECT:
        return showLoginPageResponseMessages('field.EMAIL_OR_PASSWORD_INCORRECT');
      case ERROR_CODES.tfa.REQUIRED_AUTHENTICATION:
        signInSession.regToken = response.regToken;
        signInSession.UID = response.UID;
        return 'REQUIRED_AUTHENTICATION';
      default:
        return showLoginPageResponseMessages('all.INVALID_LOGIN_REQUEST');
    }
  }

  const response = await callAPI();
  return handleResponse(response.data);
}

export default accounts_login;