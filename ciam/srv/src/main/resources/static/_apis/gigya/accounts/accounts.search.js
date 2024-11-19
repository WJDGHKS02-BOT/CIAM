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
    const userInfo = response.data.results[0]?.data;
    const APPROVAL_STATUS = userInfo?.channels[CHANNEL]?.approvalStatus;
    const USER_STATUS = userInfo.userStatus;
    const isAdUser = response.data.results[0].socialProviders.split(',').includes('saml-samsung-ad');

    if (USER_STATUS === 'active' || USER_STATUS === 'inactive') {
      if (isBtpLogin) return;

      switch (APPROVAL_STATUS) {
        case 'approved':
          if (isAdLogin) setItemWithExpiry('user_uid', response.data.results[0].UID);
          return;
        case 'inactive':
          if (isAdLogin) setItemWithExpiry('user_uid', response.data.results[0].UID);
          return;
        case 'pending':
          return location.assign(`/approval-status-error?approvalStatus=pending`);
        case 'emp-pending':
          return location.assign(`/approval-status-error?approvalStatus=emp-pending`);
        case 'disabled':
          return location.assign(`/approval-status-error?approvalStatus=disabled`);
        default:
          const SFDC = ['e2e', 'ets', 'partnerhub', 'mmp', 'edo'];

          if (SFDC.includes(CHANNEL)) return location.assign(`${HOST_URL.php}`);
          return ssoAccess(userInfo.UID, CHANNEL);
      }
    } else if (USER_STATUS === 'regSubmit') {
      return location.assign(`/approval-status-error?approvalStatus=pending`);
    } else {
      return location.assign(`/approval-status-error?approvalStatus=disabled`);
    }
  }

  const response = await callAPI({query});
  return handleResponse(response);
}

export default accounts_search;

function ssoAccess(uid, channel) {
  const form = document.createElement('form');
  form.method = 'POST';
  form.action = `${HOST_URL.java}/new-channel/ssoAccess`;

  const uidInput = document.createElement('input');
  const channelInput = document.createElement('input');

  uidInput.type = 'hidden';
  uidInput.name = 'cdc_uid';
  uidInput.value = uid;
  channelInput.type = 'hidden';
  channelInput.name = 'targetChannel';
  channelInput.value = channel;

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