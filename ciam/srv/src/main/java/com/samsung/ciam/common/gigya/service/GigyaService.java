package com.samsung.ciam.common.gigya.service;

import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.beans.GigyaConfig;
import com.samsung.ciam.utils.BeansUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 1. FileName	: GigyaService.java
 * 2. Package	: com.samsung.ciam.common.gigya.service
 * 3. Comments	: Gigya API 요청을 수행하는 서비스 클래스
 * 4. Author	: 서정환
 * 5. DateTime	: 2024. 11. 04.
 * 6. History	:
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * Date		 |	Name			|	Comment
 * <p>
 * -------------  -----------------   ------------------------------
 * <p>
 * 2024. 11. 04.		 | 서정환			|	최초작성
 * <p>
 * -----------------------------------------------------------------
 */

@Service
public class GigyaService {

    @Autowired
    private GigyaConfig gigyaConfig;

    /*
     * 1. 메소드명: executeRequest
     * 2. 클래스명: GigyaService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    지정된 채널 및 API 메소드로 Gigya 요청 QUERY유형으로 실행하고 응답을 반환
     * 2. 사용법
     *    executeRequest("channel", "accounts.search", "SELECT * FROM accounts WHERE UID = 'user123'")
     * 3. 예시 데이터
     *    - Input:
     *      channel = "toolmate"
     *      apiMethod = "accounts.search"
     *      query = "SELECT * FROM accounts WHERE UID = 'user123'"
     *    - Output (예시):
     *      응답 코드: 200
     *      응답 데이터: {"statusCode":200,"statusReason":"OK","data":[{"UID":"user123","profile":{"firstName":"John","lastName":"Doe"}}]}
     * </PRE>
     * @param channel 채널 이름
     * @param apiMethod API 메소드 이름
     * @param query 실행할 쿼리 문자열
     * @return GSResponse API 요청 응답
     */
    public GSResponse executeRequest(String channel, String apiMethod, String query) {
        GigyaConfig.ChannelConfig config = gigyaConfig.getChannels().getOrDefault(channel, gigyaConfig.getChannels().get("default"));

        GSRequest request = new GSRequest(config.getApiKey(), config.getSecretKey(), apiMethod, null, true, config.getUserKey());
        request.setParam("query", query);
        request.setAPIDomain("au1.gigya.com");

        return request.send();
    }

    /*
     * 1. 메소드명: executeRequestSetParam
     * 2. 클래스명: GigyaService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    단일 매개변수를 포함한 API 메소드 호출을 통해 Gigya 요청을 실행하고 응답 반환
     * 2. 사용법
     *    executeRequestSetParam("accounts.getAccountInfo", "UID", "user123")
     * 3. 예시 데이터
     *    - Input:
     *      apiMethod = "accounts.getAccountInfo"
     *      paramName = "UID"
     *      paramValue = "user123"
     *    - Output (예시):
     *      응답 코드: 200
     *      응답 데이터: {"statusCode":200,"statusReason":"OK","data":{"UID":"user123","profile":{"firstName":"John","lastName":"Doe"}}}
     * </PRE>
     * @param apiMethod API 메소드 이름
     * @param paramName 매개변수 이름
     * @param paramValue 매개변수 값
     * @return GSResponse API 요청 응답
     */
    public GSResponse executeRequestSetParam(String apiMethod, String paramName,String paramValue) {
        GigyaConfig.ChannelConfig config = gigyaConfig.getChannels().getOrDefault("default", gigyaConfig.getChannels().get("default"));

        GSRequest request = new GSRequest(config.getApiKey(), config.getSecretKey(), apiMethod, null, true, config.getUserKey());
        request.setParam(paramName, paramValue);
        request.setAPIDomain("au1.gigya.com");

        return request.send();
    }

    /*
     * 1. 메소드명: executeRequest
     * 2. 클래스명: GigyaService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    매개변수 맵을 포함한 API 메소드 호출을 통해 Gigya 요청을 실행하고 응답을 반환
     * 2. 사용법
     *    executeRequest("toolmate", "accounts.setAccountInfo", Map.of("UID", "user123", "profile.firstName", "Jane"))
     * 3. 예시 데이터
     *    - Input:
     *      channel = "toolmate"
     *      apiMethod = "accounts.setAccountInfo"
     *      params = {"UID":"user123","profile.firstName":"Jane"}
     *    - Output (예시):
     *      응답 코드: 200
     *      응답 데이터: {"statusCode":200,"statusReason":"OK","data":{"UID":"user123","profile":{"firstName":"Jane","lastName":"Doe"}}}
     * </PRE>
     * @param channel 채널 이름
     * @param apiMethod API 메소드 이름
     * @param params 매개변수 Map
     * @return GSResponse API 요청 응답
     */
    public GSResponse executeRequest(String channel, String apiMethod, Map<String, Object> params) {
        // 기본 채널 설정 가져오기 또는 'default' 사용
        GigyaConfig.ChannelConfig config = gigyaConfig.getChannels().getOrDefault(channel, gigyaConfig.getChannels().get("default"));

        // 새로운 GSRequest 인스턴스 생성
        GSRequest request = new GSRequest(config.getApiKey(), config.getSecretKey(), apiMethod, null, true, config.getUserKey());

        // 전달받은 매개변수들을 GSRequest에 설정
        if (params != null) {
            params.forEach((key, value) -> {
                if (value != null) {  // null 체크 추가
                    if (value instanceof String) {
                        request.setParam(key, (String) value);
                    } else if (value instanceof Integer) {
                        request.setParam(key, (Integer) value);
                    } else if (value instanceof Boolean) {
                        request.setParam(key, (Boolean) value);
                    } else if (value instanceof Long) {
                        request.setParam(key, (Long) value);
                    } else {
                        request.setParam(key, value.toString()); // 다른 객체 타입에 대해 문자열 변환 처리
                    }
                }
            });
        }

        // API 도메인 설정
        request.setAPIDomain("au1.gigya.com");

        // 요청 전송 및 응답 반환
        return request.send();
    }
}