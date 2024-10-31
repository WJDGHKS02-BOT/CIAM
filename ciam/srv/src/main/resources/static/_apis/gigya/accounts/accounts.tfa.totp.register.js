async function accounts_tfa_totp_register() {
  function callAPI() {
    return new Promise((resolve) => {
      gigya.accounts.tfa.totp.register({
        gigyaAssertion: signInSession.gigyaAssertion,
        includeSecret: true,
        callback: function (response) {
          resolve(response);
        }
      });
    });
  }

  function handleResponse(response) {
    switch (response.errorCode) {
      default:
        signInSession.sctToken = response.sctToken;
        return response;
    }
  }

  const response = await callAPI();
  return handleResponse(response);
}

export default accounts_tfa_totp_register;