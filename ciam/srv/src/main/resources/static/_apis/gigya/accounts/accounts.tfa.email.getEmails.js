async function accounts_tfa_email_getEmails() {
  function callAPI() {
    return new Promise((resolve) => {
      gigya.accounts.tfa.email.getEmails({
        gigyaAssertion: signInSession.gigyaAssertion,
        callback: function (response) {
          resolve(response);
        }
      });
    });
  }

  function handleResponse(response) {
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        signInSession.emailID = response.emails[0].id;
        signInSession.emailObfuscated = response.emails[0].obfuscated;
        return response;
      default:
        return showLoginPageResponseMessages('all.INVALID_LOGIN_REQUEST');
    }
  }

  const response = await callAPI();
  return handleResponse(response);
}

export default accounts_tfa_email_getEmails;