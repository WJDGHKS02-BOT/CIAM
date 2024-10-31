async function accounts_session_verify() {
  function callAPI() {
    return new Promise((resolve) => {
      return gigya.accounts.session.verify({
        callback: function (response) {
          resolve(response);
        }
      })
    });
  }

  async function handleResponse(response) {
    debugger;
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        const data = await accounts.getAccountInfo();
        await accounts.setAccountInfo({lastLogin: data.data.lastTenureCheck});
        await accounts.search({
          query: `SELECT data, profile, UID
                  from accounts
                  WHERE UID = '${data.UID}'`,
          updateUserRecord: false,
        });
        await isUpdateConsent({UID: data.UID});
        await signInSession.clearSession();
        return location.assign(`${location.origin}/login-proxy${location.search}`);
      default:
        break;
    }
  }

  const response = await callAPI();
  return handleResponse(response);
}


export default accounts_session_verify