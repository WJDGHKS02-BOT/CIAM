package com.samsung.ciam.dto;

import com.samsung.ciam.models.ConsentContent;

public class ConsentStatusDTO {
    private String commonTermsId;
    private String commonPrivacyId;
    private String commonMarketingId;
    private String userAgreedMarketingId;
    private ConsentContent commonTerms;
    private ConsentContent commonPrivacy;
    private ConsentContent commonMarketing;
    private ConsentContent channelTerms;
    private ConsentContent channelPrivacy;
    private boolean commonTermsAgreed;
    private boolean commonPrivacyAgreed;
    private boolean commonMarketingAgreed;
    private String commonTermsAgreedDate;
    private String commonPrivacyAgreedDate;
    private String commonMarketingAgreedDate;
    private boolean channelTermsAgreed;
    private boolean channelPrivacyAgreed;
    private String channelTermsAgreedDate;
    private String channelPrivacyAgreedDate;
    private String channelTermsAgreeId;
    private String channelPrivacyId;

    // Getters and Setters for all fields

    public String getCommonTermsId() {
        return commonTermsId;
    }

    public void setCommonTermsId(String commonTermsId) {
        this.commonTermsId = commonTermsId;
    }

    public String getCommonPrivacyId() {
        return commonPrivacyId;
    }

    public void setCommonPrivacyId(String commonPrivacyId) {
        this.commonPrivacyId = commonPrivacyId;
    }

    public String getCommonMarketingId() {
        return commonMarketingId;
    }

    public void setCommonMarketingId(String commonMarketingId) {
        this.commonMarketingId = commonMarketingId;
    }

    public String getUserAgreedMarketingId() {
        return userAgreedMarketingId;
    }

    public void setUserAgreedMarketingId(String userAgreedMarketingId) {
        this.userAgreedMarketingId = userAgreedMarketingId;
    }

    public ConsentContent getCommonTerms() {
        return commonTerms;
    }

    public void setCommonTerms(ConsentContent commonTerms) {
        this.commonTerms = commonTerms;
    }

    public ConsentContent getCommonPrivacy() {
        return commonPrivacy;
    }

    public void setCommonPrivacy(ConsentContent commonPrivacy) {
        this.commonPrivacy = commonPrivacy;
    }

    public ConsentContent getCommonMarketing() {
        return commonMarketing;
    }

    public void setCommonMarketing(ConsentContent commonMarketing) {
        this.commonMarketing = commonMarketing;
    }

    public ConsentContent getChannelTerms() {
        return channelTerms;
    }

    public void setChannelTerms(ConsentContent channelTerms) {
        this.channelTerms = channelTerms;
    }

    public ConsentContent getChannelPrivacy() {
        return channelPrivacy;
    }

    public void setChannelPrivacy(ConsentContent channelPrivacy) {
        this.channelPrivacy = channelPrivacy;
    }

    public boolean isCommonTermsAgreed() {
        return commonTermsAgreed;
    }

    public void setCommonTermsAgreed(boolean commonTermsAgreed) {
        this.commonTermsAgreed = commonTermsAgreed;
    }

    public boolean isCommonPrivacyAgreed() {
        return commonPrivacyAgreed;
    }

    public void setCommonPrivacyAgreed(boolean commonPrivacyAgreed) {
        this.commonPrivacyAgreed = commonPrivacyAgreed;
    }

    public boolean isCommonMarketingAgreed() {
        return commonMarketingAgreed;
    }

    public void setCommonMarketingAgreed(boolean commonMarketingAgreed) {
        this.commonMarketingAgreed = commonMarketingAgreed;
    }

    public String getCommonTermsAgreedDate() {
        return commonTermsAgreedDate;
    }

    public void setCommonTermsAgreedDate(String commonTermsAgreedDate) {
        this.commonTermsAgreedDate = commonTermsAgreedDate;
    }

    public String getCommonPrivacyAgreedDate() {
        return commonPrivacyAgreedDate;
    }

    public void setCommonPrivacyAgreedDate(String commonPrivacyAgreedDate) {
        this.commonPrivacyAgreedDate = commonPrivacyAgreedDate;
    }

    public String getCommonMarketingAgreedDate() {
        return commonMarketingAgreedDate;
    }

    public void setCommonMarketingAgreedDate(String commonMarketingAgreedDate) {
        this.commonMarketingAgreedDate = commonMarketingAgreedDate;
    }

    public boolean isChannelTermsAgreed() {
        return channelTermsAgreed;
    }

    public void setChannelTermsAgreed(boolean channelTermsAgreed) {
        this.channelTermsAgreed = channelTermsAgreed;
    }

    public boolean isChannelPrivacyAgreed() {
        return channelPrivacyAgreed;
    }

    public void setChannelPrivacyAgreed(boolean channelPrivacyAgreed) {
        this.channelPrivacyAgreed = channelPrivacyAgreed;
    }

    public String getChannelTermsAgreedDate() {
        return channelTermsAgreedDate;
    }

    public void setChannelTermsAgreedDate(String channelTermsAgreedDate) {
        this.channelTermsAgreedDate = channelTermsAgreedDate;
    }

    public String getChannelPrivacyAgreedDate() {
        return channelPrivacyAgreedDate;
    }

    public void setChannelPrivacyAgreedDate(String channelPrivacyAgreedDate) {
        this.channelPrivacyAgreedDate = channelPrivacyAgreedDate;
    }

    public String getChannelTermsAgreeId() {
        return channelTermsAgreeId;
    }

    public void setChannelTermsAgreeId(String channelTermsAgreeId) {
        this.channelTermsAgreeId = channelTermsAgreeId;
    }

    public String getChannelPrivacyId() {
        return channelPrivacyId;
    }

    public void setChannelPrivacyId(String channelPrivacyId) {
        this.channelPrivacyId = channelPrivacyId;
    }
}
