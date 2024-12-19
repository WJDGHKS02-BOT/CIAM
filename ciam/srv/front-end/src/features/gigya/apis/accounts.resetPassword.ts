enum ERROR_CODE {
  success = 0,
  unAuthorizedUser = 403005,
}

export const accounts_resetPassword = async ({ loginID, newPassword }: { loginID?: string; newPassword?: string }) => {
  const params = new URLSearchParams(location.search);
  const passwordResetToken = params.get('pwrt');
  let response;

  return new Promise((resolve) => {
    // 비밀번호 초기화
    if (passwordResetToken) {
      response = window.gigya.accounts.resetPassword({
        newPassword: newPassword,
        passwordResetToken: passwordResetToken,
        callback: (res: any) => {
          resolve(res);
        },
      });
    }
    // 비밀번호 초기화 이메일 요청
    else {
      response = window.gigya.accounts.resetPassword({
        loginID: loginID,
        callback: (res: any) => {
          resolve(res);
          return location.assign(`${location.pathname}/success${location.search}`);
        },
      });
    }

    switch (response.errorCode) {
      case ERROR_CODE.success:
        return response;
      case ERROR_CODE.unAuthorizedUser:
        throw new Error('Unauthorized User');
      default:
        throw new Error('Unexpected Error');
    }
  });
};

export default accounts_resetPassword;
