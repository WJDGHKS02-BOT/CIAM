import { useEffect, useState } from 'react';

import { LoadingSpinner } from '@/components';
import { getGigyaConfigByChannel, loadGigyaJS, loadGigyaSamlJS, UserAuthorization } from '@/features';
import { useLoadingSpinnerStore } from '@/states';
import type { ChannelType } from '@/types';

const SignInProxy = () => {
  const [isLoadedGigya, setIsLoadedGigya] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const { showDefaultLoading, hideLoading } = useLoadingSpinnerStore();

  const params = new URLSearchParams(window.location.search);
  const configs = getGigyaConfigByChannel(params.get('channel') as ChannelType);

  useEffect(() => {
    try {
      showDefaultLoading();

      if (isLoadedGigya) return;
      loadGigyaJS(configs.apiKey).then((isSuccess: boolean) => setIsLoadedGigya(isSuccess));
    } catch (err) {
      console.error(err);
    }
  }, [isLoadedGigya, hideLoading, showDefaultLoading, configs.apiKey]);

  useEffect(() => {
    try {
      showDefaultLoading();

      if (!isLoadedGigya) return;

      window.onGigyaServiceReady = async () => {
        window.gigya.hasSession().then(async (hasSession: boolean) => {
          if (hasSession) {
            console.log('hasSession');
            setIsLoggedIn(true);
          } else {
            console.log('hasSession false');
            // TODO: BTP 로그인 인 경우 에러 페이지 이동 로직 추가
            return await loadGigyaSamlJS(configs.apiKey, configs.loginURL, configs.logoutURL);
          }
        });
      };
    } catch (err) {
      console.error(err);
    }
  }, [configs.apiKey, configs.loginURL, configs.logoutURL, isLoadedGigya, showDefaultLoading]);

  return (
    <>
      {isLoggedIn && <UserAuthorization {...configs} />}
      <LoadingSpinner />
    </>
  );
};

export default SignInProxy;
