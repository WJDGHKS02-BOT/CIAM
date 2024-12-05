import axios from 'axios';

import type { AxiosInstance } from 'axios';

export const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// TODO: 인터셉터 추가

export default instance;
