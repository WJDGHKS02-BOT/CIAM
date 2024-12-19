import axios from 'axios';

import type { ChannelType } from '@/types';

// account status를 active로 변경하는 API
export const setAccountStatus = async (uid: string, channel: ChannelType) => {
  return await axios.post('/setAccountStatus', {
    uid,
    channel,
  });
};

export default setAccountStatus;
