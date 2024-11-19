class AuthenticationManager {
  constructor() {
    this.userInfo = null;
    this.userValidator = null;
    this.channelManager = null;
    this.isGigyaReady = false;
    this.setupGigyaServiceReady();
  }

  // 기갸 api 스크립트 불러오면 실행
  setupGigyaServiceReady() {
    window.onGigyaServiceReady = () => {
      this.isGigyaReady = true;
      this.setupGigyaEventListeners();
    };
  }

  // 기갸 스크립트에 이벤트 리스너 등록
  setupGigyaEventListeners() {
    if (!this.isGigyaReady) return;

    gigya.hasSession().then(async (hasSession) => {
      // 기갸 세션이 있는 경우
      if (hasSession) {
        debugger;
        const userInfo = await accounts.getAccountInfo();
        await this.loadUserData(userInfo);
        return await this.authenticateUser();
      } else {
        // 기갸 세션이 없는 경우
        if (new URLSearchParams(location.search).get('adLogin')) {
          // ad 로그인을 시도했지만 206001 에러가 발생한 경우 params에 adLogin, uid가 있음
          const params = new URLSearchParams(location.search);
          if (params.get('channel') === 'btp') {
            return SignInRedirect.approvalStatusError('btp');
          }
          const {data: userInfo} = await axios.post('/getAccountInfo', {uid: params.get('uid')});
          await this.loadUserData(userInfo);
          return await this.authenticateUser();
        }
        debugger;
        new SignInSession().clearSession();
        return await gigyaLoader.loadGigyaSamlJS();
      }
    })
  }

  // 초기 세팅 확인
  async checkInitialization() {
    if (!gigyaLoader) {
      console.error(new Error('Gigya loader is not initialized yet'));
      return false;
    }

    if (!instanceManager) {
      console.error(new Error('Instance Manager is not initialized yet'));
      return false;
    }

    return true;
  }

  // 사용자 인증
  async authenticateUser() {
    debugger;
    try {
      await this.validateUser();
      debugger;
      await this.handleUserStatus();
      debugger;
      await this.checkConsentUpdate();
      debugger;
      await gigyaLoader.loadGigyaSamlJS();
    } catch (error) {
      console.error('Authentication failed:', error);
      throw error;
    }
  }

  // 로그인 된 유저의 정보로 인스턴스를 생성
  async loadUserData(userInfo) {
    this.userInfo = userInfo;
    this.userValidator = new UserValidator(userInfo);
    this.channelManager = new ChannelManager(userInfo, this.userValidator.isAdUser());
  }

  // 비밀번호 변경이 필요한 사용자 인지, 비활성화된 사용자 인지 확인
  async validateUser() {
    if (this.userValidator.isPasswordChangeRequired()) {
      return this.userValidator.handlePasswordReset();
    }

    if (!this.userValidator.isValidUser()) {
      throw new Error('Invalid user');
    }
  }

  // User Status, Approval Status 에 따라 처리
  async handleUserStatus() {
    const isAdChannelRegistration = await this.channelManager.isInitialADChannelRegistration();
    // AD 사용자인 경우 AD 채널 등록 여부 확인
    if (isAdChannelRegistration) {
      // AD 로그인, 최초 채널 가입 인 경우 - btp는 에러가 되어야함.
      if (CHANNEL === 'btp') return SignInRedirect.approvalStatusError('btp');
      else return SignInRedirect.javaNewChannelAD(this.userInfo.UID, CHANNEL);
    } else if (this.userValidator.isActiveUser()) {
      return await this.channelManager.handleApprovalStatus();
    } else if (this.userValidator.getUserStatus() === 'regSubmit') {
      return SignInRedirect.approvalStatusError('pending');
    } else {
      return SignInRedirect.approvalStatusError('disabled');
    }
  }

  // 약관 업데이트가 필요한 사용자 인지 확인
  async checkConsentUpdate() {
    const isRequiredUpdateConsent = await isUpdateConsent({
      UID: this.userInfo.UID
    }) === 'Y';

    if (isRequiredUpdateConsent) {
      return SignInRedirect.consentUpdate(CHANNEL, this.userInfo.UID);
    }
  }
}