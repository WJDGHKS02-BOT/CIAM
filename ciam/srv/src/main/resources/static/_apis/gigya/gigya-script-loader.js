class GigyaScriptLoader {
  constructor({apiKey, loginURL = '', logoutURL = ''}) {
    this.apiKey = apiKey;
    this.loginURL = loginURL;
    this.logoutURL = logoutURL;
  }

  async loadGigyaScript({src, options = {}}) {
    return new Promise((resolve, reject) => {
      console.log('Gigya Script Loading...');
      const script = document.createElement('script');
      script.type = 'text/javascript';
      script.async = true;
      script.src = src;

      Object.entries(options).forEach(([key, value]) => {
        script[key] = value;
      });

      script.onload = () => {
        console.log("Gigya Script Success...")
        debugger;
        resolve();
      }
      script.onerror = (error) => {
        console.log("Gigya Script Error...")
        debugger;
        reject(error);
      }
      document.getElementsByTagName('head')[0].appendChild(script);
    });
  }

  async loadGigyaJS() {
    console.log('load gigya js');
    return this.loadGigyaScript({
      src: `https://cdns.gigya.com/js/gigya.js?apikey=${this.apiKey}`,
    });
  }

  async loadGigyaSamlJS() {
    console.log('load gigya saml js');
    return setTimeout(() => {
      this.loadGigyaScript({
        src: `https://cdns.gigya.com/js/gigya.saml.js?apiKey=${this.apiKey}`,
        options: {
          innerHTML: JSON.stringify({
            loginURL: this.loginURL,
            logoutURL: this.logoutURL,
          })
        },
      });
    }, 1000)
  }
}