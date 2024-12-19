import { gigyaAPI } from '@/features';

import type { ChannelType } from '@/types';

type SubmitParams = Record<string, string | undefined>;

interface AuthRedirectType {
  submit: (action: string, params: SubmitParams) => void;
  javaChannelExpand: (hostURL: string, uid: string, channel: ChannelType) => void;
  javaNewChannelAD: (hostURL: string, uid: string, channel: ChannelType) => void;
  approvalStatusError: (approvalStatus: string) => void;
  phpMyPage: (hostURL: string) => void;
  consentUpdate: (uid: string, channel: ChannelType) => void;
  tfaOtp: () => void;
  tfaEmail: () => void;
  forgotPassword: () => void;
  samsungEmployeeLogin: () => Promise<void>;
}

const submit = (action: string, params: SubmitParams) => {
  const form = document.createElement('form');
  form.method = 'POST';
  form.action = action;

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined) {
      const input = document.createElement('input');
      input.type = 'hidden';
      input.name = key;
      input.value = String(value);
      form.appendChild(input);
    }
  });

  document.body.appendChild(form);
  return form.submit();
};

export const authRedirect: AuthRedirectType = {
  submit,

  javaChannelExpand: (hostURL, uid, channel) => {
    return submit(`${hostURL}/new-channel/ssoAccess`, {
      cdc_uid: uid,
      targetChannel: channel,
    });
  },

  javaNewChannelAD: (hostURL, uid, channel) => {
    return submit(`${hostURL}/new-channel/register/${channel}`, {
      adCdcUid: uid,
    });
  },

  approvalStatusError: (approvalStatus) => {
    return location.assign(`/approval-status-error?approvalStatus=${approvalStatus}`);
  },

  phpMyPage: (hostURL) => {
    return location.assign(`${hostURL}`);
  },

  consentUpdate: (uid, channel) => {
    return submit(`/consent-update/${channel}`, {
      uid: uid,
    });
  },

  tfaOtp: () => {
    return location.assign(`${location.pathname}/tfa/otp${location.search}`);
  },

  tfaEmail: () => {
    return location.assign(`${location.pathname}/tfa/email${location.search}`);
  },

  forgotPassword: () => {
    return location.assign(`${location.pathname}/forgot-password${location.search}`);
  },

  samsungEmployeeLogin: () => {
    return gigyaAPI.accounts.socialLogin('saml-samsung-ad');
  },
};
