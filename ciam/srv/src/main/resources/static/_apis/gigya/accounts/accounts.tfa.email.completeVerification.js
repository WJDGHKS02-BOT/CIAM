async function accounts_tfa_email_completeVerification({ code }) {
  const ERROR_CODES = {
    SUCCESS: 0,
    INVALID_VALUE: 400006,
    WRONG_VERIFICATION_CODE: 400042,
    REQUIRED_CHANGE_PASSWORD: 403042,
  };

  function callAPI({ code }) {
    return new Promise((resolve, reject) => {
      gigya.accounts.tfa.email.completeVerification({
        gigyaAssertion: signInSession.gigyaAssertion,
        phvToken: signInSession.phvToken,
        code: code,
        callback: function(response) {
          resolve(response);
        },
      });
    });
  }

  function handleResponse(response) {
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        signInSession.providerAssertion = response.providerAssertion;
        return response;
      case ERROR_CODES.WRONG_VERIFICATION_CODE:
        showLoginPageResponseMessages('field.ENTERED_WRONG_VERIFICATION_CODE');
        break;
      case ERROR_CODES.REQUIRED_PASSWORD_CHANGE:
        return showLoginPageResponseMessages('accounts.REQUIRED_PASSWORD_CHANGE');
      case ERROR_CODES.INVALID_VALUE:
        showLoginPageResponseMessages('code.INVALID_VALUE');
        break;
      default:
        showLoginPageResponseMessages('code.EMAIL_REQUEST_EXPIRED');
        break;
    }
  }

  const response = await callAPI({ code });
  return handleResponse(response);
}

export default accounts_tfa_email_completeVerification;