import axios from 'axios';

import type { ChannelType } from '@/types';

export const consentVersionCheck = async (uid: string, channel: ChannelType) => {
  if (channel === 'btp') return;

  const res = await axios.post('/new-consent/consentVersionCheck', {
    uid,
    channel,
  });

  return res.data;
};
