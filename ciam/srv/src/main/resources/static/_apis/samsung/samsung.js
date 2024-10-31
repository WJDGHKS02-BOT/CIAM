const samsung = {
  getUserTfaSettings: async () => {
    return await samsungInstance.post('/get-user-tfa-settings', {
      apiKey: GIGYA_API_KEY,
      regToken: signInSession.regToken,
    });
  },
  initTotpTfaRegistration: async () => {
    const {data} = await samsungInstance.post('/init-totp-tfa-registration', {
      apiKey: GIGYA_API_KEY,
      regToken: signInSession.regToken,
    });
    signInSession.gigyaAssertion = data.gigyaAssertion;

    return data;
  },
  updateUserRecord: async () => {
    async function handleUpdateUserRecord() {
      const {data} = await samsungInstance.post('/update-user-record', {
        gigyaAssertion: signInSession.gigyaAssertion,
        regToken: signInSession.regToken,
        providerAssertion: signInSession.providerAssertion,
        apiKey: GIGYA_API_KEY,
      });

      return processUpdateUserRecord(data);
    }

    async function processUpdateUserRecord(data) {
      const ErrorCodes = {
        SUCCESS: 0,
        PENDING_REGISTRATION: 206001,
      };

      switch (data.errorCode) {
        case ErrorCodes.SUCCESS:
          signInSession.authCode = data.sessionInfo.cookieValue;
          return data;
        case ErrorCodes.PENDING_REGISTRATION:
          return data;
        default:
          throw new Error('Unknown Error Code By Update User Record');
      }
    }

    return await handleUpdateUserRecord();
  }
}