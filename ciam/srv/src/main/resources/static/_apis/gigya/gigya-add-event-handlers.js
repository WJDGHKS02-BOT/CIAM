function onGigyaServiceReady() {
  gigya.accounts.getScreenSets = null;
  gigya.socialize.getScreenSets = null;

  // 로그인 성공 이벤트 추가
  gigya.socialize.addEventHandlers({
    onLogin: async () => {
      await accounts.getAccountInfo();
      await continueSSO();
    },
  });

  const params = new URLSearchParams(window.location.search);
  const gig_events = params.get('gig_events');
  const isADLoginRequest = gig_events?.split(',').includes('socialize.login');

  // AD 로그인 로직
  if (isADLoginRequest) {
    const ERROR_CODES = {
      SUCCESS: '0',
      PENDING_ACCOUNT: '206001',
      PENDING_TFA_VERIFICATION: '403101',
      PENDING_TFA_REGISTRATION: '403102',
    }
    const REQUIRED_PARAMS = `channel=${params.get('channel')}&samlContext=${params.get('samlContext')}`;

    switch (params.get('errorCode')) {
      case ERROR_CODES.SUCCESS:
        return gigya.socialize.notifyLogin({
          dontHandleScreenSet: true,
          siteUID: params.get('UID'),
          UIDSig: params.get('UIDSignature'),
          UIDTimestamp: params.get('signatureTimestamp'),
          callback: function () {
            return location.assign(`${location.origin}/login-proxy${location.search}`);
          }
        });
      case ERROR_CODES.PENDING_TFA_VERIFICATION:
      case ERROR_CODES.PENDING_TFA_REGISTRATION:
        return (async function () {
          signInSession.regToken = params.get('regToken');

          const {data: userTfaSettings} = await samsung.getUserTfaSettings()
          const IS_TFA_OTP = userTfaSettings.tfaMethods.gigyaTotp;

          if (IS_TFA_OTP) return location.assign(`${location.pathname}/tfa/otp?${REQUIRED_PARAMS}&adLogin=true`);
          return location.assign(`${location.pathname}/tfa/email?${REQUIRED_PARAMS}&adLogin=true`);
        })();
      case ERROR_CODES.PENDING_ACCOUNT:
        return location.assign(`${location.origin}/login-proxy?${REQUIRED_PARAMS}&uid=${params.get('UID')}&adLogin=true`);
      default:
        if (CHANNEL === 'btp') {
          return location.href = '/approval-status-error?approvalStatus=btp';
        }
        // TODO: newChannelRegister 함수 사용 대체
        return location.assign(`${HOST_URL.java}/login-error?apiKey=${GIGYA_API_KEY}&regToken=${params.get('regToken')}&newADLogin=true`);
    }
  }
  // 세션 체크 로직
  else {
    gigya.hasSession().then(async hasSession => {
      debugger;
      if (hasSession) {
        return await socialize.notifyLogin();
      }
    });
  }
}