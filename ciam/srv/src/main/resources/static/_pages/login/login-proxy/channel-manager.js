class ChannelManager {
  constructor(userInfo, isAdUser) {
    this.userInfo = userInfo;
    this.isAdUser = isAdUser;
    this.params = new URLSearchParams(location.search);
  }

  hasChannel() {
    if (this.userInfo.data.channels === undefined) return false;
    else return JSON.stringify(this.userInfo.data.channels) !== '{}';
  }

  getChannelApprovalStatus() {
    return this.userInfo.data.channels[CHANNEL]?.approvalStatus;
  }

  isAdLogin() {
    return this.params.get('adLogin');
  }

  isBtpLogin() {
    return this.params.get('channel') === 'btp';
  }

  isSFDCChannel() {
    const SFDC = ['e2e', 'ets', 'partnerhub', 'mmp', 'edo'];
    return SFDC.includes(CHANNEL);
  }

  async isInitialADChannelRegistration() {
    return !this.hasChannel() && (this.isAdUser || this.isAdLogin());
  }

  async setAccountStatus() {
    return await axios.post('/setAccountStatus', {
      uid: this.userInfo.UID,
      channel: this.params.get('channel'),
    });
  }

  async handleApprovalStatus() {
    if (this.isBtpLogin()) return;

    const approvalStatus = this.getChannelApprovalStatus();

    switch (approvalStatus) {
      case 'approved':
      case 'inactive':
        return this.setAccountStatus();
      case 'pending':
      case 'emp-pending':
      case 'disabled':
        return SignInRedirect.approvalStatusError(approvalStatus);
      default:
        if (this.isAdUser) {
          return SignInRedirect.javaNewChannelAD(this.userInfo.UID, CHANNEL);
        }

        if (this.isSFDCChannel()) {
          return SignInRedirect.phpMyPage();
        }

        return SignInRedirect.javaChannelExpand(this.userInfo.UID, CHANNEL);
    }
  }
}