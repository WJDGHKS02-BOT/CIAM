declare global {
  interface ServerData {
    GIGYA_API_KEY: string;
    GIGYA_PARENT_KEYS: string;
    GIGYA_INSTANCE_URL: string;
    SAMSUNG_INSTANCE_URL: string;
    LOGIN_URL: string;
    LOGOUT_URL: string;
    HOST_URL: {
      java: string;
      php: string;
    };
    CHANNEL: string;
  }

  interface Window {
    serverData: ServerData;
    gigya: any;
    onGigyaServiceReady: () => void;
  }

  interface ImportMetaEnv {
    VITE_API_URL: string;
    VITE_GIGYA_API_BASE_URL: string;
    VITE_SAMSUNG_API_BASE_URL: string;

    VITE_GIGYA_API_KEY_PARENT: string;
    VITE_GIGYA_API_KEY_E2E: string;
    VITE_GIGYA_API_KEY_EBIZ: string;
    VITE_GIGYA_API_KEY_ECIMS: string;
    VITE_GIGYA_API_KEY_EDO: string;
    VITE_GIGYA_API_KEY_ETS: string;
    VITE_GIGYA_API_KEY_GMAPDA: string;
    VITE_GIGYA_API_KEY_GMAPMX: string;
    VITE_GIGYA_API_KEY_GMAPVD: string;
    VITE_GIGYA_API_KEY_JCEXT: string;
    VITE_GIGYA_API_KEY_JJS: string;
    VITE_GIGYA_API_KEY_MMP: string;
    VITE_GIGYA_API_KEY_PARTNERHUB: string;
    VITE_GIGYA_API_KEY_SBA: string;
    VITE_GIGYA_API_KEY_SLAP: string;
    VITE_GIGYA_API_KEY_TNP: string;
    VITE_GIGYA_API_KEY_TOOLMATE: string;
    VITE_HOST_URL_JAVA: string;
    VITE_HOST_URL_PHP: string;

    readonly env: ImportMetaEnv;
  }
}

export {};
