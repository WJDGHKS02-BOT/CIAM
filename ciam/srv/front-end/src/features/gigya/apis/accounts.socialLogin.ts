enum ERROR_CODE {
  success = 0,
}

export const accounts_socialLogin = async (provider: string) => {
  const response: any = await new Promise((resolve) => {
    return window.gigya.accounts.socialLogin({ provider: provider, callback: resolve });
  });

  switch (response.errorCode) {
    case ERROR_CODE.success:
      return response;
    default:
      throw new Error('Unexpected Error');
  }
};

export default accounts_socialLogin;
