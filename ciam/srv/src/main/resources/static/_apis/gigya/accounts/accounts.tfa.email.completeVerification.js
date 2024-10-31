async function accounts_tfa_email_completeVerification({code}) {
  function callAPI({code}) {
    return new Promise((resolve, reject) => {
      gigya.accounts.tfa.email.completeVerification({
        gigyaAssertion: signInSession.gigyaAssertion,
        phvToken: signInSession.phvToken,
        code: code,
        callback: function (response) {
          resolve(response);
        }
      });
    });
  }

  function handleResponse(response) {
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        signInSession.providerAssertion = response.providerAssertion;
        return response;
      case ERROR_CODES.field.ENTERED_WRONG_VERIFICATION_CODE:
        showLoginPageResponseMessages('field.ENTERED_WRONG_VERIFICATION_CODE');
        break;
      default:
        showLoginPageResponseMessages('code.EMAIL_REQUEST_EXPIRED');
        break;
    }
  }

  const response = await callAPI({code});
  return handleResponse(response);
}

export default accounts_tfa_email_completeVerification;