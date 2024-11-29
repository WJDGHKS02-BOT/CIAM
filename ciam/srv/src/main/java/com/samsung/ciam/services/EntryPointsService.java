package com.samsung.ciam.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.gigya.service.GigyaService;
import com.samsung.ciam.models.*;
import com.samsung.ciam.repositories.*;
import com.samsung.ciam.utils.BeansUtil;
import com.samsung.ciam.utils.EncryptUtil;
import com.sun.jdi.event.ExceptionEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class EntryPointsService {

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private BtpAccountsRepository btpAccountsRepository;

    @Autowired
    private CisCountryRepository cisCountryRepository;

    @Autowired
    private SecServingCountryRepository secServingCountryRepository;

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private UserAgreedConsentsRepository userAgreedConsentsRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private ChannelConversionRepository channelConversionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // 사용자 채널 계정 생성
    public ModelAndView newChannelAccount(String param, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("registration");

        // CDC 요청 준비
        String regToken = (String) session.getAttribute("regToken");
        String cdcUid = (String) session.getAttribute("adCdcUid");
        Map<String, Object> cdcRequestParams = new HashMap<>();
        if (regToken != null && !regToken.isEmpty()) {
            cdcRequestParams.put("regToken", regToken);
        } else {
            cdcRequestParams.put("UID", cdcUid);
        }
        cdcRequestParams.put("extraProfileFields", "languages, address, phones, education, samlData");
        cdcRequestParams.put("include", "identities-active, loginIDs, profile, data");

        GSResponse response = gigyaService.executeRequest(param, "accounts.getAccountInfo", cdcRequestParams);

        ObjectMapper objectMapper = new ObjectMapper(); // JSON 데이터를 Map으로 변환하기 위한 ObjectMapper 사용
        try {
            // JSON 문자열을 Map으로 변환
            Map<String, Object> accUser = objectMapper.readValue(response.getResponseText(), Map.class);

            if (response.getErrorCode() == 0) {
                session.setAttribute("cdc_uid", accUser.get("UID"));
                session.setAttribute("uid", accUser.get("UID"));

                // Account 정보를 accountObject에 담기
                Map<String, Object> accountObject = new HashMap<>();
                String firstName = "", lastName = "", mobilePhone = "", userLanguage = "", locale = "en"; // 기본 locale 값 설정
                String loginId = "";

                // loginIDs 필드가 있는지 확인하고 사용
                Map<String, Object> loginIDs = (Map<String, Object>) accUser.get("loginIDs");
                if (loginIDs != null && loginIDs.get("username") != null) {
                    userLanguage = (String) ((Map<String, Object>) accUser.get("profile")).get("languages");
                    firstName = (String) ((Map<String, Object>) accUser.get("profile")).get("firstName");
                    lastName = (String) ((Map<String, Object>) accUser.get("profile")).get("lastName");
                    locale = (String) ((Map<String, Object>) accUser.get("profile")).get("locale");
                    loginId = (String) session.getAttribute("loginId");

                    if (loginId == null) {
                        loginId = (String) ((Map<String, Object>) accUser.get("profile")).get("email"); // 이메일을 loginId로 설정
                    }
                    session.setAttribute("loginId", loginId);

                    // phones 필드 처리
                    List<Map<String, Object>> phones = (List<Map<String, Object>>) ((Map<String, Object>) accUser.get("profile")).get("phones");
                    if (phones != null) {
                        for (Map<String, Object> phone : phones) {
                            if ("mobile_phone".equals(phone.get("type"))) {
                                mobilePhone = (String) phone.get("number");
                            }
                        }
                    }
                } else {
                    // samlData를 통해 사용자 정보 추출
                    Map<String, Object> identities = (Map<String, Object>) ((List<Map<String, Object>>) accUser.get("identities")).get(0);
                    Map<String, Object> samlData = (Map<String, Object>) identities.get("samlData");

                    // 사용자 이름 (fullname)
                    String fullname = (String) samlData.get("http://schemas.sec.com/2018/05/identity/claims/UserName_EN");
                    if (fullname != null) {
                        String[] nameParts = fullname.split(" ");
                        firstName = nameParts.length > 0 ? nameParts[0] : "";
                        lastName = nameParts.length > 1 ? nameParts[1] : "";
                    }

                    // 이메일을 loginId로 설정
                    loginId = (String) ((Map<String, Object>) accUser.get("profile")).get("email");
                    mobilePhone = (String) samlData.get("http://schemas.sec.com/2018/05/identity/claims/Mobile");
                    session.setAttribute("SAMLcompanyId", samlData.get("http://schemas.sec.com/2018/05/identity/claims/CompId"));
                    session.setAttribute("SAMLcompanyName", samlData.get("http://schemas.sec.com/2018/05/identity/claims/CompName"));
                    session.setAttribute("SAMLCountryCode", samlData.get("http://schemas.sec.com/2018/05/identity/claims/Country") != null ? samlData.get("http://schemas.sec.com/2018/05/identity/claims/Country") : "KR");
                    session.setAttribute("isSamsungStaff", true);
                    locale = "en"; // 기본적으로 locale을 "en"으로 설정
                }

                // 국가 코드 확인
                String usedCountryCode = (String) session.getAttribute("SAMLCountryCode");
                if (usedCountryCode == null) {
                    usedCountryCode = (String) ((Map<String, Object>) accUser.get("profile")).get("country");
                }
                if (usedCountryCode == null) {
                    usedCountryCode = "KR";
                }

                List<CisCountry> countries = cisCountryRepository.findAllOrderedByNameEn();
                List<CommonCode> divisions = commonCodeRepository.findByHeader("BIZ_WITH_DEPT");//divisionRepository.findAllByOrderByNameEnAsc();
                List<SecServingCountry> secCountries = secServingCountryRepository.selectCountriesByChannelExcludingSubsidiary(param);
                List<CommonCode> codeList = commonCodeRepository.findByHeader("COUNTRY_CODE");

                //List<String> subsidiaries = getSubsidiaryList(usedCountryCode);

                List<Map<String, String>> languages = new ArrayList<>();
                Map<String, String> koreanLanguageMap = new HashMap<>();
                koreanLanguageMap.put("name", "Korean");
                koreanLanguageMap.put("value", "ko_KR");
                languages.add(koreanLanguageMap);

                Map<String, String> englishLanguageMap = new HashMap<>();
                englishLanguageMap.put("name", "English");
                englishLanguageMap.put("value", "en_US");
                languages.add(englishLanguageMap);

                Object accountObj = session.getAttribute("accountObject");
                if (accountObj != null && accountObj instanceof Map) {
                    Map<String, String> accountMap = (Map<String, String>) accountObj;
                    accountObject.put("firstName", accountMap.getOrDefault("firstName", ""));
                    accountObject.put("lastName", accountMap.getOrDefault("lastName", ""));
                    accountObject.put("workPhone", accountMap.getOrDefault("workPhone", ""));
                    accountObject.put("secDept", accountMap.getOrDefault("secDept", ""));
                    accountObject.put("language", accountMap.getOrDefault("language", ""));
                    accountObject.put("work_phone", accountMap.getOrDefault("work_phone", ""));
                    accountObject.put("job_title", accountMap.getOrDefault("job_title", ""));
                    accountObject.put("country_code_work", accountMap.getOrDefault("country_code_work", ""));
                } else {
                    // 사용자 정보를 accountObject에 추가
                    // accountObject.put("presetEmail", Optional.ofNullable((String) ((Map<String, Object>) accUser.get("profile")).get("email")).orElse(""));
                    accountObject.put("firstName", Optional.ofNullable(firstName).orElse(""));
                    accountObject.put("lastName", Optional.ofNullable(lastName).orElse(""));
                    accountObject.put("userLanguage", Optional.ofNullable(userLanguage).orElse(""));
                    accountObject.put("userLocale", Optional.ofNullable(locale).orElse("")); // Locale 추가
                    accountObject.put("userCountry", Optional.ofNullable(usedCountryCode).orElse(""));
                    // accountObject.put("subsidiaries", Optional.ofNullable(subsidiaries).orElse(new ArrayList<>()));
                    accountObject.put("mobilePhone", Optional.ofNullable(mobilePhone).orElse(""));
                    accountObject.put("workPhone", Optional.ofNullable((String) ((Map<String, Object>) accUser.get("profile")).get("work.phone")).orElse(""));
                    accountObject.put("workLocation", Optional.ofNullable((String) ((Map<String, Object>) accUser.get("profile")).get("work.location")).orElse(""));
                    accountObject.put("salutation", Optional.ofNullable((String) ((Map<String, Object>) accUser.get("data")).get("salutation")).orElse(""));
                    accountObject.put("subsidiary", Optional.ofNullable((String) ((Map<String, Object>) accUser.get("data")).get("subsidiary")).orElse(""));
                    accountObject.put("division", Optional.ofNullable((String) ((Map<String, Object>) accUser.get("data")).get("division")).orElse(""));
                    accountObject.put("secDept", Optional.ofNullable((String) ((Map<String, Object>) accUser.get("data")).get("userDepartment")).orElse(""));
                }
                accountObject.put("loginId", Optional.ofNullable(loginId).orElse("")); // 로그인 ID 저장
                accountObject.put("divisions", Optional.ofNullable(divisions).orElse(new ArrayList<>()));
                accountObject.put("channel", Optional.ofNullable(param).orElse(""));
                accountObject.put("codes", Optional.ofNullable(codeList).orElse(new ArrayList<>()));
                accountObject.put("languages", Optional.ofNullable(languages).orElse(new ArrayList<>()));

                modelAndView.addObject("accountObject", accountObject);

                // 회사 정보 처리
                String bpId = "C100"; // 예시로 사용
                BtpAccounts companyInfo = btpAccountsRepository.selectByBpid(bpId);
                Map<String, Object> companyObject = new HashMap<>();
                Map<String, Object> regCompanyData = new HashMap<>();

                if (companyInfo != null) {
                    companyObject.put("bpid", Optional.ofNullable(bpId).orElse(""));
                    companyObject.put("source", Optional.ofNullable(companyInfo.getSource()).orElse(""));
                    companyObject.put("type", Optional.ofNullable(companyInfo.getType()).orElse(""));
                    companyObject.put("validStatus", Optional.ofNullable(companyInfo.getValidStatus()).orElse(""));
                    companyObject.put("zip_code", Optional.ofNullable(companyInfo.getZipCode()).orElse(""));
                    companyObject.put("vendorcode", Optional.ofNullable(companyInfo.getVendorcode()).orElse(""));

                    regCompanyData.put("name", Optional.ofNullable(companyInfo.getName()).orElse(""));
                    regCompanyData.put("country", Optional.ofNullable(companyInfo.getCountry()).orElse(""));
                    regCompanyData.put("state", Optional.ofNullable(companyInfo.getState()).orElse(""));
                    regCompanyData.put("city", Optional.ofNullable(companyInfo.getCity()).orElse(""));
                    regCompanyData.put("street_address", Optional.ofNullable(companyInfo.getStreetAddress()).orElse(""));
                    regCompanyData.put("phonenumber1", Optional.ofNullable(companyInfo.getPhonenumber1()).orElse(""));
                    regCompanyData.put("fax", Optional.ofNullable(companyInfo.getFaxno()).orElse(""));
                    regCompanyData.put("email", Optional.ofNullable(companyInfo.getEmail()).orElse(""));
                    regCompanyData.put("bizregno1", Optional.ofNullable(companyInfo.getBizregno1()).orElse(""));
                    regCompanyData.put("representative", Optional.ofNullable(companyInfo.getRepresentative()).orElse(""));
                    regCompanyData.put("vendorcode", Optional.ofNullable(companyInfo.getVendorcode()).orElse("C100"));
                    regCompanyData.put("zip_code", Optional.ofNullable(companyInfo.getZipCode()).orElse(""));
                    regCompanyData.put("validStatus", Optional.ofNullable(companyInfo.getValidStatus()).orElse(""));
                    regCompanyData.put("type", Optional.ofNullable(companyInfo.getType()).orElse(""));
                    regCompanyData.put("source", Optional.ofNullable(companyInfo.getSource()).orElse(""));
                    regCompanyData.put("bpid", Optional.ofNullable(bpId).orElse(""));

                    companyObject.put("registerCompany", regCompanyData);
                }
                companyObject.put("countries", countries);
                modelAndView.addObject("companyObject", companyObject);

                String content = "fragments/samsungAdRegistration/samsungAdInfomation";

                // 모델에 값 추가
                modelAndView.addObject("channel", param);
                modelAndView.addObject("content", content);

                return modelAndView;

            } else {
                return new ModelAndView("error/403");
            }
        } catch (Exception e) {
            log.error("Error while processing account data: ", e);
            return new ModelAndView("error/500");
        }
    }

    public RedirectView newChannelAccountSubmit(Map<String, String> requestParams, String param, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // 필수 필드 검증
            if (!requestParams.containsKey("firstName") || !requestParams.containsKey("lastName")) {
                redirectAttributes.addFlashAttribute("showErrorMsg", "First name and last name are required.");
                return new RedirectView(request.getHeader("Referer")); // Replace with your form URL
            }

            // CDC에서 사용자 정보 가져오기
            GSResponse accUserResponse = gigyaService.executeRequest("default", "accounts.getAccountInfo", Map.of("UID", session.getAttribute("uid")));
            if (accUserResponse.getErrorCode() != 0) {
                redirectAttributes.addFlashAttribute("showErrorMsg", "Error retrieving user information.");
                return new RedirectView(request.getHeader("Referer")); // Replace with your form URL
            }
            Map<String, Object> accUser = objectMapper.readValue(accUserResponse.getResponseText(), Map.class);
            String userEmail = (String) ((Map<String, Object>) accUser.get("profile")).get("email");

            // 전화번호 처리
            List<Map<String, String>> phoneArray = new ArrayList<>();
            if (StringUtils.hasText(requestParams.get("mobile_phone"))) {
                phoneArray.add(Map.of("number", "+" + requestParams.get("country_code") + " " + requestParams.get("mobile_phone"), "type", "mobile_phone"));
            }
            if (StringUtils.hasText(requestParams.get("work_phone"))) {
                phoneArray.add(Map.of("number", "+" + requestParams.get("country_code_work") + " " + requestParams.get("work_phone"), "type", "work_phone"));
            }

            // 회사 관리자 여부 처리
            boolean isCompanyAdmin = requestParams.containsKey("applyAsCompanyAdmin") && Boolean.parseBoolean(requestParams.get("applyAsCompanyAdmin"));
            String adminReason = isCompanyAdmin ? requestParams.get("adminReason") : "";

            Map<String, Object> profileFields = new HashMap<>();
            Map<String, Object> dataFields = new HashMap<>();

            // 세션에 회사 정보가 있는지 확인하여 다른 설정
            if (session.getAttribute("SAMLcompanyId") != null && session.getAttribute("SAMLcompanyName") != null) {
                // 회사 정보가 있을 때
                String companyCode = "C100";  // 기본값 설정
                String companyName = (String) session.getAttribute("SAMLcompanyName");

                profileFields.put("email", userEmail.toLowerCase());
                profileFields.put("firstName", requestParams.getOrDefault("firstName","").toUpperCase());
                profileFields.put("lastName", requestParams.getOrDefault("lastName","").toUpperCase());
                profileFields.put("country", requestParams.getOrDefault("country",""));

                session.setAttribute("secCountry",requestParams.getOrDefault("country",""));
                session.setAttribute("loginId",requestParams.getOrDefault("loginUserId",""));
                session.setAttribute("company_name","삼성전자(주)");

                // language 값을 처리
                String language = requestParams.get("language");
                String languagesValue = "en_US"; // 기본값

                if (language != null) {
                    switch (language) {
                        case "ko":
                        case "ko_KR":
                            languagesValue = "ko_KR";
                            profileFields.put("locale", "ko");
                            break;
                        case "en":
                        case "en_US":
                        default:
                            languagesValue = "en_US";
                            profileFields.put("locale", "en");
                            break;
                    }
                }

                profileFields.put("languages", languagesValue);

                // username 필드를 추가
                profileFields.put("username", accUser.get("profile") != null && ((Map<String, Object>) accUser.get("profile")).get("username") != null
                        ? ((Map<String, Object>) accUser.get("profile")).get("username").toString()
                        : userEmail.toLowerCase());

                profileFields.put("phones", phoneArray);
                profileFields.put("work", Map.of("company", companyName, "companyID", companyCode));

                dataFields.put("salutation", requestParams.getOrDefault("salutation",""));
                dataFields.put("userDepartment", requestParams.getOrDefault("secDept",""));
                dataFields.put("subsidiary", requestParams.getOrDefault("subsidiary",""));
                dataFields.put("isCompanyAdmin", isCompanyAdmin);
                dataFields.put("channels", Map.of(param, Map.of("approvalStatus", "pending","channelUID","")));
                dataFields.put("jobtitle", requestParams.getOrDefault("job_title",""));
                dataFields.put("reasonCompanyAdmin", adminReason);
                dataFields.put("accountID", companyCode);
                dataFields.put("vendorCode", companyCode);
            } else {
                // 회사 정보가 없을 때
                profileFields.put("email", userEmail.toLowerCase());
                profileFields.put("firstName", requestParams.getOrDefault("firstName","").toUpperCase());
                profileFields.put("lastName", requestParams.getOrDefault("lastName","").toUpperCase());
                profileFields.put("country", requestParams.getOrDefault("country",""));

                // language 값을 처리
                String language = requestParams.get("language");
                String languagesValue = "en_US"; // 기본값

                if (language != null) {
                    switch (language) {
                        case "ko":
                        case "ko_KR":
                            languagesValue = "ko_KR";
                            profileFields.put("locale", "ko");
                            break;
                        case "en":
                        case "en_US":
                        default:
                            languagesValue = "en_US";
                            profileFields.put("locale", "en");
                            break;
                    }
                }

                profileFields.put("languages", languagesValue);

                // username 필드를 추가
                profileFields.put("username", accUser.get("profile") != null && ((Map<String, Object>) accUser.get("profile")).get("username") != null
                        ? ((Map<String, Object>) accUser.get("profile")).get("username").toString()
                        : userEmail.toLowerCase());

                profileFields.put("phones", phoneArray);

                dataFields.put("salutation", requestParams.getOrDefault("salutation",""));
                dataFields.put("division", requestParams.getOrDefault("division",""));
                dataFields.put("isCompanyAdmin", isCompanyAdmin);
                dataFields.put("channels", Map.of(param, Map.of("approvalStatus", "pending")));
                dataFields.put("jobtitle", requestParams.getOrDefault("job_title",""));
                dataFields.put("reasonCompanyAdmin", adminReason);
            }

            // 사용자 상태 추가
            if (((Map<String, Object>) accUser.get("data")).get("userStatus") == null) {
                dataFields.put("userStatus", "regEmailVerified");
            }

            // CDC API 요청
            // CDC API 요청
            Map<String, Object> requestParamsMap = new HashMap<>();
            requestParamsMap.put("UID", session.getAttribute("uid"));

            String query = String.format("SELECT * FROM Account WHERE UID= '%s'", session.getAttribute("uid"));
            GSResponse searchResponse = gigyaService.executeRequest("default", "accounts.search", query);
            JsonNode responseData = objectMapper.readTree(searchResponse.getResponseText());
            JsonNode user = responseData.get("results").get(0);

            // regSource 필드가 존재하고, 그 값이 null이 아닌지 체크
            String regSource = (user.has("regSource") && user.get("regSource") != null) ? user.get("regSource").asText("") : "";

            // regSource 값이 null이거나 빈 문자열일 때만 추가
            if (regSource.isEmpty()) {
                session.setAttribute("firstAdRegistration","Y");
                requestParamsMap.put("regSource", param);
            }

            requestParamsMap.put("profile", objectMapper.writeValueAsString(profileFields));
            requestParamsMap.put("data", objectMapper.writeValueAsString(dataFields));

            GSResponse updateResponse = gigyaService.executeRequest(param, "accounts.setAccountInfo", requestParamsMap);

            if (updateResponse.getErrorCode() == 0) {
                session.setAttribute("accountObject", requestParams);

                // 세션에 필드 저장
                session.setAttribute("subsidiary", requestParams.getOrDefault("subsidiary",""));
                session.setAttribute("division", requestParams.getOrDefault("division",""));
                session.setAttribute("department", requestParams.getOrDefault("department",""));
                session.setAttribute("language", requestParams.getOrDefault("language",""));

                return new RedirectView("/new-channel/consent" + "?param=" + param); // 성공적으로 등록 후 리다이렉트
            } else if (updateResponse.getErrorCode() == 400006) {
                redirectAttributes.addFlashAttribute("showErrorMsg", "User account update failed: " + updateResponse.getResponseText());
                return new RedirectView(request.getHeader("Referer"));
            } else {
                redirectAttributes.addFlashAttribute("showErrorMsg", "User account update failed: " + updateResponse.getResponseText());
                return new RedirectView(request.getHeader("Referer"));
            }
        } catch (Exception e) {
            log.error("Error updating account", e);
            redirectAttributes.addFlashAttribute("showErrorMsg", "An error occurred while updating the account");
            return new RedirectView(request.getHeader("Referer"));
        }
    }

    public ModelAndView consent(String param, HttpSession session) {
        String subsidiary;
        String marketingTermsYn="";

        subsidiary = (String) session.getAttribute("secSubsidiary") != null ? (String) session.getAttribute("secSubsidiary") : "";

        if (session.getAttribute("marketingTermsYn") != null) {
            marketingTermsYn = (String) session.getAttribute("marketingTermsYn");
        }

        Map<String, Object> returnArray = cdcTraitService.consentSelector((String) session.getAttribute("uid"), param, "en", subsidiary);

        List<Channels> channelTables = channelRepository.selectByChannelNameContaining(param);
        Channels channelTable = channelTables.isEmpty() ? null : channelTables.get(0);
        String channelDisplayName = channelTable != null ? channelTable.getChannelDisplayName() : "";
        String secCountry = (String) session.getAttribute("secCountry");

        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/samsungAdRegistration/consent";
        modelAndView.addObject("content", content);
        modelAndView.addObject("secCountry", secCountry);
        modelAndView.addObject("marketingCommon", returnArray.get("marketingCommon"));
        modelAndView.addObject("marketingCommonText", returnArray.get("marketingCommonText"));
        modelAndView.addObject("marketingConsentid", returnArray.get("marketingConsentid"));
        modelAndView.addObject("marketingcontentid", returnArray.get("marketingcontentid"));
        modelAndView.addObject("secCountry", returnArray.get("secCountry"));
        modelAndView.addObject("privacyCommon", returnArray.get("privacyCommon"));
        modelAndView.addObject("privacyCommonText", returnArray.get("privacyCommonText"));
        modelAndView.addObject("commonPrivacyConsentId", returnArray.get("commonPrivacyConsentId"));
        modelAndView.addObject("commonPrivacyContentId", returnArray.get("commonPrivacyContentId"));
        modelAndView.addObject("privacyChannel", returnArray.get("privacyChannel"));
        modelAndView.addObject("privacyChannelText", returnArray.get("privacyChannelText"));
        modelAndView.addObject("channelPrivacyConsentId", returnArray.get("channelPrivacyConsentId"));
        modelAndView.addObject("channelPrivacyContentId", returnArray.get("channelPrivacyContentId"));
        modelAndView.addObject("termsCommon", returnArray.get("termsCommon"));
        modelAndView.addObject("termsCommonText", returnArray.get("termsCommonText"));
        modelAndView.addObject("commonTermConsentId", returnArray.get("commonTermConsentId"));
        modelAndView.addObject("commonTermContentId", returnArray.get("commonTermContentId"));
        modelAndView.addObject("termsChannel", returnArray.get("termsChannel"));
        modelAndView.addObject("termsChannelText", returnArray.get("termsChannelText"));
        modelAndView.addObject("channelTermsConsentId", returnArray.get("channelTermsConsentId"));
        modelAndView.addObject("channelTermsContentId", returnArray.get("channelTermsContentId"));
        modelAndView.addObject("channelDisplayName", channelDisplayName);
        modelAndView.addObject("marketingTermsYn", marketingTermsYn);
        modelAndView.addObject("channel", param);

        List<String> exceptionCountry = Arrays.asList(
                "TR", "BR", "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE",
                "FI", "FR", "DE", "GR", "HU", "IE", "IT", "LV", "LT", "LU",
                "MT", "NL", "PL", "PT", "RO", "SK", "SI", "ES", "SE"
        );
        boolean isNoneAgreeBtn = exceptionCountry.contains(secCountry);

        modelAndView.addObject("isNoneAgreeBtn", isNoneAgreeBtn);

        if ("CN".equals(secCountry)) {
            // Privacy channel texts for CN
            String privacyChannel1Text = "<p class=\"slds-m-bottom_xx-medium\" data-aura-rendered-by=\"25:224;a\">三星电子充分认识到个人信息对您的重要性，并将尽最大努力确保您个人信息的安全性和可靠性。" +
                    "在使用三星B2B Partner Portal (https://partnerhub.samsung.com)之前，请仔细阅读并同意我们的条款与条件，个人信息保护政策，及如下详细内容。</p><br data-aura-rendered-by=\"27:224;a\"><p class=\"slds-m-bottom_xx-medium\" data-aura-rendered-by=\"28:224;a\"><strong data-aura-rendered-by=\"29:224;a\">[基本业务功能]</strong></p><p class=\"slds-m-bottom_xx-medium\" data-aura-rendered-by=\"31:224;a\">如果您不同意收集和使用基本业务功能所需的个人信息，B2B Partner Portal将无法正常运行，我们将无法为您服务。" +
                    "如果您希望撤回对使用产品（或）服务过程中收集和使用个人信息的授权，您可访问“联系我们”菜单（注册VOC）。</p><br data-aura-rendered-by=\"33:224;a\"><div class=\"slds-scrollable slds-box\" data-aura-rendered-by=\"34:224;a\"><div dir=\"ltr\" class=\"slds-text-longform uiOutputRichText\" data-aura-rendered-by=\"37:224;a\" data-aura-class=\"uiOutputRichText\"><table data-aura-rendered-by=\"38:224;a\"><tbody><tr><td rowspan=\"1\" colspan=\"1\"><b>□ 收集和使用一般个人信息 (基本业务功能)</b></td></tr><tr><td rowspan=\"1\" colspan=\"1\">[必要]</td></tr></tbody></table>" +
                    "<table data-aura-rendered-by=\"38:224;a\"></table><table style=\"font-size: 10pt; text-align: center;\" data-aura-rendered-by=\"38:224;a\"><tbody><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>个人信息种类</b></td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>目的</b></td><td style=\"border: 1px solid gray;\" rowspan=\"8\" colspan=\"1\">严格按照个人信<br>息保护政策处理</td><td style=\"border: 1px solid gray;\" rowspan=\"6\" colspan=\"1\">数据将会删除：<br>1)当用户要求删除时<br>2) 离最后登录时间过1年时</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">姓名</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">识别用户姓名</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">用户名</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">用户登录系统的ID（邮箱形式)</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">公司、公司地址</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">确认公司名信息，识别是否为合作伙伴</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">事业部、部门、职位</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">所属及职位确认（用于商业联系）</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">办公电话、传真、手机</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">确认商业联系方式</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">国家</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">确认国家信息</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">邮箱</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">找回用户密码</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">用户要求时删除数据</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">IP、经纬度信息（由IP推断）</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">SaaS系统中的基本登录历史记录</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">6个月后删除数据</td></tr></tbody></table></div></div>";

            String privacyChannel2Text = "<div dir=\"ltr\" data-aura-rendered-by=\"46:224;a\" class=\"uiOutputRichText\" data-aura-class=\"uiOutputRichText\"><table data-aura-rendered-by=\"47:224;a\"><tbody><tr><td rowspan=\"1\" colspan=\"1\"><b>□ 跨境传输 (基本业务功能)</b></td></tr><tr><td rowspan=\"1\" colspan=\"1\">我们仅在征得您的同意后向第三方国家或国际组织传输您的个人信息、敏感个人信息或生物识别信息，详情如下表所示。<br>在您同意之前，请认真考虑接收者的数据安全能力及其个人信息保护政策</td></tr><tr><td rowspan=\"1\" colspan=\"1\">[必要]</td></tr></tbody></table>" +
                    "<table style=\"font-size: 10pt; text-align: center;\" width=\"1000px\" data-aura-rendered-by=\"47:224;a\"><tbody><tr><td width=\"150px\" style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>个人信息种类</b></td><td width=\"100px\" style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>接收者<br>身份</b></td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>接收者<br>联系信息</b></td><td width=\"100px\" style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>接收者<br>国家</b></td><td width=\"100px\" style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>接收者<br>处理方式</b></td><td width=\"200px\" style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>目的</b></td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>接收者的个人<br>信息保护政策<br>(链接)</b></td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">姓名、邮箱、用户名、IP、<br>经纬度信息（由IP推断）、<br>部门、职位名称、公司、<br>事业部、电话、传真、<br>手机、地址、国家" +
                    "</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">Samsung Electronics Co., Ltd.</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><a rel=\"noopener\" target=\"_blank\" href=\"https://partnerhub.samsung.com/s/communitylandingmain?p_subsidiary=SCIC&language=zh_CN&country=CHINA&continent=Asia%20-%20pacific\">https://partnerhub.<br>samsung.com/s/<br>communitylandingmain?<br>p_subsidiary=SCIC&amp<br>language=zh_CN&amp<br>country=CHINA&amp<br>continent=Asia<br>%20-%20pacific</a></td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">韩国</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">所有数据都通过HTTPS/TLS进行加密传输和存储。并严格按照本服务个人信息保护政策进行处理</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">为认证B2B营业支援系统的用户认证而收集。本系统是基于Salesforce 的SaaS解决方案的系统，为使用解决方案提供者的服务器，所有数据都会保存到Salesforce在日本 服务器里</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><a rel=\"noopener\" target=\"_blank\" href=\"https://partnerhub.samsung.com/s/communitylandingmain?p_subsidiary=SEC&language=en_US&country=SOUTH%20KOREA&continent=Asia%20-%20pacific\">https://partnerhub.<br>samsung.com/s/<br>communitylandingmain?<br>p_subsidiary=SEC&amp;<br>language=en_US&amp;<br>country=SOUTH%20KOREA&amp;<br>continent=Asia<br>%20-%20pacific</a></td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">姓名、国家、职位、邮箱</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">Samsung Electronics Co., Ltd.</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><a rel=\"noopener\" target=\"_blank\" href=\"http://itvoc.sec.samsung.net/\">http://itvoc.sec.<br>samsung.net/</a></td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">韩国</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">所有数据都通过合理可行的措施进行传输和存储。并严格按照本服务个人信息保护政策进行处理</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">为了通过关联登录（SSO登录）提供产品培训，该信息会传输到SBA(Samsung Business Academy)系统, 会存储在该服务的系统解决方案提供者（Cornerstone Ondemand)在英国 服务器里</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><a rel=\"noopener\" target=\"_blank\" href=\"https://www.samsungcpfr.com/sba/privacypolicy_cn.html\">https://www.samsung<br>cpfr.com/sba/<br>privacypolicy_cn.html</a></td></tr></tbody></table></div>";

            String etcCommonText = "该服务适用于年满14岁及以上的用户.";

            modelAndView.addObject("content", content);
            modelAndView.addObject("privacyChannel1", "privacy1." + param + ".cn");
            modelAndView.addObject("privacyChannel1Text", privacyChannel1Text);
            modelAndView.addObject("channelPrivacy1ConsentId", "99999");
            modelAndView.addObject("channelPrivacy1ContentId", "1");
            modelAndView.addObject("privacyChannel2", "privacy2." + param + ".cn");
            modelAndView.addObject("privacyChannel2Text", privacyChannel2Text);
            modelAndView.addObject("channelPrivacy2ConsentId", "99999");
            modelAndView.addObject("channelPrivacy2ContentId", "2");
            modelAndView.addObject("etcCommon", "etc" + param + ".cn");
            modelAndView.addObject("etcCommonText", etcCommonText);
            modelAndView.addObject("etcCommonConsentId", "99999");
            modelAndView.addObject("etcCommonContentId", "3");
        }

        return modelAndView;
    }

    public RedirectView consentSubmit(Map<String, String> requestParams, String param, HttpSession session, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        String uid = (String) session.getAttribute("uid");
        if (uid == null) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "session expired, please restart registration process");
            return new RedirectView(request.getHeader("Referer"));
        }

        String secCountry = (String) session.getAttribute("secCountry");
        String marketingTermsYn = "";
        Map<String, Object> params = new HashMap<>();
        params.put("uid", uid);
        params.put("termsCommon", requestParams.get("termsCommon"));
        params.put("termsChannel", requestParams.get("termsChannel"));
        params.put("privacyCommon", requestParams.get("privacyCommon"));
        params.put("privacyChannel", requestParams.get("privacyChannel"));

        /*if (!"FR".equals(secCountry)) {
            params.put("marketingCommon", requestParams.get("marketingCommon"));
        }*/
        if (requestParams.containsKey("marketingCommon") && requestParams.get("marketingCommon") != null) {
            params.put("marketingCommon", requestParams.get("marketingCommon"));
        }
        if ("CN".equals(secCountry) && "partnerhub".equals(param)) {
            params.put("privacyChannel1", requestParams.get("privacyChannel1"));
            params.put("privacyChannel2", requestParams.get("privacyChannel2"));
            params.put("etcCommon", requestParams.get("etcCommon"));
        }

        try {
            session.setAttribute("consentProfile", EncryptUtil.encryptData(new ObjectMapper().writeValueAsString(params)));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            redirectAttributes.addFlashAttribute("showErrorMsg", "Error processing consent data");
            return new RedirectView(request.getHeader("Referer"));
        }

        Map<String, Object> preferencesFields = new HashMap<>();
        preferencesFields.put(requestParams.get("termsCommon"), Map.of("isConsentGranted", true));
        preferencesFields.put(requestParams.get("termsChannel"), Map.of("isConsentGranted", true));
        preferencesFields.put(requestParams.get("privacyCommon"), Map.of("isConsentGranted", true));
        preferencesFields.put(requestParams.get("privacyChannel"), Map.of("isConsentGranted", true));

        if (requestParams.containsKey("marketingCommon")) {
            String[] marketingSelection = requestParams.get("marketingCommon").split(":");
            boolean marketingTerms = "1".equals(marketingSelection[0]);
            preferencesFields.put(marketingSelection[1], Map.of("isConsentGranted", marketingTerms));
            if (marketingTerms) {
                marketingTermsYn = "Y";
            } else {
                marketingTermsYn = "N";
            }
            session.setAttribute("marketingTermsYn",marketingTermsYn);
        }

        Map<String, Object> gigyaParams = new HashMap<>();
        try {
            gigyaParams.put("preferences", new ObjectMapper().writeValueAsString(preferencesFields));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            redirectAttributes.addFlashAttribute("showErrorMsg", "Error processing preferences data");
            return new RedirectView(request.getHeader("Referer"));
        }
        gigyaParams.put("UID", uid);

        GSResponse response = gigyaService.executeRequest(param, "accounts.setAccountInfo", gigyaParams);
        if (response.getErrorCode() == 0) {
            if (requestParams.containsKey("marketingCommon")) {
                boolean marketingTerms = "1".equals(requestParams.get("marketingCommon").split(":")[0]);
                if (marketingTerms) {
                    consentResponseUpdated(Long.valueOf(requestParams.get("marketingConsentid")), Long.valueOf(requestParams.get("marketingcontentid")), uid);
                } else {
                    saveRejectedConsent(Long.valueOf(requestParams.get("marketingConsentid")), Long.valueOf(requestParams.get("marketingcontentid")), uid);
                }
            }

            consentResponseUpdated(Long.valueOf(requestParams.get("commonTermConsentId")), Long.valueOf(requestParams.get("commonTermContentId")), uid);
            consentResponseUpdated(Long.valueOf(requestParams.get("channelTermsConsentId")), Long.valueOf(requestParams.get("channelTermsContentId")), uid);
            consentResponseUpdated(Long.valueOf(requestParams.get("commonPrivacyConsentId")), Long.valueOf(requestParams.get("commonPrivacyContentId")), uid);
            consentResponseUpdated(Long.valueOf(requestParams.get("channelPrivacyConsentId")), Long.valueOf(requestParams.get("channelPrivacyContentId")), uid);

            return new RedirectView("/new-channel/mfa"+ "?param=" + param);
        } else {
            redirectAttributes.addFlashAttribute("showErrorMsg", "Consent update failed");
            return new RedirectView(request.getHeader("Referer"));
        }
    }

    private void consentResponseUpdated(Long consentId, Long consentContentId, String uid) {
        UserAgreedConsents termsAgreed = new UserAgreedConsents();
        termsAgreed.setConsentId(consentId);
        termsAgreed.setConsentContentId(consentContentId);
        termsAgreed.setUid(uid);
        termsAgreed.setStatus("agreed");
        userAgreedConsentsRepository.save(termsAgreed);
    }

    private void saveRejectedConsent(Long consentId, Long consentContentId, String uid) {
        UserAgreedConsents termsAgreed = new UserAgreedConsents();
        termsAgreed.setConsentId(consentId);
        termsAgreed.setConsentContentId(consentContentId);
        termsAgreed.setUid(uid);
        termsAgreed.setStatus("rejected");
        userAgreedConsentsRepository.save(termsAgreed);
    }

    public RedirectView mfaSubmit(String param, HttpServletRequest request, Map<String, String> payload,RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession();

        if (session.getAttribute("uid") == null) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "session expired, please restart registration process");
            return new RedirectView(request.getHeader("Referer"));
        }

        Map<String, Object> dataFields = new HashMap<>();
        Map<String, Boolean> tfaMethods = new HashMap<>();

        tfaMethods.put("gigyaEmail", "mafEmail".equals(request.getParameter("mfa")));
        tfaMethods.put("gigyaTotp", "mafTfa".equals(request.getParameter("mfa")));

        dataFields.put("tfaMethods", tfaMethods);

        if(session.getAttribute("firstAdRegistration")!=null && "Y".equals((String)session.getAttribute("firstAdRegistration"))) {
            dataFields.put("userStatus", "regSubmit");
        }

        Map<String, Object> params = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            params.put("data", objectMapper.writeValueAsString(dataFields));
            params.put("UID", session.getAttribute("uid"));

        } catch (JsonProcessingException e) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "Failed to process profile data");
            return new RedirectView(request.getHeader("Referer"));
        }

        GSResponse response = gigyaService.executeRequest(param, "accounts.setAccountInfo", params);

        if (response.getErrorCode() == 0) {

            String approvalType = "adRegistration";

            cdcTraitService.getApprovalFlow(param, (String) session.getAttribute("uid"), approvalType, param, false);

            return new RedirectView("/registration/registerComplete"+ "?param=" + param);
        } else {
            redirectAttributes.addFlashAttribute("showErrorMsg", "MFA update failed");
            return new RedirectView("/new-channel/mfa" + "?param=" + param);
        }
    }

