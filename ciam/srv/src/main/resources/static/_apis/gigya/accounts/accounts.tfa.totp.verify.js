async function accounts_tfa_totp_verify({code, sctToken}) {
  const ERROR_CODES = {
    SUCCESS: 0,
    WRONG_VERIFICATION_CODE: 400006,
    REQUIRED_PASSWORD_CHANGE: 403100,
  };

  function callAPI({code, sctToken}) {
    return new Promise((resolve) => {
      return gigya.accounts.tfa.totp.verify({
        gigyaAssertion: signInSession.gigyaAssertion,
        regToken: signInSession.regToken,
        code: code,
        ...(sctToken ? {sctToken: sctToken} : ''),
        callback: function (response) {
          resolve(response);
        }
      });
    });
  }

  function handleResponse(response) {
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        return signInSession.providerAssertion = response.providerAssertion;
      case ERROR_CODES.ENTERED_WRONG_VERIFICATION_CODE:
        return showLoginPageResponseMessages('field.ENTERED_WRONG_VERIFICATION_CODE');
      case ERROR_CODES.REQUIRED_PASSWORD_CHANGE:
        return showLoginPageResponseMessages('accounts.REQUIRED_PASSWORD_CHANGE');
      default:
        return showLoginPageResponseMessages('all.INVALID_LOGIN_REQUEST');
    }
  }

  const response = await callAPI({code, sctToken});
  return handleResponse(response);
}

export default accounts_tfa_totp_verify;
