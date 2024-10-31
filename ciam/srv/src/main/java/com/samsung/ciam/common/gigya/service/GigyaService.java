package com.samsung.ciam.common.gigya.service;

import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.beans.GigyaConfig;
import com.samsung.ciam.utils.BeansUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GigyaService {

    @Autowired
    private GigyaConfig gigyaConfig;

    public GSResponse executeRequest(String channel, String apiMethod, String query) {
        GigyaConfig.ChannelConfig config = gigyaConfig.getChannels().getOrDefault(channel, gigyaConfig.getChannels().get("default"));

        GSRequest request = new GSRequest(config.getApiKey(), config.getSecretKey(), apiMethod, null, true, config.getUserKey());
        request.setParam("query", query);
        request.setAPIDomain("au1.gigya.com");

        return request.send();
    }

    public GSResponse executeRequestSetParam(String apiMethod, String paramName,String paramValue) {
        GigyaConfig.ChannelConfig config = gigyaConfig.getChannels().getOrDefault("default", gigyaConfig.getChannels().get("default"));

        GSRequest request = new GSRequest(config.getApiKey(), config.getSecretKey(), apiMethod, null, true, config.getUserKey());
        request.setParam(paramName, paramValue);
        request.setAPIDomain("au1.gigya.com");

        return request.send();
    }

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