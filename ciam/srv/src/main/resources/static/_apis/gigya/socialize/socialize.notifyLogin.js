async function socialize_notifyLogin() {
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
      default:
        return;
    }
  }

  const response = await callAPI();
  return handleResponse(response);
}

export default socialize_notifyLogin;