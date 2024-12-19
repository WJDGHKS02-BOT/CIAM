import type { ChannelType } from '@/types';

export const accounts_setAccountInfo = async ({ lastLogin, channel }: { lastLogin: Date; channel: ChannelType }) => {
  const ERROR_CODES = {
    SUCCESS: 0,
  };

  function callAPI() {
    const channelData: { [key: string]: { lastLogin: string } } = {};
    channelData[channel] = { lastLogin: lastLogin.toISOString() };

    return new Promise((resolve) => {
      window.gigya.accounts.setAccountInfo({
        data: {
          channels: channelData,
        },
        callback: function (response: any) {
          resolve(response);
        },
      });
    });
  }

  function handleResponse(response: any) {
    switch (response.errorCode) {
      case ERROR_CODES.SUCCESS:
        return response;
      default:
        // return showLoginPageResponseMessages('all.INVALID_LOGIN_REQUEST');
        return;
    }
  }

  const response = await callAPI();
  return handleResponse(response);
};
