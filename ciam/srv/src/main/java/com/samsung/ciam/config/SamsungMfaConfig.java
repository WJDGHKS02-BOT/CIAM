package com.samsung.ciam.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class SamsungMfaConfig {
  @Value("${samsung.mfa.url:default-url}") // 기본값 지정
  private String url;

  @Value("${samsung.mfa.bio.secret-key:default-bio-secret-key}")
  private String bioSecretKey;

  @Value("${samsung.mfa.bio.consumer-key:default-bio-consumer-key}")
  private String bioConsumerKey;

  @Value("${samsung.mfa.all.secret-key:default-all-secret-key}")
  private String allSecretKey;

  @Value("${samsung.mfa.all.consumer-key:default-all-consumer-key}")
  private String allConsumerKey;
}