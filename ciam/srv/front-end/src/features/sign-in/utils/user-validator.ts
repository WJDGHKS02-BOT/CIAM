import type { ChannelType, GigyaAccountInfo } from '@/types';

type UserValidatorReturn = {
  hasChannel: (user: GigyaAccountInfo) => boolean;
  isActiveUser: (user: GigyaAccountInfo) => boolean;
  isAdUser: (user: GigyaAccountInfo) => boolean;
  isPasswordChangeRequired: (user: GigyaAccountInfo) => boolean;
  isSFDCChannel: (channel: ChannelType) => boolean;
};

export const userValidator: UserValidatorReturn = {
  hasChannel: (user) => {
    if (user.data.channels !== undefined) return false;
    else return JSON.stringify(user.data.channels) !== '{}';
  },
  isActiveUser: (user) => user.data.userStatus === 'active' || user.data.userStatus === 'inactive',
  isAdUser: (user) => user.socialProviders.split(',').includes('saml-samsung-ad'),
  isPasswordChangeRequired: (user) => {
    const today: Date = new Date();
    const lastUpdatePassword: Date = new Date(user.password.created);
    const daysSinceUpdate = (today.getTime() - lastUpdatePassword.getTime()) / (1000 * 60 * 60 * 24);

    return daysSinceUpdate >= 90;
  },
  isSFDCChannel: (channel) => {
    const SFDC = ['e2e', 'ets', 'partnerhub', 'mmp', 'edo'];
    return SFDC.includes(channel);
  },
};
