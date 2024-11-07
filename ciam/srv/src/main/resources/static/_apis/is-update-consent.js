async function isUpdateConsent({UID = getItemWithExpiry('user_uid')}) {
  const currentChannel = new URLSearchParams(location.search).get('channel');

  if (currentChannel === 'btp') return;

  const res = await axios.post('/new-consent/consentVersionCheck', {
    uid: UID,
    channel: currentChannel,
  })

  const isUpdateRequired = res.data === 'Y';

  if (isUpdateRequired) {
    return new Promise(() => {
      const form = document.createElement('form');
      form.method = 'POST';
      form.action = `/consent-update/${currentChannel}`;

      const uidInput = document.createElement('input');
      uidInput.type = 'hidden';
      uidInput.name = 'uid';
      uidInput.value = getItemWithExpiry('user_uid');

      form.appendChild(uidInput);

      document.body.appendChild(form);

      form.submit();
    })
  }

  return res;
}