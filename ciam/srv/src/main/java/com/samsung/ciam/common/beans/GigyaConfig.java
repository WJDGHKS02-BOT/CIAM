package com.samsung.ciam.common.beans;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 1. FileName	: GigyaConfig.java
 * 2. Package	: com.samsung.ciam.common.beans
 * 3. Comments	: YAML 파일의 GIGYA 관련 설정 키 값을 관리하는 클래스
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

/*
 * 1. 클래스명	: GigyaConfig
 * 2. 파일명	: GigyaConfig.java
 * 3. 패키지명	: com.samsung.ciam.common.beans
 * 4. 작성자명	: 서정환
 * 5. 작성일자	: 2024. 11. 04.
 */

/**
 * <PRE>
 * 1. 설명
 *    YAML 파일의 GIGYA 관련 설정 키 값을 관리하는 클래스
 * </PRE>
 */

@Configuration
@ConfigurationProperties(prefix = "gigya")
public class GigyaConfig {
    private Map<String, ChannelConfig> channels;

    /*
     * 1. 메소드명: getChannels
     * 2. 클래스명: GigyaConfig
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    GIGYA 채널 구성 정보를 담고 있는 Map 객체 반환
     * 2. 사용법
     *    getChannels()
     * </PRE>
     * @return Map<String, ChannelConfig>
     */
    public Map<String, ChannelConfig> getChannels() {
        return channels;
    }

    /*
     * 1. 메소드명: setChannels
     * 2. 클래스명: GigyaConfig
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    GIGYA 채널 구성 정보를 담고 있는 Map 객체 설정
     * 2. 사용법
     *    setChannels(Map<String, ChannelConfig> channels)
     * </PRE>
     * @param channels GIGYA 채널 구성 정보
     */
    public void setChannels(Map<String, ChannelConfig> channels) {
        this.channels = channels;
    }

    /*
     * 1. 클래스명: ChannelConfig
     * 2. 설명: GIGYA 채널의 API Key, Secret Key, User Key를 관리하는 클래스
     */
    public static class ChannelConfig {
        private String apiKey;
        private String secretKey;
        private String userKey;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getUserKey() {
            return userKey;
        }

        public void setUserKey(String userKey) {
            this.userKey = userKey;
        }
    }
}