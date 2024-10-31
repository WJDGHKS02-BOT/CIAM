package com.samsung.ciam.saml;

import com.onelogin.saml2.authn.AuthnRequest;
import com.onelogin.saml2.authn.AuthnRequestParams;
import com.onelogin.saml2.authn.SamlResponse;
import com.onelogin.saml2.exception.Error;
import com.onelogin.saml2.exception.SettingsException;
import com.onelogin.saml2.factory.SamlMessageFactory;
import com.onelogin.saml2.http.HttpRequest;
import com.onelogin.saml2.logout.LogoutRequest;
import com.onelogin.saml2.logout.LogoutRequestParams;
import com.onelogin.saml2.logout.LogoutResponse;
import com.onelogin.saml2.logout.LogoutResponseParams;
import com.onelogin.saml2.model.KeyStoreSettings;
import com.onelogin.saml2.model.SamlResponseStatus;
import com.samsung.ciam.saml.ServletUtils;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import com.onelogin.saml2.util.Util;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Auth {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.onelogin.saml2.Auth.class);
    private Saml2Settings settings;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String nameid;
    private String nameidFormat;
    private String nameidNameQualifier;
    private String nameidSPNameQualifier;
    private String sessionIndex;
    private DateTime sessionExpiration;
    private String lastMessageId;
    private Calendar lastMessageIssueInstant;
    private String lastAssertionId;
    private List<Instant> lastAssertionNotOnOrAfter;
    private Map<String, List<String>> attributes;
    private boolean authenticated;
    private List<String> errors;
    private String errorReason;
    private Exception validationException;
    private String lastRequestId;
    private Calendar lastRequestIssueInstant;
    private String lastRequest;
    private String lastResponse;
    private static final SamlMessageFactory DEFAULT_SAML_MESSAGE_FACTORY = new SamlMessageFactory() {
    };
    private SamlMessageFactory samlMessageFactory;

    public Auth() throws IOException, SettingsException, Error {
        this((Saml2Settings)(new SettingsBuilder()).fromFile(BeansUtil.getApplicationProperty("onelogin.saml.config-file")).build(), (HttpServletRequest)null, (HttpServletResponse)null);
    }

    public Auth(KeyStoreSettings keyStoreSetting) throws IOException, SettingsException, Error {
        this(BeansUtil.getApplicationProperty("onelogin.saml.config-file"), keyStoreSetting);
    }

    public Auth(String filename) throws IOException, SettingsException, Error {
        this(filename, (KeyStoreSettings)null, (HttpServletRequest)null, (HttpServletResponse)null);
    }

    public Auth(String filename, KeyStoreSettings keyStoreSetting) throws IOException, SettingsException, Error {
        this((Saml2Settings)(new SettingsBuilder()).fromFile(filename, keyStoreSetting).build(), (HttpServletRequest)null, (HttpServletResponse)null);
    }

    public Auth(HttpServletRequest request, HttpServletResponse response) throws IOException, SettingsException, Error {
        this((new SettingsBuilder()).fromFile(BeansUtil.getApplicationProperty("onelogin.saml.config-file")).build(), request, response);
    }

    public Auth(KeyStoreSettings keyStoreSetting, HttpServletRequest request, HttpServletResponse response) throws IOException, SettingsException, Error {
        this((new SettingsBuilder()).fromFile(BeansUtil.getApplicationProperty("onelogin.saml.config-file")).build(), request, response);
    }

    public Auth(String filename, HttpServletRequest request, HttpServletResponse response) throws SettingsException, IOException, Error {
        this(filename, (KeyStoreSettings)null, request, response);
    }

    public Auth(String filename, KeyStoreSettings keyStoreSetting, HttpServletRequest request, HttpServletResponse response) throws SettingsException, IOException, Error {
        this((new SettingsBuilder()).fromFile(filename, keyStoreSetting).build(), request, response);
    }

    public Auth(Saml2Settings settings, HttpServletRequest request, HttpServletResponse response) throws SettingsException {
        this.attributes = new LinkedHashMap();
        this.authenticated = false;
        this.errors = new ArrayList();
        this.samlMessageFactory = DEFAULT_SAML_MESSAGE_FACTORY;
        this.settings = settings;
        this.request = request;
        this.response = response;
        List<String> settingsErrors = settings.checkSettings();
        if (!settingsErrors.isEmpty()) {
            String errorMsg = "Invalid settings: ";
            errorMsg = errorMsg + StringUtils.join(settingsErrors, ", ");
            LOGGER.error(errorMsg);
            throw new SettingsException(errorMsg, 2);
        } else {
            LOGGER.debug("Settings validated");
        }
    }

    public void setStrict(Boolean value) {
        this.settings.setStrict(value);
    }

    /** @deprecated */
    @Deprecated
    public String login(String relayState, Boolean forceAuthn, Boolean isPassive, Boolean setNameIdPolicy, Boolean stay, String nameIdValueReq) throws IOException, SettingsException {
        Map<String, String> parameters = new HashMap();
        return this.login(relayState, (AuthnRequestParams)(new AuthnRequestParams(forceAuthn, isPassive, setNameIdPolicy, nameIdValueReq)), stay, (Map)parameters);
    }

    /** @deprecated */
    @Deprecated
    public String login(String relayState, Boolean forceAuthn, Boolean isPassive, Boolean setNameIdPolicy, Boolean stay, String nameIdValueReq, Map<String, String> parameters) throws IOException, SettingsException {
        return this.login(relayState, new AuthnRequestParams(forceAuthn, isPassive, setNameIdPolicy, nameIdValueReq), stay, parameters);
    }

    /** @deprecated */
    @Deprecated
    public String login(String relayState, Boolean forceAuthn, Boolean isPassive, Boolean setNameIdPolicy, Boolean stay) throws IOException, SettingsException {
        return this.login(relayState, (AuthnRequestParams)(new AuthnRequestParams(forceAuthn, isPassive, setNameIdPolicy)), stay, (Map)null);
    }

    /** @deprecated */
    @Deprecated
    public void login(String relayState, Boolean forceAuthn, Boolean isPassive, Boolean setNameIdPolicy) throws IOException, SettingsException {
        this.login(relayState, new AuthnRequestParams(forceAuthn, isPassive, setNameIdPolicy), false);
    }

    public void login() throws IOException, SettingsException {
        this.login((String)null, new AuthnRequestParams(false, false, true));
    }

    public void login(AuthnRequestParams authnRequestParams) throws IOException, SettingsException {
        this.login((String)null, authnRequestParams);
    }

    public void login(String relayState) throws IOException, SettingsException {
        this.login(relayState, new AuthnRequestParams(false, false, true));
    }

    public void login(String relayState, AuthnRequestParams authnRequestParams) throws IOException, SettingsException {
        this.login(relayState, authnRequestParams, false);
    }

    public String login(String relayState, AuthnRequestParams authnRequestParams, Boolean stay) throws IOException, SettingsException {
        return this.login(relayState, (AuthnRequestParams)authnRequestParams, stay, (Map)(new HashMap()));
    }

    public String login(String relayState, AuthnRequestParams authnRequestParams, Boolean stay, Map<String, String> parameters) throws IOException, SettingsException {
        AuthnRequest authnRequest = this.samlMessageFactory.createAuthnRequest(this.settings, authnRequestParams);
        if (parameters == null) {
            parameters = new HashMap();
        }

        String samlRequest = authnRequest.getEncodedAuthnRequest();
        ((Map)parameters).put("SAMLRequest", samlRequest);
        if (relayState == null) {
            relayState = ServletUtils.getSelfRoutedURLNoQuery(this.request);
        }

        if (!relayState.isEmpty()) {
            ((Map)parameters).put("RelayState", relayState);
        }

        String ssoUrl;
        if (this.settings.getAuthnRequestsSigned()) {
            ssoUrl = this.settings.getSignatureAlgorithm();
            String signature = this.buildRequestSignature(samlRequest, relayState, ssoUrl);
            ((Map)parameters).put("SigAlg", ssoUrl);
            ((Map)parameters).put("Signature", signature);
        }

        ssoUrl = this.getSSOurl();
        this.lastRequestId = authnRequest.getId();
        this.lastRequestIssueInstant = authnRequest.getIssueInstant();
        this.lastRequest = authnRequest.getAuthnRequestXml();
        if (!stay) {
            LOGGER.debug("AuthNRequest sent to " + ssoUrl + " --> " + samlRequest);
        }

        return ServletUtils.sendRedirect(this.response, ssoUrl, (Map)parameters, stay);
    }

    public String logout(String relayState, LogoutRequestParams logoutRequestParams, Boolean stay) throws IOException, SettingsException {
        Map<String, String> parameters = new HashMap();
        return this.logout(relayState, (LogoutRequestParams)logoutRequestParams, (Boolean)stay, (Map)parameters);
    }

    public void logout(String relayState, LogoutRequestParams logoutRequestParams) throws IOException, SettingsException {
        this.logout(relayState, logoutRequestParams, false);
    }

    /** @deprecated */
    public String logout(String relayState, String nameId, String sessionIndex, Boolean stay, String nameidFormat, String nameIdNameQualifier, String nameIdSPNameQualifier) throws IOException, SettingsException {
        Map<String, String> parameters = new HashMap();
        return this.logout(relayState, (LogoutRequestParams)(new LogoutRequestParams(sessionIndex, nameId, nameidFormat, nameIdNameQualifier, nameIdSPNameQualifier)), (Boolean)stay, (Map)parameters);
    }

    public String logout(String relayState, LogoutRequestParams logoutRequestParams, Boolean stay, Map<String, String> parameters) throws IOException, SettingsException {
        if (parameters == null) {
            parameters = new HashMap();
        }

        LogoutRequest logoutRequest = this.samlMessageFactory.createOutgoingLogoutRequest(this.settings, logoutRequestParams);
        String samlLogoutRequest = logoutRequest.getEncodedLogoutRequest();
        ((Map)parameters).put("SAMLRequest", samlLogoutRequest);
        if (relayState == null) {
            relayState = ServletUtils.getSelfRoutedURLNoQuery(this.request);
        }

        if (!relayState.isEmpty()) {
            ((Map)parameters).put("RelayState", relayState);
        }

        String sloUrl;
        if (this.settings.getLogoutRequestSigned()) {
            sloUrl = this.settings.getSignatureAlgorithm();
            String signature = this.buildRequestSignature(samlLogoutRequest, relayState, sloUrl);
            ((Map)parameters).put("SigAlg", sloUrl);
            ((Map)parameters).put("Signature", signature);
        }

        sloUrl = this.getSLOurl();
        this.lastRequestId = logoutRequest.getId();
        this.lastRequestIssueInstant = logoutRequest.getIssueInstant();
        this.lastRequest = logoutRequest.getLogoutRequestXml();
        if (!stay) {
            LOGGER.debug("Logout request sent to " + sloUrl + " --> " + samlLogoutRequest);
        }

        return ServletUtils.sendRedirect(this.response, sloUrl, (Map)parameters, stay);
    }

    /** @deprecated */
    @Deprecated
    public String logout(String relayState, String nameId, String sessionIndex, Boolean stay, String nameidFormat, String nameIdNameQualifier, String nameIdSPNameQualifier, Map<String, String> parameters) throws IOException, SettingsException {
        return this.logout(relayState, new LogoutRequestParams(sessionIndex, nameId, nameidFormat, nameIdNameQualifier, nameIdSPNameQualifier), stay, parameters);
    }

    /** @deprecated */
    @Deprecated
    public String logout(String relayState, String nameId, String sessionIndex, Boolean stay, String nameidFormat, String nameIdNameQualifier) throws IOException, SettingsException {
        return this.logout(relayState, (LogoutRequestParams)(new LogoutRequestParams(sessionIndex, nameId, nameidFormat, nameIdNameQualifier)), (Boolean)stay, (Map)null);
    }

    /** @deprecated */
    @Deprecated
    public String logout(String relayState, String nameId, String sessionIndex, Boolean stay, String nameidFormat) throws IOException, SettingsException {
        return this.logout(relayState, (LogoutRequestParams)(new LogoutRequestParams(sessionIndex, nameId, nameidFormat)), (Boolean)stay, (Map)null);
    }

    /** @deprecated */
    @Deprecated
    public String logout(String relayState, String nameId, String sessionIndex, Boolean stay) throws IOException, SettingsException {
        return this.logout(relayState, (LogoutRequestParams)(new LogoutRequestParams(sessionIndex, nameId)), (Boolean)stay, (Map)null);
    }

    /** @deprecated */
    @Deprecated
    public void logout(String relayState, String nameId, String sessionIndex, String nameidFormat, String nameIdNameQualifier, String nameIdSPNameQualifier) throws IOException, SettingsException {
        this.logout(relayState, new LogoutRequestParams(sessionIndex, nameId, nameidFormat, nameIdNameQualifier, nameIdSPNameQualifier), false);
    }

    /** @deprecated */
    @Deprecated
    public void logout(String relayState, String nameId, String sessionIndex, String nameidFormat, String nameIdNameQualifier) throws IOException, SettingsException {
        this.logout(relayState, new LogoutRequestParams(sessionIndex, nameId, nameidFormat, nameIdNameQualifier), false);
    }

    /** @deprecated */
    @Deprecated
    public void logout(String relayState, String nameId, String sessionIndex, String nameidFormat) throws IOException, SettingsException {
        this.logout(relayState, new LogoutRequestParams(sessionIndex, nameId, nameidFormat), false);
    }

    /** @deprecated */
    @Deprecated
    public void logout(String relayState, String nameId, String sessionIndex) throws IOException, SettingsException {
        this.logout(relayState, (LogoutRequestParams)(new LogoutRequestParams(sessionIndex, nameId)), (Boolean)false, (Map)null);
    }

    public void logout() throws IOException, SettingsException {
        this.logout((String)null, (LogoutRequestParams)(new LogoutRequestParams()), (Boolean)false);
    }

    public void logout(String relayState) throws IOException, SettingsException {
        this.logout(relayState, new LogoutRequestParams(), false);
    }

    public String getSSOurl() {
        return this.settings.getIdpSingleSignOnServiceUrl().toString();
    }

    public String getSLOurl() {
        return this.settings.getIdpSingleLogoutServiceUrl().toString();
    }

    public String getSLOResponseUrl() {
        return this.settings.getIdpSingleLogoutServiceResponseUrl().toString();
    }

    public void processResponse(String requestId) throws Exception {
        this.authenticated = false;
        HttpRequest httpRequest = ServletUtils.makeHttpRequest(this.request);
        String samlResponseParameter = httpRequest.getParameter("SAMLResponse");
        if (samlResponseParameter == null) {
            this.errors.add("invalid_binding");
            String errorMsg = "SAML Response not found, Only supported HTTP_POST Binding";
            LOGGER.error("processResponse error." + errorMsg);
            throw new Error(errorMsg, 3);
        } else {
            SamlResponse samlResponse = this.samlMessageFactory.createSamlResponse(this.settings, httpRequest);
            this.lastResponse = samlResponse.getSAMLResponseXml();
            if (samlResponse.isValid(requestId)) {
                this.nameid = samlResponse.getNameId();
                this.nameidFormat = samlResponse.getNameIdFormat();
                this.nameidNameQualifier = samlResponse.getNameIdNameQualifier();
                this.nameidSPNameQualifier = samlResponse.getNameIdSPNameQualifier();
                this.authenticated = true;
                this.attributes = samlResponse.getAttributes();
                this.sessionIndex = samlResponse.getSessionIndex();
                this.sessionExpiration = samlResponse.getSessionNotOnOrAfter();
                this.lastMessageId = samlResponse.getId();
                this.lastMessageIssueInstant = samlResponse.getResponseIssueInstant();
                this.lastAssertionId = samlResponse.getAssertionId();
                this.lastAssertionNotOnOrAfter = samlResponse.getAssertionNotOnOrAfter();
                LOGGER.debug("processResponse success --> " + samlResponseParameter);
            } else {
                this.errorReason = samlResponse.getError();
                this.validationException = samlResponse.getValidationException();
                SamlResponseStatus samlResponseStatus = samlResponse.getResponseStatus();
                if (samlResponseStatus.getStatusCode() != null && samlResponseStatus.getStatusCode().equals("urn:oasis:names:tc:SAML:2.0:status:Success")) {
                    this.errors.add("invalid_response");
                    LOGGER.error("processResponse error. invalid_response");
                    LOGGER.debug(" --> " + samlResponseParameter);
                } else {
                    this.errors.add("response_not_success");
                    LOGGER.error("processResponse error. sso_not_success");
                    LOGGER.debug(" --> " + samlResponseParameter);
                    this.errors.add(samlResponseStatus.getStatusCode());
                    if (samlResponseStatus.getSubStatusCode() != null) {
                        this.errors.add(samlResponseStatus.getSubStatusCode());
                    }
                }
            }

        }
    }

    public void processResponse() throws Exception {
        this.processResponse((String)null);
    }

    public String processSLO(Boolean keepLocalSession, String requestId, Boolean stay) throws Exception {
        HttpRequest httpRequest = ServletUtils.makeHttpRequest(this.request);
        String samlRequestParameter = httpRequest.getParameter("SAMLRequest");
        String samlResponseParameter = httpRequest.getParameter("SAMLResponse");
        if (samlResponseParameter == null) {
            if (samlRequestParameter != null) {
                LogoutRequest logoutRequest = this.samlMessageFactory.createIncomingLogoutRequest(this.settings, httpRequest);
                this.lastRequest = logoutRequest.getLogoutRequestXml();
                if (!logoutRequest.isValid()) {
                    this.errors.add("invalid_logout_request");
                    LOGGER.error("processSLO error. invalid_logout_request");
                    LOGGER.debug(" --> " + samlRequestParameter);
                    this.errorReason = logoutRequest.getError();
                    this.validationException = logoutRequest.getValidationException();
                    return null;
                } else {
                    this.lastMessageId = logoutRequest.getId();
                    this.lastMessageIssueInstant = logoutRequest.getIssueInstant();
                    LOGGER.debug("processSLO success --> " + samlRequestParameter);
                    if (!keepLocalSession) {
                        this.request.getSession().invalidate();
                    }

                    String inResponseTo = logoutRequest.id;
                    LogoutResponse logoutResponseBuilder = this.samlMessageFactory.createOutgoingLogoutResponse(this.settings, new LogoutResponseParams(inResponseTo, "urn:oasis:names:tc:SAML:2.0:status:Success"));
                    this.lastResponse = logoutResponseBuilder.getLogoutResponseXml();
                    String samlLogoutResponse = logoutResponseBuilder.getEncodedLogoutResponse();
                    Map<String, String> parameters = new LinkedHashMap();
                    parameters.put("SAMLResponse", samlLogoutResponse);
                    String relayState = this.request.getParameter("RelayState");
                    if (relayState != null) {
                        parameters.put("RelayState", relayState);
                    }

                    String sloUrl;
                    if (this.settings.getLogoutResponseSigned()) {
                        sloUrl = this.settings.getSignatureAlgorithm();
                        String signature = this.buildResponseSignature(samlLogoutResponse, relayState, sloUrl);
                        parameters.put("SigAlg", sloUrl);
                        parameters.put("Signature", signature);
                    }

                    sloUrl = this.getSLOResponseUrl();
                    if (!stay) {
                        LOGGER.debug("Logout response sent to " + sloUrl + " --> " + samlLogoutResponse);
                    }

                    return ServletUtils.sendRedirect(this.response, sloUrl, parameters, stay);
                }
            } else {
                this.errors.add("invalid_binding");
                String errorMsg = "SAML LogoutRequest/LogoutResponse not found. Only supported HTTP_REDIRECT Binding";
                LOGGER.error("processSLO error." + errorMsg);
                throw new Error(errorMsg, 4);
            }
        } else {
            LogoutResponse logoutResponse = this.samlMessageFactory.createIncomingLogoutResponse(this.settings, httpRequest);
            this.lastResponse = logoutResponse.getLogoutResponseXml();
            if (!logoutResponse.isValid(requestId)) {
                this.errors.add("invalid_logout_response");
                LOGGER.error("processSLO error. invalid_logout_response");
                LOGGER.debug(" --> " + samlResponseParameter);
                this.errorReason = logoutResponse.getError();
                this.validationException = logoutResponse.getValidationException();
            } else {
                SamlResponseStatus samlResponseStatus = logoutResponse.getSamlResponseStatus();
                String status = samlResponseStatus.getStatusCode();
                if (status != null && status.equals("urn:oasis:names:tc:SAML:2.0:status:Success")) {
                    this.lastMessageId = logoutResponse.getId();
                    this.lastMessageIssueInstant = logoutResponse.getIssueInstant();
                    LOGGER.debug("processSLO success --> " + samlResponseParameter);
                    if (!keepLocalSession) {
                        this.request.getSession().invalidate();
                    }
                } else {
                    this.errors.add("logout_not_success");
                    LOGGER.error("processSLO error. logout_not_success");
                    LOGGER.debug(" --> " + samlResponseParameter);
                    this.errors.add(samlResponseStatus.getStatusCode());
                    if (samlResponseStatus.getSubStatusCode() != null) {
                        this.errors.add(samlResponseStatus.getSubStatusCode());
                    }
                }
            }

            return null;
        }
    }

    public void processSLO(Boolean keepLocalSession, String requestId) throws Exception {
        this.processSLO(keepLocalSession, requestId, false);
    }

    public void processSLO() throws Exception {
        this.processSLO(false, (String)null);
    }

    public final boolean isAuthenticated() {
        return this.authenticated;
    }

    public final List<String> getAttributesName() {
        return new ArrayList(this.attributes.keySet());
    }

    public final Map<String, List<String>> getAttributes() {
        return this.attributes;
    }

    public final Collection<String> getAttribute(String name) {
        return (Collection)this.attributes.get(name);
    }

    public final String getNameId() {
        return this.nameid;
    }

    public final String getNameIdFormat() {
        return this.nameidFormat;
    }

    public final String getNameIdNameQualifier() {
        return this.nameidNameQualifier;
    }

    public final String getNameIdSPNameQualifier() {
        return this.nameidSPNameQualifier;
    }

    public final String getSessionIndex() {
        return this.sessionIndex;
    }

    public final DateTime getSessionExpiration() {
        return this.sessionExpiration;
    }

    public String getLastMessageId() {
        return this.lastMessageId;
    }

    public Calendar getLastMessageIssueInstant() {
        return this.lastMessageIssueInstant;
    }

    public String getLastAssertionId() {
        return this.lastAssertionId;
    }

    public List<Instant> getLastAssertionNotOnOrAfter() {
        return this.lastAssertionNotOnOrAfter;
    }

    public List<String> getErrors() {
        return this.errors;
    }

    public String getLastErrorReason() {
        return this.errorReason;
    }

    public Exception getLastValidationException() {
        return this.validationException;
    }

    public String getLastRequestId() {
        return this.lastRequestId;
    }

    public Calendar getLastRequestIssueInstant() {
        return this.lastRequestIssueInstant;
    }

    public Saml2Settings getSettings() {
        return this.settings;
    }

    public Boolean isDebugActive() {
        return this.settings.isDebugActive();
    }

    public String buildRequestSignature(String samlRequest, String relayState, String signAlgorithm) throws SettingsException {
        return this.buildSignature(samlRequest, relayState, signAlgorithm, "SAMLRequest");
    }

    public String buildResponseSignature(String samlResponse, String relayState, String signAlgorithm) throws SettingsException {
        return this.buildSignature(samlResponse, relayState, signAlgorithm, "SAMLResponse");
    }

    private String buildSignature(String samlMessage, String relayState, String signAlgorithm, String type) throws SettingsException, IllegalArgumentException {
        String signature = "";
        if (!this.settings.checkSPCerts()) {
            String errorMsg = "Trying to sign the " + type + " but can't load the SP private key";
            LOGGER.error("buildSignature error. " + errorMsg);
            throw new SettingsException(errorMsg, 4);
        } else {
            PrivateKey key = this.settings.getSPkey();
            String msg = type + "=" + Util.urlEncoder(samlMessage);
            if (StringUtils.isNotEmpty(relayState)) {
                msg = msg + "&RelayState=" + Util.urlEncoder(relayState);
            }

            if (StringUtils.isEmpty(signAlgorithm)) {
                signAlgorithm = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
            }

            msg = msg + "&SigAlg=" + Util.urlEncoder(signAlgorithm);

            try {
                signature = Util.base64encoder(Util.sign(msg, key, signAlgorithm));
            } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException var10) {
                GeneralSecurityException e = var10;
                String errorMsg = "buildSignature error." + e.getMessage();
                LOGGER.error(errorMsg);
            }

            if (signature.isEmpty()) {
                String errorMsg = "There was a problem when calculating the Signature of the " + type;
                LOGGER.error("buildSignature error. " + errorMsg);
                throw new IllegalArgumentException(errorMsg);
            } else {
                LOGGER.debug("buildResponseSignature success. --> " + signature);
                return signature;
            }
        }
    }

    public String getLastRequestXML() {
        return this.lastRequest;
    }

    public String getLastResponseXML() {
        return this.lastResponse;
    }

    public void setSamlMessageFactory(SamlMessageFactory samlMessageFactory) {
        this.samlMessageFactory = samlMessageFactory != null ? samlMessageFactory : DEFAULT_SAML_MESSAGE_FACTORY;
    }
}