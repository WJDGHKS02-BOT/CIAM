package com.samsung.ciam.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSKeyNotFoundException;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.cpi.enums.CpiResponseFieldMapping;
import com.samsung.ciam.common.cpi.service.CpiApiService;
import com.samsung.ciam.common.gigya.service.GigyaService;
import com.samsung.ciam.models.*;
import com.samsung.ciam.repositories.*;
import com.samsung.ciam.utils.BeansUtil;
import com.samsung.ciam.utils.EncryptUtil;
import com.samsung.ciam.utils.StringUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;



@Slf4j
@Service
public class RegistrationService {

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private SecServingCountryRepository secServingCountryRepository;

    @Autowired
    private EmailDomainsRepository emailDomainsRepository;

    @Autowired
    private CisCountryRepository cisCountryRepository;

    @Autowired
    private SfdcProductRepository sfdcProductRepository;

    @Autowired
    private NewCompanyRepository newCompanyRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private SubsidiaryRepository subsidiaryRepository;

    @Autowired
    private ChannelConversionRepository channelConversionRepository;

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private UserAgreedConsentsRepository userAgreedConsentsRepository;

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ChannelAddFieldRepository channelAddFieldRepository;

    @Autowired
    private ChannelInvitationRepository channelInvitationRepository;

    @Autowired
    private BtpAccountsRepository btpAccountsRepository;

    @Autowired
    private CpiApiService cpiApiService;

    @Autowired
    private WfMasterRepository wfMasterRepository;

    @Autowired
    private WfIdGeneratorService wfIdGeneratorService;

    @Autowired
    private ChannelAdCheckRepository channelAdCheckRepository;

    public ModelAndView signup(String key, Map<String, Object> allParams, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String previousUrl = request.getHeader("Referer");
        /*if (previousUrl != null && previousUrl.contains("registration")) {
            List<Channels> channelConfig = channelRepository.selectChannelName(key);
            if (!channelConfig.isEmpty()) {
                String config = channelConfig.get(0).getConfig();
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode configJson = objectMapper.readTree(config);
                    if (configJson.has("registration") && configJson.get("registration").asBoolean(false) == false) {
                        return new ModelAndView("error/403", Map.of("message", "Self Registration is not allowed for " + key));
                    }
                } catch (IOException e) {
                    return new ModelAndView("error/403", Map.of("message", "Error parsing channel configuration for " + key));
                }
            }
        }*/
        String channelType = List.of("sba", "mmp", "e2e", "ets", "edo").contains(key) ? "CUSTOMER" : "VENDOR";
        String channel = key;

        HttpSession session = request.getSession();

        //파라미터로 subsidiary 경우 저장
        if(allParams.getOrDefault("subsidiary","")!="") {
            session.setAttribute("secSubsidiary", allParams.getOrDefault("subsidiary", ""));
        }
        // tcpp_language가 미리 설정되지 않았을 때만 설정
        if (session.getAttribute("tcpp_language") == null) {
            session.setAttribute("tcpp_language", allParams.getOrDefault("language", "en"));
        }
        session.setAttribute("channelCompanyType",channelType);


        boolean isClimateSolutionCountry = "CLIMATE SOLUTIONS".equals(allParams.get("country")) || "SEACE".equals(allParams.get("p_subsidiary"));

        List<SecServingCountry> secCountries;
        List<SecServingCountry> tempCountries;
        List<String> climateSolutionCountries = new ArrayList<String>();
        if (isClimateSolutionCountry) {
            climateSolutionCountries = List.of("NL", "AL", "AT", "BE", "BA", "BG", "CZ", "HR", "CW", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IE", "IT", "XK", "LV", "LT", "LU", "MK", "MT", "ME", "NO", "PL", "PT", "RO", "RS", "SI", "SK", "ES", "SE", "CH", "GB");
            session.setAttribute("climateSolutionCountries", climateSolutionCountries);
            secCountries = secServingCountryRepository.selectCountryCodeIn("%" + channel + "%", climateSolutionCountries);
        } else {
            secCountries = secServingCountryRepository.selectCounrtyLikeList("%" + channel + "%");
        }

        String sfdcPresetCountry = "";
        if (allParams.containsKey("country")) {

            String countryParam = (String) allParams.get("country");
            // secCountries에서 getCountry() 값이 countryParam과 일치하는 것만 필터링하여 다시 secCountries에 할당
            tempCountries = secCountries.stream()
                    .filter(country -> countryParam.equalsIgnoreCase(country.getCountry()))
                    .collect(Collectors.toList());

            if (!tempCountries.isEmpty()) {
                // secCountries가 비어 있지 않은 경우에만 get(0)을 호출
                String countryId = secServingCountryRepository.selectCountrySsoCode(channel, tempCountries.get(0).getCountryCode());
                modelAndView.addObject("country", countryId);

                List<SecServingCountry> sfdcPreset = secServingCountryRepository.selectCountryList("%" + channel + "%", (String) allParams.get("country"));

                if (!sfdcPreset.isEmpty()) {
                    sfdcPresetCountry = sfdcPreset.get(0).getCountryCode();
                    session.setAttribute("sfdcPresetCountry", sfdcPresetCountry);
                }
            }
        }

        if (session.getAttribute("cmdmCountryCode") != null) {
            sfdcPresetCountry = (String) session.getAttribute("cmdmCountryCode");
        }

        if (allParams.containsKey("language")) {
            String presetLang = (String) allParams.get("language");
            presetLang = presetLang.length() >= 2 ? presetLang.substring(0, 2).toLowerCase() : "en";
            session.setAttribute("sfdcPresetLanguage", presetLang);
        }

        String subsidiary = (String) session.getAttribute("p_subsidiary");

        String userEmail = "";
        String userLoginId = "";
        String convertLoginId = "";

        if (session.getAttribute("convertLoginId") != null) {
            convertLoginId = (String) session.getAttribute("convertLoginId");
            userEmail = convertLoginId;
            userLoginId = convertLoginId;
            sfdcPresetCountry = (String) session.getAttribute("cmdmCountryCode");
        }

        userEmail = session.getAttribute("userEmail") != null ? (String) session.getAttribute("userEmail") : userEmail;

        List<EmailDomains> domains = emailDomainsRepository.selectBannedDomainsList(key);

        List<String> rejecteDomains = domains.stream()
                .map(EmailDomains::getDomain)  // 각 EmailDomains 객체에서 getDomains() 호출
                .collect(Collectors.toList());  // 결과를 List<String>으로 수집

        List<Channels> channelObj = channelRepository.selectChannelName(key);
        if (!channelObj.isEmpty() && channelObj.get(0).getConfig().contains("\"limit_business_location_by_country\"")) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode configJson = objectMapper.readTree(channelObj.get(0).getConfig());
                String[] allowedCountries = configJson.get("limit_business_location_by_country").asText().split(",");
                secCountries.removeIf(country -> !List.of(allowedCountries).contains(country.getCountryCode()));
            } catch (IOException e) {
                return new ModelAndView("error/403", Map.of("message", "Error parsing channel configuration for " + key));
            }
        }

        if (!channelObj.isEmpty() && channelObj.get(0).getConfig().contains("\"limit_business_location_by_subsidiary\"")) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode configJson = objectMapper.readTree(channelObj.get(0).getConfig());
                String[] allowedCountries = configJson.get("limit_business_location_by_subsidiary").asText().split(",");
                secCountries.removeIf(country -> !List.of(allowedCountries).contains(country.getSubsidiary()));
            } catch (IOException e) {
                return new ModelAndView("error/403", Map.of("message", "Error parsing channel configuration for " + key));
            }
        }

        String channelAdCheckYn = Optional.ofNullable(channelAdCheckRepository.selectChannelAdCheckYn(key))
                .orElse("N");

        String content = "fragments/registration/registrationContent";

