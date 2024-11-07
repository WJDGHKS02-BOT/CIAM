package com.samsung.ciam.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSResponse;
import com.onelogin.saml2.authn.AuthnRequestParams;
import com.samsung.ciam.repositories.ChannelRepository;
import com.samsung.ciam.saml.Auth;
import com.onelogin.saml2.exception.Error;
import com.onelogin.saml2.exception.SettingsException;
import com.onelogin.saml2.exception.ValidationError;
import com.samsung.ciam.saml.OneloginLoginEvent;
import com.onelogin.saml2.settings.Saml2Settings;
import com.samsung.ciam.common.gigya.service.GigyaService;
import com.samsung.ciam.models.Users;
import com.samsung.ciam.repositories.UsersRepository;
import com.samsung.ciam.services.CdcTraitService;
import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 1. 파일명   : OneloginController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : SAML 인증 관련 SP 구현 로직을 포함한 컨트롤러
 * 4. 작성자   : 서정환
 * 5. 작성일자 : 2024. 11. 04.
 * 6. 참고자료 : https://github.com/SAML-Toolkits/java-saml
 * 7. 히스토리 :
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
@Slf4j
@Controller
@RequestMapping("/sso")
public class OneloginController {

    private Auth auth;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private LocaleResolver localeResolver;  // LocaleResolver 주입

    @Autowired
    private ChannelRepository channelRepository;

    /*
     * 1. 메소드명: metadata
     * 2. 클래스명: OneloginController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    SAML 메타데이터 정보를 제공하여 SAML 인증 설정 검증
     * 2. 사용법
     *    '/sso/metadata' 경로로 GET 요청
     * </PRE>
     * @return ResponseEntity 메타데이터 XML 또는 오류 메시지
     */
    @GetMapping("/metadata")
    @ResponseBody
    public ResponseEntity<String> metadata() throws Exception {
        String samlConfig = BeansUtil.getApplicationProperty("onelogin.saml.config-file");
        Auth auth = new Auth(String.format("%s", samlConfig));
        String metaData = "";
        List<String> errors = new ArrayList<>();
        try {
            metaData = auth.getSettings().getSPMetadata();
            errors = Saml2Settings.validateMetadata(metaData); // 메타데이터 검증
        } catch (Exception e) {
            errors.add(e.getMessage());
        }

        if (!errors.isEmpty()) {
            // 오류가 있을 경우, 내부 서버 오류 상태와 메시지를 반환
            String errorMessage = "Onelogin metadata errors: " + String.join(",", errors);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(errorMessage);
        }

        // 성공 시 XML 메타데이터 반환
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(metaData);
    }

    /*
     * 1. 메소드명: login
     * 2. 클래스명: OneloginController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    SAML 로그인 요청을 전송하여 인증 절차 시작
     * 2. 사용법
     *    '/sso/login' 경로로 접근하여 호출
     * </PRE>
     * @param request HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     */
    @GetMapping("/login")
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException, SettingsException, Error {
        // Auth 객체 초기화 (Auth 클래스는 사용자 정의 클래스일 수 있음)
        String samlConfig = BeansUtil.getApplicationProperty("onelogin.saml.config-file");
        Auth auth = new Auth(String.format("%s", samlConfig), request, response);
        // 현재 요청의 URL을 RelayState로 사용
        String relayState = (String) request.getSession().getAttribute("relayState");

        // AuthnRequestParams 객체 생성
        AuthnRequestParams authnRequestParams = new AuthnRequestParams(false, false, true);

        // SAML 인증 요청을 보내기 위해 Auth 객체의 login 메소드 호출
        try {
            auth.login(relayState, authnRequestParams); // 로그인 요청 시작
        } catch (Exception e) {
            log.error(e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SAML login error: " + e.getMessage());
        }
    }

    /*
     * 1. 메소드명: slo
     * 2. 클래스명: OneloginController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    SAML Single Logout(SLO) 요청을 전송하여 로그아웃 절차 수행
     * 2. 사용법
     *    '/sso/slo' 경로로 GET 요청
     * </PRE>
     * @param request HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @return RedirectView 로그아웃 이후 리다이렉트 URL
     */
    @GetMapping("/slo")
    public RedirectView slo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Default to routing fallback URL
        String samlConfig = BeansUtil.getApplicationProperty("onelogin.saml.config-file");
        Auth auth = new Auth(String.format("%s", samlConfig), request, response);
        String redirectUrl = "/"; // 기본 리다이렉트 URL 설정
        try {
            // Process SLO setting stay parameter to true to allow flow handling
            auth.processSLO(true, null, false); // SLO 처리
            String error = auth.getLastErrorReason();

            // 오류 메시지가 있는 경우 500 상태 코드와 함께 예외를 던짐
            if (error != null && !error.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Onelogin SLO validation errors: " + error);
            }

            // 사용자 세션에서 cdc_uid 가져오기
            HttpSession session = request.getSession(false);
            if (session != null) {
                String cdcUid = (String) session.getAttribute("cdc_uid");

                if (cdcUid != null) {
                    // Gigya 사용자 로그아웃 처리
                    String method = "accounts.logout";
                    Map<String, Object> params = new HashMap<>();
                    params.put("UID", cdcUid);
                    GSResponse gigyaResponse = gigyaService.executeRequest("", method, params);
                    log.info("do user logout for: " + cdcUid + " | " + gigyaResponse);
                }
            }
            // 세션 삭제
            request.getSession().invalidate();

        } catch (Exception e) {
            // SLO 또는 Gigya 로그아웃 오류 처리
            log.error(e.getMessage());
            return new RedirectView("/error?message=A user could not be resolved by the Onelogin Controller");
        }
        return new RedirectView(redirectUrl);
    }

