async function accounts_getAccountInfo() {
  const ERROR_CODES = {
    SUCCESS: 0,
    UNAUTHORIZED_USER: 403005,
  }

  function callAPI() {
    return new Promise((resolve) => {
      gigya.accounts.getAccountInfo({
        callback: function (response) {
          resolve(response);
        }
      });
    });
  }

  function handleResponse(response) {
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        return response;
      case ERROR_CODES.UNAUTHORIZED_USER:
        throw new Error('Unauthorized User')
      default:
        return showLoginPageResponseMessages('all.INVALID_LOGIN_REQUEST');
    }
  }

  const response = await callAPI();
  return handleResponse(response);
}

export default accounts_getAccountInfo;