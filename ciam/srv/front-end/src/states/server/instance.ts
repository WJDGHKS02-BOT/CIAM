import axios from 'axios';

import type { AxiosInstance, CreateAxiosDefaults } from 'axios';

type ApiType = 'samsung' | 'gigya';

class httpClient {
  private static instances: Record<ApiType, AxiosInstance> = {} as Record<ApiType, AxiosInstance>;

  private constructor() {}

  private static getBaseUrl(type: ApiType): string {
    const urls: Record<ApiType, string> = {
      samsung: import.meta.env.VITE_SAMSUNG_API_URL,
      gigya: import.meta.env.VITE_GIGYA_API_URL,
    };
    return urls[type];
  }

  private static createDefaultOptions(type: ApiType): CreateAxiosDefaults {
    return {
      baseURL: this.getBaseUrl(type),
      headers: {
        'Content-Type': 'application/json',
      },
      withCredentials: true,
    };
  }

  public static getInstance(type: ApiType): AxiosInstance {
    if (!this.instances[type]) {
      this.instances[type] = axios.create(this.createDefaultOptions(type));
      // this.setupInterceptors(this.instances[type]);
    }
    return this.instances[type];
  }
  // TODO: 인터셉터 코드 추가 예정
  // private static setupInterceptors(instance: AxiosInstance): void {
  //   instance.interceptors.request.use(
  //     (config) => {
  //       return config;
  //     },
  //     (error) => {
  //       return Promise.reject(error);
  //     },
  //   );

  //   instance.interceptors.response.use(
  //     (response) => {
  //       return response;
  //     },
  //     (error) => {
  //       return Promise.reject(error);
  //     },
  //   );
  // }
}

export const gigyaInstance = httpClient.getInstance('gigya');
export const samsungInstance = httpClient.getInstance('samsung');

export default httpClient;
