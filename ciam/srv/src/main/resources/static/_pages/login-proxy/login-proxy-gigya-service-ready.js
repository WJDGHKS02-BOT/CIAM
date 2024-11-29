/*
  로그인 프록시 페이지에서 사용되는 해당 서비스는
  이벤트 핸들러를 추가하지 않고, Gigya Session 확인 작업만을 진행합니다.
*/
const params = new URLSearchParams(location.search);
const isAdLogin = params.get('adLogin');
const isBtpLogin = params.get('channel') === 'btp';

export function setupGigyaServiceReady(callback) {
  window.onGigyaServiceReady = () => {
    callback();
  };
}

export async function setupGigyaEventListeners(isGigyaReady, handlers) {
  if (!isGigyaReady) return;

  const hasSession = await gigya.hasSession();

  if (hasSession) {
    const userInfo = await accounts.getAccountInfo();
    await handlers.onHasSession(userInfo);
  } else {
    if (isAdLogin) {
      if (isBtpLogin) {
        return handlers.onBtpError();
      }
      const {data: userInfo} = await axios.post('/getAccountInfo', {uid: params.get('uid')});
      await handlers.onAdLogin(userInfo);
    } else {
      return handlers.onNoSession();
    }
  }
}