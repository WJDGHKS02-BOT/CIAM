async function isUpdateConsent({UID = ''}) {
  const currentChannel = new URLSearchParams(location.search).get('channel');

  if (currentChannel === 'btp') return;

  const res = await axios.post('/new-consent/consentVersionCheck', {
    uid: UID,
    channel: currentChannel,
  })

  return res.data;
}