//        String popupAdCheck = "popups/adCheck";
//        modelAndView.addObject("adCheckModal",popupAdCheck);

        // 데이터를 모델에 추가하고 뷰를 반환합니다.

        modelAndView.addObject("convertLoginId", convertLoginId);
        modelAndView.addObject("userEmail", userEmail);
        modelAndView.addObject("userLoginId", userLoginId);
        modelAndView.addObject("secCountries", secCountries);
        modelAndView.addObject("sfdcPresetCountry", sfdcPresetCountry);
        modelAndView.addObject("subsidiary", subsidiary);
        modelAndView.addObject("channel", key);
        modelAndView.addObject("ssoAccessYn", (String) session.getAttribute("ssoAccessYn"));
        modelAndView.addObject("convertYn", (String) session.getAttribute("convertYn"));
        modelAndView.addObject("content", content);
        modelAndView.addObject("rejecteDomains", rejecteDomains);
        modelAndView.addObject("channelAdCheckYn", channelAdCheckYn);
        modelAndView.addObject("apiKey",BeansUtil.getApiKeyForChannel(key));

        if(session.getAttribute("loginUserId")!=null) {
            modelAndView.addObject("loginUserId", session.getAttribute("loginUserId"));
        }
        if(session.getAttribute("country")!=null) {
            String countryId = secServingCountryRepository.selectCountrySsoCode(channel,(String) session.getAttribute("country"));
            modelAndView.addObject("country", countryId);
        }
        if(session.getAttribute("convertYn")!=null && "Y".equals(session.getAttribute("convertYn"))) {
            Map<String, String> convertData = (Map<String, String>) session.getAttribute("convertData");
            if (convertData != null) {
                modelAndView.addObject("loginUserId", convertData.getOrDefault("loginID",""));
                String countryId = secServingCountryRepository.selectCountrySsoCode(channel,convertData.getOrDefault("countryCode",""));
                modelAndView.addObject("country", countryId);
                // 추가적인 로직 수행
            } else {
                // convertData가 null인 경우 처리
            }

        }

        // allParams의 값을 Map<String, String>에 저장
        Map<String, String> paramsMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : allParams.entrySet()) {
            if (entry.getValue() != null) {
                paramsMap.put(entry.getKey(), entry.getValue().toString());
            }
        }
        // 생성된 paramsMap을 세션에 저장
        session.setAttribute("paramsMap", paramsMap);

        return modelAndView;
    }

    public Map<String, Object> checkPwd(Map<String, String> payload) {
        Map<String, Object> registerMapper = new HashMap<>();
        Map<String, Object> jsonResponse = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        registerMapper.put("username", "testreg");
        registerMapper.put("password", payload.get("pwd"));
        try {
            GSResponse response = gigyaService.executeRequest("default", "accounts.register", registerMapper);
            JsonNode responseData = objectMapper.readTree(response.getResponseText());
            if (responseData.has("validationErrors")) {
                for (JsonNode emailError : responseData.get("validationErrors")) {
                    if ("password".equals(emailError.path("fieldName").asText())) {
                        String formattedErrorMsg = "Password must contain at least 8-15 characters and 3 of the following: An uppercase letter, a lowercase letter, a number, a special symbol";
                        jsonResponse.put("error", formattedErrorMsg);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing registration data", e);
            jsonResponse.put("error", "Exception occurred: " + e.getMessage());
        }

        return jsonResponse;
    }

    public Map<String, Object> checkUserId(Map<String, String> payload) {
        String loginUserId = payload.get("loginUserId");
        //String channel = payload.get("channel");
        Map<String, Object> jsonResponse = new HashMap<>();

        if (!StringUtil.isValidEmail(loginUserId)) {
            jsonResponse.put("errorCode", 1);
            jsonResponse.put("status", "email format error");
            jsonResponse.put("message", "login ID must be using email format");

            return jsonResponse;
        }

        try {
            GSResponse response = gigyaService.executeRequestSetParam("accounts.isAvailableLoginID", "loginID", loginUserId);

            if (response.getErrorCode() == 0) {
                if (response.getData().getBool("isAvailable")) {
                    jsonResponse.put("errorCode", 0);
                    jsonResponse.put("status", "available");
                } else {
                    jsonResponse.put("errorCode", 1);
                    jsonResponse.put("status", "unavailable");
                    jsonResponse.put("message", "NOT Available");
                }
            } else {
                jsonResponse.put("errorCode", response.getErrorCode());
                jsonResponse.put("errorMessage", response.getErrorMessage());
            }
        } catch (Exception e) {
            jsonResponse.put("errorCode", -1); // Custom error code for exception
            jsonResponse.put("errorMessage", "Exception occurred: " + e.getMessage());
        }

        return jsonResponse;
    }

    public RedirectView processSignup(String param, Map<String, String> payload, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Map<String, String> errors = new HashMap<>();
        String url = "";
        validateInputs(payload, errors,param);
        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("showErrorMsg", errors.get("showErrorMsg"));
            return new RedirectView( request.getHeader("Referer"));
        }

        handleUserAffiliation(payload, session, request, errors);

        session.setAttribute("loginUserId",payload.get("loginUserId"));
        session.setAttribute("country",payload.get("country"));

        /*if (session.getAttribute("channelUID") != null) {
            String convertLoginId = payload.get("loginUserId");
            ChannelConversion convertDup = channelConversionRepository.selectFirstConversion(param, convertLoginId);

            if (convertDup == null) {
                ChannelConversion newConversion = new ChannelConversion();
                newConversion.setChannel(param);
                newConversion.setConvertLoginId(convertLoginId);
                newConversion.setCiamUser(true);
                newConversion.setConvertUid((String) session.getAttribute("channelUID"));
                channelConversionRepository.save(newConversion);
            }
        }*/

        Map<String, Object> checkUserIdResult  = checkUserId(payload);
        if (isUserIdAvailable(checkUserIdResult)) {
            setupSessionAttributesBasedOnCountry(payload.get("country"), session);
            url = performRegistration(param, payload, session, redirectAttributes);
            return new RedirectView(url + "?param=" + param);
        } else {
            redirectAttributes.addFlashAttribute("showErrorMsg", "loginId NOT Available");
            url = request.getHeader("Referer");
            return new RedirectView(url + "?param=" + param);
        }


    }

    public void validateInputs(Map<String, String> payload, Map<String, String> errors,String param) {
        String email = payload.get("email");
        String loginId = payload.get("loginUserId");

        validateEmail(loginId, errors,param);
        validateLoginId(loginId, errors);
        validateCountry(payload.get("country"), errors);
    }

    public void handleUserAffiliation(Map<String, String> payload, HttpSession session, HttpServletRequest request, Map<String, String> errors) {
        String email = payload.get("email");
        String loginId = payload.get("loginUserId");

        /*if (loginId.toLowerCase().contains("@samsung")) {
            errors.put("isSamsungEmployee", "Invalid email format or banned domain.");
            session.setAttribute("isSamsungEmployee", true);
            return;
        }*/

        checkEmailFormatAndDomain(loginId, session, errors);
    }

    private void validateEmail(String email, Map<String, String> errors, String channelName) {
        // 이메일 유효성 검사
        if (email == null || email.isEmpty() || !isValidEmail(email)) {
            errors.put("showErrorMsg", "The email is required and must be a valid email address.");
            return;  // 이메일이 유효하지 않으면 바로 종료
        }

        // 금지된 도메인 목록을 가져오기
        List<EmailDomains> domains = emailDomainsRepository.selectBannedDomainsList(channelName);

        // 도메인 목록이 비어 있지 않을 경우 처리
        if (domains != null && !domains.isEmpty()) {
            List<String> rejecteDomains = domains.stream()
                    .map(EmailDomains::getDomain)
                    .collect(Collectors.toList());

            // 이메일 도메인 추출
            String domainPart = email.substring(email.indexOf('@'));

            // 금지된 도메인과 비교
            for (String rejectDomain : rejecteDomains) {
                // 와일드카드 도메인 (@*.samsung.com) 거부
                if (rejectDomain.equals("@*.samsung.com")) {
                    // @*.samsung.com 형태에 해당하는 도메인들은 거부
                    if (domainPart.matches("@[a-zA-Z0-9.-]+\\.samsung\\.com$")) {
                        errors.put("showErrorMsg", "Emails from @samsung.com or @partner.samsung.com are not allowed.");
                        return;
                    }
                }
                // @samsung.com과 같은 정확한 도메인 거부
                if (domainPart.equals(rejectDomain)) {
                    errors.put("showErrorMsg", "Emails from @samsung.com or @partner.samsung.com are not allowed.");
                    return;
                }
            }
        }
    }

    private void validateLoginId(String loginId, Map<String, String> errors) {
        if (loginId == null || loginId.isEmpty()) {
            errors.put("showErrorMsg", "The login ID is required.");
        } else if (loginId.length() > 255) {
            errors.put("showErrorMsg", "The login ID must not exceed 255 characters.");
        }
    }

    private void validateCountry(String country, Map<String, String> errors) {
        if (country == null || country.isEmpty()) {
            errors.put("country", "Please select the SEC serving country.");
        }
    }

    private void checkEmailFormatAndDomain(String email, HttpSession session, Map<String, String> errors) {
        String[] parts = email.split("@");
        if (parts.length < 2 || isDomainBanned(parts[1])) {
            errors.put("emailDomain", "Invalid email format or banned domain.");
        }
    }

    public void setupSessionAttributesBasedOnCountry(String country, HttpSession session) {
        if ("other".equals(country)) {
            session.setAttribute("usingDefaultSubsidiary", "1");
            session.setAttribute("toolmateOtherCountry", "1");
            session.setAttribute("countryCodeId", country);
            session.setAttribute("secCountry", "other");
        } else {
            Long countryCode = Long.parseLong(country);
            Optional<SecServingCountry> secCountryOpt = secServingCountryRepository.findByCountryCode(countryCode);
            SecServingCountry secCountry = secCountryOpt.get();
            session.setAttribute("usingDefaultSubsidiary", "1");
            
            //파라미터로 secSubsidiary가 안들어올경우 secServingCountry로 저장
            if(session.getAttribute("secSubsidiary")==null) {
                session.setAttribute("secSubsidiary", secCountry.getSubsidiary());
            }
            session.setAttribute("secCountry", secCountry.getCountryCode());
            session.setAttribute("toolmateOtherCountry", "0");
            session.setAttribute("countryCodeId", countryCode);
        }
    }

    private String performRegistration(String param, Map<String, String> payload, HttpSession session, RedirectAttributes redirectAttributes) {
        Map<String, Object> registrationData = setupRegistrationData(param, payload, session);
        GSResponse response = gigyaService.executeRequest(param, "accounts.register", registrationData);
        return handleRegistrationResponse(param, payload, response, session, redirectAttributes);
    }

    private Map<String, Object> setupRegistrationData(String param, Map<String, String> payload, HttpSession session) {
        Map<String, String> dataFields = new HashMap<>();
        dataFields.put("userStatus", "registerEmail");
        dataFields.put("subsidiary", (String) session.getAttribute("cmdmSubsidiary"));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> registrationData = new HashMap<>();
        try {
            registrationData.put("data", objectMapper.writeValueAsString(dataFields));
            registrationData.put("preferences", objectMapper.writeValueAsString(setupPreferences()));
        } catch (JsonProcessingException e) {
            log.error("Error serializing registration data", e);
        }
        String email = payload.get("loginUserId");
        String[] emailSplit = email.split("@");
        String emailId = emailSplit[0];
        String domain = emailSplit[1];

        long timestamp = System.currentTimeMillis();

        // 임시 이메일 생성
        String tempEmail = emailId + "-" + timestamp + "@" + domain;

        registrationData.put("username", tempEmail);
        registrationData.put("email", payload.get("loginUserId"));
        registrationData.put("password", BeansUtil.getApplicationProperty("cdc.temp-reg-pwd"));
        registrationData.put("regSource", param);

        return registrationData;
    }

    private Map<String, Object> setupPreferences() {
        Map<String, Boolean> provideInformation = new HashMap<>();
        provideInformation.put("isConsentGranted", true);
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("provideInformation", provideInformation);
        return preferences;
    }

    private String handleRegistrationResponse(String param, Map<String, String> payload, GSResponse response, HttpSession session, RedirectAttributes redirectAttributes) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode responseJson = objectMapper.readTree(response.getResponseText());
            /*if (response.getErrorCode() != 0) {
                session.setAttribute("showErrorMsg", "Registration failed: " + response.getErrorMessage());
            }*/
            if (response.getErrorCode() == 206006 || response.getErrorCode() == 206001) {
                session.setAttribute("uid", responseJson.get("UID").asText());
                Map<String, Object> otpData = new HashMap<>();
                otpData.put("lang", Locale.getDefault());
                otpData.put("email", payload.get("loginUserId"));
                otpData.put("status", "active");

                GSResponse otpResponse = gigyaService.executeRequest(param, "accounts.otp.sendCode", otpData);
                JsonNode otpResponseJson = objectMapper.readTree(otpResponse.getResponseText());
                if (otpResponseJson.get("vToken") == null || otpResponseJson.get("vToken").isEmpty()) {
                    //에러처리
                }

                Map<String, Object> data = new HashMap<>();
                data.put("vToken", otpResponseJson.get("vToken"));
                String jsonData = "";
                // ObjectMapper를 사용하여 Map을 JSON으로 변환
                try {
                    jsonData = objectMapper.writeValueAsString(data);
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage()); // JSON 변환 실패 처리
                }

                // 암호화 수행
                String encryptedToken = EncryptUtil.encryptData(jsonData);
                session.setAttribute("vToken", encryptedToken);
                if (otpResponse.getErrorCode() == 0) {

                    //session.setAttribute("email", payload.get("email"));
                    session.setAttribute("loginId", payload.get("loginUserId"));
                    return "/registration/signupVerify";
                } else {
                    // 에러처리
                    //return back()->withFlashDanger('authVerifyFailed');
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON response", e);
            redirectAttributes.addFlashAttribute("showErrorMsg", "Error processing registration response.");
        }
        return "/registration/signupVerify";
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private boolean isDomainBanned(String domain) {
        return !emailDomainsRepository.selectBannedDomains(domain).isEmpty();
    }

    // 24.07.03 홍정인 추가 - 회원가입 Information 입력 화면 예비 URL
    public ModelAndView signupInformation(String key, Map<String, Object> allParams, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/registration/registrationInformationTest";
        modelAndView.addObject("content", content);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<?>> fieldMap = cdcTraitService.generateFieldMap(key, objectMapper, true, false,false); // Company 데이터만 포함

        modelAndView.addObject("fieldMap", fieldMap);

        modelAndView.addObject("mobileHeader", searchService.getColumnInfoList());

        String popupSearchCompany = "popups/searchCompany";
        modelAndView.addObject("searchCompanyModal", popupSearchCompany);

        return modelAndView;
    }

    public RedirectView signupVerified(Map<String, String> payload, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            String vTokenEncrypted = (String) session.getAttribute("vToken");
            if (vTokenEncrypted == null) {
                redirectAttributes.addFlashAttribute("showErrorMsg", "Token not found in session");
                log.error("Token not found in session");
                return new RedirectView(request.getHeader("Referer"));
            }

            String vTokenDecrypted = EncryptUtil.decryptData(vTokenEncrypted);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> vTokenMap = objectMapper.readValue(vTokenDecrypted, new TypeReference<Map<String, String>>() {
            });
            String vToken = vTokenMap.get("vToken");

            // Validate code
            Map<String, Object> params = new HashMap<>();
            params.put("code", payload.get("verCode"));
            params.put("vToken", vToken);
            GSResponse response = gigyaService.executeRequest("defaultChannel", "accounts.auth.otp.verify", params);
            String convertYn = (String) session.getAttribute("convertYn");
            if (response.getErrorCode() == 0) {
                // Verify user emails
                params.clear();
                params.put("UID", (String) session.getAttribute("uid"));
                params.put("isVerified", true);
                Map<String, String> dataFields = new HashMap<>();
                dataFields.put("userStatus", "regEmailVerified");
                String dataFieldsJSON = objectMapper.writeValueAsString(dataFields);
                if(session.getAttribute("ssoAccessYn") == null && session.getAttribute("ciamConvertYn")==null) {
                    params.put("data", dataFieldsJSON);
                }
                response = gigyaService.executeRequest("defaultChannel", "accounts.setAccountInfo", params);

                session.removeAttribute("vToken"); // 세션에서 'vToken'을 제거합니다.
                String referer = request.getHeader("Referer");

                if (referer != null && referer.equals(request.getRequestURL().toString().replace("/verifyCode", "/changeEmailVerify"))) {
                    return new RedirectView("/registration/consent"+ "?param=" + payload.get("channel"));
                } else {
                    return new RedirectView("/registration/company" + "?param=" + payload.get("channel"));
                }
            } else {
                String errorDetails = response.getErrorDetails();
                String errorMsg = "";

                if (errorDetails != null) {
                    errorMsg = errorDetails;
                } else {
                    errorMsg = response.getErrorMessage();
                }
                redirectAttributes.addFlashAttribute("showErrorMsg", "Verification failed: " + errorMsg);
                return new RedirectView(request.getHeader("Referer"));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "Token expired or invalid, please request a new token");
            return new RedirectView(request.getHeader("Referer"));
        }
    }

    public boolean isUserIdAvailable(Map<String, Object> checkUserIdResult) {
        int errorCode;
        try {
            errorCode = checkUserIdResult.get("errorCode") instanceof Integer
                    ? (Integer) checkUserIdResult.get("errorCode")
                    : Integer.parseInt((String) checkUserIdResult.get("errorCode"));
        } catch (NumberFormatException e) {
            return false;
        }

        return errorCode == 0 && "available".equals(checkUserIdResult.get("status"));
    }

    // 24.07.04 홍정인 추가 - 회원가입 Consent 화면 예비 URL
    public ModelAndView signupConsent(String key, Map<String, Object> allParams, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/registration/consent";
        modelAndView.addObject("content", content);

        return modelAndView;
    }

    public Channels getChannel(String channelID) {
        return channelRepository.findByChannelName(channelID)
                .orElseThrow(() -> new EntityNotFoundException("Channel not found"));
    }

    public List<CisCountry> getCountries() {
        return cisCountryRepository.findAllOrderedByNameEn();
    }

    public List<SecServingCountry> getSecServingCountries(String channel) {
        return secServingCountryRepository.selectCountryCodeChannel(channel);
    }

    public Map<String, Object> getRegisterCompanyData(HttpSession session) {
        Map<String, Object> registerCompany = new HashMap<>();
        registerCompany.put("name", "");
        registerCompany.put("country", (String) session.getAttribute("secCountry") != null ? (String) session.getAttribute("secCountry") : "");
        registerCompany.put("street_address", "");
        registerCompany.put("phonenumber1", "");
        registerCompany.put("vendorCode", "");
        registerCompany.put("city", "");
        registerCompany.put("zip_code", "");
        registerCompany.put("bizregno1", "");
        registerCompany.put("regch", "");
        //registerCompany.put("isNewCompany", (String) session.getAttribute("isNewCompany") != null ? (String) session.getAttribute("isNewCompany") : "");
        registerCompany.put("channelType", "");
        registerCompany.put("orgSource", "");
        registerCompany.put("bpid", "");
        registerCompany.put("type", "");

        return registerCompany;
    }

    public Map<String, Object> getRegisterMtData() {
        Map<String, Object> registerMt = new HashMap<>();
        registerMt.put("mtLicense", "");
        registerMt.put("mtStartDate", "");
        registerMt.put("mtEndDate", "");

        return registerMt;
    }

    public SecServingCountry getSecServingCountry(String countryCode, String secChannel) {
        return secServingCountryRepository.findByCountryCodeAndChannelAndSubsidiaryNot(countryCode, secChannel)
                .orElse(null);
    }

    public List<SfdcProduct> getSfdcProducts(String subsidiary) {
        return sfdcProductRepository.findBySubsidiaryAndLevel(subsidiary, "1");
    }

    public List<SfdcProduct> getClimateSfdcProducts() {
        return sfdcProductRepository.findBySubsidiaryAndLevel("SEACE", "1");
    }

    public boolean shouldHideClimateSolution(String channelID, String countryCode) {
        ObjectMapper objectMapper = new ObjectMapper();
        Channels channel = getChannel(channelID);
        if (channel.getConfig() != null) {
            try {
                Map<String, Object> configMap = objectMapper.readValue(channel.getConfig(), new TypeReference<Map<String, Object>>() {
                });
                if (configMap.containsKey("hide_climatesolution_by_country")) {
                    String[] countryList = ((String) configMap.get("hide_climatesolution_by_country")).split(",");
                    return Arrays.asList(countryList).contains(countryCode);
                }
            } catch (Exception e) {
                log.error(e.getMessage()); // 로그를 남기는 것이 좋습니다.
                // 예외 처리 로직을 추가할 수 있습니다.
            }
        }
        return false;
    }

    public boolean isCountrySeace(String countryCode) {
        List<String> seaceCountries = Arrays.asList(
                "NL", "AL", "AT", "BE", "BA", "BG", "CZ", "HR", "CW", "DK", "EE",
                "FI", "FR", "DE", "GR", "HU", "IE", "IT", "XK", "LV", "LT", "LU",
                "MK", "MT", "ME", "NO", "PL", "PT", "RO", "RS", "SI", "SK", "ES",
                "SE", "CH", "GB"
        );
        return seaceCountries.contains(countryCode);
    }

    public void companySubmit(Map<String, String> requestParams, String param, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // Set industry array
        List<String> industryArray = new ArrayList<>();
        if (requestParams.containsKey("industry_1")) {
            industryArray.add(requestParams.get("industry_1"));
        }
        if (requestParams.containsKey("industry_2")) {
            industryArray.add(requestParams.get("industry_2"));
        }
        if (requestParams.containsKey("industry_3")) {
            industryArray.add(requestParams.get("industry_3"));
        }

        // Prepare register company data
        Map<String, Object> registerCompany = prepareRegisterCompanyData(requestParams, session, industryArray);
        session.setAttribute("registerCompany", registerCompany);

        // Session management
        if (requestParams.containsKey("isNewCompany")) {
            session.setAttribute("isNewCompany", requestParams.get("isNewCompany"));
        }

        if (requestParams.containsKey("products") && requestParams.get("products") != null && !requestParams.get("products").isEmpty()) {
            session.setAttribute("products", requestParams.get("products"));
        }

        // Handle company registration logic
        String usedBpid = handleCompanyRegistration(requestParams, industryArray, registerCompany,param,"");

        session.setAttribute("bpid", usedBpid);
        session.setAttribute("company_name", requestParams.get("name"));
        session.setAttribute("companyObject", requestParams);
        processVendorCompanyRegistration(requestParams, param, session, usedBpid,redirectAttributes);

        // Additional validation logic
        /*if (validateRequest(requestParams, registerCompany, param)) {
            // Proceed with company registration
            processCompanyRegistration(requestParams, param, session, usedBpid,redirectAttributes);
        } else {
            // Handle validation errors
            redirectAttributes.addFlashAttribute("showErrorMsg", "Validation failed");
        }*/
    }

    private Map<String, Object> prepareRegisterCompanyData(Map<String, String> requestParams, HttpSession session, List<String> industryArray) {
        Map<String, Object> registerCompany = new HashMap<>();
        registerCompany.put("name", requestParams.get("name"));
       // registerCompany.put("country", session.getAttribute("secCountry"));
        registerCompany.put("country", requestParams.get("country"));
        registerCompany.put("street_address", requestParams.get("street_address"));
        registerCompany.put("phonenumber1", requestParams.get("phonenumber1"));
        registerCompany.put("vendorCode", requestParams.get("vendorCode"));
        registerCompany.put("city", requestParams.get("city"));
        registerCompany.put("zip_code", requestParams.get("zip_code"));
        registerCompany.put("bizregno1", requestParams.get("bizregno1"));
        registerCompany.put("regch", requestParams.get("regch"));
        //registerCompany.put("isNewCompany", requestParams.get("isNewCompany"));
        registerCompany.put("channelType", requestParams.get("channelType"));
        registerCompany.put("orgSource", requestParams.get("orgSource"));
        registerCompany.put("bpid", requestParams.get("bpid"));
        registerCompany.put("type", requestParams.get("type"));
        registerCompany.put("state", requestParams.get("state"));
        registerCompany.put("region", requestParams.get("region"));
        registerCompany.put("email", requestParams.get("email"));
        registerCompany.put("fax", requestParams.get("fax"));
        registerCompany.put("website", requestParams.get("website"));
        registerCompany.put("representative", requestParams.get("representative"));
        registerCompany.put("industry", industryArray);

        return registerCompany;
    }

    /*private void handleFileAttachments(List<MultipartFile> attachmentFiles, HttpSession session) {
        List<Map<String, Object>> attachFileArr = new ArrayList<>();

        for (MultipartFile attachedFile : attachmentFiles) {
            try {
                String filenameWithExtension = attachedFile.getOriginalFilename();
                String filename = filenameWithExtension != null ? filenameWithExtension.substring(0, filenameWithExtension.lastIndexOf('.')) : "";
                String extension = filenameWithExtension != null ? filenameWithExtension.substring(filenameWithExtension.lastIndexOf('.') + 1) : "";

                String filenameToStore = filename + "_" + System.currentTimeMillis() + "." + extension;

                // Upload File to s3 (mocked with local file system for the example)
                String url = uploadFileToS3(attachedFile, filenameToStore);

                // Store file info in the database
                FileStorage fileStorage = new FileStorage();
                fileStorage.setFilename(filenameToStore);
                fileStorage.setType(extension);
                fileStorage.setPath(url);
                fileStorageRepository.save(fileStorage);

                Map<String, Object> fileData = new HashMap<>();
                fileData.put("id", fileStorage.getId());
                fileData.put("originalName", filenameWithExtension);
                fileData.put("savedFileName", filenameToStore);
                fileData.put("url", url);
                fileData.put("extension", extension);
                fileData.put("attach_date", new Date());

                attachFileArr.add(fileData);
            } catch (IOException e) {
                // Handle the exception
                log.error(e.getMessage());
            }
        }

        if (!attachFileArr.isEmpty()) {
            session.setAttribute("attachment_file_json", attachFileArr);
        } else {
            session.removeAttribute("attachment_file_json");
        }
    }*/

    private String uploadFileToS3(MultipartFile file, String filenameToStore) throws IOException {
        // Mocking S3 upload by saving to local file system
        String uploadDir = "uploads/";
        Files.createDirectories(Paths.get(uploadDir));
        Files.copy(file.getInputStream(), Paths.get(uploadDir).resolve(filenameToStore));
        return Paths.get(uploadDir).resolve(filenameToStore).toString();
    }

    private String handleCompanyRegistration(Map<String, String> requestParams, List<String> industryArray, Map<String, Object> registerCompany,String param,String type) {
        String usedBpid;

        String bizregno1 = requestParams.get("bizregno1").isEmpty() ? "NOBIZREG-" + new Random().nextInt(10000) + '-' + System.currentTimeMillis() : requestParams.get("bizregno1");

        if ("true".equals(requestParams.get("isNewCompany")) && "customer".equals(type)) {
            HttpServletRequest requestServlet = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            HttpSession session = requestServlet.getSession();
            session.setAttribute("isNewCompany",true);
            usedBpid = "CMDM-" + bizregno1.replaceAll("[/ ]", "");

            String productsFullList = "";

            Optional<NewCompany> duplicateCompany = newCompanyRepository.findFirstByBizRegNo1(bizregno1);

            if (duplicateCompany.isPresent()) {
                NewCompany existingCompany = duplicateCompany.get();
                updateCompany(existingCompany, requestParams, industryArray, productsFullList);
            } else {
                NewCompany newCompany = new NewCompany();
                newCompany.setName(requestParams.getOrDefault("name",""));
                newCompany.setBpid(usedBpid);
                newCompany.setChannelType(requestParams.getOrDefault("channelType",""));
                newCompany.setBizRegNo1(bizregno1);
                newCompany.setVatNo(requestParams.getOrDefault("vatno",""));
                newCompany.setDunsNo(requestParams.getOrDefault("dunsno",""));
                newCompany.setCisCode(requestParams.getOrDefault("ciscode",""));
                newCompany.setRepresentative(requestParams.getOrDefault("representative",""));
                newCompany.setIndustryType(industryArray != null && !industryArray.isEmpty() ? industryArray.toString() : "[]");
                newCompany.setAccountWebsite(requestParams.getOrDefault("website",""));
                newCompany.setEmail(requestParams.getOrDefault("email",""));
                newCompany.setPhoneNumber1(requestParams.getOrDefault("phonenumber1",""));
                newCompany.setFaxNo(requestParams.getOrDefault("fax",""));
                newCompany.setCountry(requestParams.getOrDefault("country",""));
                newCompany.setStreetAddress(requestParams.getOrDefault("street_address",""));
                newCompany.setCity(requestParams.getOrDefault("city",""));
                newCompany.setState(requestParams.getOrDefault("state",""));
                newCompany.setRegion(requestParams.getOrDefault("region",""));
                newCompany.setZipCode(requestParams.getOrDefault("zip_code",""));
                newCompany.setType("CMDM");
                newCompany.setOrgId(requestParams.getOrDefault("orgId",""));
                newCompany.setOrgId(requestParams.getOrDefault("orgId",""));
                newCompany.setSource("CMDM");
                String subsidiary = (String) session.getAttribute("secSubsidiary");
                newCompany.setSubsidiary(subsidiary != null ? subsidiary : "");

                String regch = channelRepository.selectChannelRegCh(param);

                //newCompany.setRegCh(requestParams.getOrDefault("regch",""));
                newCompany.setRegCh(regch);
                newCompany.setProducts(requestParams.getOrDefault("products",""));
                newCompany.setProductLists(productsFullList);
                newCompany.setProductCategoryLists(requestParams.getOrDefault("productCategoryLists",""));
                newCompany.setMtLicense(requestParams.getOrDefault("magicinfo",""));
                newCompany.setMtStartDate(requestParams.getOrDefault("startDttm",""));
                newCompany.setMtEndDate(requestParams.getOrDefault("endDttm",""));

                newCompanyRepository.save(newCompany);
            }
        } else {
            Optional<NewCompany> existingCdcCompanyOpt = newCompanyRepository.findFirstByBizRegNo1(bizregno1);
            String productsFullList = "";

            if (existingCdcCompanyOpt.isPresent()) {
                NewCompany existingCdcCompany = existingCdcCompanyOpt.get();
                if (requestParams.containsKey("bpid") && requestParams.get("bpid") != null && !requestParams.get("bpid").isEmpty()) {
                    if (requestParams.get("bpid").toUpperCase().contains("CMDM")) {
                        existingCdcCompany.setBpidInCdc(requestParams.get("bpid"));
                    }
                }
                updateCompany(existingCdcCompany, requestParams, industryArray, productsFullList);
            }

            usedBpid = requestParams.get("bpid");
            try {
                if (requestParams.containsKey("bpid") && requestParams.get("bpid") != null && !requestParams.get("bpid").isEmpty()) {
                    updateCdcCompany(requestParams, industryArray);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        return usedBpid;
    }

    private void updateCompany(NewCompany company, Map<String, String> requestParams, List<String> industryArray, String productsFullList) {
        company.setChannelType(requestParams.get("channelType") != null ? requestParams.get("channelType") : "");
        company.setIndustryType(String.join(",", industryArray));
        company.setProducts(requestParams.get("products") != null ? requestParams.get("products") : "");
        company.setProductLists(productsFullList);
        company.setProductCategoryLists(requestParams.get("productCategoryLists") != null ? requestParams.get("productCategoryLists") : "");
        company.setDunsNo(requestParams.getOrDefault("dunsno",""));
        // mmp channel custom field
        company.setMtLicense(requestParams.get("magicinfo") != null ? requestParams.get("magicinfo") : "");
        company.setMtStartDate(requestParams.get("startDttm") != null ? requestParams.get("startDttm") : "");
        company.setMtEndDate(requestParams.get("endDttm") != null ? requestParams.get("endDttm") : "");

        newCompanyRepository.save(company);
    }

    private void updateCdcCompany(Map<String, String> requestParams, List<String> industryArray) {
        try {
            // Gigya 요청 준비
            Map<String, Object> organization = new HashMap<>();

            // industryArray가 비어있으면 빈 문자열로 설정
            if (industryArray != null && !industryArray.isEmpty()) {
                // industryArray 요소를 따옴표로 감싸서 배열 형식의 문자열로 변환
                cdcTraitService.setIndustryType(organization, industryArray);
            } else {
                organization.put("industry_type", "[]"); // 빈 배열 형식의 문자열
            }

            // 다른 값 설정

            organization.put("channeltype", requestParams.getOrDefault("channelType", ""));
            String organizationJSON = new ObjectMapper().writeValueAsString(organization);

            // 로그 출력
            log.info("organizationJSON: " + organizationJSON);

            // Gigya 요청 매개변수 설정
            Map<String, Object> params = new HashMap<>();
            params.put("info", organizationJSON);
            params.put("bpid", requestParams.get("bpid"));

            // Gigya API 요청 실행
            GSResponse response = gigyaService.executeRequest("default", "accounts.b2b.setOrganizationInfo", params);

            // 응답 처리
            if (response.getErrorCode() == 0) {
                log.info("CDC company updated successfully: " + response.getResponseText());
            } else {
                log.error("Failed to update CDC company: " + response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Error updating CDC company: " + e.getMessage());
            log.error(e.getMessage());
        }
    }

    private boolean validateRequest(Map<String, String> requestParams, Map<String, Object> registerCompany, String param) {
        // Implement validation logic
        // Return true if validation is successful, false otherwise
        boolean isValid = true;
        // Add your validation logic here

        // Example validation
        if (requestParams.get("name") == null || requestParams.get("name").isEmpty()) {
            isValid = false;
        }
        if ("KR".equals(registerCompany.get("country")) && (requestParams.get("bizregno1") == null || !requestParams.get("bizregno1").matches("\\d{10}"))) {
            isValid = false;
        }

        return isValid;
    }

    private void processCompanyRegistration(Map<String, String> requestParams, String param, HttpSession session, String usedBpid,RedirectAttributes redirectAttributes) {
        // Getting the user details
        JsonNode cdcUser = cdcTraitService.getCdcUser((String) session.getAttribute("uid"), 0);

        // profile 내의 work 항목을 JsonNode로 받아오기
        JsonNode profile = cdcUser.path("profile");
        JsonNode work = profile.path("work");

        // 각 필드를 String으로 변환
        String adminReason = work.path("description").asText();
        String workTitle = work.path("title").asText();
        String workLocation = work.path("location").asText();
        // Setting CDC request
        Map<String, Object> profileFields = new HashMap<>();
        if ("1".equals(session.getAttribute("toolmateOtherCountry")) && "toolmate".equals(param)) {
            profileFields.put("work", Map.of("company", requestParams.get("name")));
            profileFields.put("country", requestParams.get("country"));
        } else {
            profileFields.put("work", Map.of("company", requestParams.get("name")));
        }

        String profileJSON = "";
        try {
            profileJSON = new ObjectMapper().writeValueAsString(profileFields);
        } catch (Exception e) {
            log.error("Error serializing profile fields", e);
        }

        Map<String, Object> cdcParams = new HashMap<>();
        Map<String, Object> oranizationParams = new HashMap<>();

        cdcParams.put("UID", (String) session.getAttribute("uid"));
        cdcParams.put("profile", profileJSON);

        Map<String, Object> accountParams = new HashMap<>();
        Map<String, Object> companyParams = new HashMap<>();

        /*if ("mmp".equals(param)) {
            accountParams.put("data", Map.of("accountID", usedBpid, "channels", Map.of(param, Map.of("mtLicense", requestParams.getOrDefault("magicinfo", ""), "mtStartDate", requestParams.getOrDefault("startDttm", ""), "mtEndDate", requestParams.getOrDefault("endDttm", "")))));
        } else {
            accountParams.put("accountID", Map.of("accountID", usedBpid));
        }*/
        String dataFieldsJson = "";
        String companyFieldsJSon = "";
        try {
            List<ChannelAddField> additionalFields = channelAddFieldRepository.selectAdditionalField(param,"additional","account");
            List<ChannelAddField> specFields = channelAddFieldRepository.selectAdditionalField(param,"spec","account");
            List<ChannelAddField> companyAdditionalFields = channelAddFieldRepository.selectAdditionalField(param,"additional","company");
            List<ChannelAddField> companySpecFields = channelAddFieldRepository.selectAdditionalField(param,"spec","company");


            if(additionalFields.size()>0) {

                // 필드를 메모리에서 비교하며 매핑
                for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                    String elementId = entry.getKey(); // elementId를 가져옴
                    String value = entry.getValue();
                    //if(value!="") {
                        // 해당 element_id와 일치하는 필드를 찾기
                        ChannelAddField matchingField = additionalFields.stream()
                                .filter(field -> field.getElementId().equals(elementId))
                                .findFirst()
                                .orElse(null);

                        if (matchingField != null) {
                            // cdc_data_field 값에 $channelname을 넣어서 최종 필드 이름을 생성
                            String cdcDataField = matchingField.getCdcDataField().replace("$channelname", param);

                            // 최종 필드 이름으로 accountParams에 값 설정
                            accountParams.put(cdcDataField, value);
                        }
                    //}
                }
            }
            if(specFields.size()>0) {

                // 필드를 메모리에서 비교하며 매핑
                for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                    String elementId = entry.getKey(); // elementId를 가져옴
                    String value = entry.getValue();
                    //if(value!="") {
                        // 해당 element_id와 일치하는 필드를 찾기
                        ChannelAddField matchingField = specFields.stream()
                                .filter(field -> field.getElementId().equals(elementId))
                                .findFirst()
                                .orElse(null);

                        if (matchingField != null) {
                            // cdc_data_field 값에 $channelname을 넣어서 최종 필드 이름을 생성
                            String cdcDataField = matchingField.getCdcDataField().replace("$channelname", param);

                            // 최종 필드 이름으로 accountParams에 값 설정
                            accountParams.put(cdcDataField, value);
                        }
                    //}
                }
            }
            if (companyAdditionalFields.size() > 0) {
                // 필드를 메모리에서 비교하며 매핑
                for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                    String elementId = entry.getKey(); // elementId를 가져옴
                    String value = entry.getValue();

                    // 해당 element_id와 일치하는 필드를 찾기
                    ChannelAddField matchingField = companyAdditionalFields.stream()
                            .filter(field -> field.getElementId().equals(elementId))
                            .findFirst()
                            .orElse(null);

                    if (matchingField != null) {
                        // cdc_data_field 값에 $channelname을 넣어서 최종 필드 이름을 생성
                        String cdcDataField = matchingField.getCdcDataField();

                        // 배열 형태로 넣어야 하는 경우 처리
                        if ("Y".equals(matchingField.getArrayYn())) {
                            List<String> valueList;

                            // companyParams에 이미 값이 있으면 해당 값을 리스트로 변환
                            if (companyParams.containsKey(cdcDataField)) {
                                String existingValue = companyParams.get(cdcDataField).toString();
                                valueList = new ArrayList<>(Arrays.asList(existingValue.replace("[", "").replace("]", "").split(",")));
                            } else {
                                valueList = new ArrayList<>();
                            }

                            // 값 추가
                            valueList.add(value);

                            // 리스트를 JSON 문자열로 변환하여 저장
                            String jsonValueList = new ObjectMapper().writeValueAsString(valueList);
                            companyParams.put(cdcDataField, jsonValueList);
                        } else {
                            // 배열이 아닐 경우, 기존 방식대로 처리
                            companyParams.put(cdcDataField, value);
                        }
                    }
                }
            }

            if (companySpecFields.size() > 0) {
                // 필드를 메모리에서 비교하며 매핑
                for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                    String elementId = entry.getKey(); // elementId를 가져옴
                    String value = entry.getValue();

                    // 해당 element_id와 일치하는 필드를 찾기
                    ChannelAddField matchingField = companySpecFields.stream()
                            .filter(field -> field.getElementId().equals(elementId))
                            .findFirst()
                            .orElse(null);

                    if (matchingField != null) {
                        // cdc_data_field 값에 $channelname을 넣어서 최종 필드 이름을 생성
                        String cdcDataField = matchingField.getCdcDataField();

                        // 배열 형태로 넣어야 하는 경우 처리
                        if ("Y".equals(matchingField.getArrayYn())) {
                            List<String> valueList;

                            // companyParams에 이미 값이 있으면 해당 값을 리스트로 변환
                            if (companyParams.containsKey(cdcDataField)) {
                                String existingValue = companyParams.get(cdcDataField).toString();
                                valueList = new ArrayList<>(Arrays.asList(existingValue.replace("[", "").replace("]", "").split(",")));
                            } else {
                                valueList = new ArrayList<>();
                            }

                            // 값 추가
                            valueList.add(value);

                            // 리스트를 JSON 문자열로 변환하여 저장
                            String jsonValueList = new ObjectMapper().writeValueAsString(valueList);
                            companyParams.put(cdcDataField, jsonValueList);
                        } else {
                            // 배열이 아닐 경우, 기존 방식대로 처리
                            companyParams.put(cdcDataField, value);
                        }
                    }
                }
            }

            accountParams.put("accountID",usedBpid);
            dataFieldsJson = new ObjectMapper().writeValueAsString(accountParams);
            cdcParams.put("data",dataFieldsJson);

            companyFieldsJSon = new ObjectMapper().writeValueAsString(requestParams);
            oranizationParams.put("bpid",usedBpid);
            oranizationParams.put("info",companyFieldsJSon);
            session.setAttribute("companyParams",companyParams);
            JsonNode company = cdcTraitService.getB2bOrg(usedBpid);
            String bpid = "";
            ObjectMapper objectMapper = new ObjectMapper();

            if (company.has("errorCode") && company.get("errorCode").asInt() == 0) {
                if(companyParams.size()>0) {
                    companyFieldsJSon = new ObjectMapper().writeValueAsString(companyParams);
                    oranizationParams.put("bpid",usedBpid);
                    oranizationParams.put("info",companyFieldsJSon);
                    session.setAttribute("companyParams",companyParams);
                    GSResponse OrganizationResponse = gigyaService.executeRequest("default", "accounts.b2b.setOrganizationInfo", oranizationParams);
//                if (OrganizationResponse.getErrorCode() == 0) {
//                    log.info("setOrganizationInfo success");
//                } else {
//                    log.error("company registration fail : " + OrganizationResponse.getResponseText());
//                    log.warn("company registration context : " + session.getAttribute("uid"));
//                    String tmpErrorMsg = OrganizationResponse.getData().containsKey("errorMessage") ? " : " + OrganizationResponse.getData().getString("errorMessage") : "";
//                    redirectAttributes.addFlashAttribute("showErrorMsg", "company registration failed" + tmpErrorMsg);
//                }
                }
            }
            else {
                // CMDM에서 회사 데이터를 검색하기 위한 파라미터 설정
                Map<String, Object> searchParams = new HashMap<>();
                searchParams.put("accountId", usedBpid);  // CDC 계정 ID를 사용하여 CMDM에서 검색
                CpiResponseFieldMapping responseFieldMapping = CpiResponseFieldMapping.fromString(param);

                // CMDM에서 데이터 검색
                List<Map<String, Object>> companyDataList = cpiApiService.accountSearch(
                        Collections.singletonMap("acctid", usedBpid),
                        BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.accountSerachUrl"),
                        responseFieldMapping,
                        session
                );

                Map<String, Object> organization = new HashMap<>();

                if (!companyDataList.isEmpty()) {
                    // CMDM에서 검색된 첫 번째 회사 데이터를 사용
                    Map<String, Object> companyData = companyDataList.get(0);

                    organization.put("name", companyData.getOrDefault("orgName", ""));
                    organization.put("name_search", companyData.getOrDefault("orgName", ""));
                    organization.put("bpid", companyData.getOrDefault("bpid", ""));
                    organization.put("bizregno1", companyData.getOrDefault("bizregno1", ""));
                    organization.put("phonenumber1", companyData.getOrDefault("phonenumber1", ""));
                    organization.put("country", companyData.getOrDefault("country", ""));
                    organization.put("street_address", companyData.getOrDefault("street_address", "").toString().toLowerCase());
                    organization.put("city", companyData.getOrDefault("city", ""));
                    organization.put("state", companyData.getOrDefault("state", ""));
                    organization.put("zip_code", companyData.getOrDefault("zip_code", ""));
                    organization.put("type", "CMDM");
                    organization.put("regch", companyData.getOrDefault("regch", ""));
                    // organization에 배열 형태로 추가
                    List<String> industryTypeList = StringUtil.extractValueAsList(requestParams, "industryType");
                    organization.put("industry_type", StringUtil.convertListToJsonArray(industryTypeList));
                    organization.put("channeltype", companyData.getOrDefault("channeltype", ""));

                } else {
                    // CMDM에서 회사 데이터가 없으면 requestParams를 사용하여 organization 매핑
                    organization.put("name", requestParams.getOrDefault("name", ""));  // "name" -> "3213219999999"
                    organization.put("name_search", requestParams.getOrDefault("name", ""));  // "name_search"도 동일한 값 사용
                    String cdcBpid = requestParams.get("bpid");
                    if (cdcBpid == null || cdcBpid.isEmpty()) {
                        cdcBpid = usedBpid;  // requestParams에서 bpid가 없거나 비어있으면 usedBpid 사용
                    }
                    organization.put("bpid", cdcBpid);
                    organization.put("bizregno1", requestParams.getOrDefault("bizregno1", ""));  // "bizregno1" -> ""
                    organization.put("phonenumber1", requestParams.getOrDefault("phonenumber1", ""));  // "phonenumber1" -> ""
                    organization.put("country", requestParams.getOrDefault("country", ""));  // "country" -> "US"
                    organization.put("street_address", requestParams.getOrDefault("street_address", "").toString().toLowerCase());  // "street_address" -> ""
                    organization.put("city", requestParams.getOrDefault("city", ""));  // "city" -> ""
                    organization.put("state", requestParams.getOrDefault("state", ""));  // "state" -> ""
                    organization.put("zip_code", requestParams.getOrDefault("zip_code", ""));  // "zip_code" -> ""
                    organization.put("type", "CMDM");  // "type" -> ""
                    organization.put("regch", requestParams.getOrDefault("regch", ""));  // "regch" -> ""
                    List<String> industryTypeList = StringUtil.extractValueAsList(requestParams, "industryType");
                    organization.put("industry_type", StringUtil.convertListToJsonArray(industryTypeList));
                    organization.put("channeltype", requestParams.getOrDefault("channeltype", ""));  // "channeltype" -> "sba"
                }

                // CDC 데이터를 동적으로 추출
                cdcTraitService.extractDynamicCompanyCdcDataFields(companyParams, organization, param);

                try {
                    // organization을 JSON으로 변환
                    String organizationJson = objectMapper.writeValueAsString(organization);

                    // 요청자 정보 매핑
                    Map<String, Object> requester = new HashMap<>();
                    requester.put("firstName", requestParams.getOrDefault("firstName", ""));
                    requester.put("lastName", requestParams.getOrDefault("lastName", ""));
                    requester.put("email", requestParams.getOrDefault("loginUserId", ""));

                    String requesterJson = objectMapper.writeValueAsString(requester);

                    // CDC 파라미터 설정
                    Map<String, Object> registraionOrganizationParams = new HashMap<>();
                    registraionOrganizationParams.put("organization", organizationJson);
                    registraionOrganizationParams.put("requester", requesterJson);
                    registraionOrganizationParams.put("status", "approved");

                    // CDC에 조직 등록 요청
                    GSResponse registerOrgResponse = gigyaService.executeRequest(
                            "default", "accounts.b2b.registerOrganization", registraionOrganizationParams
                    );
                    log.info("registerOrganization: {}", registerOrgResponse.getResponseText());

                    // BPID 처리
                    bpid = (String) organization.getOrDefault("bpid", usedBpid);  // requestParams 또는 CMDM에서 가져온 BPID

                    if (registerOrgResponse.getErrorCode() == 0) {
                        log.info("New CMDM company with bpid {} created in CDC", bpid);
                    } else {
                        log.error("Failed to create CMDM company with bpid {} in CDC. Error: {}", bpid, registerOrgResponse.getResponseText());
                    }
                } catch (Exception e) {
                    log.error("Error processing registerOrganization request", e);
                }
            }
//                if (OrganizationResponse.getErrorCode() == 0) {
//                    log.info("setOrganizationInfo success");
//                } else {
//                    log.error("company registration fail : " + OrganizationResponse.getResponseText());
//                    log.warn("company registration context : " + session.getAttribute("uid"));
//                    String tmpErrorMsg = OrganizationResponse.getData().containsKey("errorMessage") ? " : " + OrganizationResponse.getData().getString("errorMessage") : "";
//                    redirectAttributes.addFlashAttribute("showErrorMsg", "company registration failed" + tmpErrorMsg);
//                }

        } catch (Exception e) {
            log.error("Error serializing profile fields", e);
        }

        GSResponse response = gigyaService.executeRequest("default", "accounts.setAccountInfo", cdcParams);

        try {
            if (response.getErrorCode() == 0) {
                log.info("setAccountInfo success");
            } else {
                log.error("company registration fail : " + response.getResponseText());
                log.warn("company registration context : " + session.getAttribute("uid"));
                String tmpErrorMsg = response.getData().containsKey("errorMessage") ? " : " + response.getData().getString("errorMessage") : "";
                redirectAttributes.addFlashAttribute("showErrorMsg", "company registration failed" + tmpErrorMsg);
            }
        } catch (Exception e) {
            log.error("Error during CDC company registration", e);
        }
    }

    // 24.07.04 홍정인 추가 - 회원가입 MultiFactorAuthentication 화면 예비 URL
    public ModelAndView signupMultiFactorAuthentication(String key, Map<String, Object> allParams, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/registration/multiFactorAuthentication";
        modelAndView.addObject("content", content);

        return modelAndView;
    }

    // 24.07.04 홍정인 추가 - 회원가입 Complete 화면 예비 URL
    public ModelAndView signupComplete(String param, Map<String, Object> allParams, HttpServletRequest request,HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("registration");

        String uid = (String) session.getAttribute("uid");
        JsonNode result = cdcTraitService.getCdcUser(uid,0);
        JsonNode data = result.path("data");
        JsonNode channels = data.path("channels");
        JsonNode sbaChannel = channels.get(param);
        String content = "";

        if (sbaChannel != null) {
            // "sba" 채널의 데이터를 가져오기
            String approvalStatus = sbaChannel.path("approvalStatus").asText();

            if("approved".equals(approvalStatus)) {
                content = "fragments/registration/registrationComplete";
            } else if("pending".equals(approvalStatus)) {
                content = "fragments/registration/registrationInProgress";
            } else {
                content = "fragments/registration/registrationInProgress";
            }
        } else {
            content = "fragments/registration/registrationInProgress";
        }

        modelAndView.addObject("content", content);
        modelAndView.addObject("channel", param);
        //session.invalidate();
        return modelAndView;
    }

    // 24.07.08 홍정인 추가 - 회원가입 email 화면 예비 URL
    public ModelAndView email(String key, Map<String, Object> allParams, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/registration/mailVerifyContent";

        modelAndView.addObject("content", content);

        return modelAndView;
    }

    // 24.07.08 홍정인 추가 - 회원가입 progess 화면 예비 URL
    public ModelAndView inProgress(String key, Map<String, Object> allParams, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/registration/registrationInProgress";

        modelAndView.addObject("content", content);

        return modelAndView;
    }

    public ChannelInvitation processInvitationFlow(String invitationToken, HttpSession session) {
        if (invitationToken == null) {
            return null;
        }

        // 초대 토큰으로 초대 정보 조회
        ChannelInvitation channelInvitation = channelInvitationRepository.findByToken(invitationToken)
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));

        // 세션에 초대 ID 저장
        session.setAttribute("invitation_id", channelInvitation.getId());

        // 한국 시간으로 현재 시간 가져오기
        ZonedDateTime koreaTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime localKoreaTime = koreaTime.toLocalDateTime();

        // 만료 시간이 null인 경우를 처리
        if (channelInvitation.getExpiry() == null) {
            throw new RuntimeException("Invitation expiry time is not set");
        }

        // 초대 만료 확인 (한국 시간 기준)
        Timestamp now = Timestamp.valueOf(localKoreaTime);
        if (channelInvitation.getExpiry().compareTo(now) < 0) {
            throw new RuntimeException("Invitation has expired at " + channelInvitation.getExpiry());
        }

        // 초대 상태 확인
        if (!"pending".equals(channelInvitation.getStatus())) {
            throw new RuntimeException("Invitation is not pending");
        }

        // 초대가 활성화되어 있으면 사용자 정보 가져오기
        JsonNode invitedUserData = cdcTraitService.getCdcUser(channelInvitation.getLoginUid(),0);

        if (invitedUserData.get("data").hasNonNull("accountID")) {
            session.setAttribute("uid", channelInvitation.getLoginUid());
            session.setAttribute("email", invitedUserData.get("profile").get("email").asText(""));
            session.setAttribute("loginId", invitedUserData.get("profile").get("username").asText(""));
            session.setAttribute("country_code", invitedUserData.get("profile").get("country").asText(""));
            session.setAttribute("secCountry", invitedUserData.get("profile").get("country").asText(""));
            session.setAttribute("channelType", invitedUserData.get("profile").get("country").asText(""));
            session.setAttribute("secSubsidiary", invitedUserData.get("data").get("subsidiary").asText(""));
        } else {
            throw new RuntimeException("Unable to retrieve user account information");
        }

        return channelInvitation;
    }

    public Map<String, Object> populateRegisterCompanyFromInvitation(ChannelInvitation channelInvitation, JsonNode invitedUserData,HttpSession session,String channelType) {
        Map<String, Object> registerCompany = new HashMap<>();
        boolean companyPopulated = false;

        switch (channelType) {
            case "VENDOR":
                // BTP에서 회사 데이터를 가져옵니다.
                BtpAccounts companyData = btpAccountsRepository.findByBpid(invitedUserData.get("data").get("accountID").asText())
                        .orElseThrow(() -> new RuntimeException("Company not found"));
                populateRegisterCompanyData(registerCompany, companyData,invitedUserData);
                companyPopulated = true;
                break;

            default:
                Map<String,Object> dataMap = new HashMap<String,Object>();
                dataMap.put("acctid",invitedUserData.get("data").get("accountID").asText());
                CpiResponseFieldMapping responseFieldMapping = CpiResponseFieldMapping.fromString((channelInvitation.getChannel()));
                List<Map<String, Object>> companyDataList = cpiApiService.accountSearch(
                        Collections.singletonMap("acctid", invitedUserData.get("data").get("accountID").asText()),
                        BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.accountSerachUrl"),
                        responseFieldMapping,
                        session
                );

                if (!companyDataList.isEmpty()) {
                    Map<String, Object> companyDataMap = companyDataList.get(0);
                    populateRegisterCompanyDataFromCmdm(registerCompany, companyDataMap,invitedUserData);
                    companyPopulated = true;
                }

                // CDC에서 회사 데이터를 가져옵니다.
                if (!companyPopulated) {
                    JsonNode cdcCompanyData = cdcTraitService.getB2bOrg(invitedUserData.get("data").get("accountID").asText());
                    if (cdcCompanyData != null && cdcCompanyData.hasNonNull("bpid")) {
                        populateRegisterCompanyDataFromCdc(registerCompany, cdcCompanyData,invitedUserData);
                        companyPopulated = true;
                    }
                }

                // BTP에서 회사 데이터를 가져옵니다.
                if (!companyPopulated) {
                    BtpAccounts fallbackCompanyData = btpAccountsRepository.findByBpid(invitedUserData.get("data").get("accountID").asText())
                            .orElse(null);
                    if (fallbackCompanyData != null) {
                        populateRegisterCompanyData(registerCompany, fallbackCompanyData,invitedUserData);
                        companyPopulated = true;
                    }
                }

                // BTP New Company에서 데이터를 가져옵니다.
                if (!companyPopulated) {
                    NewCompany newCompanyData = newCompanyRepository.findByBpid(invitedUserData.get("data").get("accountID").asText())
                            .orElse(null);
                    if (newCompanyData != null) {
                        populateRegisterCompanyDataFromNewCompany(registerCompany, newCompanyData,invitedUserData);
                        companyPopulated = true;
                    }
                }
                break;
        }

        if (!companyPopulated) {
            throw new RuntimeException("Company information could not be populated from any data source");
        }

        return registerCompany;
    }

    public void populateRegisterCompanyData(Map<String, Object> registerCompany, BtpAccounts companyData, JsonNode invitedUserData) {
        registerCompany.put("name", companyData.getName());
        registerCompany.put("country", invitedUserData.get("profile").get("country").asText());
        registerCompany.put("street_address", companyData.getStreetAddress());
        registerCompany.put("phonenumber1", companyData.getPhonenumber1());
        registerCompany.put("vendorcode", companyData.getVendorcode());
        registerCompany.put("city", companyData.getCity());
        registerCompany.put("zip_code", companyData.getZipCode());
        registerCompany.put("bizregno1", companyData.getBizregno1());
        registerCompany.put("regch", "");
        //registerCompany.put("isNewCompany", "false");
        registerCompany.put("channelType", "");
        registerCompany.put("orgSource", companyData.getSource());
        registerCompany.put("bpid", companyData.getBpid());
        registerCompany.put("type", companyData.getType());
    }

    public void populateRegisterCompanyDataFromCmdm(Map<String, Object> registerCompany, Map<String, Object> companyData,JsonNode invitedUserData) {
        registerCompany.put("name", companyData.getOrDefault("orgName", ""));
        registerCompany.put("country", invitedUserData.get("profile").get("country").asText());
        registerCompany.put("street_address", companyData.getOrDefault("street_address", ""));
        registerCompany.put("phonenumber1",  companyData.getOrDefault("phonenumber1", ""));
        registerCompany.put("vendorcode", companyData.getOrDefault("vendorcode", ""));
        registerCompany.put("city", companyData.getOrDefault("city", ""));
        registerCompany.put("zip_code", companyData.getOrDefault("zip_code", ""));
        registerCompany.put("bizregno1", companyData.getOrDefault("bizregno1", ""));
        registerCompany.put("regch", companyData.getOrDefault("regch", ""));
        //registerCompany.put("isNewCompany", "false");
        registerCompany.put("channelType","");
        registerCompany.put("orgSource", "");
        registerCompany.put("bpid", companyData.get("bpid"));
        registerCompany.put("type", companyData.get("type"));
    }

    public void populateRegisterCompanyDataFromCdc(Map<String, Object> registerCompany, JsonNode cdcCompanyData,JsonNode invitedUserData) {
        registerCompany.put("name", cdcCompanyData.get("orgName").asText(""));
        registerCompany.put("country", invitedUserData.get("profile").get("country").asText());
        registerCompany.put("street_address", cdcCompanyData.get("info").get("street_address").get(0).asText(""));
        registerCompany.put("phonenumber1", cdcCompanyData.get("info").get("phonenumber1").get(0).asText(""));
        registerCompany.put("vendorcode", cdcCompanyData.get("info").get("vendorcode").get(0).asText(""));
        registerCompany.put("city", cdcCompanyData.get("info").get("city").get(0).asText(""));
        registerCompany.put("zip_code", cdcCompanyData.get("info").get("zip_code").get(0).asText(""));
        registerCompany.put("bizregno1", cdcCompanyData.get("info").get("bizregno1").get(0).asText(""));
        registerCompany.put("regch", cdcCompanyData.get("info").get("regch").get(0).asText(""));
        //registerCompany.put("isNewCompany", "false");
        registerCompany.put("channelType", cdcCompanyData.get("info").get("channeltype").get(0).asText(""));
        registerCompany.put("orgSource", cdcCompanyData.get("source").asText(""));
        registerCompany.put("bpid", cdcCompanyData.get("bpid").asText(""));
        registerCompany.put("type", cdcCompanyData.get("type").asText(""));
    }

    public void populateRegisterCompanyDataFromNewCompany(Map<String, Object> registerCompany, NewCompany newCompanyData,JsonNode invitedUserData) {
        registerCompany.put("name", newCompanyData.getName());
        registerCompany.put("country", invitedUserData.get("profile").get("country").asText());
        registerCompany.put("street_address", newCompanyData.getStreetAddress());
        registerCompany.put("phonenumber1", newCompanyData.getPhoneNumber1());
        registerCompany.put("vendorcode", newCompanyData.getVendorCode());
        registerCompany.put("city", newCompanyData.getCity());
        registerCompany.put("zip_code", newCompanyData.getZipCode());
        registerCompany.put("bizregno1", newCompanyData.getBizRegNo1());
        registerCompany.put("regch", newCompanyData.getRegCh());
        //registerCompany.put("isNewCompany", "true");
        registerCompany.put("channelType", newCompanyData.getChannelType());
        registerCompany.put("orgSource", newCompanyData.getSource());
        registerCompany.put("bpid", newCompanyData.getBpid());
        registerCompany.put("type", newCompanyData.getType());
    }

    public ModelAndView company(String param, String t, HttpSession session,String channelType) {
        Channels channel = getChannel(param);
        ObjectMapper objectMapper = new ObjectMapper();
        List<SecServingCountry> countries = getSecServingCountries(param);
        //String isNewCompany = (String) (session.getAttribute("isNewCompany") != null ? session.getAttribute("isNewCompany") : "");
        Map<String, Object> registerCompany = getRegisterCompanyData(session);
        Map<String, Object> registerMt = getRegisterMtData();
        boolean companySearchEnabled = true;

        String regType;
        if (t != null) {
            regType = "invitation";
        } else if (session.getAttribute("convertYn") != null && "Y".equals(session.getAttribute("convertYn"))) {
            regType = "conversion";
        } else if (session.getAttribute("ssoAccessYn") != null && session.getAttribute("ssoAccessYn") != "") {
            regType = "ssoAccess";
        } else {
            regType = "self-registration";
        }
        session.setAttribute("regType", regType);

        // 초대 흐름 처리
        ChannelInvitation channelInvitation = null;
        if (t != null) {
            try {
                channelInvitation = processInvitationFlow(t, session);
                companySearchEnabled = false; // 초대인 경우 회사 검색을 비활성화

                // 초대 흐름 이후 회사 데이터 가져오기
                JsonNode invitedUserData = cdcTraitService.getCdcUser(channelInvitation.getLoginUid(),0);
                registerCompany = populateRegisterCompanyFromInvitation(channelInvitation, invitedUserData,session,channelType);
            } catch (RuntimeException e) {
                // 초대가 만료되거나 유효하지 않은 경우 만료 페이지로 리다이렉트
                return new ModelAndView("error/403", Map.of("message", "Invitation has expired. Please request a new invitation"));
            }
        }

        Map<String, Object> regCompanyData = session.getAttribute("registerCompany") != null ?
                (Map<String, Object>) session.getAttribute("registerCompany") : registerCompany;

        String[] channelTypes = {
                "Consultant",
                "Corporate Reseller",
                "Distributor",
                "Installer",
                "OEM",
                "Online Store",
                "Operator/Carrier",
                "Purchasing Association",
                "Reseller",
                "Retailer",
                "SI/VAR",
                "Wholesaler",
                "Contractor",
                "Architect",
                "Service Company",
                "Builder",
                "Regional Sales Home Appliance",
                "Student",
                "Field Trainer",
                "Sales Agent",
                "Call Center"
        };

        String[] industries = {
                "Automotive",
                "Communications",
                "Construction",
                "Corporate",
                "DOOH",
                "Education",
                "Finance",
                "Government",
                "Healthcare",
                "Hospitality",
                "Manufacturing",
                "Others",
                "Professional Services",
                "QSR",
                "Retail",
                "Transportation & Logistics",
                "Utility"
        };

        String secChannel = Arrays.asList("mmp", "e2e", "ets", "edo").contains(param) ? "partnerhub" : param;

        SecServingCountry secCountry = getSecServingCountry((String) registerCompany.get("country"), secChannel);

//        if (secCountry != null) {
//            session.setAttribute("secSubsidiary", secCountry.getSubsidiary());
//            session.setAttribute("secCountry", secCountry.getCountryCode());
//            session.setAttribute("usingDefaultSubsidiary", "1");
//        }

        List<SfdcProduct> sfdcProducts = getSfdcProducts((String) session.getAttribute("secSubsidiary"));
        List<SfdcProduct> climateSfdcProducts = getClimateSfdcProducts();
        Map<String, String> paramsMap = new HashMap<String,String>();
        boolean hideClimateSolution = shouldHideClimateSolution(param, (String) session.getAttribute("secCountry"));
        boolean isSeace = isCountrySeace((String) session.getAttribute("secCountry"));

        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/registration/registrationInformation";
        modelAndView.addObject("content", content);
        String popupSearchCompany = "popups/searchCompany";
        modelAndView.addObject("searchCompanyModal", popupSearchCompany);
        modelAndView.addObject("mobileHeader", searchService.getColumnInfoList());
        modelAndView.addObject("channel",param);
        modelAndView.addObject("channelCompanyType",(String) session.getAttribute("channelCompanyType"));
        modelAndView.addObject("convertAccountId", session.getAttribute("convertAccountId") != null ? session.getAttribute("convertAccountId") : "");
        Map<String, Object> companyObject = new HashMap<>();
        companyObject.put("channelString", channel.getExternalId());

        Object companyObj = session.getAttribute("companyObject");

        if(session.getAttribute("convertYn")!=null && "Y".equals((String) session.getAttribute("convertYn"))) {
            regCompanyData = (Map<String, Object>) session.getAttribute("convertObject");
        }
        else if (companyObj != null && companyObj instanceof Map) {
            Map<String, String> companyMap = (Map<String, String>) companyObj;
            regCompanyData.put("bpid", companyMap.getOrDefault("bpid", ""));
            regCompanyData.put("source", companyMap.getOrDefault("source", ""));
            regCompanyData.put("type", companyMap.getOrDefault("type", ""));
            regCompanyData.put("validStatus", companyMap.getOrDefault("validStatus", ""));
            regCompanyData.put("zip_code", companyMap.getOrDefault("zipCode", ""));
            regCompanyData.put("vendorcode", companyMap.getOrDefault("vendorcode", ""));

            List<ChannelAddField> addFields = channelAddFieldRepository.selectFieldList(param,"company");
            cdcTraitService.populateAccountObject(companyMap, regCompanyData, addFields, param);
        } else {
            // 새로운 동적 필드 세팅 로직
            paramsMap = (Map<String, String>) session.getAttribute("paramsMap");

            if (paramsMap != null && paramsMap.size() > 0) {
                List<ChannelAddField> dynamicFields = channelAddFieldRepository.selectFieldList(param, "company");
                for (ChannelAddField field : dynamicFields) {
                    String parameterId = field.getParameterId();
                    String elementId = field.getElementId();
                    String fieldValue = paramsMap.getOrDefault(parameterId, "");
                    regCompanyData.put(elementId, fieldValue);
                }
            }
        }



        companyObject.put("registerCompany", regCompanyData);
        companyObject.put("registerMagicInfo", registerMt);
        companyObject.put("registration_type", regType);
        //companyObject.put("companySearchEnabled", companySearchEnabled);
        companyObject.put("countries", countries);
        companyObject.put("isSeace", isSeace);
        companyObject.put("sfdcProducts", sfdcProducts);
        companyObject.put("channelTypes", channelTypes);
        companyObject.put("industries", industries);
        companyObject.put("climateSfdcProducts", climateSfdcProducts);
        companyObject.put("isUpdatePage", false);
        companyObject.put("isConvertFlow", session.getAttribute("convertFlow"));
        companyObject.put("hideClimateSolution", hideClimateSolution);
        companyObject.put("channel", param);
        companyObject.put("countryCodeId", session.getAttribute("countryCodeId"));

        Map<String, List<?>> fieldMap = cdcTraitService.generateFieldMap(param, objectMapper, true, true,false,paramsMap); // Company 데이터만 포함

        modelAndView.addObject("fieldMap", fieldMap);

        modelAndView.addObject("companyObject", companyObject);
        return modelAndView;
    }

    public List<ChannelAddField> parseOptions(List<ChannelAddField> fields, ObjectMapper objectMapper) {
        for (ChannelAddField field : fields) {
            // options 필드가 null이 아니고 비어 있지 않은 경우에만 파싱
            if (field.getOption() != null && !field.getOption().isEmpty()) {
                try {
                    // options 필드를 JSON 배열로 파싱
                    List<Map<String, String>> options = objectMapper.readValue(field.getOption(), new TypeReference<List<Map<String, String>>>(){});
                    field.setOptions(options);  // 파싱된 값을 새로운 필드에 설정
                } catch (Exception e) {
                    // 예외 처리: 파싱 실패 시 로깅
                    log.error(e.getMessage());
                }
            }
        }
        return fields;
    }

    public ModelAndView account(String param,String t,String channelCompanyType, HttpSession session, HttpServletRequest request, ModelAndView modelAndView,RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loginId") == null) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "session expired, please restart registration process");
            modelAndView.setViewName(request.getHeader("Referer"));
            return modelAndView;
        }

        if (session.getAttribute("convertUid") == null && session.getAttribute("uid") == null) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "session expired, please restart registration process");
            modelAndView.setViewName(request.getHeader("Referer"));
            return modelAndView;
        }

        String email = (String) session.getAttribute("loginId");
        String[] emailParts = email.split("@");
        List<String> domainArray = new ArrayList<>();
        domainArray.add(emailParts[1]);

        String countryChannel;
        if (Arrays.asList("e2e", "edo", "ets", "mmp").contains(param)) {
            countryChannel = "partnerhub";
        } else {
            countryChannel = param;
        }

        List<SecServingCountry> countries = secServingCountryRepository.selectCountriesByChannel(countryChannel);
        List<CommonCode> divisions = commonCodeRepository.findByHeader("BIZ_WITH_DEPT");//divisionRepository.findAllByOrderByNameEnAsc();
        String secCountry = (String) session.getAttribute("secCountry");
        SecServingCountry countryCode = secServingCountryRepository.selectCountryByChannelAndCountryCode(countryChannel, secCountry).orElse(null);
        String channelDisplayName="";
        String adminType="";
        String role="";

        List<String> subsidiaries;
        if (countryCode != null) {
            subsidiaries = subsidiaryRepository.findByCountryKeyOrderByNameEnAsc(countryCode.getCountryCode());
        } else {
            subsidiaries = subsidiaryRepository.findByCountryKeyOrderByNameEnAsc("999999999");
        }

        String uid = session.getAttribute("convertUid") != null
                ? session.getAttribute("convertUid").toString()
                : session.getAttribute("uid").toString();
        JsonNode accUser = cdcTraitService.getCdcUser(uid, 0);

        if (t != null) {
            try {
                Map<String, String> accountInvitationObject = new HashMap<>();
                Map<String, String> phoneInfo = extractWorkPhoneInfo(accUser);
                String countryWorkCode = phoneInfo.get("countryCode");
                String phoneNumber = phoneInfo.get("phoneNumber");

                // 초대 흐름 이후 회사 데이터 가져오기
                accountInvitationObject.put("salutation", accUser.get("data").path("salutation").asText(""));
                String locale = accUser.path("profile").path("locale").asText("");
                locale = "en".equals(locale) ? "en_US" : "ko".equals(locale) ? "ko_KR" : locale;
                accountInvitationObject.put("language", locale);
                accountInvitationObject.put("firstName", accUser.path("profile").path("firstName").asText(""));
                accountInvitationObject.put("lastName", accUser.path("profile").path("lastName").asText(""));
                accountInvitationObject.put("country_code_work", countryWorkCode);
                accountInvitationObject.put("work_phone", phoneNumber);
                accountInvitationObject.put("secDept", accUser.path("data").path("userDepartment").asText(""));
                accountInvitationObject.put("job_title", accUser.path("data").path("jobtitle").asText(""));
                session.setAttribute("accountObject",accountInvitationObject);
                //List<CommonCode> roleList = commonCodeRepository.selectRoleList("ROLE_CODE",accUser.path("data").path("channels").);
                //accountObject.put("roles", roleList);
                channelDisplayName = channelRepository.selectChannelDisplayName(param);
                ObjectNode channelDataNode = (ObjectNode) accUser.path("data").path("channels").path(param);
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> channelData = mapper.convertValue(channelDataNode, Map.class);
                Object adminTypeObj = channelData.get("adminType");
                adminType = String.valueOf(adminTypeObj);
                role = commonCodeRepository.selectRoleSearch("ROLE_CODE",adminType);

            } catch (RuntimeException e) {
                // 초대가 만료되거나 유효하지 않은 경우 만료 페이지로 리다이렉트
                return new ModelAndView("error/403", Map.of("message", "Invitation has expired. Please request a new invitation"));
            }
        }

        String mobilePhone = "";
        String workPhone = "";
        String mobilePhoneCode = "";
        String workPhoneCode = "";
        List<CommonCode> codeList = new ArrayList<>();

        /*if (accUser != null && accUser.get("profile") != null && accUser.get("profile").get("phones") != null) {
            JsonNode phonesNode = accUser.get("profile").get("phones");
            if (phonesNode.isArray()) {
                for (JsonNode userPhone : phonesNode) {
                    if (userPhone.get("type").asText().equals("mobile_phone")) {
                        mobilePhone = userPhone.get("number").asText();
                        if (session.getAttribute("country_code") != null) {
                            mobilePhoneCode = (String) session.getAttribute("country_code");
                            mobilePhone = mobilePhone.replace(mobilePhoneCode, "");
                        }
                        if (session.getAttribute("convertFlow") != null && (Boolean) session.getAttribute("convertFlow")) {
                            if (session.getAttribute("User_Mobilephone") != null) {
                                String[] mobilePhoneArray = ((String) session.getAttribute("User_Mobilephone")).split(" ");
                                mobilePhoneCode = mobilePhoneArray[0];
                                mobilePhone = mobilePhoneArray.length > 1 ? mobilePhoneArray[1] : "";
                            }
                        }
                    }

                    if (userPhone.get("type").asText().equals("work_phone")) {
                        workPhone = userPhone.get("number").asText();
                        if (session.getAttribute("country_code") != null) {
                            workPhoneCode = (String) session.getAttribute("country_code");
                            workPhone = workPhone.replace(workPhoneCode, "");
                        }
                        if (session.getAttribute("convertFlow") != null && (Boolean) session.getAttribute("convertFlow")) {
                            if (session.getAttribute("User_Workphone") != null) {
                                String[] workPhoneArray = ((String) session.getAttribute("User_Workphone")).split(" ");
                                workPhoneCode = workPhoneArray[0];
                                workPhone = workPhoneArray.length > 1 ? workPhoneArray[1] : "";
                            }
                        }
                    }
                }
            } else {
                if (phonesNode.get("type").asText().equals("mobile_phone")) {
                    mobilePhone = phonesNode.get("number").asText();
                    if (session.getAttribute("country_code") != null) {
                        mobilePhoneCode = (String) session.getAttribute("country_code");
                        mobilePhone = mobilePhone.replace(mobilePhoneCode, "");
                    }
                }
            }
        } else {
            codeList = commonCodeRepository.findByHeader("COUNTRY_CODE");
        }*/

        codeList = commonCodeRepository.findByHeader("COUNTRY_CODE");

        String workLocation = accUser != null
                ? accUser.path("profile").path("work").path("location").asText("")
                : "";

        String salutation = accUser != null
                ? accUser.path("data").path("salutation").asText("")
                : "";

        String subsidiary = accUser != null
                ? accUser.path("data").path("subsidiary").asText("")
                : "";

        String division = accUser != null
                ? accUser.path("data").path("division").asText("")
                : "";

        String secDept = accUser != null
                ? accUser.path("data").path("userDepartment").asText("")
                : "";

        List<Map<String, String>> languages = new ArrayList<>();

        Map<String, String> koreanLanguageMap = new HashMap<>();
        koreanLanguageMap.put("name", "Korean");
        koreanLanguageMap.put("value", "ko_KR");
        languages.add(koreanLanguageMap);

        Map<String, String> englishLanguageMap = new HashMap<>();
        englishLanguageMap.put("name", "English");
        englishLanguageMap.put("value", "en_US");
        languages.add(englishLanguageMap);
        /*for (Object[] rawLanguage : rawLanguages) {
            Map<String, String> languageMap = new HashMap<>();
            languageMap.put("language_name", (String) rawLanguage[0]);
            languageMap.put("language", ((String) rawLanguage[1]).substring(0, 2));
            languages.add(languageMap);
        }*/

        List<SecServingCountry> secCountries = secServingCountryRepository.selectCountriesByChannelExcludingSubsidiary(countryChannel);

        if (session.getAttribute("sfdcPresetCountry") == null && session.getAttribute("secCountry") != null) {
            session.setAttribute("sfdcPresetCountry", session.getAttribute("secCountry"));
        }

        String isConvertFlow = (session.getAttribute("convertFlow") != null && (Boolean) session.getAttribute("convertFlow")) ? "Y" : "N";

        Map<String, Object> accountObject = new HashMap<>();
        accountObject.put("loginId", session.getAttribute("loginId"));
        accountObject.put("presetEmail", emailParts[0]);
        accountObject.put("subsidiaries", subsidiaries);
        accountObject.put("accUser", accUser);
        accountObject.put("domainArray", domainArray);
        accountObject.put("mobilePhone", mobilePhone);
        accountObject.put("mobilePhoneCode", mobilePhoneCode);
        accountObject.put("workPhone", workPhone);
        accountObject.put("workPhoneCode", workPhoneCode);
        accountObject.put("workLocation", workLocation);
        accountObject.put("codes", codeList);
        accountObject.put("salutation", salutation);
        accountObject.put("subsidiary", subsidiary);
        accountObject.put("division", division);
        accountObject.put("divisions", divisions);
        accountObject.put("secDept", secDept);
        accountObject.put("secCountries", secCountries);
        accountObject.put("language", accUser.get("profile").get("languages"));
        accountObject.put("languages", languages);
        accountObject.put("isConvertFlow", isConvertFlow);
        accountObject.put("channelDisplayName", channelDisplayName);
        accountObject.put("role", role);
        Object accountObj = session.getAttribute("accountObject");
        if (accountObj != null && accountObj instanceof Map) {
            Map<String, String> accountMap = (Map<String, String>) accountObj;
            accountObject.put("salutation", accountMap.getOrDefault("salutation", ""));
            accountObject.put("language", accountMap.getOrDefault("language", ""));
            accountObject.put("firstName", accountMap.getOrDefault("firstName", ""));
            accountObject.put("lastName", accountMap.getOrDefault("lastName", ""));
            accountObject.put("password", accountMap.getOrDefault("password", ""));
            accountObject.put("password_confirmation", accountMap.getOrDefault("password_confirmation", ""));
            accountObject.put("country_code_work", accountMap.getOrDefault("country_code_work", ""));
            accountObject.put("work_phone", accountMap.getOrDefault("work_phone", ""));
            accountObject.put("secDept", accountMap.getOrDefault("secDept", ""));
            accountObject.put("job_title", accountMap.getOrDefault("job_title", ""));
            // channel_add_field 테이블에서 조회된 필드 목록을 가져옴
            List<ChannelAddField> addFields = channelAddFieldRepository.selectFieldList(param,"user");
            cdcTraitService.populateAccountObject(accountMap, accountObject, addFields, param);
        } else {
            // 새로운 동적 필드 세팅 로직
            Map<String, String> paramsMap = (Map<String, String>) session.getAttribute("paramsMap");

            if (paramsMap!=null && paramsMap.size()>0) {
                List<ChannelAddField> dynamicFields = channelAddFieldRepository.selectFieldList(param, "user");
                for (ChannelAddField field : dynamicFields) {
                    String parameterId = field.getParameterId();
                    String elementId = field.getElementId();
                    String fieldValue = paramsMap.getOrDefault(parameterId, "");
                    accountObject.put(elementId, fieldValue);
                }
            }
        }



        modelAndView.addObject("accountObject", accountObject);
        modelAndView.addObject("ssoAccessYn",(String) session.getAttribute("ssoAccessYn"));
        modelAndView.addObject("ciamConvertYn",(String) session.getAttribute("ciamConvertYn"));

        return modelAndView;
    }

    public RedirectView accountSubmit(Map<String, String> requestParams, String param, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            String userEmail;
            if (requestParams.containsKey("checkAltEmail") && Boolean.parseBoolean(requestParams.get("checkAltEmail"))) {
                userEmail = requestParams.get("altEmail");
            } else {
                userEmail = requestParams.get("loginUserId");// + "@" + requestParams.get("domain");
            }

            String[] checkDomain = userEmail.split("@");
            if (isDomainBanned(checkDomain[1])) {
                redirectAttributes.addFlashAttribute("showErrorMsg", "User update failed");
                return new RedirectView(request.getHeader("Referer")); // Replace with your form URL
            }

            Map<String, Object> profileFields = new HashMap<>();
            profileFields.put("email", userEmail.toLowerCase());

            List<Map<String, String>> phoneArray = new ArrayList<>();
            if (StringUtils.hasText(requestParams.get("mobile_phone"))) {
                phoneArray.add(Map.of("number", "+" + requestParams.get("country_code") + " " + requestParams.get("mobile_phone"), "type", "mobile_phone"));
            }
            if (StringUtils.hasText(requestParams.get("work_phone"))) {
                phoneArray.add(Map.of("number", "+" + requestParams.get("country_code_work") + " " + requestParams.get("work_phone"), "type", "work_phone"));
            }

            boolean isCompanyAdmin = requestParams.containsKey("applyAsCompanyAdmin") && Boolean.parseBoolean(requestParams.get("applyAsCompanyAdmin"));
            String adminReason = isCompanyAdmin ? requestParams.get("adminReason") : "";

            Map<String, String> userInputs = Map.of(
                    "locale", "language",
                    "firstName", "firstName",
                    "lastName", "lastName",
                    "address", "street",
                    "city", "city",
                    "state", "state",
                    "timezone", "timezone",
                    "zip", "postal_code"
            );

            userInputs.forEach((key, value) -> {
                String currentValue = requestParams.get(value);
                if (StringUtils.hasText(currentValue)) {
                    if (key.equals("locale")) {
                        // 언어 코드만 추출하여 profileFields에 추가
                        String languageCode = currentValue.contains("_") ? currentValue.split("_")[0] : currentValue;
                        profileFields.put(key, languageCode);
                    } else {
                        profileFields.put(key, currentValue);
                    }
                }
                if ((key.equals("firstName") || key.equals("lastName")) && StringUtils.hasText(currentValue)) {
                    profileFields.put(key, currentValue);
                }
            });



            if (!phoneArray.isEmpty()) {
                profileFields.put("phones", phoneArray);
            }
            if(session.getAttribute("secCountry") != null && "other".equals(session.getAttribute("secCountry"))) {
                profileFields.put("country", requestParams.get("country"));
                session.setAttribute("secCountry",requestParams.get("country"));
            } else {
                profileFields.put("country", session.getAttribute("secCountry"));
            }

            String companyName = (String) session.getAttribute("company_name");
            if (StringUtils.hasText(companyName)) {
                profileFields.put("work", Map.of("company", companyName));
            }

            if (profileFields.containsKey("locale")) {
                String locale = (String) profileFields.get("locale");
                // locale 값이 'en'이면 'en', 'ko'면 'ko', 나머지는 그대로 사용
                String languagesMap = "en_US".equals(locale) ? "en" : "ko_KR".equals(locale) ? "ko" : locale;
                profileFields.put("locale", languagesMap != null ? languagesMap : "en"); // 기본값은 'en'
            }

            // session에서 가져온 'tcpp_language' 값으로 languages 설정 ('en'을 'en_US', 'ko'를 'ko_KR'로 변환)
            String tcppLanguage = (String) session.getAttribute("tcpp_language");
            String languageMap = "en".equals(tcppLanguage) ? "en_US" : "ko".equals(tcppLanguage) ? "ko_KR" : tcppLanguage;
            profileFields.put("languages", languageMap != null ? languageMap : "en_US"); // 기본값은 'en_US'

            String secDept = requestParams.getOrDefault("secDept", "");
            String[] parts = secDept.split("%%%");
            String secDeptResult = parts[parts.length - 1];

            requestParams.put("secDept",secDeptResult);
            session.setAttribute("accountObject", requestParams);

            Map<String, Object> dataFields = new HashMap<>();
            dataFields.put("userDepartment", secDeptResult);
            dataFields.put("salutation", requestParams.get("salutation"));
            dataFields.put("division", requestParams.get("division"));
            dataFields.put("subsidiary", session.getAttribute("cmdmSubsidiary") != null ? session.getAttribute("cmdmSubsidiary") : requestParams.get("subsidiary"));
            dataFields.put("isCompanyAdmin", session.getAttribute("conversion_isCompanyAdmin") != null ? session.getAttribute("conversion_isCompanyAdmin") : isCompanyAdmin);

            if(session.getAttribute("channelUID")!=null) {
                dataFields.put("channels", Map.of(param, Map.of("approvalStatus", "pending","channelUID",session.getAttribute("channelUID"))));
            } else {
                dataFields.put("channels", Map.of(param, Map.of("approvalStatus", "pending","channelUID","")));
            }

            dataFields.put("jobtitle", requestParams.get("job_title"));
            dataFields.put("reasonCompanyAdmin", adminReason);
            dataFields.put("accountID", session.getAttribute("bpid"));

            //cdcDataCheck(param, dataFields, "accountSubmit", "subsidiary");

            ObjectMapper objectMapper = new ObjectMapper();
            GSResponse response = gigyaService.executeRequest(param, "accounts.setAccountInfo", Map.of(
                    "UID", (String) session.getAttribute("uid"),
                    "profile", objectMapper.writeValueAsString(profileFields),
                    "data", objectMapper.writeValueAsString(dataFields)
            ));

            log.info("update account: {}", response.getResponseText());

            if (response.getErrorCode() == 0) {
                session.setAttribute("subsidiary", requestParams.get("subsidiary"));
                session.setAttribute("division", requestParams.get("division"));
                session.setAttribute("department", requestParams.get("department"));
                session.setAttribute("language", requestParams.get("language"));
                session.setAttribute("country_code", requestParams.get("country_code"));
                session.setAttribute("loginId", requestParams.get("loginUserId"));
                session.setAttribute("newPwd", EncryptUtil.encryptData(requestParams.get("password")));

                if (!userEmail.equals(session.getAttribute("loginId"))) {
                    session.setAttribute("changedEmail", userEmail);

                    GSResponse emailResponse = gigyaService.executeRequest("default", "accounts.otp.sendCode", Map.of(
                            "lang", Locale.getDefault().getLanguage(),
                            "email", userEmail,
                            "status", "active"
                    ));

                    JsonNode emailResponseData = objectMapper.readTree(emailResponse.getResponseText());
                    session.setAttribute("vToken", EncryptUtil.encryptData(emailResponseData.get("vToken").asText()));

                    return new RedirectView("/registration/changeEmailVerify/" + param); // Replace with your change email verification URL
                } else {
                    return new RedirectView("/registration/consent"+ "?param=" + param); // Replace with your consent URL
                }
            } else if (response.getErrorCode() == 400006) {
                log.info("register account update failed 400006: {}", response.getResponseText());
                redirectAttributes.addFlashAttribute("showErrorMsg", "User account update failed");
                return new RedirectView(request.getHeader("Referer")); // Replace with your form URL
            } else {
                log.info("register account update failed: {}", response.getResponseText());
                redirectAttributes.addFlashAttribute("showErrorMsg", "User account update failed");
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

        Map<String, Object> returnArray = cdcTraitService.consentSelector((String) session.getAttribute("uid"), param, (String) session.getAttribute("tcpp_language"), subsidiary);

        List<Channels> channelTables = channelRepository.selectByChannelNameContaining(param);
        Channels channelTable = channelTables.isEmpty() ? null : channelTables.get(0);
        String channelDisplayName = channelTable != null ? channelTable.getChannelDisplayName() : "";
        String secCountry = (String) session.getAttribute("secCountry");

        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/registration/consent";
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


    public RedirectView mfaSubmit(String param, HttpServletRequest request, Map<String, String> payload,RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession();

        if (session.getAttribute("uid") == null || session.getAttribute("newPwd") == null) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "session expired, please restart registration process");
            return new RedirectView(request.getHeader("Referer"));
        }

        String decryptedPwd;
        try {
            decryptedPwd = EncryptUtil.decryptData((String) session.getAttribute("newPwd"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "session expired, please restart registration process");
            return new RedirectView(request.getHeader("Referer"));
        }

        /*boolean tfaSelected = request.getParameter("mafTfa") != null;
        boolean emailSelected = request.getParameter("mafEmail") != null;

        if (tfaSelected && emailSelected) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "Please select only 1 MFA method");
            return new RedirectView(request.getHeader("Referer"));
        }

        if (!tfaSelected && !emailSelected) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "Please select at least 1 MFA method");
            return new RedirectView(request.getHeader("Referer"));
        }*/

        Map<String, Object> dataFields = new HashMap<>();
        Map<String, Boolean> tfaMethods = new HashMap<>();

        tfaMethods.put("gigyaEmail", "mafEmail".equals(request.getParameter("mfa")));
        tfaMethods.put("gigyaTotp", "mafTfa".equals(request.getParameter("mfa")));

        dataFields.put("tfaMethods", tfaMethods);
        if(session.getAttribute("ssoAccessYn") == null && session.getAttribute("ciamConvertYn") == null) {
            dataFields.put("userStatus", "regSubmit");
        }

        Map<String, Object> params = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            params.put("data", objectMapper.writeValueAsString(dataFields));
            params.put("UID", session.getAttribute("uid"));

            if (session.getAttribute("convertUid") == null && session.getAttribute("ssoAccessYn") == null && session.getAttribute("ciamConvertYn") == null) {
                params.put("password", BeansUtil.getApplicationProperty("cdc.temp-reg-pwd"));
                params.put("newPassword", decryptedPwd);
            }

            Map<String, Object> profileFields = new HashMap<>();
            profileFields.put("username", session.getAttribute("loginId"));
            params.put("profile", objectMapper.writeValueAsString(profileFields));
            params.put("username", session.getAttribute("loginId"));

            if ("invitation".equals(session.getAttribute("regType"))) {
                params.put("isVerified", true);
            }
        } catch (JsonProcessingException e) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "Failed to process profile data");
            return new RedirectView(request.getHeader("Referer"));
        }

        GSResponse response = gigyaService.executeRequest(param, "accounts.setAccountInfo", params);

        if (session.getAttribute("convertLoginId") != null) {
            String convertLoginId = (String) session.getAttribute("convertLoginId");
            ChannelConversion convertUserOpt = channelConversionRepository.selectFirstByChannelAndConvertLoginIdAndStatus(param, convertLoginId);

            if (convertUserOpt != null) {
                convertUserOpt.setStatus("completed");
                convertUserOpt.setCompletionDate(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                channelConversionRepository.save(convertUserOpt);
            }
        }

        if (response.getErrorCode() == 0) {
            handleSuccessfulMFA(param, session, response);
            return new RedirectView("/registration/registerComplete"+ "?param=" + param);
        } else {
            redirectAttributes.addFlashAttribute("showErrorMsg", "MFA update failed");
            return new RedirectView("/registration/mfa" + "?param=" + param);
        }
    }

    public void handleSuccessfulMFA(String param, HttpSession session, GSResponse response) {

        String regType = (String) session.getAttribute("regType");
        String approvalType;
        // 일반 등록 또는 변환 흐름 처리
        if (session.getAttribute("regType") != null && "invitation".equals(regType)) {
            approvalType = "invitation";
        } else if (session.getAttribute("channelUID") != null) {
            approvalType = "conversion";
        } else if  (session.getAttribute("ssoAccessYn") != null && session.getAttribute("ssoAccessYn") != "") {
            approvalType = "ssoAccess";
        } else {
            approvalType = "registration";
        }
        cdcTraitService.getApprovalFlow(param, (String) session.getAttribute("uid"), approvalType, param, false);
    }

    private void clearSessionAttributes(HttpSession session) {
        session.removeAttribute("uid");
        session.removeAttribute("convertLoginId");
        session.removeAttribute("convertUid");
        session.removeAttribute("email");
        session.removeAttribute("secCountry");
        session.removeAttribute("loginId");
        session.removeAttribute("newChannel");
        session.removeAttribute("regToken");
        session.removeAttribute("newPwd");
        session.removeAttribute("attachment_file_json");
        session.removeAttribute("invitation_id");
        session.removeAttribute("products");
    }

    public Map<String, Object> resendEmailCode( Map<String, String> payload, HttpSession session) {
        Map<String, Object> params = new HashMap<>();
        params.put("lang", LocaleContextHolder.getLocale().getLanguage());
        params.put("email", payload.get("loginUserId"));
        params.put("status", "active");

        GSResponse response = gigyaService.executeRequest("default", "accounts.otp.sendCode", params);

        if (response.getErrorCode() == 0) {
            try {
                String vToken = response.getData().getString("vToken");
                Map<String, String> data = Map.of("vToken", vToken);
                session.setAttribute("vToken", EncryptUtil.encryptData(new ObjectMapper().writeValueAsString(data)));
                return Map.of("result", "ok", "msg", "verification code sent");
            } catch (JsonProcessingException e) {
                return Map.of("result", "error", "msg", "failed to process verification token");
            } catch (Exception e) {
                return Map.of("result", "error", "msg", "failed to retrieve verification token");
            }
        } else {
            return Map.of("result", "error", "msg", "failed to send verification code, please try again");
        }
    }

    public ModelAndView authVerify(String param) {
        return new ModelAndView("selfRegistration.authVerify").addObject("channel", param);
    }

    public RedirectView authVerifySubmit(String param, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession();
        String vToken;
        try {
            vToken = EncryptUtil.decryptData((String) session.getAttribute("vToken"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "Verification failed");
            return new RedirectView("/selfRegistration/authVerify/" + param);
        }

        String verificationCode = request.getParameter("verCode");

        Map<String, Object> params = new HashMap<>();
        params.put("code", verificationCode);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            params.put("vToken", objectMapper.readTree(vToken).get("vToken").asText());
        } catch (JsonProcessingException e) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "Verification token processing failed");
            return new RedirectView("/selfRegistration/authVerify/" + param);
        }

        GSResponse response = gigyaService.executeRequest(param, "accounts.auth.otp.verify", params);

        if (response.getErrorCode() == 0) {
            params.clear();
            params.put("UID", session.getAttribute("uid"));
            params.put("isVerified", true);

            Map<String, Object> dataFields = new HashMap<>();
            dataFields.put("userStatus", "regEmailVerified");
            ObjectMapper objectMapper = new ObjectMapper();
            String dataFieldsJSON;
            try {
                dataFieldsJSON = objectMapper.writeValueAsString(dataFields);
                if(session.getAttribute("ssoAccessYn") == null && session.getAttribute("ciamConvertYn") == null) {
                    params.put("data", dataFieldsJSON);
                }
            } catch (JsonProcessingException e) {
                log.error("Error processing JSON for data fields", e);
                redirectAttributes.addFlashAttribute("showErrorMsg", "Verification failed due to internal error");
                return new RedirectView("/selfRegistration/authVerify/" + param);
            }


            gigyaService.executeRequest(param, "accounts.setAccountInfo", params);

            redirectAttributes.addFlashAttribute("showErrorMsg", "Verification successful");
            return new RedirectView("/selfRegistration/registerComplete/" + param);
        } else {
            redirectAttributes.addFlashAttribute("showErrorMsg", "Verification failed");
            return new RedirectView("/selfRegistration/authVerify/" + param);
        }
    }

    public RedirectView consentSubmit(Map<String, String> requestParams, String param, HttpSession session, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        if("ecims".equals(param)) { //하드코딩
            return new RedirectView("/registration/mfa"+ "?param=" + param);
        }

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

            return new RedirectView("/registration/mfa"+ "?param=" + param);
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

    public RedirectView processSsoSignup(String channel, Map<String, String> payload, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String uid = (String) session.getAttribute("cdc_uid");
        JsonNode CDCUser = cdcTraitService.getCdcUser(uid, 0);

        String username = CDCUser.path("profile").path("username").asText("");

        if (!username.equals(payload.get("loginUserId"))) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "The extension is not possible if the username and email do not match. Please contact B2B CIAM.");
            return new RedirectView( request.getHeader("Referer"));
        }

        session.setAttribute("loginUserId",payload.get("loginUserId"));
        session.setAttribute("country",payload.get("country"));

        setupSessionAttributesBasedOnCountry(payload.get("country"), session);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            session.setAttribute("uid", (String) session.getAttribute("cdc_uid"));
            Map<String, Object> otpData = new HashMap<>();
            otpData.put("lang", Locale.getDefault());
            otpData.put("email", payload.get("loginUserId"));
            otpData.put("status", "active");

            GSResponse otpResponse = gigyaService.executeRequest(channel, "accounts.otp.sendCode", otpData);
            JsonNode otpResponseJson = objectMapper.readTree(otpResponse.getResponseText());
            if (otpResponseJson.get("vToken") == null || otpResponseJson.get("vToken").isEmpty()) {
                //에러처리
            }

            Map<String, Object> data = new HashMap<>();
            data.put("vToken", otpResponseJson.get("vToken"));
            String jsonData = "";
            // ObjectMapper를 사용하여 Map을 JSON으로 변환
            try {
                jsonData = objectMapper.writeValueAsString(data);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage()); // JSON 변환 실패 처리
            }

            // 암호화 수행
            String encryptedToken = EncryptUtil.encryptData(jsonData);
            session.setAttribute("vToken", encryptedToken);
            if (otpResponse.getErrorCode() == 0) {

                //session.setAttribute("email", payload.get("email"));
                session.setAttribute("loginId", payload.get("loginUserId"));
                return new RedirectView("/registration/signupVerify" + "?param=" + channel);
            } else {
                // 에러처리
                //return back()->withFlashDanger('authVerifyFailed');
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON response", e);
            redirectAttributes.addFlashAttribute("showErrorMsg", "Error processing registration response.");
        }
        return new RedirectView("/registration/signupVerify" + "?param=" + channel);
    }

    public ModelAndView partnerCompany(String param, String t, HttpSession session,String channelType) {
        Channels channel = getChannel(param);
        List<SecServingCountry> countries = getSecServingCountries(param);
        //String isNewCompany = (String) (session.getAttribute("isNewCompany") != null ? session.getAttribute("isNewCompany") : "");
        Map<String, Object> registerCompany = getRegisterCompanyData(session);
        Map<String, Object> registerMt = getRegisterMtData();
        boolean companySearchEnabled = true;

        String regType;
        if (t != null) {
            regType = "invitation";
        } else if (session.getAttribute("convertYn") != null && "Y".equals(session.getAttribute("convertYn"))) {
            regType = "conversion";
        } else if (session.getAttribute("ssoAccessYn") != null && session.getAttribute("ssoAccessYn") != "") {
            regType = "ssoAccess";
        } else {
            regType = "self-registration";
        }
        session.setAttribute("regType", regType);

        // 초대 흐름 처리
        ChannelInvitation channelInvitation = null;
        if (t != null) {
            try {
                channelInvitation = processInvitationFlow(t, session);
                companySearchEnabled = false; // 초대인 경우 회사 검색을 비활성화

                // 초대 흐름 이후 회사 데이터 가져오기
                JsonNode invitedUserData = cdcTraitService.getCdcUser(channelInvitation.getLoginUid(),0);
                registerCompany = populateRegisterCompanyFromInvitation(channelInvitation, invitedUserData,session,channelType);
            } catch (RuntimeException e) {
                // 초대가 만료되거나 유효하지 않은 경우 만료 페이지로 리다이렉트
                return new ModelAndView("error/403", Map.of("message", "Invitation has expired. Please request a new invitation"));
            }
        }

        Map<String, Object> regCompanyData = session.getAttribute("registerCompany") != null ?
                (Map<String, Object>) session.getAttribute("registerCompany") : registerCompany;

        String[] channelTypes = {
                "Consultant",
                "Corporate Reseller",
                "Distributor",
                "Installer",
                "OEM",
                "Online Store",
                "Operator/Carrier",
                "Purchasing Association",
                "Reseller",
                "Retailer",
                "SI/VAR",
                "Wholesaler",
                "Contractor",
                "Architect",
                "Service Company",
                "Builder",
                "Regional Sales Home Appliance",
                "Student",
                "Field Trainer",
                "Sales Agent",
                "Call Center"
        };

        String[] industries = {
                "Automotive",
                "Communications",
                "Construction",
                "Corporate",
                "DOOH",
                "Education",
                "Finance",
                "Government",
                "Healthcare",
                "Hospitality",
                "Manufacturing",
                "Others",
                "Professional Services",
                "QSR",
                "Retail",
                "Transportation & Logistics",
                "Utility"
        };

        String secChannel = Arrays.asList("mmp", "e2e", "ets", "edo").contains(param) ? "partnerhub" : param;

        SecServingCountry secCountry = getSecServingCountry((String) registerCompany.get("country"), secChannel);

//        if (secCountry != null) {
//            session.setAttribute("secSubsidiary", secCountry.getSubsidiary());
//            session.setAttribute("secCountry", secCountry.getCountryCode());
//            session.setAttribute("usingDefaultSubsidiary", "1");
//        }

        List<SfdcProduct> sfdcProducts = getSfdcProducts((String) session.getAttribute("secSubsidiary"));
        List<SfdcProduct> climateSfdcProducts = getClimateSfdcProducts();
        Map<String, String> paramsMap = new HashMap<String,String>();

        boolean hideClimateSolution = shouldHideClimateSolution(param, (String) session.getAttribute("secCountry"));
        boolean isSeace = isCountrySeace((String) session.getAttribute("secCountry"));

        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/registration/registrationPartnerInformation";
        modelAndView.addObject("content", content);
        String popupSearchCompany = "popups/partnerSearchCompany";
        modelAndView.addObject("searchCompanyModal", popupSearchCompany);
        modelAndView.addObject("mobileHeader", searchService.getColumnInfoList());
        modelAndView.addObject("channel",param);
        modelAndView.addObject("channelCompanyType",(String) session.getAttribute("channelCompanyType"));
        modelAndView.addObject("convertAccountId", session.getAttribute("convertAccountId") != null ? session.getAttribute("convertAccountId") : "");
        Map<String, Object> companyObject = new HashMap<>();
        companyObject.put("channelString", channel.getExternalId());
        Object companyObj = session.getAttribute("companyObject");

        if(session.getAttribute("convertYn")!=null && "Y".equals((String) session.getAttribute("convertYn"))) {
            regCompanyData = (Map<String, Object>) session.getAttribute("convertObject");
        }
        else if (companyObj != null && companyObj instanceof Map) {
            Map<String, String> companyMap = (Map<String, String>) companyObj;
            regCompanyData.put("bpid", companyMap.getOrDefault("bpid", ""));
            regCompanyData.put("source", companyMap.getOrDefault("source", ""));
            regCompanyData.put("type", companyMap.getOrDefault("type", ""));
            regCompanyData.put("validStatus", companyMap.getOrDefault("validStatus", ""));
            regCompanyData.put("zip_code", companyMap.getOrDefault("zipCode", ""));
            regCompanyData.put("vendorcode", companyMap.getOrDefault("vendorcode", ""));
            regCompanyData.put("dunsno", companyMap.getOrDefault("dunsno", ""));

            List<ChannelAddField> addFields = channelAddFieldRepository.selectFieldList(param,"company");
            cdcTraitService.populateAccountObject(companyMap, regCompanyData, addFields, param);
        } else {
            // 새로운 동적 필드 세팅 로직
            paramsMap = (Map<String, String>) session.getAttribute("paramsMap");

            if (paramsMap != null && paramsMap.size() > 0) {
                List<ChannelAddField> dynamicFields = channelAddFieldRepository.selectFieldList(param, "company");
                for (ChannelAddField field : dynamicFields) {
                    String parameterId = field.getParameterId();
                    String elementId = field.getElementId();
                    String fieldValue = paramsMap.getOrDefault(parameterId, "");
                    regCompanyData.put(elementId, fieldValue);
                }
            }
        }

        companyObject.put("registerCompany", regCompanyData);
        companyObject.put("registerMagicInfo", registerMt);
        companyObject.put("registration_type", regType);
        //companyObject.put("companySearchEnabled", companySearchEnabled);
        companyObject.put("countries", countries);
        companyObject.put("isSeace", isSeace);
        companyObject.put("sfdcProducts", sfdcProducts);
        companyObject.put("channelTypes", channelTypes);
        companyObject.put("industries", industries);
        companyObject.put("climateSfdcProducts", climateSfdcProducts);
        companyObject.put("isUpdatePage", false);
        companyObject.put("isConvertFlow", session.getAttribute("convertFlow"));
        companyObject.put("hideClimateSolution", hideClimateSolution);
        companyObject.put("channel", param);
        companyObject.put("countryCodeId", session.getAttribute("countryCodeId"));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<?>> fieldMap = new HashMap<String, List<?>>();
        if(session.getAttribute("convertYn")!=null && "Y".equals((String) session.getAttribute("convertYn"))) {
            fieldMap  = cdcTraitService.generateFieldMap(param, objectMapper, true, true,true,paramsMap); // Company 데이터만 포함
        } else {
            fieldMap  = cdcTraitService.generateFieldMap(param, objectMapper, true, true,false,paramsMap); // Company 데이터만 포함
        }

        modelAndView.addObject("fieldMap", fieldMap);

        modelAndView.addObject("companyObject", companyObject);
        return modelAndView;
    }

    public ModelAndView partnerAccount(String param, String t,String channelCompanyType,HttpSession session, HttpServletRequest request, ModelAndView modelAndView, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loginId") == null) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "session expired, please restart registration process");
            modelAndView.setViewName(request.getHeader("Referer"));
            return modelAndView;
        }

        if (session.getAttribute("convertUid") == null && session.getAttribute("uid") == null) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "session expired, please restart registration process");
            modelAndView.setViewName(request.getHeader("Referer"));
            return modelAndView;
        }

        String email = (String) session.getAttribute("loginId");
        String[] emailParts = email.split("@");
        List<String> domainArray = new ArrayList<>();
        domainArray.add(emailParts[1]);

        String countryChannel;
        if (Arrays.asList("e2e", "edo", "ets", "mmp").contains(param)) {
            countryChannel = "partnerhub";
        } else {
            countryChannel = param;
        }

        List<SecServingCountry> countries = secServingCountryRepository.selectCountriesByChannel(countryChannel);
        List<CommonCode> divisions = commonCodeRepository.findByHeader("BIZ_WITH_DEPT");//divisionRepository.findAllByOrderByNameEnAsc();
        String secCountry = (String) session.getAttribute("secCountry");
        SecServingCountry countryCode = secServingCountryRepository.selectCountryByChannelAndCountryCode(countryChannel, secCountry).orElse(null);
        String channelDisplayName="";
        String adminType="";
        String role="";

        List<String> subsidiaries;
        if (countryCode != null) {
            subsidiaries = subsidiaryRepository.findByCountryKeyOrderByNameEnAsc(countryCode.getCountryCode());
        } else {
            subsidiaries = subsidiaryRepository.findByCountryKeyOrderByNameEnAsc("999999999");
        }

        String uid = session.getAttribute("convertUid") != null
                ? session.getAttribute("convertUid").toString()
                : session.getAttribute("uid").toString();
        JsonNode accUser = cdcTraitService.getCdcUser(uid, 0);

        if (t != null) {
            try {
                Map<String, String> accountInvitationObject = new HashMap<>();
                Map<String, String> phoneInfo = extractWorkPhoneInfo(accUser);
                String countryWorkCode = phoneInfo.get("countryCode");
                String phoneNumber = phoneInfo.get("phoneNumber");

                // 초대 흐름 이후 회사 데이터 가져오기
                accountInvitationObject.put("salutation", accUser.get("data").path("salutation").asText(""));
                String locale = accUser.path("profile").path("locale").asText("");
                locale = "en".equals(locale) ? "en_US" : "ko".equals(locale) ? "ko_KR" : locale;
                accountInvitationObject.put("language", locale);
                accountInvitationObject.put("firstName", accUser.path("profile").path("firstName").asText(""));
                accountInvitationObject.put("lastName", accUser.path("profile").path("lastName").asText(""));
                accountInvitationObject.put("country_code_work", countryWorkCode);
                accountInvitationObject.put("work_phone", phoneNumber);
                accountInvitationObject.put("secDept", accUser.path("data").path("userDepartment").asText(""));
                accountInvitationObject.put("job_title", accUser.path("data").path("jobtitle").asText(""));
                session.setAttribute("accountObject",accountInvitationObject);
                //List<CommonCode> roleList = commonCodeRepository.selectRoleList("ROLE_CODE",accUser.path("data").path("channels").);
                //accountObject.put("roles", roleList);
                channelDisplayName = channelRepository.selectChannelDisplayName(param);
                ObjectNode channelDataNode = (ObjectNode) accUser.path("data").path("channels").path(param);
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> channelData = mapper.convertValue(channelDataNode, Map.class);
                Object adminTypeObj = channelData.get("adminType");
                adminType = String.valueOf(adminTypeObj);
                role = commonCodeRepository.selectRoleSearch("ROLE_CODE",adminType);

            } catch (RuntimeException e) {
                // 초대가 만료되거나 유효하지 않은 경우 만료 페이지로 리다이렉트
                return new ModelAndView("error/403", Map.of("message", "Invitation has expired. Please request a new invitation"));
            }
        }

        String mobilePhone = "";
        String workPhone = "";
        String mobilePhoneCode = "";
        String workPhoneCode = "";
        List<CommonCode> codeList = new ArrayList<>();

        /*if (accUser != null && accUser.get("profile") != null && accUser.get("profile").get("phones") != null) {
            JsonNode phonesNode = accUser.get("profile").get("phones");
            if (phonesNode.isArray()) {
                for (JsonNode userPhone : phonesNode) {
                    if (userPhone.get("type").asText().equals("mobile_phone")) {
                        mobilePhone = userPhone.get("number").asText();
                        if (session.getAttribute("country_code") != null) {
                            mobilePhoneCode = (String) session.getAttribute("country_code");
                            mobilePhone = mobilePhone.replace(mobilePhoneCode, "");
                        }
                        if (session.getAttribute("convertFlow") != null && (Boolean) session.getAttribute("convertFlow")) {
                            if (session.getAttribute("User_Mobilephone") != null) {
                                String[] mobilePhoneArray = ((String) session.getAttribute("User_Mobilephone")).split(" ");
                                mobilePhoneCode = mobilePhoneArray[0];
                                mobilePhone = mobilePhoneArray.length > 1 ? mobilePhoneArray[1] : "";
                            }
                        }
                    }

                    if (userPhone.get("type").asText().equals("work_phone")) {
                        workPhone = userPhone.get("number").asText();
                        if (session.getAttribute("country_code") != null) {
                            workPhoneCode = (String) session.getAttribute("country_code");
                            workPhone = workPhone.replace(workPhoneCode, "");
                        }
                        if (session.getAttribute("convertFlow") != null && (Boolean) session.getAttribute("convertFlow")) {
                            if (session.getAttribute("User_Workphone") != null) {
                                String[] workPhoneArray = ((String) session.getAttribute("User_Workphone")).split(" ");
                                workPhoneCode = workPhoneArray[0];
                                workPhone = workPhoneArray.length > 1 ? workPhoneArray[1] : "";
                            }
                        }
                    }
                }
            } else {
                if (phonesNode.get("type").asText().equals("mobile_phone")) {
                    mobilePhone = phonesNode.get("number").asText();
                    if (session.getAttribute("country_code") != null) {
                        mobilePhoneCode = (String) session.getAttribute("country_code");
                        mobilePhone = mobilePhone.replace(mobilePhoneCode, "");
                    }
                }
            }
        } else {
            codeList = commonCodeRepository.findByHeader("COUNTRY_CODE");
        }*/

        codeList = commonCodeRepository.findByHeader("COUNTRY_CODE");

        String workLocation = accUser != null
                ? accUser.path("profile").path("work").path("location").asText("")
                : "";

        String salutation = accUser != null
                ? accUser.path("data").path("salutation").asText("")
                : "";

        String subsidiary = accUser != null
                ? accUser.path("data").path("subsidiary").asText("")
                : "";

        String division = accUser != null
                ? accUser.path("data").path("division").asText("")
                : "";

        String secDept = accUser != null
                ? accUser.path("data").path("userDepartment").asText("")
                : "";

        List<Map<String, String>> languages = new ArrayList<>();

        Map<String, String> koreanLanguageMap = new HashMap<>();
        koreanLanguageMap.put("name", "Korean");
        koreanLanguageMap.put("value", "ko_KR");
        languages.add(koreanLanguageMap);

        Map<String, String> englishLanguageMap = new HashMap<>();
        englishLanguageMap.put("name", "English");
        englishLanguageMap.put("value", "en_US");
        languages.add(englishLanguageMap);
        /*for (Object[] rawLanguage : rawLanguages) {
            Map<String, String> languageMap = new HashMap<>();
            languageMap.put("language_name", (String) rawLanguage[0]);
            languageMap.put("language", ((String) rawLanguage[1]).substring(0, 2));
            languages.add(languageMap);
        }*/

        List<SecServingCountry> secCountries = secServingCountryRepository.selectCountriesByChannelExcludingSubsidiary(countryChannel);

        if (session.getAttribute("sfdcPresetCountry") == null && session.getAttribute("secCountry") != null) {
            session.setAttribute("sfdcPresetCountry", session.getAttribute("secCountry"));
        }

        String isConvertFlow = (session.getAttribute("convertFlow") != null && (Boolean) session.getAttribute("convertFlow")) ? "Y" : "N";

        Map<String, Object> accountObject = new HashMap<>();
        accountObject.put("loginId", session.getAttribute("loginId"));
        accountObject.put("presetEmail", emailParts[0]);
        accountObject.put("subsidiaries", subsidiaries);
        accountObject.put("accUser", accUser);
        accountObject.put("domainArray", domainArray);
        accountObject.put("mobilePhone", mobilePhone);
        accountObject.put("mobilePhoneCode", mobilePhoneCode);
        accountObject.put("workPhone", workPhone);
        accountObject.put("workPhoneCode", workPhoneCode);
        accountObject.put("workLocation", workLocation);
        accountObject.put("codes", codeList);
        accountObject.put("salutation", salutation);
        accountObject.put("subsidiary", subsidiary);
        accountObject.put("division", division);
        accountObject.put("divisions", divisions);
        accountObject.put("secDept", secDept);
        accountObject.put("secCountries", secCountries);
        accountObject.put("language", accUser.get("profile").get("languages"));
        accountObject.put("languages", languages);
        accountObject.put("isConvertFlow", isConvertFlow);
        accountObject.put("channelDisplayName", channelDisplayName);
        accountObject.put("role", role);
        Object accountObj = session.getAttribute("accountObject");

        if (accountObj != null && accountObj instanceof Map) {
            Map<String, String> accountMap = (Map<String, String>) accountObj;
            accountObject.put("salutation", accountMap.getOrDefault("salutation", ""));
            accountObject.put("language", accountMap.getOrDefault("language", ""));
            accountObject.put("firstName", accountMap.getOrDefault("firstName", ""));
            accountObject.put("lastName", accountMap.getOrDefault("lastName", ""));
            accountObject.put("password", accountMap.getOrDefault("password", ""));
            accountObject.put("password_confirmation", accountMap.getOrDefault("password_confirmation", ""));
            accountObject.put("country_code_work", accountMap.getOrDefault("country_code_work", ""));
            accountObject.put("work_phone", accountMap.getOrDefault("work_phone", ""));
            accountObject.put("secDept", accountMap.getOrDefault("secDept", ""));
            accountObject.put("job_title", accountMap.getOrDefault("job_title", ""));

            List<ChannelAddField> addFields = channelAddFieldRepository.selectFieldList(param,"user");
            cdcTraitService.populateAccountObject(accountMap, accountObject, addFields, param);

        } else {
            // 새로운 동적 필드 세팅 로직
            Map<String, String> paramsMap = (Map<String, String>) session.getAttribute("paramsMap");

            if (paramsMap!=null && paramsMap.size()>0) {
                List<ChannelAddField> dynamicFields = channelAddFieldRepository.selectFieldList(param, "user");
                for (ChannelAddField field : dynamicFields) {
                    String parameterId = field.getParameterId();
                    String elementId = field.getElementId();
                    String fieldValue = paramsMap.getOrDefault(parameterId, "");
                    accountObject.put(elementId, fieldValue);
                }
            }
        }

        modelAndView.addObject("accountObject", accountObject);
        modelAndView.addObject("ssoAccessYn",(String) session.getAttribute("ssoAccessYn"));
        modelAndView.addObject("ciamConvertYn",(String) session.getAttribute("ciamConvertYn"));

        return modelAndView;
    }

    public void customerCompanySubmit(Map<String, String> requestParams, String param, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
//        Map<String,Object> companyDupCheckReq = new HashMap<String,Object>();
//        companyDupCheckReq.put("filter_bizregno1",requestParams.getOrDefault("bizregno1",""));
//        companyDupCheckReq.put("filter_dunsno",requestParams.getOrDefault("dunsno",""));
//        companyDupCheckReq.put("name",requestParams.getOrDefault("name",""));
//        companyDupCheckReq.put("channel",param);
//        Map<String, Object> response = cpiApiService.partnerCompanyDuplicateCheck(companyDupCheckReq, session);
//
//        if ("duplicate".equals(response.get("result"))) {
//            String errorMessage = (String) response.get("msg");
//            redirectAttributes.addFlashAttribute("shohCompanyDuplicateMsg", errorMessage);
//            return;
//        }

        List<String> industryArray = new ArrayList<>();
        if (requestParams.containsKey("industryType")) {
            industryArray.add(requestParams.get("industryType"));
        }
        if (requestParams.containsKey("industry_2")) {
            industryArray.add(requestParams.get("industry_2"));
        }
        if (requestParams.containsKey("industry_3")) {
            industryArray.add(requestParams.get("industry_3"));
        }

        // Prepare register company data
        Map<String, Object> registerCompany = prepareRegisterCompanyData(requestParams, session, industryArray);
        session.setAttribute("registerCompany", registerCompany);

        // Session management
        if (requestParams.containsKey("isNewCompany")) {
            session.setAttribute("isNewCompany", requestParams.get("isNewCompany"));
        }

        if (requestParams.containsKey("products") && requestParams.get("products") != null && !requestParams.get("products").isEmpty()) {
            session.setAttribute("products", requestParams.get("products"));
        }

        // Handle company registration logic
        String usedBpid = handleCompanyRegistration(requestParams, industryArray, registerCompany,param,"customer");

        session.setAttribute("bpid", usedBpid);
        session.setAttribute("company_name", requestParams.get("name"));
        session.setAttribute("companyObject", requestParams);
        processCompanyRegistration(requestParams, param, session, usedBpid,redirectAttributes);

        // Additional validation logic
        /*if (validateRequest(requestParams, registerCompany, param)) {
            // Proceed with company registration
            processCompanyRegistration(requestParams, param, session, usedBpid,redirectAttributes);
        } else {
            // Handle validation errors
            redirectAttributes.addFlashAttribute("showErrorMsg", "Validation failed");
        }*/
    }

    public RedirectView customerAccountSubmit(Map<String, String> requestParams, String param, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            String userEmail;
            if (requestParams.containsKey("checkAltEmail") && Boolean.parseBoolean(requestParams.get("checkAltEmail"))) {
                userEmail = requestParams.get("altEmail");
            } else {
                userEmail = requestParams.get("loginUserId");// + "@" + requestParams.get("domain");
            }

            String[] checkDomain = userEmail.split("@");
            if (isDomainBanned(checkDomain[1])) {
                redirectAttributes.addFlashAttribute("showErrorMsg", "User update failed");
                return new RedirectView(request.getHeader("Referer")); // Replace with your form URL
            }

            Map<String, Object> profileFields = new HashMap<>();
            profileFields.put("email", userEmail.toLowerCase());

            List<Map<String, String>> phoneArray = new ArrayList<>();
            if (StringUtils.hasText(requestParams.get("mobile_phone"))) {
                phoneArray.add(Map.of("number", "+" + requestParams.get("country_code") + " " + requestParams.get("mobile_phone"), "type", "mobile_phone"));
            }
            if (StringUtils.hasText(requestParams.get("work_phone"))) {
                phoneArray.add(Map.of("number", "+" + requestParams.get("country_code_work") + " " + requestParams.get("work_phone"), "type", "work_phone"));
            }

            boolean isCompanyAdmin = requestParams.containsKey("applyAsCompanyAdmin") && Boolean.parseBoolean(requestParams.get("applyAsCompanyAdmin"));
            String adminReason = isCompanyAdmin ? requestParams.get("adminReason") : "";

            Map<String, String> userInputs = Map.of(
                    "locale", "language",
                    "firstName", "firstName",
                    "lastName", "lastName",
                    "address", "street",
                    "city", "city",
                    "state", "state",
                    "timezone", "timezone",
                    "zip", "postal_code"
            );

            userInputs.forEach((key, value) -> {
                String currentValue = requestParams.get(value);
                if (StringUtils.hasText(currentValue)) {
                    if (key.equals("locale")) {
                        // 언어 코드만 추출하여 profileFields에 추가
                        String languageCode = currentValue.contains("_") ? currentValue.split("_")[0] : currentValue;
                        profileFields.put(key, languageCode);
                    } else {
                        profileFields.put(key, currentValue);
                    }
                }
                if ((key.equals("firstName") || key.equals("lastName")) && StringUtils.hasText(currentValue)) {
                    profileFields.put(key, currentValue);
                }
            });



            if (!phoneArray.isEmpty()) {
                profileFields.put("phones", phoneArray);
            }
            profileFields.put("country", session.getAttribute("secCountry"));

            String companyName = (String) session.getAttribute("company_name");
            if (StringUtils.hasText(companyName)) {
                profileFields.put("work", Map.of("company", companyName));
            }

            if (profileFields.containsKey("locale")) {
                String locale = (String) profileFields.get("locale");
                // locale 값이 'en'이면 'en', 'ko'면 'ko', 나머지는 그대로 사용
                String languagesMap = "en_US".equals(locale) ? "en" : "ko_KR".equals(locale) ? "ko" : locale;
                profileFields.put("locale", languagesMap != null ? languagesMap : "en"); // 기본값은 'en'
            }

            // session에서 가져온 'tcpp_language' 값으로 languages 설정 ('en'을 'en_US', 'ko'를 'ko_KR'로 변환)
            String tcppLanguage = (String) session.getAttribute("tcpp_language");
            String languageMap = "en".equals(tcppLanguage) ? "en_US" : "ko".equals(tcppLanguage) ? "ko_KR" : tcppLanguage;
            profileFields.put("languages", languageMap != null ? languageMap : "en_US"); // 기본값은 'en_US'

            String secDept = requestParams.getOrDefault("secDept", "");
            String[] parts = secDept.split("%%%");
            String secDeptResult = parts[parts.length - 1];

            requestParams.put("secDept",secDeptResult);
            session.setAttribute("accountObject", requestParams);

            Map<String, Object> dataFields = new HashMap<>();
            dataFields.put("userDepartment", secDeptResult);
            dataFields.put("salutation", requestParams.get("salutation"));
            dataFields.put("division", requestParams.get("division"));
            dataFields.put("subsidiary", session.getAttribute("secSubsidiary") != null ? session.getAttribute("secSubsidiary") : requestParams.get("subsidiary"));
            dataFields.put("isCompanyAdmin", session.getAttribute("conversion_isCompanyAdmin") != null ? session.getAttribute("conversion_isCompanyAdmin") : isCompanyAdmin);

            if(session.getAttribute("channelUID")!=null) {
                dataFields.put("channels", Map.of(param, Map.of("approvalStatus", "pending","channelUID",session.getAttribute("channelUID"))));
            } else {
                dataFields.put("channels", Map.of(param, Map.of("approvalStatus", "pending","channelUID","")));
            }

            dataFields.put("jobtitle", requestParams.get("job_title"));
            dataFields.put("reasonCompanyAdmin", adminReason);
            dataFields.put("accountID", session.getAttribute("bpid"));

            //cdcDataCheck(param, dataFields, "accountSubmit", "subsidiary");

            ObjectMapper objectMapper = new ObjectMapper();
            GSResponse response = gigyaService.executeRequest(param, "accounts.setAccountInfo", Map.of(
                    "UID", (String) session.getAttribute("uid"),
                    "profile", objectMapper.writeValueAsString(profileFields),
                    "data", objectMapper.writeValueAsString(dataFields)
            ));

            log.info("update account: {}", response.getResponseText());

            if (response.getErrorCode() == 0) {
                session.setAttribute("subsidiary", requestParams.get("subsidiary"));
                session.setAttribute("division", requestParams.get("division"));
                session.setAttribute("department", requestParams.get("department"));
                session.setAttribute("language", requestParams.get("language"));
                session.setAttribute("country_code", requestParams.get("country_code"));
                session.setAttribute("loginId", requestParams.get("loginUserId"));
                session.setAttribute("newPwd", EncryptUtil.encryptData(requestParams.get("password")));


                String divisionField = channelAddFieldRepository.selectDivisionYnField(param);

                if(divisionField!=null && divisionField!="") {
                    String division = requestParams.get(divisionField);
                    session.setAttribute("approval_division",division);
                }

                if (!userEmail.equals(session.getAttribute("loginId"))) {
                    session.setAttribute("changedEmail", userEmail);

                    GSResponse emailResponse = gigyaService.executeRequest("default", "accounts.otp.sendCode", Map.of(
                            "lang", Locale.getDefault().getLanguage(),
                            "email", userEmail,
                            "status", "active"
                    ));

                    JsonNode emailResponseData = objectMapper.readTree(emailResponse.getResponseText());
                    session.setAttribute("vToken", EncryptUtil.encryptData(emailResponseData.get("vToken").asText()));

                    return new RedirectView("/registration/changeEmailVerify/" + param); // Replace with your change email verification URL
                } else {
                    return new RedirectView("/registration/consent"+ "?param=" + param); // Replace with your consent URL
                }
            } else if (response.getErrorCode() == 400006) {
                log.info("register account update failed 400006: {}", response.getResponseText());
                redirectAttributes.addFlashAttribute("showErrorMsg", "User account update failed");
                return new RedirectView(request.getHeader("Referer")); // Replace with your form URL
            } else {
                log.info("register account update failed: {}", response.getResponseText());
                redirectAttributes.addFlashAttribute("showErrorMsg", "User account update failed");
                return new RedirectView(request.getHeader("Referer"));
            }
        } catch (Exception e) {
            log.error("Error updating account", e);
            redirectAttributes.addFlashAttribute("showErrorMsg", "An error occurred while updating the account");
            return new RedirectView(request.getHeader("Referer"));
        }
    }

    public List<List<ChannelAddField>> groupFieldsInPairs(List<ChannelAddField> fields) {
        List<List<ChannelAddField>> groupedFields = new ArrayList<>();
        for (int i = 0; i < fields.size(); i += 2) {
            List<ChannelAddField> pair = fields.subList(i, Math.min(i + 2, fields.size()));
            groupedFields.add(pair);
        }
        return groupedFields;
    }

    public Map<String, String> extractWorkPhoneInfo(JsonNode accUser) {
        JsonNode phonesNode = accUser.path("profile").path("phones");
        String workPhone = null;

        if (phonesNode.isArray()) {
            for (JsonNode phoneNode : phonesNode) {
                if ("work_phone".equals(phoneNode.path("type").asText())) {
                    workPhone = phoneNode.path("number").asText(null);
                    break;
                }
            }
        }

        // 국가 코드와 전화번호 분리
        return splitPhoneNumber(workPhone);
    }

    public Map<String, String> splitPhoneNumber(String phoneNumber) {
        Map<String, String> phoneInfo = new HashMap<>();
        String countryCode = "";
        String number = "";

        if (phoneNumber != null) {
            String[] parts = phoneNumber.split(" ");

            for (String part : parts) {
                if (part.startsWith("+")) {
                    countryCode = part.replace("+", "").replaceAll("\\D", "");
                } else if (!part.isEmpty()) {
                    number = part;
                }
            }
        }

        phoneInfo.put("countryCode", countryCode);
        phoneInfo.put("phoneNumber", number);
        return phoneInfo;
    }

    public RedirectView processConvertSingup(String param, Map<String, String> payload, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Map<String, String> errors = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String url = "";
        validateInputs(payload, errors,param);
        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("showErrorMsg", errors.get("showErrorMsg"));
            return new RedirectView( request.getHeader("Referer"));
        }

        Map<String, String> convertData = (Map<String, String>) session.getAttribute("convertData");

        handleUserAffiliation(payload, session, request, errors);

        session.setAttribute("loginUserId",payload.get("loginUserId"));
        session.setAttribute("country",payload.get("country"));

        Map<String, Object> checkUserIdResult  = checkUserId(payload);
        //CIAM에 계정이 없을경우 전환프로세스 진행
        if (isUserIdAvailable(checkUserIdResult)) {
            setupSessionAttributesBasedOnCountry(payload.get("country"), session);

            Map<String, Object> registrationData = setupConvertRegistrationData(param, convertData, session,payload);
            GSResponse response = gigyaService.executeRequest(param, "accounts.register", registrationData);

            url =  handleRegistrationResponse(param, payload, response, session, redirectAttributes);

        } else { //CIAM 계정이 있을경우 전환확장 진행
            setupSessionAttributesBasedOnCountry(payload.get("country"), session);

            try {
                String loginId = payload.get("loginUserId");
                String query = String.format("SELECT * FROM Account WHERE email= '%s'", loginId);
                GSResponse searchResponse = gigyaService.executeRequest("default", "accounts.search", query);
                JsonNode responseData = objectMapper.readTree(searchResponse.getResponseText());
                JsonNode user = responseData.get("results").get(0);
                String uid = user.get("UID").asText("");
                session.setAttribute("uid",uid);

                Map<String, Object> registrationData = setupConvertUpdateData(param, convertData, session, payload);
                registrationData.put("UID",uid);

                GSResponse response = gigyaService.executeRequest(param, "accounts.setAccountInfo", registrationData);

                if (response.getErrorCode() == 0) {
                    session.setAttribute("loginUserId", payload.get("loginUserId"));
                    session.setAttribute("country", payload.get("country"));

                    try {
                        Map<String, Object> otpData = new HashMap<>();
                        otpData.put("lang", Locale.getDefault());
                        otpData.put("email", payload.get("loginUserId"));
                        otpData.put("status", "active");

                        GSResponse otpResponse = gigyaService.executeRequest(param, "accounts.otp.sendCode", otpData);
                        JsonNode otpResponseJson = objectMapper.readTree(otpResponse.getResponseText());

                        if (otpResponseJson.get("vToken") == null || otpResponseJson.get("vToken").asText("").isEmpty()) {
                            // 에러 처리
                            log.error("OTP vToken is missing or empty.");
                        }

                        Map<String, Object> data = new HashMap<>();
                        data.put("vToken", otpResponseJson.get("vToken").asText(""));
                        String jsonData = "";

                        try {
                            jsonData = objectMapper.writeValueAsString(data);
                        } catch (JsonProcessingException e) {
                            log.error(e.getMessage()); // JSON 변환 실패 처리
                        }

                        // 암호화 수행
                        String encryptedToken = EncryptUtil.encryptData(jsonData);
                        session.setAttribute("vToken", encryptedToken);

                        if (otpResponse.getErrorCode() == 0) {
                            JsonNode CDCUser = cdcTraitService.getCdcUser(uid,0);
                            String companyId = CDCUser.path("data").path("accountID").asText("");
                            String accountId = (String) session.getAttribute("convertAccountId");
                            if (companyId != null && accountId != null && !companyId.isEmpty() && !accountId.isEmpty() && !Objects.equals(companyId, accountId)) {
                                redirectAttributes.addFlashAttribute("showErrorMsg", "The company information doesn't match. Please contact the SBA Admin.");
                                return new RedirectView(request.getHeader("Referer"));
                            } else if (companyId != null && !companyId.isEmpty() && (accountId == null || accountId.isEmpty())) {
                                // accountId가 비어있고 companyId는 비어있지 않을 경우 처리
                                session.setAttribute("convertAccountId", companyId);  // convertAccountId를 companyId로 설정

                                // convertData 세션에서 cmdmAccountID 값을 변경
                                Map<String, String> convertDataMap = (Map<String, String>) session.getAttribute("convertData");
                                if (convertDataMap != null) {
                                    convertDataMap.put("cmdmAccountID", companyId);  // cmdmAccountID 값을 companyId로 변경
                                    session.setAttribute("convertData", convertDataMap);  // 변경된 convertData 다시 세션에 설정
                                }
                                convertData.put("cmdmAccountID", companyId);
                            }

                            session.setAttribute("loginId", payload.get("loginUserId"));
                            session.setAttribute("ciamConvertYn", "Y");
                            url = "/registration/signupVerify";
                        } else {
                            // 에러 처리
                            log.error("OTP response error: {}", otpResponse.getErrorMessage());
                            redirectAttributes.addFlashAttribute("showErrorMsg", "authVerifyFailed");
                        }

                    } catch (JsonProcessingException e) {
                        log.error("Failed to parse JSON response", e);
                        redirectAttributes.addFlashAttribute("showErrorMsg", "Error processing registration response.");
                    }

                } else {
                    log.info("register account update failed: {}", response.getResponseText());
                    redirectAttributes.addFlashAttribute("showErrorMsg", "User account update failed");
                    return new RedirectView(request.getHeader("Referer"));
                }

            } catch (Exception e) {
                log.error("Error during user account update process", e);
                redirectAttributes.addFlashAttribute("showErrorMsg", "An error occurred while updating the user account.");
                return new RedirectView(request.getHeader("Referer"));
            }
        }

        // 추가 작업 - 사용자 및 회사 정보 가져오기
        String uid = (String) session.getAttribute("uid");
        Map<String, Object> userDetails = cdcTraitService.populateUserManagementDetails(uid, convertData.getOrDefault("cmdmAccountID",""), param, payload,session);

        // userDetails로 필요한 작업을 수행
        // 예: 세션에 추가 정보 저장
        session.setAttribute("convertObject", userDetails.get("convertObject"));
        session.setAttribute("accountObject", userDetails.get("accountObject"));
        return new RedirectView(url + "?param=" + param);
    }

    public Map<String, Object> setupConvertRegistrationData(String param, Map<String, String> convertData, HttpSession session,Map<String, String> payload) {
        Map<String, Object> dataFields = new HashMap<>();
        dataFields.put("accountID",convertData.getOrDefault("cmdmAccountID",""));
        dataFields.put("userDepartment",convertData.getOrDefault("department",""));
        dataFields.put("subsidiary",convertData.getOrDefault("subsidiaryName",""));
        session.setAttribute("secSubsidiary",convertData.getOrDefault("subsidiaryName",""));

        //dataFields.put("channels", Map.of(param, Map.of("channelUID",convertData.getOrDefault("channelUID",""))));

        // 추가 필드 로딩 및 매핑
        List<ChannelAddField> additionalFields = channelAddFieldRepository.selectAdditionalField(param, "additional", "account");
        List<ChannelAddField> specFields = channelAddFieldRepository.selectAdditionalField(param, "spec", "account");

        // additionalFields 처리
        cdcTraitService.mapFieldsToDataFields(additionalFields, param, convertData, dataFields);

        // specFields 처리
        cdcTraitService.mapFieldsToDataFields(specFields, param, convertData, dataFields);

        // 회사 관련 필드 처리
        processCompanyFields(param, convertData, session, payload);

        Map<String, Object> profileFields = new HashMap<>();
        profileFields.put("firstName",convertData.getOrDefault("firstName",""));
        profileFields.put("lastName",convertData.getOrDefault("lastName",""));
        //profileFields.put("username",payload.getOrDefault("loginID",""));
        profileFields.put("languages",convertData.getOrDefault("languages",""));
        String countryCode = convertData.getOrDefault("countryCode", "");
        if (countryCode.isEmpty()) {
            // countryCode가 비어있을 경우 secCountry 사용
            profileFields.put("country", (String) session.getAttribute("secCountry"));
        } else {
            // countryCode가 비어있지 않으면 그대로 사용
            profileFields.put("country", countryCode);
        }
        List<Map<String, String>> phoneArray = new ArrayList<>();
        Map<String, String> phoneMap = new HashMap<>();

        String userPhone = (String) convertData.get("userPhone");
        if (userPhone != null && !userPhone.isEmpty()) {

            //phoneMap.put("number", "+"+payload.get("userPhoneCode")+" "+payload.get("userPhone"));
            phoneMap.put("number", userPhone);
            phoneMap.put("type", "work_phone");
            phoneArray.add(phoneMap);
        }
        profileFields.put("phones", phoneArray);

        dataFields.put("userStatus", "registerEmail");

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> registrationData = new HashMap<>();
        try {
            registrationData.put("data", objectMapper.writeValueAsString(dataFields));
            registrationData.put("profile", objectMapper.writeValueAsString(profileFields));
            registrationData.put("preferences", objectMapper.writeValueAsString(setupPreferences()));
        } catch (JsonProcessingException e) {
            log.error("Error serializing registration data", e);
        }
        String email =  payload.get("loginUserId");
        String[] emailSplit = email.split("@");
        String emailId = emailSplit[0];
        String domain = emailSplit[1];

        long timestamp = System.currentTimeMillis();

        // 임시 이메일 생성
        String tempEmail = emailId + "-" + timestamp + "@" + domain;

        registrationData.put("username", tempEmail);
        registrationData.put("email", payload.get("loginUserId"));
        registrationData.put("password", BeansUtil.getApplicationProperty("cdc.temp-reg-pwd"));
        registrationData.put("regSource", param);

        return registrationData;
    }

    public Map<String, Object> setupConvertUpdateData(String param, Map<String, String> convertData, HttpSession session,Map<String, String> payload) {
        Map<String, Object> dataFields = new HashMap<>();
        session.setAttribute("secSubsidiary",convertData.getOrDefault("subsidiaryName",""));

        // 추가 필드 로딩 및 매핑
        List<ChannelAddField> additionalFields = channelAddFieldRepository.selectAdditionalField(param, "additional", "account");
        List<ChannelAddField> specFields = channelAddFieldRepository.selectAdditionalField(param, "spec", "account");

        // additionalFields 처리
        cdcTraitService.mapFieldsToDataFields(additionalFields, param, convertData, dataFields);

        // specFields 처리
        cdcTraitService.mapFieldsToDataFields(specFields, param, convertData, dataFields);

        // 회사 관련 필드 처리
        processCompanyFields(param, convertData, session, payload);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> registrationData = new HashMap<>();
        try {
            registrationData.put("data", objectMapper.writeValueAsString(dataFields));
        } catch (JsonProcessingException e) {
            log.error("Error serializing registration data", e);
        }

        return registrationData;
    }

    private void processCompanyFields(String param, Map<String, String> convertData, HttpSession session, Map<String, String> payload) {
        List<ChannelAddField> companyAdditionalFields = channelAddFieldRepository.selectAdditionalField(param, "additional", "company");
        List<ChannelAddField> companySpecFields = channelAddFieldRepository.selectAdditionalField(param, "spec", "company");

        Map<String, String> companyParams = new HashMap<>();
        if (!companyAdditionalFields.isEmpty()) {
            mapFieldsToCompanyParams(companyAdditionalFields, convertData, companyParams);
        }

        if (!companySpecFields.isEmpty()) {
            mapFieldsToCompanyParams(companySpecFields, convertData, companyParams);
        }

        if (!companyParams.isEmpty()) {
            processCompanyParams(companyParams, session,convertData);
        }
    }

    public void mapFieldsToCompanyParams(List<ChannelAddField> fields, Map<String, String> requestParams, Map<String, String> companyParams) {
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            String elementId = entry.getKey();
            String value = entry.getValue();

            ChannelAddField matchingField = fields.stream()
                    .filter(field -> field.getElementId().equals(elementId))
                    .findFirst()
                    .orElse(null);

            if (matchingField != null) {
                String cdcDataField = matchingField.getCdcDataField();
                companyParams.put(cdcDataField, value);
            }
        }
    }

    public void processCompanyParams(Map<String, String> companyParams, HttpSession session ,Map<String, String> convertData) {
        try {
            // companyParams를 JSON 문자열로 변환
            String companyFieldsJson = new ObjectMapper().writeValueAsString(companyParams);

            // organizationParams를 Map<String, Object>로 생성
            Map<String, Object> organizationParams = new HashMap<>();
            organizationParams.put("bpid", convertData.getOrDefault("cmdmAccountID",""));
            organizationParams.put("info", companyFieldsJson);

            // Gigya 서비스로 조직 정보 설정 요청
            GSResponse organizationResponse = gigyaService.executeRequest("default", "accounts.b2b.setOrganizationInfo", organizationParams);

            log.debug("setOrganizationInfo");
            // 요청 결과 처리
//            if (organizationResponse.getErrorCode() == 0) {
//                log.info("setOrganizationInfo success");
//            } else {
//                log.error("company registration failed: " + organizationResponse.getResponseText());
//                log.warn("company registration context: " + session.getAttribute("uid"));
//                String tmpErrorMsg = organizationResponse.getData().containsKey("errorMessage")
//                        ? " : " + organizationResponse.getData().getString("errorMessage")
//                        : "";
//            }
        } catch (JsonProcessingException e) {
            log.error("Error serializing company params", e);
        }
    }

    private void processVendorCompanyRegistration(Map<String, String> requestParams, String param, HttpSession session, String usedBpid,RedirectAttributes redirectAttributes) {
        // Getting the user details
        JsonNode cdcUser = cdcTraitService.getCdcUser((String) session.getAttribute("uid"), 0);

        // profile 내의 work 항목을 JsonNode로 받아오기
        JsonNode profile = cdcUser.path("profile");
        JsonNode work = profile.path("work");

        // 각 필드를 String으로 변환
        String adminReason = work.path("description").asText();
        String workTitle = work.path("title").asText();
        String workLocation = work.path("location").asText();
        // Setting CDC request
        Map<String, Object> profileFields = new HashMap<>();
        if ("1".equals(session.getAttribute("toolmateOtherCountry")) && "toolmate".equals(param)) {
            profileFields.put("work", Map.of("company", requestParams.get("name")));
            profileFields.put("country", requestParams.get("country"));
        } else {
            profileFields.put("work", Map.of("company", requestParams.get("name")));
        }

        String profileJSON = "";
        try {
            profileJSON = new ObjectMapper().writeValueAsString(profileFields);
        } catch (Exception e) {
            log.error("Error serializing profile fields", e);
        }

        Map<String, Object> cdcParams = new HashMap<>();
        Map<String, Object> oranizationParams = new HashMap<>();

        cdcParams.put("UID", (String) session.getAttribute("uid"));
        cdcParams.put("profile", profileJSON);

        Map<String, Object> accountParams = new HashMap<>();
        Map<String, Object> companyParams = new HashMap<>();

        /*if ("mmp".equals(param)) {
            accountParams.put("data", Map.of("accountID", usedBpid, "channels", Map.of(param, Map.of("mtLicense", requestParams.getOrDefault("magicinfo", ""), "mtStartDate", requestParams.getOrDefault("startDttm", ""), "mtEndDate", requestParams.getOrDefault("endDttm", "")))));
        } else {
            accountParams.put("accountID", Map.of("accountID", usedBpid));
        }*/
        String dataFieldsJson = "";
        String companyFieldsJSon = "";
        try {
            List<ChannelAddField> additionalFields = channelAddFieldRepository.selectAdditionalField(param,"additional","account");
            List<ChannelAddField> specFields = channelAddFieldRepository.selectAdditionalField(param,"spec","account");
            List<ChannelAddField> companyAdditionalFields = channelAddFieldRepository.selectAdditionalField(param,"additional","company");
            List<ChannelAddField> companySpecFields = channelAddFieldRepository.selectAdditionalField(param,"spec","company");


            if(additionalFields.size()>0) {

                // 필드를 메모리에서 비교하며 매핑
                for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                    String elementId = entry.getKey(); // elementId를 가져옴
                    String value = entry.getValue();
                    //if(value!="") {
                    // 해당 element_id와 일치하는 필드를 찾기
                    ChannelAddField matchingField = additionalFields.stream()
                            .filter(field -> field.getElementId().equals(elementId))
                            .findFirst()
                            .orElse(null);

                    if (matchingField != null) {
                        // cdc_data_field 값에 $channelname을 넣어서 최종 필드 이름을 생성
                        String cdcDataField = matchingField.getCdcDataField().replace("$channelname", param);

                        // 최종 필드 이름으로 accountParams에 값 설정
                        accountParams.put(cdcDataField, value);
                    }
                    //}
                }
            }
            if(specFields.size()>0) {

                // 필드를 메모리에서 비교하며 매핑
                for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                    String elementId = entry.getKey(); // elementId를 가져옴
                    String value = entry.getValue();
                    //if(value!="") {
                    // 해당 element_id와 일치하는 필드를 찾기
                    ChannelAddField matchingField = specFields.stream()
                            .filter(field -> field.getElementId().equals(elementId))
                            .findFirst()
                            .orElse(null);

                    if (matchingField != null) {
                        // cdc_data_field 값에 $channelname을 넣어서 최종 필드 이름을 생성
                        String cdcDataField = matchingField.getCdcDataField().replace("$channelname", param);

                        // 최종 필드 이름으로 accountParams에 값 설정
                        accountParams.put(cdcDataField, value);
                    }
                    //}
                }
            }
            if (companyAdditionalFields.size() > 0) {
                // 필드를 메모리에서 비교하며 매핑
                for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                    String elementId = entry.getKey(); // elementId를 가져옴
                    String value = entry.getValue();

                    // 해당 element_id와 일치하는 필드를 찾기
                    ChannelAddField matchingField = companyAdditionalFields.stream()
                            .filter(field -> field.getElementId().equals(elementId))
                            .findFirst()
                            .orElse(null);

                    if (matchingField != null) {
                        // cdc_data_field 값에 $channelname을 넣어서 최종 필드 이름을 생성
                        String cdcDataField = matchingField.getCdcDataField();

                        // 배열 형태로 넣어야 하는 경우 처리
                        if ("Y".equals(matchingField.getArrayYn())) {
                            List<String> valueList;

                            // companyParams에 이미 값이 있으면 해당 값을 리스트로 변환
                            if (companyParams.containsKey(cdcDataField)) {
                                String existingValue = companyParams.get(cdcDataField).toString();
                                valueList = new ArrayList<>(Arrays.asList(existingValue.replace("[", "").replace("]", "").split(",")));
                            } else {
                                valueList = new ArrayList<>();
                            }

                            // 값 추가
                            valueList.add(value);

                            // 리스트를 JSON 문자열로 변환하여 저장
                            String jsonValueList = new ObjectMapper().writeValueAsString(valueList);
                            companyParams.put(cdcDataField, jsonValueList);
                        } else {
                            // 배열이 아닐 경우, 기존 방식대로 처리
                            companyParams.put(cdcDataField, value);
                        }
                    }
                }
            }

            if (companySpecFields.size() > 0) {
                // 필드를 메모리에서 비교하며 매핑
                for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                    String elementId = entry.getKey(); // elementId를 가져옴
                    String value = entry.getValue();

                    // 해당 element_id와 일치하는 필드를 찾기
                    ChannelAddField matchingField = companySpecFields.stream()
                            .filter(field -> field.getElementId().equals(elementId))
                            .findFirst()
                            .orElse(null);

                    if (matchingField != null) {
                        // cdc_data_field 값에 $channelname을 넣어서 최종 필드 이름을 생성
                        String cdcDataField = matchingField.getCdcDataField();

                        // 배열 형태로 넣어야 하는 경우 처리
                        if ("Y".equals(matchingField.getArrayYn())) {
                            List<String> valueList;

                            // companyParams에 이미 값이 있으면 해당 값을 리스트로 변환
                            if (companyParams.containsKey(cdcDataField)) {
                                String existingValue = companyParams.get(cdcDataField).toString();
                                valueList = new ArrayList<>(Arrays.asList(existingValue.replace("[", "").replace("]", "").split(",")));
                            } else {
                                valueList = new ArrayList<>();
                            }

                            // 값 추가
                            valueList.add(value);

                            // 리스트를 JSON 문자열로 변환하여 저장
                            String jsonValueList = new ObjectMapper().writeValueAsString(valueList);
                            companyParams.put(cdcDataField, jsonValueList);
                        } else {
                            // 배열이 아닐 경우, 기존 방식대로 처리
                            companyParams.put(cdcDataField, value);
                        }
                    }
                }
            }

            accountParams.put("accountID",usedBpid);
            dataFieldsJson = new ObjectMapper().writeValueAsString(accountParams);
            cdcParams.put("data",dataFieldsJson);

            companyFieldsJSon = new ObjectMapper().writeValueAsString(requestParams);
            oranizationParams.put("bpid",usedBpid);
            oranizationParams.put("info",companyFieldsJSon);
            session.setAttribute("companyParams",companyParams);
            JsonNode company = cdcTraitService.getB2bOrg(usedBpid);
            String bpid = "";
            ObjectMapper objectMapper = new ObjectMapper();

            if (company.has("errorCode") && company.get("errorCode").asInt() == 0) {
                if(companyParams.size()>0) {
                    companyFieldsJSon = new ObjectMapper().writeValueAsString(companyParams);
                    oranizationParams.put("bpid",usedBpid);
                    oranizationParams.put("info",companyFieldsJSon);
                    session.setAttribute("companyParams",companyParams);
                    GSResponse OrganizationResponse = gigyaService.executeRequest("default", "accounts.b2b.setOrganizationInfo", oranizationParams);

//                if (OrganizationResponse.getErrorCode() == 0) {
//                    log.info("setOrganizationInfo success");
//                } else {
//                    log.error("company registration fail : " + OrganizationResponse.getResponseText());
//                    log.warn("company registration context : " + session.getAttribute("uid"));
//                    String tmpErrorMsg = OrganizationResponse.getData().containsKey("errorMessage") ? " : " + OrganizationResponse.getData().getString("errorMessage") : "";
//                    redirectAttributes.addFlashAttribute("showErrorMsg", "company registration failed" + tmpErrorMsg);
//                }
                }
            } else {
                // 회사 정보가 없거나 에러코드가 0이 아닌 경우 - btpAccount를 통해 처리
                BtpAccounts btpAccount = btpAccountsRepository.selectByBpid(usedBpid);

                if (btpAccount != null) {
                    try {
                        // btpAccount로부터 필요한 데이터 추출
                        String bpidValue = btpAccount.getBpid();
                        String[] bizCheck = bpidValue.split("-");
                        //String usedBizRegNo = (bizCheck.length > 1 && "NOBIZREG".equals(bizCheck[1])) ? "" : btpAccount.getBizregno1();

                        // btpAccount 기반으로 조직 정보 구성
                        Map<String, Object> organizationMap = new HashMap<>();
                        organizationMap.put("name", btpAccount.getName());
                        organizationMap.put("name_search", btpAccount.getName());
                        organizationMap.put("bpid", bpidValue);
                        organizationMap.put("bizregno1", btpAccount.getBizregno1());
                        organizationMap.put("phonenumber1", btpAccount.getPhonenumber1());
                        organizationMap.put("country", btpAccount.getCountry());
                        organizationMap.put("street_address", btpAccount.getStreetAddress().toLowerCase());
                        organizationMap.put("city", btpAccount.getCity());
                        organizationMap.put("state", btpAccount.getState());
                        organizationMap.put("zip_code", btpAccount.getZipCode());
                        organizationMap.put("type", btpAccount.getType());
                        organizationMap.put("regch", "");
                        organizationMap.put("vendorcode", btpAccount.getVendorcode());
                        organizationMap.put("industry_type", "");
//                        organizationMap.put("products", btpAccount.getProducts());
                        organizationMap.put("channeltype", "");

                        // 조직 정보 JSON으로 변환
                        String organizationJson = new ObjectMapper().writeValueAsString(organizationMap);

                        // 요청자 정보 매핑
                        Map<String, Object> requester = new HashMap<>();
                        requester.put("firstName", requestParams.getOrDefault("firstName", ""));
                        requester.put("lastName", requestParams.getOrDefault("lastName", ""));
                        requester.put("email", requestParams.getOrDefault("loginUserId", ""));

                        String requesterJson = objectMapper.writeValueAsString(requester);

                        // CDC 파라미터 설정
                        Map<String, Object> registraionOrganizationParams = new HashMap<>();
                        registraionOrganizationParams.put("organization", organizationJson);
                        registraionOrganizationParams.put("requester", requesterJson);
                        registraionOrganizationParams.put("status", "approved");

                        // CDC에 조직 등록 요청
                        GSResponse registerOrgResponse = gigyaService.executeRequest(
                                "default", "accounts.b2b.registerOrganization", registraionOrganizationParams
                        );
                        log.info("registerOrganization response: {}", registerOrgResponse.getResponseText());

                        if (registerOrgResponse.getErrorCode() == 0) {
                            log.info("New company registered with BPID: {}", bpidValue);
                        } else {
                            log.error("Failed to register company. Error: {}", registerOrgResponse.getResponseText());
                        }

                    } catch (Exception e) {
                        log.error("Error processing registerOrganization request for btpAccount", e);
                    }
                } else {
                    log.error("No BtpAccount found for BPID: {}", usedBpid);
                }
            }
