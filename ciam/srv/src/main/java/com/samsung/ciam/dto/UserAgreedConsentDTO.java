package com.samsung.ciam.dto;

import com.samsung.ciam.models.Consent;
import com.samsung.ciam.models.ConsentContent;
import com.samsung.ciam.models.UserAgreedConsents;

public class UserAgreedConsentDTO {
    private UserAgreedConsents userAgreedConsent;
    private Consent consent;
    private ConsentContent content;

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
