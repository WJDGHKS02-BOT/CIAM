package com.samsung.ciam.services;

import com.samsung.ciam.config.SamsungMfaConfig;
import com.samsung.ciam.controllers.SingleIdMfaJwtToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SamsungMfaService {
  private final SamsungMfaConfig mfaConfig;
  private final String REQUEST_ID = UUID.randomUUID().toString(); // 인증 결과 확인 시에 검증 용도로 유니크 Key를 생성합니다.
  private final long EXPIRATION_MILLS = 600000L; // 인증 요청 토큰 만료 시간 (milliseconds 단위)

  public SamsungMfaService(SamsungMfaConfig mfaConfig) {
    this.mfaConfig = mfaConfig;
  }

  public String getLoginMFA(String uid, String returnUrl, String email, String displayUid, Boolean isBioOnly) throws Exception {
    byte[] decodedSecretKey;
    String consumerId;
    String jwtRequest;

    // 생체 인증 Only 여부에 따라 Secret Key를 설정합니다.
    if (isBioOnly) {
      decodedSecretKey = Decoders.BASE64.decode(mfaConfig.getBioSecretKey());
      consumerId = mfaConfig.getBioConsumerKey();
    } else {
      decodedSecretKey = Decoders.BASE64.decode(mfaConfig.getAllSecretKey());
      consumerId = mfaConfig.getAllConsumerKey();
    }

    //공통 Util 호출하여 토큰 서명, 암호화에 사용할 Key를 구성합니다.
    SingleIdMfaJwtToken.CompositeKey compositeKey = new SingleIdMfaJwtToken.CompositeKey(decodedSecretKey);

    try {
      jwtRequest = SingleIdMfaJwtToken.createJwtToken(uid, consumerId, REQUEST_ID, email, displayUid, EXPIRATION_MILLS, compositeKey, returnUrl);
    } catch (Exception e) {
      return "";
    }

    return mfaConfig.getUrl() + "?jwtTokenRequest=" + jwtRequest;
  }


  public void authenticate(String jwtTokenResponse, Boolean isBioOnly) throws Exception {
    byte[] decodedSecretKey;

    if (isBioOnly) {
      decodedSecretKey = Decoders.BASE64.decode(mfaConfig.getBioSecretKey());
    } else {
      decodedSecretKey = Decoders.BASE64.decode(mfaConfig.getAllSecretKey());
    }

    // 공통 Util 호출하여 토큰 검증에 사용할 Key를 구성합니다.
    SingleIdMfaJwtToken.CompositeKey compositeKey = new SingleIdMfaJwtToken.CompositeKey(decodedSecretKey);

    // SingleID에서 보낸 토큰의 만료 시간 오차범위 설정.
    long clockSkewSeconds = 600000L;

    Claims claims = SingleIdMfaJwtToken.verifySingleIDToken(jwtTokenResponse, compositeKey, clockSkewSeconds);
    String username = claims.get("uid", String.class); // 사용자 아이디
    String requestId = claims.get("req", String.class); // consumer key
    String result = claims.get("ret", String.class); // 결과 값 0: 실패, 1: 성공
    String message = claims.get("msg", String.class); // 실패 메시지


    if ("1".equals(result)) {

      //별도 구현 필요: 인증 요청 시 저장한 requestId와 사용자 매핑 정보가 존재하는지 확인 하고 사용 완료 또는 삭제 처리 하는 로직을 별도 구현 합니다.

      //별도 구현 필요: 각 시스템 상황에 맞게 인증 완료된 사용자 인증 세션을 생성합니다.

    } else {

      //별도 구현 필요: 사용자가 인증 취소 했을 때, 각 시스템 별로 상황에 맞게 로직을 별도 구현 합니다.

    }
  }
}
