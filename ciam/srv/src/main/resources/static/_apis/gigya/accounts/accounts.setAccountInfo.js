async function accounts_setAccountInfo({lastLogin}) {
  function callAPI() {
    const CHANNEL = new URLSearchParams(location.search).get('channel');
    const channelData = {};
    channelData[CHANNEL] = {lastLogin: lastLogin};

    return new Promise((resolve) => {
      gigya.accounts.setAccountInfo({
        data: {
          channels: channelData
        },
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
      default:
        return showLoginPageResponseMessages('all.INVALID_LOGIN_REQUEST');
    }
  }

  const response = await callAPI({lastLogin});
  return handleResponse(response);
}

export default accounts_setAccountInfo;