interface GigyaPassword {
  created: string;
}

interface GigyaProfile {
  firstName: string;
  lastName: string;
  country: string;
  email: string;
}

interface GigyaChannels {
  // channels 객체의 구체적인 타입 정의 필요
  [key: string]: any;
}

interface GigyaData {
  accountID: string;
  isCompanyAdmin: boolean;
  userStatus: 'active' | string;
  channels: GigyaChannels;
  reasonCompanyAdmin: string;
  [key: string]: any; // 추가 데이터 필드를 위한 인덱스 시그니처
}

interface GigyaRequestParams {
  connectWithoutLoginBehavior: string;
  defaultRegScreenSet: string;
  defaultMobileRegScreenSet: string;
  sessionExpiration: number;
  rememberSessionExpiration: number;
  [key: string]: any; // 추가 파라미터를 위한 인덱스 시그니처
}

export interface GigyaAccountInfo {
  UID: string;
  UIDSignature: string;
  apiVersion: number;
  callId: string;
  context: undefined;
  created: string;
  createdTimestamp: number;
  data: GigyaData;
  errorCode: number;
  errorMessage: string;
  isActive: boolean;
  isRegistered: boolean;
  isVerified: boolean;
  lastLogin: string;
  lastLoginTimestamp: number;
  lastUpdated: string;
  lastUpdatedTimestamp: number;
  loginProvider: string;
  oldestDataUpdated: string;
  oldestDataUpdatedTimestamp: number;
  operation: string;
  password: GigyaPassword;
  profile: GigyaProfile;
  registered: string;
  registeredTimestamp: number;
  requestParams: GigyaRequestParams;
  signatureTimestamp: string;
  socialProviders: string;
  status: string;
  statusMessage: string;
  time: string;
  verified: string;
  verifiedTimestamp: number;
}