    /*
     * 1. 메소드명: logout
     * 2. 클래스명: OneloginController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    SAML 로그아웃 요청을 전송하여 사용자의 세션을 종료하고 SAML 세션을 로그아웃 처리
     * 2. 사용법
     *    '/sso/logout' 경로로 GET 요청을 통해 호출
     * </PRE>
     * @param request HttpServletRequest 객체로 사용자 요청 정보를 포함
     * @param response HttpServletResponse 객체로 응답 정보를 포함
     */
    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException, SettingsException, Error {
        // 현재 요청의 URL을 RelayState로 사용
        String samlConfig = BeansUtil.getApplicationProperty("onelogin.saml.config-file");
        Auth auth = new Auth(String.format("%s", samlConfig), request, response);
        String relayState = request.getRequestURL().toString(); // 현재 URL을 relayState로 사용
        String returnTo = "";

        // 세션에서 SAML 관련 식별자 및 세션 인덱스 가져오기
        String nameId = (String) request.getSession().getAttribute("samlNameId");
        String sessionIndex = (String) request.getSession().getAttribute("samlSessionIndex");
        String nameIdFormat = (String) request.getSession().getAttribute("samlNameIdFormat");
        String samlNameIdNameQualifier = (String) request.getSession().getAttribute("samlNameIdNameQualifier");
        String samlNameIdSPNameQualifier = (String) request.getSession().getAttribute("samlNameIdSPNameQualifier");

        // 매개변수를 HashMap으로 생성
        Map<String, String> parameters = new HashMap<>();

        try {
            // 로그아웃 요청 시작
            auth.logout(returnTo, nameId, sessionIndex, false, nameIdFormat, samlNameIdNameQualifier, samlNameIdSPNameQualifier, parameters);
        } catch (Exception e) {
            log.error(e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SAML logout error: " + e.getMessage());
        }
    }

    /*
     * 1. 메소드명: acs
     * 2. 클래스명: OneloginController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    SAML ACS(Assertion Consumer Service) 요청을 처리하여 인증된 사용자 세션을 설정
     * 2. 사용법
     *    '/sso/acs' 경로로 POST 요청
     * </PRE>
     * @param request HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @return RedirectView 인증 후 리다이렉트할 URL
     */
    @PostMapping("/acs")
    public RedirectView acs(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Auth auth = new Auth(request, response);
        String error = null;
        try {
            auth.processResponse(); // 응답 처리
            error = auth.getLastErrorReason();
        } catch (ValidationError | Error errorException) {
            error = errorException.getMessage();
        }

        if (error != null && !error.isEmpty()) {
            return new RedirectView("/error?message=Onelogin ACS validation errors: " + error);
        }

        if (!auth.isAuthenticated()) {
            return new RedirectView("/error?message=Unauthorized to use this application");
        }

        Map<String, List<String>> userAttributes = auth.getAttributes(); // 인증된 사용자 속성 가져오기

        OneloginLoginEvent loginEvent = new OneloginLoginEvent(userAttributes);
        Users user = findUserByEvent(loginEvent); // 이벤트로 사용자 찾기

        if (user == null) {
            user = resolveUser(userAttributes); // 사용자 속성으로 사용자 생성
        }

        if (user == null) {
            return new RedirectView("/error?message=A user could not be resolved by the Onelogin Controller");
        }

        List<String> uidList = userAttributes.get("uid");
        String uid = "";
        if (uidList != null && !uidList.isEmpty()) {
            uid = uidList.get(0);  // 리스트에서 첫 번째 값을 가져옴
        } else {
            uid = user.getCdcUid();
        }

        // 사용자 로그인 세션 설정
        setUserSession(request, user,uid);

        // 세션에 인증된 사용자 정보 저장
        request.getSession().setAttribute("authenticatedUser", user);

        // 기본 리다이렉트 URL 설정
        String defaultRedirectUrl = "/myPage/personalInformation";
        String relayState = request.getParameter("RelayState");
        if (relayState != null) {
            String rootUrl = request.getContextPath();
            if (relayState.equals(rootUrl) || relayState.equals(rootUrl + "/")) {
                return new RedirectView(defaultRedirectUrl);
            } else {
                return new RedirectView(relayState);
            }
        } else {
            return new RedirectView(defaultRedirectUrl);
        }
    }

    /*
     * 1. 메소드명: resolveUser
     * 2. 클래스명: OneloginController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    주어진 사용자 속성 맵을 통해 사용자를 검색하거나 새로운 사용자를 생성하여 반환
     * 2. 사용법
     *    userAttributes 파라미터로 사용자 속성들을 포함하여 호출
     * 3. 예시 데이터
     *    - Input: 사용자 속성 맵 (예: uid, 이메일, 사용자 이름)
     *    - Output: Users 객체
     * </PRE>
     * @param userAttributes 사용자 속성을 포함한 맵
     * @return Users 객체
     */
    private Users resolveUser(Map<String, List<String>> userAttributes) {
        String uidAttributeName = "uid"; // Adjust this to your actual attribute name
        String emailAttributeName = "User.Email";
        String userNameAttributeName = "User.Username";

        List<String> uidList = userAttributes.get(uidAttributeName);
        List<String> emailList = userAttributes.get(emailAttributeName);
        List<String> userNameList = userAttributes.get(userNameAttributeName);

        if (uidList == null || uidList.isEmpty() || emailList == null || emailList.isEmpty() || userNameList == null || userNameList.isEmpty()) {
            return null;
        }

        String uid = uidList.get(0);
        String email = emailList.get(0);

        Users user = usersRepository.findByEmail(email);
        if (user == null) {
            // User does not exist, create a new one
            user = new Users();
            user.setCdcUid(uid);
            user.setEmail(email);
            usersRepository.save(user);
        } else {
            // Update existing user with the UID if necessary
            user.setCdcUid(uid); // 기존 사용자 UID 업데이트
            usersRepository.save(user);
        }

        return user;
    }

    /*
     * 1. 메소드명: setUserSession
     * 2. 클래스명: OneloginController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    주어진 사용자 정보를 세션에 설정하여 사용자 인증 세션을 관리
     * 2. 사용법
     *    사용자가 로그인 후 호출되어 세션에 사용자 데이터를 설정
     * 3. 예시 데이터
     *    - Input: 사용자 요청 정보와 Users 객체
     *    - Output: 세션에 사용자 정보 저장
     * </PRE>
     * @param request HttpServletRequest 객체
     * @param user Users 객체
     * @param uid 사용자 고유 식별자
     */
    public void setUserSession(HttpServletRequest request, Users user,String uid) {
        Set<String> approvedChannels = new HashSet<>();
        JsonNode CDCUserProfile = cdcTraitService.getCdcUser(uid, 0);

        boolean isCIAMAdmin = CDCUserProfile.path("data").path("isCIAMAdmin").asBoolean(false);
        boolean isCompanyAdmin = CDCUserProfile.path("data").path("isCompanyAdmin").asBoolean(false);
        JsonNode channels = CDCUserProfile.path("data").path("channels");
        String companyID = CDCUserProfile.path("data").path("accountID").asText(null);
        String companyName = CDCUserProfile.path("profile").path("work").path("company").asText(null);
        String language = CDCUserProfile.path("profile").path("languages").asText(null);
        String userLocale = CDCUserProfile.path("profile").path("locale").asText(null);
        String regSource = CDCUserProfile.path("regSource").asText(null);
        String socialProviders = CDCUserProfile.path("socialProviders").asText("");
        String samsungAdYn = (socialProviders != null && !socialProviders.isEmpty() && socialProviders.contains("saml-samsung-ad")) ? "Y" : "N";

        if (userLocale != null && !userLocale.isEmpty()) {
            Locale locale;
            // language 값에 따른 로케일 설정 (ko, en만 처리)
            if (userLocale.equals("ko")) {
                locale = Locale.KOREAN;
            } else if (userLocale.equals("en")) {
                locale = Locale.ENGLISH;
            } else {
                locale = Locale.getDefault();  // 기본 로케일 사용
            }

            // localeResolver를 사용하여 요청의 로케일 설정
            localeResolver.setLocale(request, null, locale);
        }

        // regSource 값을 변환
        regSource = channelRepository.selectChannelRegCh(regSource);

        // 사용자 세션 속성 설정
        request.getSession().setAttribute("cdc_uid", uid);
        request.getSession().setAttribute("cdc_locale", userLocale);
        request.getSession().setAttribute("cdc_language", language);
        request.getSession().setAttribute("cdc_username", CDCUserProfile.path("profile").path("username").asText());
        request.getSession().setAttribute("cdc_email", user.getEmail());
        request.getSession().setAttribute("cdc_regSource", regSource);
        request.getSession().setAttribute("samsungAdYn", samsungAdYn);

        // channels 노드가 객체인 경우 key-value 쌍을 탐색
        Iterator<String> fieldNames = channels.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode channelNode = channels.path(fieldName);
            String approvalStatus = channelNode.path("approvalStatus").asText();

            // approvalStatus가 approved인 경우 key 값을 Set에 추가
            if ("approved".equalsIgnoreCase(approvalStatus) || "inactive".equalsIgnoreCase(approvalStatus)) {
                approvedChannels.add(fieldName);
            }
        }
        // 세션에 approved된 채널 key 값을 설정
        request.getSession().setAttribute("cdc_channels", approvedChannels);
        request.getSession().setAttribute("admin_channels", channels);
        request.getSession().setAttribute("cdc_companyid", companyID);
        request.getSession().setAttribute("cdc_companyname", companyName);

        if(request.getSession().getAttribute("session_channel")!=null) {
            cdcTraitService.setAdminSession(request.getSession());
        }

    }

