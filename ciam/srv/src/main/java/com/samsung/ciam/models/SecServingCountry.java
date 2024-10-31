package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sec_serving_countries")
public class SecServingCountry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel", nullable = false)
    private String channel;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "sfdc_id")
    private String sfdcId;

    @Column(name = "headquater")
    private String headquater;

    @Column(name = "continent")
    private String continent;

    @Column(name = "subsidiary")
    private String subsidiary;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "language")
    private String language;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "currency_iso_code")
    private String currencyIsoCode;

    @Column(name = "language_name")
    private String languageName;

    @Column(name = "locale_sid_key")
    private String localeSidKey;

    @Column(name = "timezone_sid_key")
    private String timezoneSidKey;

    @Column(name = "email_encoding_key")
    private String emailEncodingKey;

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