async function accounts_isAvailable(email) {
  const API_END_POINT = '/accounts.isAvailableLoginID';
  const API_PARAMS = {
    loginID: email,
    apiKey: GIGYA_PARENT_KEYS.apiKey,
  }
  const ERROR_CODES = {
    LOGIN_SUCCESS: 0,
  };

  async function callAPI() {
    try {
      return await gigyaInstance.post(API_END_POINT, new URLSearchParams(API_PARAMS));
    } catch (error) {
      console.error('Account IsAvailable API call failed:', error);
      throw new Error('Network error during login');
    }
  }

  async function handleResponse(res) {
    switch (res.errorCode) {
      case ERROR_CODES.LOGIN_SUCCESS:
        return res;
      default:
        return res;
    }
  }

  const response = await callAPI();
  return handleResponse(response.data);
}

export default accounts_isAvailable;