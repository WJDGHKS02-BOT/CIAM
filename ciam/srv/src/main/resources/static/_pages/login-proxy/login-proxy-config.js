import processManager
  from "../../_scripts/states/process-manager/process-manager.js";

export class LoginProxyConfig {
  constructor({
    GIGYA_API_KEY,
    GIGYA_INSTANCE_URL,
    SAMSUNG_INSTANCE_URL,
    LOGIN_URL,
    LOGOUT_URL
  }) {
    this.GIGYA_API_KEY = GIGYA_API_KEY;
    this.GIGYA_INSTANCE_URL = GIGYA_INSTANCE_URL;
    this.SAMSUNG_INSTANCE_URL = SAMSUNG_INSTANCE_URL;
    this.LOGIN_URL = LOGIN_URL;
    this.LOGOUT_URL = LOGOUT_URL;

    this.validateConfig({GIGYA_API_KEY, LOGIN_URL, LOGOUT_URL});
  }

  validateConfig({GIGYA_API_KEY, LOGIN_URL, LOGOUT_URL}) {
    if (!GIGYA_API_KEY || !LOGIN_URL || !LOGOUT_URL) {
      processManager.error('Required parameters are missing');
    }
  }
}

window.LoginProxyConfig = LoginProxyConfig;

export default LoginProxyConfig;