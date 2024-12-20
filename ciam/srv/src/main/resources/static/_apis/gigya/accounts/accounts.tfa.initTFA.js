async function accounts_tfa_initTfa({provider, isSetGigyaAssertion}) {
  const ERROR_CODES = {
    SUCCESS: 0,
    PENDING_TFA: 400006,
  }

  function callAPI() {
    return new Promise((resolve) => {
      gigya.accounts.tfa.initTFA({
        regToken: signInSession.regToken,
        provider: provider,
        mode: 'verify',
        callback: function (response) {
          resolve(response);
        }
      });
    });
  }

  function handleResponse({response, isSetGigyaAssertion}) {
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        return isSetGigyaAssertion ? signInSession.gigyaAssertion = response.gigyaAssertion : null;
      case ERROR_CODES.PENDING_TFA:
        return;
        // return throw new Error('Pending Registration')
      default:
        return showLoginPageResponseMessages('all.INVALID_LOGIN_REQUEST');
    }
  }

  const response = await callAPI({provider, isSetGigyaAssertion});
  return handleResponse({response, isSetGigyaAssertion});
}

export default accounts_tfa_initTfa;