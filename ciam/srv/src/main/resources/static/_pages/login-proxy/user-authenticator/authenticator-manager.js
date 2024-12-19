import {
  setupGigyaEventListeners,
  setupGigyaServiceReady,
} from '../login-proxy-gigya-service-ready.js';
import { ChannelManager } from '../user-authenticator/channel-manager.js';
import { UserValidator } from '../user-authenticator/user-validator.js';

import { Redirect } from '../../redirect.js';

export class AuthenticationManager {
  constructor(gigyaLoader) {
    this._gigyaLoader = gigyaLoader;
    this.userInfo = null;
    this.userValidator = null;
    this.channelManager = null;
    this.isGigyaReady = false;

    setupGigyaServiceReady(async () => {
      this.isGigyaReady = true;
      await this.initGigyaEvents();
    });
  }

  async updateLastLogin() {
    if (CHANNEL === 'btp') {
      return;
    }
    
    return await accounts.setAccountInfo({
      lastLogin: new Date().toISOString(),
    });
  }

  async initGigyaEvents() {
    const handlers = {
      onHasSession: async (userInfo) => {
        await this.loadUserData(userInfo);
        return await this.authenticateUser();
      },
      onAdLogin: async (userInfo) => {
        await this.loadUserData(userInfo);
        return await this.authenticateUser();
      },
      onBtpError: () => {
        return Redirect.approvalStatusError('btp');
      },
      onNoSession: async () => {
        new SignInSession().clearSession();
        return await this._gigyaLoader.loadGigyaSamlJS();
      },
    };

    await setupGigyaEventListeners(this.isGigyaReady, handlers);
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
      await this.updateLastLogin();
      debugger;
      await this._gigyaLoader.loadGigyaSamlJS();
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
      return processManager.error('Invalid user');
    }
  }

  // User Status, Approval Status 에 따라 처리
  async handleUserStatus() {
    const isAdChannelRegistration = await this.channelManager.isInitialADChannelRegistration();
    // AD 사용자인 경우 AD 채널 등록 여부 확인
    if (isAdChannelRegistration) {
      // AD 로그인, 최초 채널 가입 인 경우 - btp는 에러가 되어야함.
      if (CHANNEL === 'btp') return Redirect.approvalStatusError('btp');
      else return Redirect.javaNewChannelAD(this.userInfo.UID, CHANNEL);
    } else if (this.userValidator.isActiveUser()) {
      return await this.channelManager.handleApprovalStatus();
    } else if (this.userValidator.getUserStatus() === 'regSubmit') {
      return Redirect.approvalStatusError('pending');
    } else if (this.userValidator.getUserStatus() === 'regEmailVerified') {
      return Redirect.approvalStatusError('emailVerified');
    } else {
      return Redirect.approvalStatusError('disabled');
    }
  }

  // 약관 업데이트가 필요한 사용자 인지 확인
  async checkConsentUpdate() {
    const isRequiredUpdateConsent = await isUpdateConsent({
      UID: this.userInfo.UID,
    }) === 'Y';

    if (isRequiredUpdateConsent) {
      return Redirect.consentUpdate(CHANNEL, this.userInfo.UID);
    }
  }
}

window.AuthenticationManager = AuthenticationManager;

export default AuthenticationManager;