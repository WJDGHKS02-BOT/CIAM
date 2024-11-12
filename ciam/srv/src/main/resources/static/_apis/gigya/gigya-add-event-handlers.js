function onGigyaServiceReady() {
  gigya.accounts.getScreenSets = null;
  gigya.socialize.getScreenSets = null;

  const params = new URLSearchParams(window.location.search);
  const gig_events = params.get('gig_events');

  const isADLoginRequest = gig_events?.split(',').includes('socialize.login');
  if (isADLoginRequest) {
    const errorCode = params.get('errorCode');
    const isSuccessAdLogin = errorCode === '0';
    const isRequiredAuthentication = errorCode === '403101' || errorCode === '403102';
    const isPendingAccount = errorCode === '206001';

    // AD 로그인 성공
    if (isSuccessAdLogin) {
      setItemWithExpiry('user_uid', params.get('UID'));
      debugger;
      return gigya.socialize.notifyLogin({
        dontHandleScreenSet: true,
        siteUID: params.get('UID'),
        UIDSig: params.get('UIDSignature'),
        UIDTimestamp: params.get('signatureTimestamp'),
        callback: function () {
          return location.assign(`${location.origin}/login-proxy${location.search}`);
        }
      });
    }
    // AD 로그인 실패
    else {
      setItemWithExpiry('user_uid', params.get('UID'));
      debugger;
      if (isRequiredAuthentication) {
        (async function () {
          const REQUIRED_PARAMS = `channel=${params.get('channel')}&samlContext=${params.get('samlContext')}`;

          signInSession.regToken = params.get('regToken');

          const {data: userTfaSettings} = await samsung.getUserTfaSettings()
          const IS_TFA_OTP = userTfaSettings.tfaMethods.gigyaTotp;

          if (IS_TFA_OTP) return location.assign(`${location.pathname}/tfa/otp?${REQUIRED_PARAMS}&adLogin=true`);
          return location.assign(`${location.pathname}/tfa/email?${REQUIRED_PARAMS}&adLogin=true`);
        })()
      }
      else if (isPendingAccount) {
        return continueSSO();
      } else {
        if (CHANNEL === 'btp') {
          return location.href = '/approval-status-error?approvalStatus=btp';
        }
        // TODO: newChannelRegister 함수 사용 대체
        return location.assign(`${HOST_URL.java}/login-error?apiKey=${GIGYA_API_KEY}&regToken=${params.get('regToken')}&newADLogin=true`);
      }
    }
  }

  gigya.socialize.addEventHandlers({
    onLogin: async () => {
      debugger;
      await accounts.getAccountInfo();
      await continueSSO();
    },
    onLogout: () => {
    },
    onError: () => {
    },
    onConnectionAdded: () => {
    },
    onConnectionRemoved: () => {
    },
    onLinkBack: () => {
    },
    onAfterResponse: (res) => {
      const isGetAccountInfo = res.methodName === 'accounts.getAccountInfo';
      if (isGetAccountInfo) {
        signInSession.userInfo = res.response;
        setItemWithExpiry('user_uid', res.response.UID);
      }
    },
  });

  gigya.accounts.addEventHandlers({
    onLogin: () => {
    },
    onLogout: () => {
    },
    onError: () => {
    },
    onConnectionAdded: () => {
    },
    onConnectionRemoved: () => {
    },
    onLinkBack: () => {
    },
    onAfterResponse: () => {
    },
  });
  // 세션 체크
  gigya.hasSession(async (session) => {
    if (session) {
      debugger;
      await accounts.getAccountInfo();
      return await continueSSO();
    }
  })
}