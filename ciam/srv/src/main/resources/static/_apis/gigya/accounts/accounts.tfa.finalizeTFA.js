async function accounts_tfa_finalizeTFA() {
  function callAPI() {
    return new Promise((resolve) => {
      gigya.accounts.tfa.finalizeTFA({
        gigyaAssertion: signInSession.gigyaAssertion,
        providerAssertion: signInSession.providerAssertion,
        regToken: signInSession.regToken,
        tempDevice: true,
        callback: async function (response) {
          resolve(response);
        }
      });
    });
  }

  function handleResponse(response) {
    return response;
  }

  const response = await callAPI();
  return handleResponse(response);
}

export default accounts_tfa_finalizeTFA;