//                if (OrganizationResponse.getErrorCode() == 0) {
//                    log.info("setOrganizationInfo success");
//                } else {
//                    log.error("company registration fail : " + OrganizationResponse.getResponseText());
//                    log.warn("company registration context : " + session.getAttribute("uid"));
//                    String tmpErrorMsg = OrganizationResponse.getData().containsKey("errorMessage") ? " : " + OrganizationResponse.getData().getString("errorMessage") : "";
//                    redirectAttributes.addFlashAttribute("showErrorMsg", "company registration failed" + tmpErrorMsg);
//                }

        } catch (Exception e) {
            log.error("Error serializing profile fields", e);
        }

        GSResponse response = gigyaService.executeRequest("default", "accounts.setAccountInfo", cdcParams);

        try {
            if (response.getErrorCode() == 0) {
                log.info("setAccountInfo success");
            } else {
                log.error("company registration fail : " + response.getResponseText());
                log.warn("company registration context : " + session.getAttribute("uid"));
                String tmpErrorMsg = response.getData().containsKey("errorMessage") ? " : " + response.getData().getString("errorMessage") : "";
                redirectAttributes.addFlashAttribute("showErrorMsg", "company registration failed" + tmpErrorMsg);
            }
        } catch (Exception e) {
            log.error("Error during CDC company registration", e);
        }
    }

    public ModelAndView adLoginProcessing(String key ,HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/adLoginProcessing";
        modelAndView.addObject("content", content);
        modelAndView.addObject("apiKey",BeansUtil.getApiKeyForChannel(key));

        return modelAndView;
    }
}
