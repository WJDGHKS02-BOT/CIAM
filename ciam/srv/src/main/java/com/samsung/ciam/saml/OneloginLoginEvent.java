package com.samsung.ciam.saml;

import java.util.List;
import java.util.Map;

public class OneloginLoginEvent {

    private Map<String, List<String>> userAttributes;

    public OneloginLoginEvent(Map<String, List<String>> userAttributes) {
        this.userAttributes = userAttributes;
    }

    public Map<String, List<String>> getUserAttributes() {
        return userAttributes;
    }

    public void setUserAttributes(Map<String, List<String>> userAttributes) {
        this.userAttributes = userAttributes;
    }
}
