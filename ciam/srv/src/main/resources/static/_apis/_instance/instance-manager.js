export class SamsungAPI {
  constructor(apiKey) {
    this.apiKey = apiKey;
    this.instance = axios.create({
      baseURL: '',
      headers: {
        'Content-Type': 'application/json'
      },
    });
  }

  async request(endpoint, data = {}) {
    return this.instance.post(endpoint, {
      ...data,
      apiKey: this.apiKey
    });
  }

  setBaseURL(url) {
    this.instance.defaults.baseURL = url;
  }
}

export class InstanceManager {
  constructor(apiKey) {
    this.gigyaInstance = axios.create({
      baseURL: '',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      withCredentials: true
    });

    this.samsung = new SamsungAPI(apiKey);
  }

  getInstances() {
    return {
      gigyaInstance: this.gigyaInstance,
      samsung: this.samsung
    };
  }

  setUpInstanceSettings({gigyaURL, samsungURL}) {
    this.gigyaInstance.defaults.baseURL = gigyaURL;
    this.samsung.setBaseURL(samsungURL);
  }
}