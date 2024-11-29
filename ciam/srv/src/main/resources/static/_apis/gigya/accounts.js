import accounts_login from './accounts/accounts.login.js';
import accounts_logout from './accounts/accounts.logout.js';
import accounts_getProviders from "./accounts/accounts.getProviders.js";
import accounts_tfa_initTfa from "./accounts/accounts.tfa.initTFA.js";
import accounts_tfa_email_getEmails
  from "./accounts/accounts.tfa.email.getEmails.js";
import accounts_tfa_email_completeVerification
  from "./accounts/accounts.tfa.email.completeVerification.js";
import accounts_tfa_email_sendVerification
  from "./accounts/accounts.tfa.email.sendVerification.js";
import accounts_tfa_totp_register
  from "./accounts/accounts.tfa.totp.register.js";
import accounts_tfa_totp_verify from "./accounts/accounts.tfa.totp.verify.js";
import accounts_tfa_finalizeTFA from "./accounts/accounts.tfa.finalizeTFA.js";
import accounts_finalizeRegistration
  from "./accounts/accounts.finalizeRegistration.js";
import accounts_getAccountInfo from "./accounts/accounts.getAccountInfo.js";
import accounts_resetPassword from "./accounts/accounts.resetPassword.js";
import accounts_search from "./accounts/accounts.search.js";
import accounts_setAccountInfo from "./accounts/accounts.setAccountInfo.js";
import accounts_isAvailable from "./accounts/accounts.isAvailable.js";
import accounts_identifier_createToken
  from "./accounts/accounts.identifier.createToken.js";
import accounts_auth_getMethods from "./accounts/accounts.auth.getMethods.js";
import accounts_auth_fido_register
  from "./accounts/accounts.auth.fido.register.js";
import accounts_auth_fido_login from "./accounts/accounts.auth.fido.login.js";
import accounts_auth_fido_initRegisterCredentials
  from "./accounts/accounts.auth.fido.initRegisterCredentials.js";

const accounts = {
  auth: {
    getMethods: accounts_auth_getMethods,
    fido: {
      initRegisterCredentials: accounts_auth_fido_initRegisterCredentials,
      login: accounts_auth_fido_login,
      register: accounts_auth_fido_register,
    }
  },
  login: accounts_login,
  logout: accounts_logout,
  finalizeRegistration: accounts_finalizeRegistration,
  getAccountInfo: accounts_getAccountInfo,
  isAvailableLoginID: accounts_isAvailable,
  identifier: {
    createToken: accounts_identifier_createToken,
  },
  resetPassword: accounts_resetPassword,
  search: accounts_search,
  setAccountInfo: accounts_setAccountInfo,
  socialLogin: ({provider}) => {
    return gigya.accounts.socialLogin({provider: provider});
  },
  tfa: {
    email: {
      completeVerification: accounts_tfa_email_completeVerification,
      sendVerificationCode: accounts_tfa_email_sendVerification,
    },
    finalizeTFA: accounts_tfa_finalizeTFA,
    getEmails: accounts_tfa_email_getEmails,
    getProviders: accounts_getProviders,
    initTFA: accounts_tfa_initTfa,
    totp: {
      register: accounts_tfa_totp_register,
      verify: accounts_tfa_totp_verify,
    },
  },
}

window.accounts = accounts;