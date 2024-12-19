enum ERROR_CODE {
  success = 0,
  unAuthorizedUser = 403005,
}

export const accounts_getAccountInfo = async () => {
  const response: any = await new Promise((resolve) => {
    window.gigya.accounts.getAccountInfo({ callback: resolve });
  });

  switch (response.errorCode) {
    case ERROR_CODE.success:
      return response;
    case ERROR_CODE.unAuthorizedUser:
      throw new Error('Unauthorized User');
    default:
      throw new Error('Unexpected Error');
  }
};

export default accounts_getAccountInfo;
