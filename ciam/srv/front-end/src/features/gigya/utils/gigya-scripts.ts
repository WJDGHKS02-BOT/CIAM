import { loadScript } from '@/utils';

export const loadGigyaJS = async (apikey: string) => {
  return await loadScript(`https://cdns.gigya.com/js/gigya.js?apiKey=${apikey}`);
};

export const loadGigyaSamlJS = async (apikey: string, loginUrl: string, logoutUrl: string) => {
  return await loadScript(
    `https://cdns.gigya.com/js/gigya.saml.js?apiKey=${apikey}`,
    JSON.stringify({
      loginURL: loginUrl,
      logoutURL: logoutUrl,
    }),
  );
};