//    public ModelAndView signupComplete(String param, Map<String, Object> allParams, HttpServletRequest request,HttpSession session) {
//        ModelAndView modelAndView = new ModelAndView("registration");
//
//        String uid = (String) session.getAttribute("uid");
//        JsonNode result = cdcTraitService.getCdcUser(uid,0);
//        JsonNode data = result.path("data");
//        JsonNode channels = data.path("channels");
//        JsonNode sbaChannel = channels.get(param);
//        String content = "";
//
//        if (sbaChannel != null) {
//            // "sba" 채널의 데이터를 가져오기
//            String approvalStatus = sbaChannel.path("approvalStatus").asText();
//
//            if("approved".equals(approvalStatus)) {
//                content = "fragments/new/registrationComplete";
//            } else if("pending".equals(approvalStatus)) {
//                content = "fragments/registration/registrationInProgress";
//            } else {
//                content = "fragments/registration/registrationInProgress";
//            }
//        } else {
//            content = "fragments/registration/registrationInProgress";
//        }
//
//        modelAndView.addObject("content", content);
//        modelAndView.addObject("channel", param);
//        session.invalidate();
//        return modelAndView;
//    }

    public RedirectView ssoAccess(String targetChannel, String cdcUid, HttpSession session) {
        JsonNode CDCUserProfile = cdcTraitService.getCdcUser(cdcUid, 0);

        // Extract user profile information
        String country = CDCUserProfile.path("profile").path("country").asText(null);
        String loginUserId = CDCUserProfile.path("profile").path("email").asText(null);
        String bpId = CDCUserProfile.path("data").path("accountID").asText(null);
        String vendorcode = CDCUserProfile.path("data").path("vendorCode").asText(null);

        String salutation = CDCUserProfile.path("salutation").asText(null);
        String language = CDCUserProfile.path("profile").path("languages").asText(null);
        String firstName = CDCUserProfile.path("profile").path("firstName").asText(null);
        String lastName = CDCUserProfile.path("profile").path("lastName").asText(null);

        // Process phone numbers
        JsonNode phonesNode = CDCUserProfile.path("profile").path("phones");
        String workPhone = null;
        if (phonesNode.isArray()) {
            for (JsonNode phoneNode : phonesNode) {
                if ("work_phone".equals(phoneNode.path("type").asText())) {
                    workPhone = phoneNode.path("number").asText(null);
                    break;
                }
            }
        }

        // Process phone and country code
        String countryCode = "";
        String phoneNumber = "";
        if (workPhone != null) {
            String[] parts = workPhone.split(" ");
            for (String part : parts) {
                if (part.startsWith("+")) {
                    countryCode = part.replace("+", "").replaceAll("\\D", "");
                } else if (!part.isEmpty()) {
                    phoneNumber = part;
                }
            }
        }

        // Extract additional fields
        String secDept = CDCUserProfile.path("data").path("userDepartment").asText(null);
        String job_title = CDCUserProfile.path("data").path("jobtitle").asText(null);

        // Set session attributes for later use
        session.setAttribute("country", country);
        session.setAttribute("loginUserId", loginUserId);
        session.setAttribute("ssoAccessYn", "Y");

        // Get company information
        JsonNode companyNode = cdcTraitService.getB2bOrg(bpId);
        JsonNode infoNode = companyNode.path("info");

        // Populate companyObject with relevant data
        Map<String, String> companyObject = new HashMap<>();
        companyObject.put("bpid", companyNode.path("bpid").asText(""));
        companyObject.put("source", companyNode.path("source").asText(""));
        companyObject.put("type", companyNode.path("type").asText(""));
        companyObject.put("validStatus", companyNode.path("status").asText(""));
        companyObject.put("zip_code", infoNode.path("zip_code").asText(""));
        companyObject.put("vendorcode", companyNode.path("bpid").asText(""));

        // Populate registerCompany with data
        Map<String, String> registerCompany = new HashMap<>();
        registerCompany.put("name", companyNode.path("orgName").asText(""));
        String extractedCountry = infoNode.has("country") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("country")) : "";
        registerCompany.put("country", !extractedCountry.isEmpty() ? extractedCountry : country);
        registerCompany.put("state", infoNode.has("state") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("state")) : "");
        registerCompany.put("city", infoNode.has("city") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("city")) : "");
        registerCompany.put("street_address", infoNode.has("street_address") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("street_address")) : "");
        registerCompany.put("phonenumber1", infoNode.has("phonenumber1") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("phonenumber1")) : "");
        registerCompany.put("fax", infoNode.has("fax") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("fax")) : "");
        registerCompany.put("email", companyNode.path("email").asText(""));
        registerCompany.put("bizregno1", infoNode.has("bizregno1") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("bizregno1")) : "");
        registerCompany.put("representative", infoNode.has("representative") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("representative")) : "");

        // Set session attributes for the company and user
        session.setAttribute("registerCompany", registerCompany);
        session.setAttribute("companyObject", companyObject);
        session.setAttribute("cdc_uid", cdcUid);

        // Set account data for the user
        Map<String, String> accountObject = new HashMap<>();
        accountObject.put("salutation", salutation);
        accountObject.put("language", language);
        accountObject.put("firstName", firstName);
        accountObject.put("lastName", lastName);
        accountObject.put("country_code_work", countryCode);
        accountObject.put("work_phone", phoneNumber);
        accountObject.put("secDept", secDept);
        accountObject.put("job_title", job_title);
        session.setAttribute("accountObject", accountObject);

        // Redirect to the target channel
        String redirectUrl = "/registration/" + targetChannel;
        return new RedirectView(redirectUrl);
    }

    public ModelAndView newConsent(String uid, String channel,HttpSession session) {
        ObjectMapper objectMapper = new ObjectMapper();
        ModelAndView modelAndView = new ModelAndView("registration");
        String subsidiary;
        String languages;
        String marketingTermsYn="";
        String secCountry ="";

        //String regToken = (String) session.getAttribute("regToken");

//        Map<String, Object> cdcRequestParams = new HashMap<>();
//        cdcRequestParams.put("regToken", regToken);
//        cdcRequestParams.put("extraProfileFields", "languages, address, phones, education, educationLevel, honors, publications, patents, certifications, professionalHeadline, bio, industry, specialties, work, skills, religion, politicalView, interestedIn, relationshipStatus, hometown, favorites,followersCount, followingCount, username, name, locale, verified,timezone, likes,samlData");
//        cdcRequestParams.put("include", "identities-active , identities-all , identities-global , loginIDs , emails, profile, data, memberships, password, isLockedOut, lastLoginLocation, regSource, irank, rba, subscriptions, userInfo, preferences,groups,internal, customIdentifiers");

        Map<String, Object> cdcRequestParams = new HashMap<>();
        cdcRequestParams.put("UID", uid);

        GSResponse response = gigyaService.executeRequest(channel, "accounts.getAccountInfo", cdcRequestParams);

        try {
            JsonNode CDCUserProfile = cdcTraitService.getCdcUser(uid, 0);

            if (response.getErrorCode() == 0) {
                session.setAttribute("cdc_uid", uid);
                session.setAttribute("uid", uid);
            }

            subsidiary = CDCUserProfile.path("data").path("subsidiary").asText("");
            //languages = CDCUserProfile.path("profile").path("languages").asText("");
            secCountry = CDCUserProfile.path("profile").path("country").asText("");

            languages = (String) session.getAttribute("newConsentLanguage");

            Map<String, Object> returnArray = cdcTraitService.consentSelector((String) session.getAttribute("uid"), channel, languages, subsidiary);

            List<Channels> channelTables = channelRepository.selectByChannelNameContaining(channel);
            Channels channelTable = channelTables.isEmpty() ? null : channelTables.get(0);
            String channelDisplayName = channelTable != null ? channelTable.getChannelDisplayName() : "";

            String content = "fragments/newconsent/newConsent";
            modelAndView.addObject("content", content);
            modelAndView.addObject("secCountry", secCountry);
            modelAndView.addObject("marketingCommon", returnArray.get("marketingCommon"));
            modelAndView.addObject("marketingCommonText", returnArray.get("marketingCommonText"));
            modelAndView.addObject("marketingConsentid", returnArray.get("marketingConsentid"));
            modelAndView.addObject("marketingcontentid", returnArray.get("marketingcontentid"));
            modelAndView.addObject("secCountry", returnArray.get("secCountry"));
            modelAndView.addObject("privacyCommon", returnArray.get("privacyCommon"));
            modelAndView.addObject("privacyCommonText", returnArray.get("privacyCommonText"));
            modelAndView.addObject("commonPrivacyConsentId", returnArray.get("commonPrivacyConsentId"));
            modelAndView.addObject("commonPrivacyContentId", returnArray.get("commonPrivacyContentId"));
            modelAndView.addObject("privacyChannel", returnArray.get("privacyChannel"));
            modelAndView.addObject("privacyChannelText", returnArray.get("privacyChannelText"));
            modelAndView.addObject("channelPrivacyConsentId", returnArray.get("channelPrivacyConsentId"));
            modelAndView.addObject("channelPrivacyContentId", returnArray.get("channelPrivacyContentId"));
            modelAndView.addObject("termsCommon", returnArray.get("termsCommon"));
            modelAndView.addObject("termsCommonText", returnArray.get("termsCommonText"));
            modelAndView.addObject("commonTermConsentId", returnArray.get("commonTermConsentId"));
            modelAndView.addObject("commonTermContentId", returnArray.get("commonTermContentId"));
            modelAndView.addObject("termsChannel", returnArray.get("termsChannel"));
            modelAndView.addObject("termsChannelText", returnArray.get("termsChannelText"));
            modelAndView.addObject("channelTermsConsentId", returnArray.get("channelTermsConsentId"));
            modelAndView.addObject("channelTermsContentId", returnArray.get("channelTermsContentId"));
            modelAndView.addObject("channelDisplayName", channelDisplayName);
            modelAndView.addObject("marketingTermsYn", marketingTermsYn);
            modelAndView.addObject("channel", channel);

            String newConsentMktYn="N";

            if(session.getAttribute("newConsentMktYn")!=null) {
                newConsentMktYn = (String) session.getAttribute("newConsentMktYn");
            }

            modelAndView.addObject("newConsentMktYn", newConsentMktYn);

            List<String> exceptionCountry = Arrays.asList(
                    "TR", "BR", "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE",
                    "FI", "FR", "DE", "GR", "HU", "IE", "IT", "LV", "LT", "LU",
                    "MT", "NL", "PL", "PT", "RO", "SK", "SI", "ES", "SE"
            );
            boolean isNoneAgreeBtn = exceptionCountry.contains(secCountry);

            modelAndView.addObject("isNoneAgreeBtn", isNoneAgreeBtn);

            if ("CN".equals(secCountry)) {
                // Privacy channel texts for CN
                String privacyChannel1Text = "<p class=\"slds-m-bottom_xx-medium\" data-aura-rendered-by=\"25:224;a\">三星电子充分认识到个人信息对您的重要性，并将尽最大努力确保您个人信息的安全性和可靠性。" +
                        "在使用三星B2B Partner Portal (https://partnerhub.samsung.com)之前，请仔细阅读并同意我们的条款与条件，个人信息保护政策，及如下详细内容。</p><br data-aura-rendered-by=\"27:224;a\"><p class=\"slds-m-bottom_xx-medium\" data-aura-rendered-by=\"28:224;a\"><strong data-aura-rendered-by=\"29:224;a\">[基本业务功能]</strong></p><p class=\"slds-m-bottom_xx-medium\" data-aura-rendered-by=\"31:224;a\">如果您不同意收集和使用基本业务功能所需的个人信息，B2B Partner Portal将无法正常运行，我们将无法为您服务。" +
                        "如果您希望撤回对使用产品（或）服务过程中收集和使用个人信息的授权，您可访问“联系我们”菜单（注册VOC）。</p><br data-aura-rendered-by=\"33:224;a\"><div class=\"slds-scrollable slds-box\" data-aura-rendered-by=\"34:224;a\"><div dir=\"ltr\" class=\"slds-text-longform uiOutputRichText\" data-aura-rendered-by=\"37:224;a\" data-aura-class=\"uiOutputRichText\"><table data-aura-rendered-by=\"38:224;a\"><tbody><tr><td rowspan=\"1\" colspan=\"1\"><b>□ 收集和使用一般个人信息 (基本业务功能)</b></td></tr><tr><td rowspan=\"1\" colspan=\"1\">[必要]</td></tr></tbody></table>" +
                        "<table data-aura-rendered-by=\"38:224;a\"></table><table style=\"font-size: 10pt; text-align: center;\" data-aura-rendered-by=\"38:224;a\"><tbody><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>个人信息种类</b></td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>目的</b></td><td style=\"border: 1px solid gray;\" rowspan=\"8\" colspan=\"1\">严格按照个人信<br>息保护政策处理</td><td style=\"border: 1px solid gray;\" rowspan=\"6\" colspan=\"1\">数据将会删除：<br>1)当用户要求删除时<br>2) 离最后登录时间过1年时</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">姓名</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">识别用户姓名</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">用户名</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">用户登录系统的ID（邮箱形式)</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">公司、公司地址</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">确认公司名信息，识别是否为合作伙伴</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">事业部、部门、职位</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">所属及职位确认（用于商业联系）</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">办公电话、传真、手机</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">确认商业联系方式</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">国家</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">确认国家信息</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">邮箱</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">找回用户密码</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">用户要求时删除数据</td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">IP、经纬度信息（由IP推断）</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">SaaS系统中的基本登录历史记录</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">6个月后删除数据</td></tr></tbody></table></div></div>";

                String privacyChannel2Text = "<div dir=\"ltr\" data-aura-rendered-by=\"46:224;a\" class=\"uiOutputRichText\" data-aura-class=\"uiOutputRichText\"><table data-aura-rendered-by=\"47:224;a\"><tbody><tr><td rowspan=\"1\" colspan=\"1\"><b>□ 跨境传输 (基本业务功能)</b></td></tr><tr><td rowspan=\"1\" colspan=\"1\">我们仅在征得您的同意后向第三方国家或国际组织传输您的个人信息、敏感个人信息或生物识别信息，详情如下表所示。<br>在您同意之前，请认真考虑接收者的数据安全能力及其个人信息保护政策</td></tr><tr><td rowspan=\"1\" colspan=\"1\">[必要]</td></tr></tbody></table>" +
                        "<table style=\"font-size: 10pt; text-align: center;\" width=\"1000px\" data-aura-rendered-by=\"47:224;a\"><tbody><tr><td width=\"150px\" style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>个人信息种类</b></td><td width=\"100px\" style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>接收者<br>身份</b></td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>接收者<br>联系信息</b></td><td width=\"100px\" style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>接收者<br>国家</b></td><td width=\"100px\" style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>接收者<br>处理方式</b></td><td width=\"200px\" style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>目的</b></td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><b>接收者的个人<br>信息保护政策<br>(链接)</b></td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">姓名、邮箱、用户名、IP、<br>经纬度信息（由IP推断）、<br>部门、职位名称、公司、<br>事业部、电话、传真、<br>手机、地址、国家" +
                        "</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">Samsung Electronics Co., Ltd.</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><a rel=\"noopener\" target=\"_blank\" href=\"https://partnerhub.samsung.com/s/communitylandingmain?p_subsidiary=SCIC&language=zh_CN&country=CHINA&continent=Asia%20-%20pacific\">https://partnerhub.<br>samsung.com/s/<br>communitylandingmain?<br>p_subsidiary=SCIC&amp<br>language=zh_CN&amp<br>country=CHINA&amp<br>continent=Asia<br>%20-%20pacific</a></td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">韩国</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">所有数据都通过HTTPS/TLS进行加密传输和存储。并严格按照本服务个人信息保护政策进行处理</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">为认证B2B营业支援系统的用户认证而收集。本系统是基于Salesforce 的SaaS解决方案的系统，为使用解决方案提供者的服务器，所有数据都会保存到Salesforce在日本 服务器里</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><a rel=\"noopener\" target=\"_blank\" href=\"https://partnerhub.samsung.com/s/communitylandingmain?p_subsidiary=SEC&language=en_US&country=SOUTH%20KOREA&continent=Asia%20-%20pacific\">https://partnerhub.<br>samsung.com/s/<br>communitylandingmain?<br>p_subsidiary=SEC&amp;<br>language=en_US&amp;<br>country=SOUTH%20KOREA&amp;<br>continent=Asia<br>%20-%20pacific</a></td></tr><tr><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">姓名、国家、职位、邮箱</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">Samsung Electronics Co., Ltd.</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><a rel=\"noopener\" target=\"_blank\" href=\"http://itvoc.sec.samsung.net/\">http://itvoc.sec.<br>samsung.net/</a></td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">韩国</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">所有数据都通过合理可行的措施进行传输和存储。并严格按照本服务个人信息保护政策进行处理</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\">为了通过关联登录（SSO登录）提供产品培训，该信息会传输到SBA(Samsung Business Academy)系统, 会存储在该服务的系统解决方案提供者（Cornerstone Ondemand)在英国 服务器里</td><td style=\"border: 1px solid gray;\" rowspan=\"1\" colspan=\"1\"><a rel=\"noopener\" target=\"_blank\" href=\"https://www.samsungcpfr.com/sba/privacypolicy_cn.html\">https://www.samsung<br>cpfr.com/sba/<br>privacypolicy_cn.html</a></td></tr></tbody></table></div>";

                String etcCommonText = "该服务适用于年满14岁及以上的用户.";

                modelAndView.addObject("content", content);
                modelAndView.addObject("privacyChannel1", "privacy1." + channel + ".cn");
                modelAndView.addObject("privacyChannel1Text", privacyChannel1Text);
                modelAndView.addObject("channelPrivacy1ConsentId", "99999");
                modelAndView.addObject("channelPrivacy1ContentId", "1");
                modelAndView.addObject("privacyChannel2", "privacy2." + channel + ".cn");
                modelAndView.addObject("privacyChannel2Text", privacyChannel2Text);
                modelAndView.addObject("channelPrivacy2ConsentId", "99999");
                modelAndView.addObject("channelPrivacy2ContentId", "2");
                modelAndView.addObject("etcCommon", "etc" + channel + ".cn");
                modelAndView.addObject("etcCommonText", etcCommonText);
                modelAndView.addObject("etcCommonConsentId", "99999");
                modelAndView.addObject("etcCommonContentId", "3");
            }
        } catch (Exception e) {
            log.error("Error processing newConsent response", e);
        }

        return modelAndView;
    }

    public RedirectView newConsentSubmit(Map<String, String> requestParams, String param, HttpSession session, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        String uid = (String) session.getAttribute("uid");
        if (uid == null) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "session expired, please restart registration process");
            return new RedirectView(request.getHeader("Referer"));
        }


        String secCountry = (String) session.getAttribute("secCountry");
        String marketingTermsYn = "";
        Map<String, Object> params = new HashMap<>();
        params.put("uid", uid);
        params.put("termsCommon", requestParams.get("termsCommon"));
        params.put("termsChannel", requestParams.get("termsChannel"));
        params.put("privacyCommon", requestParams.get("privacyCommon"));
        params.put("privacyChannel", requestParams.get("privacyChannel"));

        String mktYn = (String) session.getAttribute("newConsentMktYn");

        /*if (!"FR".equals(secCountry)) {
            params.put("marketingCommon", requestParams.get("marketingCommon"));
        }*/
        if (requestParams.containsKey("marketingCommon") && requestParams.get("marketingCommon") != null && "Y".equals(mktYn)) {
            params.put("marketingCommon", requestParams.get("marketingCommon"));
        }
        if ("CN".equals(secCountry) && "partnerhub".equals(param)) {
            params.put("privacyChannel1", requestParams.get("privacyChannel1"));
            params.put("privacyChannel2", requestParams.get("privacyChannel2"));
            params.put("etcCommon", requestParams.get("etcCommon"));
        }

        try {
            session.setAttribute("consentProfile", EncryptUtil.encryptData(new ObjectMapper().writeValueAsString(params)));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            redirectAttributes.addFlashAttribute("showErrorMsg", "Error processing consent data");
            return new RedirectView(request.getHeader("Referer"));
        }

        Map<String, Object> preferencesFields = new HashMap<>();
        preferencesFields.put(requestParams.get("termsCommon"), Map.of("isConsentGranted", true));
        preferencesFields.put(requestParams.get("termsChannel"), Map.of("isConsentGranted", true));
        preferencesFields.put(requestParams.get("privacyCommon"), Map.of("isConsentGranted", true));
        preferencesFields.put(requestParams.get("privacyChannel"), Map.of("isConsentGranted", true));

        if (requestParams.containsKey("marketingCommon") && "Y".equals(mktYn)) {
            String[] marketingSelection = requestParams.get("marketingCommon").split(":");
            boolean marketingTerms = "1".equals(marketingSelection[0]);
            preferencesFields.put(marketingSelection[1], Map.of("isConsentGranted", marketingTerms));
            if (marketingTerms) {
                marketingTermsYn = "Y";
            } else {
                marketingTermsYn = "N";
            }
            session.setAttribute("marketingTermsYn",marketingTermsYn);
        }

        Map<String, Object> gigyaParams = new HashMap<>();
        try {
            gigyaParams.put("preferences", new ObjectMapper().writeValueAsString(preferencesFields));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            redirectAttributes.addFlashAttribute("showErrorMsg", "Error processing preferences data");
            return new RedirectView(request.getHeader("Referer"));
        }
        gigyaParams.put("UID", uid);

        GSResponse response = gigyaService.executeRequest(param, "accounts.setAccountInfo", gigyaParams);
        if (response.getErrorCode() == 0) {
            if (requestParams.containsKey("marketingCommon")) {
                boolean marketingTerms = "1".equals(requestParams.get("marketingCommon").split(":")[0]);
                if (marketingTerms) {
                    consentResponseUpdated(Long.valueOf(requestParams.get("marketingConsentid")), Long.valueOf(requestParams.get("marketingcontentid")), uid);
                } else {
                    saveRejectedConsent(Long.valueOf(requestParams.get("marketingConsentid")), Long.valueOf(requestParams.get("marketingcontentid")), uid);
                }
            }

            consentResponseUpdated(Long.valueOf(requestParams.get("commonTermConsentId")), Long.valueOf(requestParams.get("commonTermContentId")), uid);
            consentResponseUpdated(Long.valueOf(requestParams.get("channelTermsConsentId")), Long.valueOf(requestParams.get("channelTermsContentId")), uid);
            consentResponseUpdated(Long.valueOf(requestParams.get("commonPrivacyConsentId")), Long.valueOf(requestParams.get("commonPrivacyContentId")), uid);
            consentResponseUpdated(Long.valueOf(requestParams.get("channelPrivacyConsentId")), Long.valueOf(requestParams.get("channelPrivacyContentId")), uid);

            return new RedirectView("/new-consent/newConsentComplete"+ "?param=" + param);
        } else {
            redirectAttributes.addFlashAttribute("showErrorMsg", "Consent update failed");
            return new RedirectView(request.getHeader("Referer"));
        }
    }

    public String consentVersionCheck(Map<String, String> payload, HttpSession session, RedirectAttributes redirectAttributes) {

        String uid = payload.get("uid"); // uid는 payload에서 가져옴
        String channel = payload.get("channel");

        JsonNode CDCUserProfile = cdcTraitService.getCdcUser(uid, 0);

        String subsidiary = CDCUserProfile.path("data").path("subsidiary").asText("");

        // SQL 쿼리를 StringBuilder를 사용하여 작성
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("WITH RankedConsents AS ( ");
        queryBuilder.append("    SELECT ");
        queryBuilder.append("        uac.id, ");
        queryBuilder.append("        uac.consent_id, ");
        queryBuilder.append("        uac.consent_content_id, ");
        queryBuilder.append("        uac.uid, ");
        queryBuilder.append("        uac.status, ");
        queryBuilder.append("        c.cdc_consent_id, ");
        queryBuilder.append("        c.coverage, ");
        queryBuilder.append("        c.type_id, ");
        queryBuilder.append("        c.subsidiary, ");
        queryBuilder.append("        cc.version, ");
        queryBuilder.append("        cc.status_id, ");
        queryBuilder.append("        cc.language_id, ");
        queryBuilder.append("        ROW_NUMBER() OVER (PARTITION BY c.coverage, c.type_id ORDER BY uac.id DESC) AS rn ");
        queryBuilder.append("    FROM user_agreed_consents uac ");
        queryBuilder.append("    JOIN consents c ON uac.consent_id = c.id ");
        queryBuilder.append("    JOIN consent_contents cc ON cc.id = uac.consent_content_id ");
        queryBuilder.append("    WHERE uac.uid = :uid ");  // Named Parameter 사용
        queryBuilder.append("      AND c.coverage IN ('common', :channel) ");  // c.coverage 필터 추가
        queryBuilder.append(") ");
        queryBuilder.append("SELECT ");
        queryBuilder.append("    id, ");
        queryBuilder.append("    consent_id, ");
        queryBuilder.append("    consent_content_id, ");
        queryBuilder.append("    uid, ");
        queryBuilder.append("    status, ");
        queryBuilder.append("    cdc_consent_id, ");
        queryBuilder.append("    coverage, ");
        queryBuilder.append("    type_id, ");
        queryBuilder.append("    subsidiary, ");
        queryBuilder.append("    version, ");
        queryBuilder.append("    status_id,");
        queryBuilder.append("    language_id ");
        queryBuilder.append("FROM RankedConsents ");
        queryBuilder.append("WHERE rn = 1 ");
        queryBuilder.append("ORDER BY id DESC;");

        // Native Query 생성
        Query query = entityManager.createNativeQuery(queryBuilder.toString());
        query.setParameter("channel", channel); // channel 파라미터 추가

        // uid 파라미터 설정
        query.setParameter("uid", uid);

        // 쿼리 결과를 리스트로 가져오기
        List<Object[]> userConsentAgreedList = query.getResultList();
        List<Map<String, Object>> userConsentAgreedData = new ArrayList<>();

        // 쿼리 결과 매핑
        for (Object[] row : userConsentAgreedList) {
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("id", row[0]); // T.id
            rowData.put("consentId", row[1]); // T.consent_id
            rowData.put("consentContentId", row[2].toString()); // T.consent_content_id
            rowData.put("uid", row[3]); // T.uid
            rowData.put("status", row[4]); // T.status
            rowData.put("cdcConsentId", row[5].toString()); // c.cdc_consent_id
            rowData.put("coverage", row[6]); // c.coverage
            rowData.put("type", row[7]); // c.coverage
            rowData.put("subsidiary", row[8]); // c.subsidiary
            rowData.put("version", row[9]); // cc.version
            rowData.put("statusId", row[10]); // cc.status_id
            rowData.put("languageId", row[11]); // cc.status_id
            userConsentAgreedData.add(rowData);
        }

        // 비교 로직 구현
        boolean isMkt = userConsentAgreedData.stream()
                .filter(consent -> "b2b".equals(consent.get("type")))
                .map(consent -> consent.get("status").toString())
                .anyMatch(status -> "agreed".equals(status)); // 'agreed'가 있으면 true 반환 // 초기값을 false로 설정

        if(isMkt) {
            session.setAttribute("newConsentMktYn","Y");
        } else {
            session.setAttribute("newConsentMktYn","N");
        }

        boolean isValid = false;
        isValid = userConsentAgreedData.stream()
                .filter(userConsent -> !"b2b.common.all".equals(userConsent.get("cdcConsentId"))) // "b2b.common.all" 제외
                .anyMatch(userConsent -> "historic".equals(userConsent.get("statusId"))); // "historic" 상태 체크

                       userConsentAgreedData.stream()
                .filter(consent -> channel.equals(consent.get("coverage")) && "privacy".equals(consent.get("type")))
                .findFirst()
                .ifPresentOrElse(
                        consent -> session.setAttribute("newConsentLanguage", consent.get("languageId")),
                        () -> session.setAttribute("newConsentLanguage", "en") // 값이 없을 경우 기본값으로 "en" 설정
                );

        // 최종 결과 반환
        return isValid ? "Y" : "N";

    }
}
