class SignInRedirect {
  static submit(action, params) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = action;

    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = key;
        input.value = value;
        form.appendChild(input);
      }
    });

    document.body.appendChild(form);
    return form.submit();
  }

  static javaChannelExpand(uid, channel) {
    return this.submit(`${HOST_URL.java}/new-channel/ssoAccess`, {
      cdc_uid: uid,
      targetChannel: channel
    });
  }

  static javaNewChannelAD(uid, channel) {
    return this.submit(`${HOST_URL.java}/new-channel/register/${channel}`, {
      adCdcUid: uid,
    });
  }

  static approvalStatusError(approvalStatus) {
    return location.assign(`/approval-status-error?approvalStatus=${approvalStatus}`);
  }

  static phpMyPage() {
    return location.assign(`${HOST_URL.php}`);
  }

  static consentUpdate(channel, uid) {
    return this.submit(`/consent-update/${channel}`, {
      uid: uid,
    });
  }

  static tfaOtp() {
    return location.assign(`${location.pathname}/tfa/otp${location.search}`);
  }

  static tfaEmail() {
    return location.assign(`${location.pathname}/tfa/email${location.search}`);
  }

  static forgotPassword() {
    return location.assign(`${location.pathname}/forgot-password${location.search}`);
  }

  static samsungEmployeeLogin() {
    return accounts.socialLogin({provider: 'saml-samsung-ad'});
  }
}