async function accounts_auth_fido_register(aToken) {
  const ERROR_CODES = {
    SUCCESS: 0,
  };

  function callAPI(aToken) {
    return new Promise((resolve) => {
      return gigya.accounts.auth.fido.register({
        aToken: aToken,
        apiKey: GIGYA_PARENT_KEYS.apiKey,
        callback: (res) => {
          debugger;
          resolve(res);
        }
      })
    });
  }

  function handleResponse(response) {
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        return response;
      default:
        return response;
    }
  }

  const response = await callAPI(aToken);
  return handleResponse(response);
}

export default accounts_auth_fido_register;