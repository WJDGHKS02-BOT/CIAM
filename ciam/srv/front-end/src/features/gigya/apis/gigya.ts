import accounts_getAccountInfo from './accounts.getAccountInfo';
import accounts_resetPassword from './accounts.resetPassword';
import { accounts_setAccountInfo } from './accounts.setAccountInfo';
import accounts_socialLogin from './accounts.socialLogin';

export const gigyaAPI = {
  accounts: {
    getAccountInfo: accounts_getAccountInfo,
    resetPassword: accounts_resetPassword,
    setAccountInfo: accounts_setAccountInfo,
    socialLogin: accounts_socialLogin,
  },
};
export default gigyaAPI;
