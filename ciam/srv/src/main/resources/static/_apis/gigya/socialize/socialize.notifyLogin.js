async function socialize_notifyLogin() {
  const ERROR_CODES = {
    SUCCESS: 0,
    REQUIRED_PASSWORD_CHANGE: 403100,
  };

  function callAPI() {
    return new Promise((resolve) => {
      gigya.socialize.notifyLogin({
        regToken: signInSession.regToken,
        authCode: signInSession.authCode,
        callback: function (response) {
          resolve(handleResponse(response));
        }
      });
    });
  }

  function handleResponse(response) {
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        return response;
      case ERROR_CODES.REQUIRED_PASSWORD_CHANGE:
        return showLoginPageResponseMessages('accounts.REQUIRED_PASSWORD_CHANGE');
      default:
        return;
    }
  }

  const response = await callAPI();
  return handleResponse(response);
}

export default socialize_notifyLogin;