import { useEffect, useState } from 'react';

import type { GigyaConfigType } from '@/features';
import { gigyaAPI, loadGigyaSamlJS } from '@/features';
import { AuthValidator } from '../utils/auth-validator';
import type { GigyaAccountInfo } from '@/types';

export const UserAuthorization = (configs: GigyaConfigType) => {
  const [info, setInfo] = useState<GigyaAccountInfo>();
  const [processOk, setProcessOk] = useState(false);

  useEffect(() => {
    const handleGetAccountInfo = async () => {
      try {
        const accountInfo = await gigyaAPI.accounts.getAccountInfo();
        console.log('account info', accountInfo);
        return setInfo(accountInfo);
      } catch (err) {
        console.error(err);
      }
    };
    handleGetAccountInfo();
  }, []);

  useEffect(() => {
    const authProcess = async () => {
      if (info) {
        const validator = new AuthValidator(info, configs.channel);
        const result = await validator.validateAll();

        if (!result.success) {
          if (result.redirect) await result.redirect();
          else if (result.error) console.error(result.error);
        } else setProcessOk(true);
      }
    };

    authProcess();
  }, [configs.channel, info]);

  // 모든 분기 처리, 인증 끝날 시 saml 스크립트 로드
  useEffect(() => {
    if (processOk) {
      const handleLoadSAML = async () => {
        return await loadGigyaSamlJS(configs.apiKey, configs.loginURL, configs.logoutURL);
      };
      handleLoadSAML();
    }
  }, [configs.apiKey, configs.loginURL, configs.logoutURL, processOk]);

  return <></>;
};

export default UserAuthorization;
