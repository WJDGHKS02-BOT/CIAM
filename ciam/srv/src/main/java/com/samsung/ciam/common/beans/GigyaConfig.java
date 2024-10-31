package com.samsung.ciam.common.beans;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "gigya")
public class GigyaConfig {
    private Map<String, ChannelConfig> channels;

    public Map<String, ChannelConfig> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, ChannelConfig> channels) {
        this.channels = channels;
    }

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