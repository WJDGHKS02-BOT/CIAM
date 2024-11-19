async function accounts_finalizeRegistration() {
  const ERROR_CODES = {
    SUCCESS: 0,
    PENDING_REGISTRATION: 200601,
    REQUIRED_PASSWORD_CHANGE: 403100,
  };

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
      case ERROR_CODES.PENDING_REGISTRATION:
        return location.assign(`/approval-status-error?approvalStatus=pending`);
      case ERROR_CODES.REQUIRED_PASSWORD_CHANGE:
        return showLoginPageResponseMessages('accounts.REQUIRED_PASSWORD_CHANGE');
      default:
        return showLoginPageResponseMessages('all.INVALID_LOGIN_REQUEST');
    }
  }

  const response = await callAPI();
  return handleResponse(response);
}

export default accounts_finalizeRegistration;