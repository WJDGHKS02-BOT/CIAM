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
import com.samsung.ciam.saml.ServletUtils;
import com.onelogin.saml2.settings.Saml2Settings;
import com.samsung.ciam.common.gigya.service.GigyaService;
import com.samsung.ciam.models.Users;
import com.samsung.ciam.repositories.UsersRepository;
import com.samsung.ciam.services.CdcTraitService;
import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
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
import java.util.stream.Collectors;

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
    private ResourceLoader resourceLoader;

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private LocaleResolver localeResolver;  // LocaleResolver 주입

    @Autowired
    private ChannelRepository channelRepository;

    @GetMapping("/metadata")
    @ResponseBody
    public ResponseEntity<String> metadata() throws Exception {
        String samlConfig = BeansUtil.getApplicationProperty("onelogin.saml.config-file");
        Auth auth = new Auth(String.format("%s", samlConfig));
        String metaData = "";
        List<String> errors = new ArrayList<>();
        try {
            metaData = auth.getSettings().getSPMetadata();
            errors = Saml2Settings.validateMetadata(metaData);
        } catch (Exception e) {
            errors.add(e.getMessage());
        }

        if (!errors.isEmpty()) {
            String errorMessage = "Onelogin metadata errors: " + String.join(",", errors);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(errorMessage);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(metaData);
    }

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
            auth.login(relayState, authnRequestParams);
        } catch (Exception e) {
            log.error(e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SAML login error: " + e.getMessage());
        }
    }


    @GetMapping("/slo")
    public RedirectView slo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Default to routing fallback URL
        String samlConfig = BeansUtil.getApplicationProperty("onelogin.saml.config-file");
        Auth auth = new Auth(String.format("%s", samlConfig), request, response);
        String redirectUrl = "/";
        try {
            // Process SLO setting stay parameter to true to allow flow handling
            auth.processSLO(true, null, false);
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
            // Invalidate session
            request.getSession().invalidate();

        } catch (Exception e) {
            // Handle SLO or Gigya logout errors
            log.error(e.getMessage());
            return new RedirectView("/error?message=A user could not be resolved by the Onelogin Controller");
        }
        return new RedirectView(redirectUrl);
    }


    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException, SettingsException, Error {
        // 현재 요청의 URL을 RelayState로 사용
        String samlConfig = BeansUtil.getApplicationProperty("onelogin.saml.config-file");
        Auth auth = new Auth(String.format("%s", samlConfig), request, response);
        String relayState = request.getRequestURL().toString();
        String returnTo = "";

        // PHP 코드에서 사용된 변수들을 Java에서 세션에서 가져오기
        String nameId = (String) request.getSession().getAttribute("samlNameId");
        String sessionIndex = (String) request.getSession().getAttribute("samlSessionIndex");
        String nameIdFormat = (String) request.getSession().getAttribute("samlNameIdFormat");
        String samlNameIdNameQualifier = (String) request.getSession().getAttribute("samlNameIdNameQualifier");
        String samlNameIdSPNameQualifier = (String) request.getSession().getAttribute("samlNameIdSPNameQualifier");

        // PHP의 parameters 배열에 해당하는 HashMap 생성
        Map<String, String> parameters = new HashMap<>();

        try {
            auth.logout(returnTo, nameId, sessionIndex, false, nameIdFormat, samlNameIdNameQualifier, samlNameIdSPNameQualifier, parameters);
        } catch (Exception e) {
            log.error(e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SAML logout error: " + e.getMessage());
        }
    }

    @PostMapping("/acs")
    public RedirectView acs(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Auth auth = new Auth(request, response);
        String error = null;
        try {
            auth.processResponse();
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

        Map<String, List<String>> userAttributes = auth.getAttributes();

        OneloginLoginEvent loginEvent = new OneloginLoginEvent(userAttributes);
        Users user = findUserByEvent(loginEvent);

        if (user == null) {
            user = resolveUser(userAttributes);
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
        //return new RedirectView(defaultRedirectUrl);
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
            user.setCdcUid(uid);
            usersRepository.save(user);
        }

        return user;
    }

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

//        if ("gmapvd".equalsIgnoreCase(regSource)) {
//            regSource = "GMAP-VD";
//        } else if ("gmapda".equalsIgnoreCase(regSource)) {
//            regSource = "GMAP-DA";
//        } else if ("sba".equalsIgnoreCase(regSource)) {
//            regSource = "SBA";
//        } else if ("tnp".equalsIgnoreCase(regSource)) {
//            regSource = "TNP";
//        } else if ("mmp".equalsIgnoreCase(regSource)) {
//            regSource = "SFDC-G";
//        } else if ("e2e".equalsIgnoreCase(regSource)) {
//            regSource = "SFDC-G";
//        } else if ("ets".equalsIgnoreCase(regSource)) {
//            regSource = "SFDC-G";
//        } else if ("toolmate".equalsIgnoreCase(regSource)) {
//            regSource = "ToolMate";
//        } else if ("partnerhub".equalsIgnoreCase(regSource)) {
//            regSource = "PartnerH";
//        } else if ("edo".equalsIgnoreCase(regSource)) {
//            regSource = "PartnerH";
//        }



        request.getSession().setAttribute("cdc_uid", uid);
        request.getSession().setAttribute("cdc_locale", userLocale);
        request.getSession().setAttribute("cdc_language", language);
        request.getSession().setAttribute("cdc_username", CDCUserProfile.path("profile").path("username").asText());
        request.getSession().setAttribute("cdc_email", user.getEmail());
        request.getSession().setAttribute("cdc_regSource", regSource);
//        request.getSession().setAttribute("cdc_ciamadmin", isCIAMAdmin);
//        request.getSession().setAttribute("cdc_companyadmin", isCompanyAdmin);

        // channels 노드가 객체인 경우 key-value 쌍을 탐색
        Iterator<String> fieldNames = channels.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode channelNode = channels.path(fieldName);
            String approvalStatus = channelNode.path("approvalStatus").asText();

            // approvalStatus가 approved인 경우 key 값을 Set에 추가
            if ("approved".equalsIgnoreCase(approvalStatus)) {
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

        //determineChannelAdminType(request);

//        String myRole = "GeneralUser";
//        if (Boolean.TRUE.equals(request.getSession().getAttribute("cdc_companyadmin"))) {
//            myRole = "CompanyAdmin";
//        }
//        if (Boolean.TRUE.equals(request.getSession().getAttribute("cdc_channeladmin"))) {
//            Integer channelAdminType = (Integer) request.getSession().getAttribute("cdc_channeladminType");
//            if (channelAdminType != null) {
//                if (channelAdminType == 1) {
//                    myRole = "ChannelSystemAdmin";
//                } else if (channelAdminType == 2) {
//                    myRole = "ChannelBusinessAdmin";
//                }
//            }
//        }
//        if (Boolean.TRUE.equals(request.getSession().getAttribute("cdc_ciamadmin"))) {
//            myRole = "CIAMAdmin";
//        }
//
//        request.getSession().setAttribute("btp_myrole", myRole);
    }

    // Method to determine the channel admin type
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