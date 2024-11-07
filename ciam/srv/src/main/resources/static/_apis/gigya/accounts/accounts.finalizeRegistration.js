async function accounts_finalizeRegistration() {
  function callAPI() {
    return new Promise((resolve) => {
      gigya.accounts.finalizeRegistration({
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
      case ERROR_CODES.accounts.PENDING:
        return location.assign(`/approval-status-error?approvalStatus=pending`);
      case ERROR_CODES.accounts.REQUIRED_PASSWORD_CHANGE:
        return showLoginPageResponseMessages('accounts.REQUIRED_PASSWORD_CHANGE');
      default:
        return showLoginPageResponseMessages('all.INVALID_LOGIN_REQUEST');
    }
  }

  const response = await callAPI();
  return handleResponse(response);
}

export default accounts_finalizeRegistration;