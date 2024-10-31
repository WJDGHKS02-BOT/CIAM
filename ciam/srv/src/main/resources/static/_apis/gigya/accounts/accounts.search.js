async function accounts_search({query, updateUserRecord}) {
  function callAPI({query}) {
    return gigyaInstance.post('/accounts.search', new URLSearchParams({
      apiKey: GIGYA_PARENT_KEYS.apiKey,
      secret: GIGYA_PARENT_KEYS.secretKey,
      userKey: GIGYA_PARENT_KEYS.userKey,
      query: query,
    }))
  }

  async function handleResponse(response) {
    const userInfo = response.data.results[0].data;
    const APPROVAL_STATUS = userInfo.channels[CHANNEL]?.approvalStatus;
    const USER_STATUS = userInfo.userStatus;

    if (isBtpLogin) return;

    switch (APPROVAL_STATUS) {
      case 'approved' || 'inactive':
        if (isAdLogin) signInSession.UID = response.data.results[0].UID;

        if (updateUserRecord) {
          return await samsung.updateUserRecord();
        } else return response;
      case 'pending':
        debugger;
        return location.assign(`/approval-status-error?approvalStatus=pending`);
      case 'emp-pending':
        debugger;
        return location.assign(`/approval-status-error?approvalStatus=emp-pending`);
      case 'disabled':
        debugger;
        return location.assign(`/approval-status-error?approvalStatus=disabled`);
      default:
        const SFDC = ['e2e', 'ets', 'partnerhub', 'mmp', 'edo'];

        if (isAdLogin || response.data.results[0].profile?.samlData) {
          signInSession.UID = response.data.results[0].UID;
          return newChannelRegister({UID: response.data.results[0].UID});
        }

        if (USER_STATUS === 'active') {
          SFDC.includes(CHANNEL)
              ? location.assign(`${HOST_URL.php}/login-error?apiKey=${GIGYA_API_KEY}&regToken=${params.get('regToken')}&newADLogin=true`)
              : ssoAccess();
        }

        return location.assign(`/approval-status-error?approvalStatus=pending`);
    }
  }

  const response = await callAPI({query});
  return handleResponse(response);
}

export default accounts_search;

function ssoAccess() {
  const form = document.createElement('form');
  form.method = 'POST';
  form.action = `${HOST_URL.java}/new-channel/ssoAccess`;

  const uidInput = document.createElement('input');
  const channelInput = document.createElement('input');

  uidInput.type = 'hidden';
  uidInput.name = 'cdc_uid';
  channelInput.type = 'hidden';
  channelInput.name = 'targetChannel';

  form.appendChild(uidInput);
  form.appendChild(channelInput);

  document.body.appendChild(form);

  return form.submit();
}

function newChannelRegister({UID}) {
  const form = document.createElement('form');
  form.method = 'POST';
  form.action = `${HOST_URL.java}/new-channel/register/${CHANNEL}`;

  const regTokenInput = document.createElement('input');
  const adCdcUidInput = document.createElement('input');

  regTokenInput.type = 'hidden';
  regTokenInput.name = 'regToken';

  adCdcUidInput.type = 'hidden';
  adCdcUidInput.name = 'adCdcUid';
  adCdcUidInput.value = UID;
  console.log(UID);
  console.log(adCdcUidInput.value)

  form.appendChild(regTokenInput);
  form.appendChild(adCdcUidInput);

  document.body.appendChild(form);

  return form.submit();
}