package com.samsung.ciam.dto;

import com.samsung.ciam.models.ConsentContent;

/**
 * 1. FileName   : ConsentStatusDTO.java
 * 2. Package    : com.samsung.ciam.dto
 * 3. Comments   : 사용자 동의 상태와 관련된 정보를 담는 DTO(Data Transfer Object)로, 일반 약관, 개인정보 보호, 마케팅 약관, 채널 관련 동의 정보 등을 포함.
 * 4. Author     : 서정환
 * 5. DateTime   : 2024. 11. 04.
 * 6. History    :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * Date         | Name          | Comment
 * <p>
 * -------------  -------------   ------------------------------
 * <p>
 * 2024. 11. 04.   | 서정환        | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
public class ConsentStatusDTO {
    private String commonTermsId;                  // 일반 약관 ID
    private String commonPrivacyId;                // 개인정보 보호 동의 ID
    private String commonMarketingId;              // 마케팅 동의 ID
    private String userAgreedMarketingId;          // 사용자가 동의한 마케팅 ID
    private ConsentContent commonTerms;            // 일반 약관 동의 내용
    private ConsentContent commonPrivacy;          // 개인정보 보호 동의 내용
    private ConsentContent commonMarketing;        // 마케팅 동의 내용
    private ConsentContent channelTerms;           // 채널 약관 동의 내용
    private ConsentContent channelPrivacy;         // 채널 개인정보 보호 동의 내용
    private boolean commonTermsAgreed;             // 일반 약관 동의 여부
    private boolean commonPrivacyAgreed;           // 개인정보 보호 동의 여부
    private boolean commonMarketingAgreed;         // 마케팅 동의 여부
    private String commonTermsAgreedDate;          // 일반 약관 동의 날짜
    private String commonPrivacyAgreedDate;        // 개인정보 보호 동의 날짜
    private String commonMarketingAgreedDate;      // 마케팅 동의 날짜
    private boolean channelTermsAgreed;            // 채널 약관 동의 여부
    private boolean channelPrivacyAgreed;          // 채널 개인정보 보호 동의 여부
    private String channelTermsAgreedDate;         // 채널 약관 동의 날짜
    private String channelPrivacyAgreedDate;       // 채널 개인정보 보호 동의 날짜
    private String channelTermsAgreeId;            // 채널 약관 동의 ID
    private String channelPrivacyId;               // 채널 개인정보 보호 ID

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