    /*
     * 1. 메소드명: determineChannelAdminType
     * 2. 클래스명: OneloginController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    세션에 채널 관리자 여부와 관리자 유형을 설정
     * 2. 사용법
     *    사용자의 관리자 권한을 결정하여 세션에 설정할 때 호출
     * 3. 예시 데이터
     *    - Input: 사용자 요청 정보
     *    - Output: 세션에 관리자 권한 정보 설정
     * </PRE>
     * @param request HttpServletRequest 객체
     */
    private void determineChannelAdminType(HttpServletRequest request) {
        request.getSession().setAttribute("cdc_channeladmin", false);
        request.getSession().setAttribute("cdc_channeladminType", null);

        ObjectNode channelsNode = (ObjectNode) request.getSession().getAttribute("admin_channels");
        if (channelsNode != null) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> channels = mapper.convertValue(channelsNode, Map.class);

            for (Map.Entry<String, Object> entry : channels.entrySet()) {
                String channelID = entry.getKey();
                Map<String, Object> channelData = (Map<String, Object>) entry.getValue();

                String approvalStatus = (String) channelData.get("approvalStatus");
                String adminTypeString = (String) channelData.get("adminType");

                if ("approved".equals(approvalStatus) && adminTypeString != null) {
                    Integer adminType = Integer.parseInt(adminTypeString);
                    boolean isChannelAdmin = adminType > 0;

                    if (request.getSession().getAttribute("cdc_channeladminType") != null) {
                        Integer currentAdminType = (Integer) request.getSession().getAttribute("cdc_channeladminType");
                        if (adminType > currentAdminType) {
                            request.getSession().setAttribute("cdc_channeladminType", adminType);
                        }
                    } else {
                        request.getSession().setAttribute("cdc_channeladmin", isChannelAdmin);
                        request.getSession().setAttribute("cdc_channeladminType", adminType);
                    }
                }
            }
        }
    }

    /*
     * 1. 메소드명: findUserByEvent
     * 2. 클래스명: OneloginController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    Onelogin 로그인 이벤트에서 제공된 이메일을 통해 사용자를 검색
     * 2. 사용법
     *    OneloginLoginEvent 객체로 호출하여 사용자를 검색
     * 3. 예시 데이터
     *    - Input: OneloginLoginEvent 객체
     *    - Output: Users 객체
     * </PRE>
     * @param event Onelogin 로그인 이벤트
     * @return Users 객체
     */
    public Users findUserByEvent(OneloginLoginEvent event) {
        Map<String, List<String>> attributes = event.getUserAttributes();
        List<String> emails = attributes.get("User.Email");
        if (emails == null || emails.isEmpty()) {
            return null;
        }
        String email = emails.get(0);
        // 여기서 사용자를 이메일로 찾는 로직을 구현
        Users user = usersRepository.findByEmail(email);
        return user;
    }

}