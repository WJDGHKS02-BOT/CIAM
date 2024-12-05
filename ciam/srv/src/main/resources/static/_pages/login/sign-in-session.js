class SignInSession {
  constructor() {
    this.sessionKey = 'cdc-sign-in';
    this.fields = [
      'regToken',
      'provider',
      'gigyaAssertion',
      'emailID',
      'emailObfuscated',
      'authCode',
      'phvToken',
      'providerAssertion',
      'sctToken',
      'UID',
      'userInfo',
      'reloaded'
    ];
  }

  getSession() {
    return JSON.parse(sessionStorage.getItem(this.sessionKey)) || {};
  }

  setSession(updates) {
    const updatedData = {...this.getSession(), ...updates};
    sessionStorage.setItem(this.sessionKey, JSON.stringify(updatedData));
  }

  clearSession() {
    return sessionStorage.removeItem(this.sessionKey);
  }

  generateAccessors() {
    this.fields.forEach(field => {
      Object.defineProperty(this, field, {
        get: () => this.getSession()[field] || null,
        set: (value) => this.setSession({[field]: value})
      });
    });
  }
}

const signInSession = new SignInSession();
signInSession.generateAccessors();