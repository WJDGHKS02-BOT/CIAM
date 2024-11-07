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

/**
 * 1. FileName   : Auth.java
 * 2. Package    : com.samsung.ciam.saml
 * 3. Comments   : SAML 인증을 위한 Auth 클래스, OneLogin SAML 라이브러리를 기반으로 Jakarta를 사용하도록 수정하여 인증 및 로그아웃 요청을 관리
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
 * 2024. 11. 04.   | 서정환        | 최초작성 및 Jakarta 호환성 변경
 * <p>
 * -----------------------------------------------------------------
 */
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

    // 기본 생성자: 설정 파일을 통해 SAML2Settings를 기본 설정으로 로드하여 인증 및 로그아웃을 처리할 준비를 함.
    public Auth() throws IOException, SettingsException, Error {
        this((Saml2Settings)(new SettingsBuilder()).fromFile(BeansUtil.getApplicationProperty("onelogin.saml.config-file")).build(), (HttpServletRequest)null, (HttpServletResponse)null);
    }

    // 인증서 관련 설정을 위해 KeyStoreSettings를 포함하여 초기화, SAML 구성 파일을 로드하여 보안 설정을 추가함.
    public Auth(KeyStoreSettings keyStoreSetting) throws IOException, SettingsException, Error {
        this(BeansUtil.getApplicationProperty("onelogin.saml.config-file"), keyStoreSetting);
    }

    // 파일 이름을 통해 설정 파일을 로드하는 생성자, KeyStoreSettings가 필요하지 않은 경우에 사용.
    public Auth(String filename) throws IOException, SettingsException, Error {
        this(filename, (KeyStoreSettings)null, (HttpServletRequest)null, (HttpServletResponse)null);
    }

    // 특정 파일과 KeyStoreSettings를 기반으로 초기화하여, 인증서와 설정 파일에 따라 SAML2Settings를 생성함.
    public Auth(String filename, KeyStoreSettings keyStoreSetting) throws IOException, SettingsException, Error {
        this((Saml2Settings)(new SettingsBuilder()).fromFile(filename, keyStoreSetting).build(), (HttpServletRequest)null, (HttpServletResponse)null);
    }

    // HttpServletRequest와 HttpServletResponse를 받아 인증 요청을 관리하는 생성자, 설정 파일을 로드하여 SAML2Settings를 생성함.
    public Auth(HttpServletRequest request, HttpServletResponse response) throws IOException, SettingsException, Error {
        this((new SettingsBuilder()).fromFile(BeansUtil.getApplicationProperty("onelogin.saml.config-file")).build(), request, response);
    }

    // KeyStoreSettings와 HttpServletRequest, HttpServletResponse를 사용해 인증 및 로그아웃 요청을 관리하며, 보안 설정을 적용함.
    public Auth(KeyStoreSettings keyStoreSetting, HttpServletRequest request, HttpServletResponse response) throws IOException, SettingsException, Error {
        this((new SettingsBuilder()).fromFile(BeansUtil.getApplicationProperty("onelogin.saml.config-file")).build(), request, response);
    }

    // 파일 이름과 요청, 응답 객체를 사용해 초기화하며, KeyStoreSettings가 필요하지 않은 경우 사용.
    public Auth(String filename, HttpServletRequest request, HttpServletResponse response) throws SettingsException, IOException, Error {
        this(filename, (KeyStoreSettings)null, request, response);
    }

    // 파일 이름과 KeyStoreSettings, 요청 및 응답 객체를 통해 SAML 설정을 로드하여 초기화하는 생성자.
    public Auth(String filename, KeyStoreSettings keyStoreSetting, HttpServletRequest request, HttpServletResponse response) throws SettingsException, IOException, Error {
        this((new SettingsBuilder()).fromFile(filename, keyStoreSetting).build(), request, response);
    }

    /*
     * 1. 메소드명: Auth
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    주어진 SAML 설정 파일을 통해 인증 설정을 초기화하는 생성자.
     *    설정 오류가 있을 경우 로그에 오류 메시지를 출력하고 예외를 발생시킴.
     * 2. 사용법
     *    새 Auth 객체를 생성할 때 호출됨.
     * 3. 예시 데이터
     *    - Input: settings 객체, HttpServletRequest, HttpServletResponse
     *    - Output: 초기화된 Auth 객체 또는 SettingsException 발생
     * </PRE>
     * @param settings SAML 설정을 포함하는 Saml2Settings 객체
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @throws SettingsException 설정 오류 시 발생
     */
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

    /*
     * 1. 메소드명: setStrict
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    SAML 설정에서 엄격 모드를 활성화 또는 비활성화하는 메소드.
     *    보안 관련 검사를 강화할 수 있음.
     * 2. 사용법
     *    Boolean 값을 전달하여 엄격 모드를 설정함.
     * 3. 예시 데이터
     *    - Input: true (엄격 모드 활성화)
     *    - Output: 설정의 엄격 모드가 true로 설정됨
     * </PRE>
     * @param value 엄격 모드 설정 여부를 나타내는 Boolean 값
     */
    public void setStrict(Boolean value) {
        this.settings.setStrict(value);
    }

    /*
     * 1. 메소드명: login (Deprecated)
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    로그인 요청을 생성하여 SSO URL로 리디렉션하는 메소드.
     *    이 메소드는 Deprecated 되었으며 최신 메소드 사용을 권장.
     * 2. 사용법
     *    로그인 요청 시 다양한 인증 요구사항을 설정하여 호출됨.
     * 3. 예시 데이터
     *    - Input: relayState, forceAuthn, isPassive, setNameIdPolicy, stay, nameIdValueReq
     *    - Output: 생성된 로그인 요청 URL
     * </PRE>
     * @param relayState 리디렉션할 상태 정보를 포함하는 URL
     * @param forceAuthn 사용자에게 인증을 강제할지 여부
     * @param isPassive 인증이 수동적인지 여부
     * @param setNameIdPolicy NameIDPolicy 설정 여부
     * @param stay 리디렉션 여부
     * @param nameIdValueReq 요청된 NameID 값
     * @return 생성된 로그인 요청 URL
     * @throws IOException 입력/출력 오류 발생 시
     * @throws SettingsException 설정 오류 발생 시
     */
    /** @deprecated */
    @Deprecated
    public String login(String relayState, Boolean forceAuthn, Boolean isPassive, Boolean setNameIdPolicy, Boolean stay, String nameIdValueReq) throws IOException, SettingsException {
        Map<String, String> parameters = new HashMap();
        return this.login(relayState, (AuthnRequestParams)(new AuthnRequestParams(forceAuthn, isPassive, setNameIdPolicy, nameIdValueReq)), stay, (Map)parameters);
    }

    /*
     * 1. 메소드명: login
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    로그인 요청을 생성하여 SSO URL로 리디렉션하는 메소드.
     *    주어진 매개변수와 AuthnRequestParams를 기반으로 로그인 요청을 생성함.
     * 2. 사용법
     *    다양한 인증 요구사항을 설정하여 로그인 요청을 보낼 때 호출됨.
     * 3. 예시 데이터
     *    - Input: relayState, authnRequestParams, stay, parameters
     *    - Output: 생성된 로그인 요청 URL
     * </PRE>
     * @param relayState 리디렉션할 상태 정보를 포함하는 URL
     * @param authnRequestParams 인증 요청 매개변수
     * @param stay 리디렉션 여부
     * @param parameters 요청에 포함할 추가 매개변수
     * @return 생성된 로그인 요청 URL
     * @throws IOException 입력/출력 오류 발생 시
     * @throws SettingsException 설정 오류 발생 시
     */
    /** @deprecated */
    @Deprecated
    public String login(String relayState, Boolean forceAuthn, Boolean isPassive, Boolean setNameIdPolicy, Boolean stay, String nameIdValueReq, Map<String, String> parameters) throws IOException, SettingsException {
        return this.login(relayState, new AuthnRequestParams(forceAuthn, isPassive, setNameIdPolicy, nameIdValueReq), stay, parameters);
    }

    /*
     * 1. 메소드명: logout
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    로그아웃 요청을 생성하여 SLO URL로 리디렉션하는 메소드.
     *    로그아웃 요청 매개변수를 통해 인증 상태를 해제함.
     * 2. 사용법
     *    로그아웃 요청 시 호출되어 사용자의 세션을 무효화.
     * 3. 예시 데이터
     *    - Input: relayState, logoutRequestParams, stay
     *    - Output: 생성된 로그아웃 요청 URL
     * </PRE>
     * @param relayState 리디렉션할 상태 정보를 포함하는 URL
     * @param logoutRequestParams 로그아웃 요청 매개변수
     * @param stay 리디렉션 여부
     * @return 생성된 로그아웃 요청 URL
     * @throws IOException 입력/출력 오류 발생 시
     * @throws SettingsException 설정 오류 발생 시
     */
    /** @deprecated */
    @Deprecated
    public String login(String relayState, Boolean forceAuthn, Boolean isPassive, Boolean setNameIdPolicy, Boolean stay) throws IOException, SettingsException {
        return this.login(relayState, (AuthnRequestParams)(new AuthnRequestParams(forceAuthn, isPassive, setNameIdPolicy)), stay, (Map)null);
    }

    /*
     * 1. 메소드명: login (Deprecated)
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    특정 relayState와 인증 설정을 기반으로 로그인 요청을 생성하여 리디렉션함.
     *    이 메소드는 Deprecated 되었으며 최신 메소드 사용을 권장.
     * 2. 사용법
     *    인증 요청을 보낼 때 호출되며, 세션에 강제 인증이나 비활성 인증을 포함할 수 있음.
     * 3. 예시 데이터
     *    - Input: relayState, forceAuthn, isPassive, setNameIdPolicy
     *    - Output: 생성된 로그인 요청 URL로 리디렉션
     * </PRE>
     * @param relayState 리디렉션할 상태 정보를 포함하는 URL
     * @param forceAuthn 사용자에게 인증을 강제할지 여부
     * @param isPassive 인증이 수동적인지 여부
     * @param setNameIdPolicy NameIDPolicy 설정 여부
     * @throws IOException 입력/출력 오류 발생 시
     * @throws SettingsException 설정 오류 발생 시
     */
    /** @deprecated */
    @Deprecated
    public void login(String relayState, Boolean forceAuthn, Boolean isPassive, Boolean setNameIdPolicy) throws IOException, SettingsException {
        this.login(relayState, new AuthnRequestParams(forceAuthn, isPassive, setNameIdPolicy), false);
    }

    /*
     * 1. 메소드명: login
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    기본 설정을 사용하여 로그인 요청을 생성하고 리디렉션하는 메소드.
     *    추가 파라미터 없이 기본 로그인 요청을 보냄.
     * 2. 사용법
     *    사용자 인증을 시작할 때 호출됨.
     * 3. 예시 데이터
     *    - Input: 없음
     *    - Output: 생성된 로그인 요청 URL로 리디렉션
     * </PRE>
     * @throws IOException 입력/출력 오류 발생 시
     * @throws SettingsException 설정 오류 발생 시
     */
    public void login() throws IOException, SettingsException {
        this.login((String)null, new AuthnRequestParams(false, false, true));
    }

    /*
     * 1. 메소드명: login
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    인증 요청 매개변수를 사용하여 로그인 요청을 생성하고 리디렉션하는 메소드.
     *    사용자 지정 인증 요청을 전달할 때 사용.
     * 2. 사용법
     *    특정 인증 파라미터를 설정하여 호출됨.
     * 3. 예시 데이터
     *    - Input: authnRequestParams
     *    - Output: 생성된 로그인 요청 URL로 리디렉션
     * </PRE>
     * @param authnRequestParams 인증 요청 매개변수
     * @throws IOException 입력/출력 오류 발생 시
     * @throws SettingsException 설정 오류 발생 시
     */
    public void login(AuthnRequestParams authnRequestParams) throws IOException, SettingsException {
        this.login((String)null, authnRequestParams);
    }

    /*
     * 1. 메소드명: login
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    특정 relayState를 사용하여 로그인 요청을 생성하고 리디렉션하는 메소드.
     *    relayState로 로그인 성공 후 이동할 URL을 지정할 수 있음.
     * 2. 사용법
     *    로그인 후 특정 페이지로 이동을 원하는 경우 사용.
     * 3. 예시 데이터
     *    - Input: relayState
     *    - Output: 생성된 로그인 요청 URL로 리디렉션
     * </PRE>
     * @param relayState 리디렉션할 상태 정보를 포함하는 URL
     * @throws IOException 입력/출력 오류 발생 시
     * @throws SettingsException 설정 오류 발생 시
     */
    public void login(String relayState) throws IOException, SettingsException {
        this.login(relayState, new AuthnRequestParams(false, false, true));
    }

    /*
     * 1. 메소드명: login
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    특정 relayState와 인증 요청 매개변수를 사용하여 로그인 요청을 생성함.
     * 2. 사용법
     *    사용자 지정 relayState와 인증 요청 파라미터를 설정하여 로그인 요청을 보낼 때 사용.
     * 3. 예시 데이터
     *    - Input: relayState, authnRequestParams
     *    - Output: 생성된 로그인 요청 URL로 리디렉션
     * </PRE>
     * @param relayState 리디렉션할 상태 정보를 포함하는 URL
     * @param authnRequestParams 인증 요청 매개변수
     * @throws IOException 입력/출력 오류 발생 시
     * @throws SettingsException 설정 오류 발생 시
     */
    public void login(String relayState, AuthnRequestParams authnRequestParams) throws IOException, SettingsException {
        this.login(relayState, authnRequestParams, false);
    }

    /*
     * 1. 메소드명: login
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    인증 요청 매개변수를 사용하여 로그인 요청을 생성하고 stay 플래그에 따라 리디렉션을 결정함.
     * 2. 사용법
     *    인증 요청 생성 시 relayState 및 stay 플래그를 설정하여 호출함.
     * 3. 예시 데이터
     *    - Input: relayState, authnRequestParams, stay
     *    - Output: 생성된 로그인 요청 URL로 리디렉션
     * </PRE>
     * @param relayState 리디렉션할 상태 정보를 포함하는 URL
     * @param authnRequestParams 인증 요청 매개변수
     * @param stay 리디렉션 여부 (true일 경우 리디렉션하지 않음)
     * @return 생성된 로그인 요청 URL
     * @throws IOException 입력/출력 오류 발생 시
     * @throws SettingsException 설정 오류 발생 시
     */
    public String login(String relayState, AuthnRequestParams authnRequestParams, Boolean stay) throws IOException, SettingsException {
        return this.login(relayState, (AuthnRequestParams)authnRequestParams, stay, (Map)(new HashMap()));
    }

    /*
     * 1. 메소드명: login
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    인증 요청을 생성하고, relayState, 요청 파라미터를 설정하여 리디렉션을 수행함.
     *    필요 시 인증 요청에 서명을 추가.
     * 2. 사용법
     *    로그인 요청 시 설정을 추가하여 인증 요청을 생성할 때 호출함.
     * 3. 예시 데이터
     *    - Input: relayState, authnRequestParams, stay, parameters
     *    - Output: 생성된 로그인 요청 URL로 리디렉션
     * </PRE>
     * @param relayState 리디렉션할 상태 정보를 포함하는 URL
     * @param authnRequestParams 인증 요청 매개변수
     * @param stay 리디렉션 여부
     * @param parameters 요청에 포함할 추가 매개변수
     * @return 생성된 로그인 요청 URL
     * @throws IOException 입력/출력 오류 발생 시
     * @throws SettingsException 설정 오류 발생 시
     */
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

    /*
     * 1. 메소드명: buildRequestSignature
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    SAML 요청 메시지에 대해 서명을 생성하는 메소드.
     * 2. 사용법
     *    주어진 SAML 요청과 서명 알고리즘을 기반으로 서명을 생성.
     * 3. 예시 데이터
     *    - Input: samlRequest (SAML 요청 메시지), relayState (리레이 상태), signAlgorithm (서명 알고리즘)
     *    - Output: 서명된 문자열
     * </PRE>
     * @param samlRequest SAML 요청 메시지
     * @param relayState 리레이 상태 값
     * @param signAlgorithm 서명에 사용할 알고리즘
     * @return 서명된 SAML 요청 메시지 문자열
     * @throws SettingsException 서명 생성에 필요한 설정 오류 시 발생
     */
    public String buildRequestSignature(String samlRequest, String relayState, String signAlgorithm) throws SettingsException {
        return this.buildSignature(samlRequest, relayState, signAlgorithm, "SAMLRequest");
    }

    /*
     * 1. 메소드명: buildResponseSignature
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    SAML 응답 메시지에 대해 서명을 생성하는 메소드.
     * 2. 사용법
     *    주어진 SAML 응답과 서명 알고리즘을 기반으로 서명을 생성.
     * 3. 예시 데이터
     *    - Input: samlResponse (SAML 응답 메시지), relayState (리레이 상태), signAlgorithm (서명 알고리즘)
     *    - Output: 서명된 문자열
     * </PRE>
     * @param samlResponse SAML 응답 메시지
     * @param relayState 리레이 상태 값
     * @param signAlgorithm 서명에 사용할 알고리즘
     * @return 서명된 SAML 응답 메시지 문자열
     * @throws SettingsException 서명 생성에 필요한 설정 오류 시 발생
     */
    public String buildResponseSignature(String samlResponse, String relayState, String signAlgorithm) throws SettingsException {
        return this.buildSignature(samlResponse, relayState, signAlgorithm, "SAMLResponse");
    }

    /*
     * 1. 메소드명: buildSignature
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    주어진 SAML 메시지를 기반으로 서명을 생성하는 메소드.
     * 2. 사용법
     *    요청 또는 응답 메시지에 대해 서명을 생성하고 설정에 따른 키 유효성을 검사.
     * 3. 예시 데이터
     *    - Input: samlMessage (SAML 메시지), relayState (리레이 상태), signAlgorithm (서명 알고리즘), type (메시지 타입)
     *    - Output: 생성된 서명 문자열
     * </PRE>
     * @param samlMessage SAML 메시지
     * @param relayState 리레이 상태 값
     * @param signAlgorithm 서명에 사용할 알고리즘
     * @param type 메시지 유형 (SAMLRequest 또는 SAMLResponse)
     * @return 생성된 서명 문자열
     * @throws SettingsException 설정 오류 시 발생
     * @throws IllegalArgumentException 서명 생성 오류 시 발생
     */
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

    /*
     * 1. 메소드명: getLastRequestXML
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    마지막 SAML 요청의 XML 문자열을 반환.
     * 2. 사용법
     *    최근 요청된 SAML XML 데이터를 조회.
     * 3. 예시 데이터
     *    - Output: 최근 요청된 XML 데이터
     * </PRE>
     * @return String 최근 요청된 SAML 요청 XML
     */
    public String getLastRequestXML() {
        return this.lastRequest;
    }

    /*
     * 1. 메소드명: getLastResponseXML
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    마지막 SAML 응답의 XML 문자열을 반환.
     * 2. 사용법
     *    최근 응답된 SAML XML 데이터를 조회.
     * 3. 예시 데이터
     *    - Output: 최근 응답된 XML 데이터
     * </PRE>
     * @return String 최근 응답된 SAML 응답 XML
     */
    public String getLastResponseXML() {
        return this.lastResponse;
    }

    /*
     * 1. 메소드명: setSamlMessageFactory
     * 2. 클래스명: Auth
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    SAML 메시지를 생성하는 팩토리 객체를 설정.
     * 2. 사용법
     *    기본 SAML 메시지 팩토리 객체를 사용자 정의 팩토리로 변경할 때 사용.
     * 3. 예시 데이터
     *    - Input: samlMessageFactory - SAML 메시지 팩토리 객체
     * </PRE>
     * @param samlMessageFactory SAML 메시지 팩토리 객체
     */
    public void setSamlMessageFactory(SamlMessageFactory samlMessageFactory) {
        this.samlMessageFactory = samlMessageFactory != null ? samlMessageFactory : DEFAULT_SAML_MESSAGE_FACTORY;
    }
}