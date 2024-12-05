declare global {
  interface ServerData {
    GIGYA_API_KEY: string;
    GIGYA_PARENT_KEYS: string;
    GIGYA_INSTANCE_URL: string;
    SAMSUNG_INSTANCE_URL: string;
    LOGIN_URL: string;
    LOGOUT_URL: string;
    HOST_URL: string;
    CHANNEL: string;
  }

  interface Window {
    serverData: ServerData;
  }

  interface ImportMetaEnv {
    VITE_API_URL: string;

    readonly env: ImportMetaEnv;
  }
}

export {};
