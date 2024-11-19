async function accounts_tfa_email_sendVerification() {
  const ERROR_CODES = {
    SUCCESS: 0,
  };

  function callAPI() {
    return new Promise((resolve) => {
      gigya.accounts.tfa.email.sendVerificationCode({
        emailID: signInSession.emailID,
        gigyaAssertion: signInSession.gigyaAssertion,
        regToken: signInSession.regToken,
        callback: function (response) {
          resolve(response);
        }
      });
    });
  }

  function handleResponse(response) {
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        signInSession.phvToken = response.phvToken;
        return response;
      default:
        showLoginPageResponseMessages('code.EMAIL_REQUEST_EXPIRED');
        break;
    }
  }

  const response = await callAPI();
  return handleResponse(response);
}

export default accounts_tfa_email_sendVerification;