package com.samsung.ciam.dto;

import com.samsung.ciam.models.Consent;
import com.samsung.ciam.models.ConsentContent;
import com.samsung.ciam.models.UserAgreedConsents;

/**
 * 1. FileName   : UserAgreedConsentDTO.java
 * 2. Package    : com.samsung.ciam.dto
 * 3. Comments   : 사용자 동의 내역, 해당 동의 항목, 동의 항목의 콘텐츠 정보를 담는 DTO(Data Transfer Object)로, 사용자 동의 정보를 하나의 객체로 관리.
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
public class UserAgreedConsentDTO {
    private UserAgreedConsents userAgreedConsent;  // 사용자가 동의한 동의 내역 정보
    private Consent consent;                       // 동의 항목 정보
    private ConsentContent content;                // 동의 항목의 콘텐츠 정보

    public UserAgreedConsentDTO(UserAgreedConsents userAgreedConsent, Consent consent, ConsentContent content) {
        this.userAgreedConsent = userAgreedConsent;
        this.consent = consent;
        this.content = content;
    }

    public UserAgreedConsents getUserAgreedConsent() {
        return userAgreedConsent;
    }

    public void setUserAgreedConsent(UserAgreedConsents userAgreedConsent) {
        this.userAgreedConsent = userAgreedConsent;
    }

    public Consent getConsent() {
        return consent;
    }

    public void setConsent(Consent consent) {
        this.consent = consent;
    }

    public ConsentContent getContent() {
        return content;
    }

    public void setContent(ConsentContent content) {
        this.content = content;
    }
}
