async function accounts_resetPassword({loginID, newPassword}) {
  const ERROR_CODES = {
    SUCCESS: 0,
    INVALID_PASSWORD_PATTERN: 401030,
    SAME_AS_CURRENT_PASSWORD: 400006,
  };

  function callAPI({loginID, newPassword}) {
    const params = new URLSearchParams(location.search);
    const passwordResetToken = params.get('pwrt');

    // 패스워드 초기화
    return new Promise((resolve) => {
      if (passwordResetToken) {
        gigya.accounts.resetPassword({
          newPassword: newPassword,
          passwordResetToken: passwordResetToken,
          callback: function (response) {
            resolve(response);
          }
        });
      }
      // 패스워드 초기화 이메일 요청
      else {
        return new Promise((resolve) => {
          gigya.accounts.resetPassword({
            loginID: loginID,
            callback: function (response) {
              resolve(response);
              return location.assign(`${location.pathname}/success${location.search}`);
            }
          });
        })
      }
    });
  }

  function handleResponse(response) {
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        return location.assign(`${location.pathname}/success${location.search}`);
      case ERROR_CODES.PASSWORD_PATTERN_INVALID:
        return showLoginPageResponseMessages('field.PASSWORD_PATTERN_INVALID');
      case ERROR_CODES.SAME_AS_CURRENT_PASSWORD:
        return showLoginPageResponseMessages('field.SAME_AS_CURRENT_PASSWORD');
      default:
        return response
    }
  }

  const response = await callAPI({loginID, newPassword});
  return handleResponse(response);
}

export default accounts_resetPassword;