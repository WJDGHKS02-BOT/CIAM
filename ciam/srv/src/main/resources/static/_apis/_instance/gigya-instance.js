const GIGYA_API_URL = "";
const gigyaInstance = axios.create({
  baseURL: GIGYA_API_URL,
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded'
  },
  withCredentials: true
});