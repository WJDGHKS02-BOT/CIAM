async function accounts_getProviders() {
  function callAPI() {
    return new Promise((resolve) => {
      gigya.accounts.tfa.getProviders({
        regToken: signInSession.regToken,
        callback: async function (response) {
          resolve(response);
        }
      });
    });
  }

  function handleResponse(response) {
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        return response;
      default:
        return showLoginPageResponseMessages('all.INVALID_LOGIN_REQUEST');
    }
  }

  const response = await callAPI();
  return handleResponse(response);
}

export default accounts_getProviders;