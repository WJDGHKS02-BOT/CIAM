package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 1. 파일명   : SecServingCountry.java
 * 2. 패키지   : com.samsung.ciam.models
 * 3. 설명     : 특정 채널이 제공되는 국가 및 관련 지역 설정을 관리하는 테이블 (JPA)
 * 4. 작성자   : 서정환
 * 5. 작성일자 : 2024. 11. 04.
 * 6. 히스토리 :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * 날짜         | 이름         | 설명
 * <p>
 * -------------|--------------|------------------------------------
 * <p>
 * 2024. 11. 04 | 서정환       | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@Entity
@Table(name = "sec_serving_countries")
public class SecServingCountry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //고유 식별자

    @Column(name = "channel", nullable = false)
    private String channel; //채널명

    @Column(name = "country", nullable = false)
    private String country; //국가코드

    @Column(name = "created_at")
    private LocalDateTime createdAt; //등록일시 

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //생성일시

    @Column(name = "sfdc_id")
    private String sfdcId; //Salesforce 고유 ID

    @Column(name = "headquater")
    private String headquater; //본사위치

    @Column(name = "continent")
    private String continent; //대륙코드

    @Column(name = "subsidiary")
    private String subsidiary; //법인코드

    @Column(name = "country_code")
    private String countryCode; //국가 코드

    @Column(name = "language")
    private String language; //언어

    @Column(name = "language_code")
    private String languageCode; //언어 코드

    @Column(name = "currency_iso_code")
    private String currencyIsoCode; //ISO 코드

    @Column(name = "language_name")
    private String languageName; //언어 코드

    @Column(name = "locale_sid_key")
    private String localeSidKey; //로케일 SID 키

    @Column(name = "timezone_sid_key")
    private String timezoneSidKey; //시간 SID 키

    @Column(name = "email_encoding_key")
    private String emailEncodingKey; //이메일 인코딩 키

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSfdcId() {
        return sfdcId;
    }

    public void setSfdcId(String sfdcId) {
        this.sfdcId = sfdcId;
    }

    public String getHeadquater() {
        return headquater;
    }

    public void setHeadquater(String headquater) {
        this.headquater = headquater;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getSubsidiary() {
        return subsidiary;
    }

    public void setSubsidiary(String subsidiary) {
        this.subsidiary = subsidiary;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getCurrencyIsoCode() {
        return currencyIsoCode;
    }

    public void setCurrencyIsoCode(String currencyIsoCode) {
        this.currencyIsoCode = currencyIsoCode;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getLocaleSidKey() {
        return localeSidKey;
    }

    public void setLocaleSidKey(String localeSidKey) {
        this.localeSidKey = localeSidKey;
    }

    public String getTimezoneSidKey() {
        return timezoneSidKey;
    }

    public void setTimezoneSidKey(String timezoneSidKey) {
        this.timezoneSidKey = timezoneSidKey;
    }

    public String getEmailEncodingKey() {
        return emailEncodingKey;
    }

    public void setEmailEncodingKey(String emailEncodingKey) {
        this.emailEncodingKey = emailEncodingKey;
    }
}