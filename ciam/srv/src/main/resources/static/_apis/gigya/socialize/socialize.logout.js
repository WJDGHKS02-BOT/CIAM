async function socialize_logout() {
  const ERROR_CODES = {
    SUCCESS: 0,
  };

  function callAPI() {
    return new Promise((resolve) => {
      return gigya.socialize.logout({
        callback: function (response) {
          resolve(handleResponse(response));
        }
      });
    });
  }

  function handleResponse(response) {
    console.log(response);
    debugger;
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        return response;
      default:
        return response;
    }
  }

  const response = await callAPI();
  return handleResponse(response);
}

export default socialize_logout;