import {GigyaScriptLoader} from "../../_apis/gigya/gigya-script-loader.js";
import {InstanceManager} from "../../_apis/_instance/instance-manager.js";

export class LoginProxyLoader {
  constructor(config) {
    this.gigyaLoader = null;
    this.instanceManager = null;

    this.initialize(config);
  }

  initialize(config) {
    const gigyaConfig = {
      apiKey: config.GIGYA_API_KEY,
      loginURL: config.LOGIN_URL,
      logoutURL: config.LOGOUT_URL,
    };

    const instanceURLs = {
      gigyaURL: config.GIGYA_INSTANCE_URL,
      samsungURL: config.SAMSUNG_INSTANCE_URL,
    };

    this.instanceManager = new InstanceManager(config.GIGYA_API_KEY);
    this.instanceManager.setUpInstanceSettings(instanceURLs);
    this.gigyaLoader = new GigyaScriptLoader(gigyaConfig);

    return {
      gigyaLoader: this.gigyaLoader,
      instanceManager: this.instanceManager,
    };
  }
}

window.LoginProxyLoader = LoginProxyLoader;

export default LoginProxyLoader;