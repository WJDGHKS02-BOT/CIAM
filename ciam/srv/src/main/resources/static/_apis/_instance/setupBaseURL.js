function setupBaseURL({gigyaURL, samsungURL}) {
  samsungInstance.defaults.baseURL = samsungURL;
  gigyaInstance.defaults.baseURL = gigyaURL;
}