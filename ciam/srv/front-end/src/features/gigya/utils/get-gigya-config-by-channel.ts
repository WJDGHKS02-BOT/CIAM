import type { ChannelType } from '@/types';

export type GigyaConfigType = {
  parentKey: string;
  apiKey: string;
  loginURL: string;
  logoutURL: string;
  channel: ChannelType;
};

export const getGigyaConfigByChannel = (channel: ChannelType): GigyaConfigType => {
  const parentKey = import.meta.env.VITE_GIGYA_PARENT_KEY;

  const { origin: hostURL, search: params } = window.location;

  const createLoginURL = () => {
    return `${hostURL}/sign-in${params}`;
  };

  const createLogoutURL = () => {
    return `${hostURL}/signin/${channel}/logout${params}`;
  };

  const defaultConfigs = {
    parentKey,
    loginURL: createLoginURL(),
    logoutURL: createLogoutURL(),
    channel: channel,
  };

  switch (channel) {
    case 'btp':
      return {
        ...defaultConfigs,
        apiKey: import.meta.env.VITE_GIGYA_API_KEY_PARENT,
      };
    case 'sba':
      return {
        ...defaultConfigs,
        apiKey: import.meta.env.VITE_GIGYA_API_KEY_SBA,
      };
  }
};
