function onGigyaServiceReady() {
  gigya.accounts.getScreenSets = null;
  gigya.socialize.getScreenSets = null;

  const params = new URLSearchParams(window.location.search);
  const gig_events = params.get('gig_events');

  const isADLoginRequest = gig_events?.includes('socialize.login');
  if (isADLoginRequest) {
    const isSuccessAdLogin = params.get('errorCode') === '0';
    const isRequiredAuthentication = params.get('errorCode') === '403101' || params.get('errorCode') === '403102';

    // AD 로그인 성공
    if (isSuccessAdLogin) {
      debugger;
      return gigya.socialize.notifyLogin({
        dontHandleScreenSet: true,
        siteUID: params.get('UID'),
        UIDSig: params.get('UIDSignature'),
        UIDTimestamp: params.get('signatureTimestamp'),
        callback: function () {
          // return location.assign(`${location.origin}/login-proxy${location.search}`);
          return continueSSO();
        }
      });
    }
    // AD 로그인 실패
    else {
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
      } else {
        if (CHANNEL === 'btp') {
          return location.href = '/approval-status-error?approvalStatus=btp';
        }
        return location.assign(`${HOST_URL.java}/login-error?apiKey=${GIGYA_API_KEY}&regToken=${params.get('regToken')}&newADLogin=true`);
      }
    }
  }

  gigya.socialize.addEventHandlers({
    onLogin: async () => {
      if (isBtpLogin) return gigya.fidm.saml.continueSSO();
      else {
        const {data: accountInfo} = await accounts.getAccountInfo();
        await accounts.setAccountInfo({lastLogin: accountInfo.lastTenureCheck});
        await accounts.search({
          query: `SELECT data, profile, UID
                  from accounts
                  WHERE UID = '${signInSession.UID ?? accountInfo.UID ?? params.get('UID')}'`,
          updateUserRecord: false,
        });
        await isUpdateConsent({UID: signInSession.UID ?? accountInfo.UID ?? params.get('UID')});
        await continueSSO();
      }
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
        localStorage.setItem('user_uid', res.response.UID);
      }
    },
  });

  gigya.accounts.addEventHandlers({
    onLogin: async () => {
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
  gigya.hasSession((session) => {
    if (session) return continueSSO();
  })
}