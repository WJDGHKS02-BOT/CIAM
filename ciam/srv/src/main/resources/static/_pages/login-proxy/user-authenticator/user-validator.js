export class UserValidator {
  constructor(userInfo) {
    this.userInfo = userInfo;
  }

  isValidUser() {
    return !!this.userInfo;
  }

  isAdUser() {
    return this.userInfo.socialProviders.split(',').includes('saml-samsung-ad');
  }

  isPasswordChangeRequired() {
    if (this.isAdUser()) return false;

    const today = new Date();
    const lastUpdatePassword = new Date(this.userInfo.password.created);
    const daysSinceUpdate = (today - lastUpdatePassword) / (1000 * 60 * 60 * 24);

    return daysSinceUpdate >= 90;
  }

  async handlePasswordReset() {
    const res = await accounts.resetPassword({ loginID: this.userInfo.profile.email });

    if (res) return location.assign(`/approval-status-error?approvalStatus=pending`);
    else alert('비밀번호 재설정 이메일 발송에 실패했습니다. 다시 시도해주세요.');
  }

  getUserStatus() {
    return this.userInfo.data.userStatus;
  }

  isActiveUser() {
    const status = this.getUserStatus();
    return status === 'active' || status === 'inactive';
  }
}

export default UserValidator;