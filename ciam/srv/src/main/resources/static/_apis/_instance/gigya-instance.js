const gigyaInstance = axios.create({
  baseURL: '',
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded'
  },
  withCredentials: true
});