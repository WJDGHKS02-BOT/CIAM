package com.samsung.ciam.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.cpi.service.CpiApiService;
import com.samsung.ciam.common.gigya.service.GigyaService;
import com.samsung.ciam.models.*;
import com.samsung.ciam.repositories.*;
import com.samsung.ciam.utils.BeansUtil;
import com.samsung.ciam.utils.EncryptUtil;
import com.samsung.ciam.utils.StringUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UserProfileService {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private CmdmApiService cmdmApiService;

    @Autowired
    private ApprovalConfigurationService approvalConfigurationService;

    @Autowired
    private ConsentProfileService consentProfileService;

    @Autowired
    private UserAgreedConsentsRepository userAgreedConsentsRepository;
    
    @Autowired
    private ConsentContentRepository consentContentRepository;

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private CisCountryRepository cisCountryRepository;

    @Autowired
    private ConsentTypesRepository consentTypesRepository;

    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private SubsidiaryRepository subsidiaryRepository;

    @Autowired
    private NewSubsidiaryRepository newSubsidiaryRepository;

    @Autowired
    private ConsentLanguagesRepository consentLanguagesRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ChannelAddFieldRepository channelAddFieldRepository;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private ApprovalRuleRepository approvalRuleRepository;

    @Autowired
    private CpiApiService cpiApiService;

    @Autowired
    private ApprovalAdminRepository approvalAdminRepository;

    @Autowired
    private AuditLogService auditLogService;

    // 24.07.24 kimjy 추가 - personalInformation 화면 예비 URL
    @SuppressWarnings("unchecked")
    public ModelAndView personalInformation(HttpServletRequest request, HttpSession session) {
        Map<String, Object> jsonResponse = new HashMap<>();
        // uid 하드코딩 47835cb8b053490294937e333ad5ceb3
        String hUid = "47835cb8b053490294937e333ad5ceb3";

        // 리턴받은 uid
        String uid = "";
        Map<String, Object> userDataResult = null;
        List<Map<String, String>> phoneArray = new ArrayList<>();
        JsonNode cdcUser = null;
        String socialProviders ="";
        try {
            if (session.getAttribute("cdc_uid") != null) {
                cdcUser = cdcTraitService.getCdcUser((String) session.getAttribute("cdc_uid"), 0);
            } else {
                cdcUser = cdcTraitService.getCdcUser(hUid, 0);
            }

            uid = StringUtil.getNonEmptyValue(cdcUser.path("UID"));
            socialProviders =  StringUtil.getNonEmptyValue(cdcUser.path("socialProviders"));

            // profile 내의 work 항목을 JsonNode로 받아오기
            JsonNode profile = cdcUser.path("profile");
            JsonNode data = cdcUser.path("data");
            JsonNode loginIDs = cdcUser.path("loginIDs");
            JsonNode work = profile.path("work");

            ObjectNode objectNode = objectMapper.createObjectNode();

            if (profile.isObject()) {
                log.warn("profile : {}", profile);

                // 바로 가져오는 키 목록 : cdc_uid, firstName, lastName, email, language, country_code, city, state
                objectNode.put("cdc_uid", uid);
                objectNode.put("firstName", StringUtil.getNonEmptyValue(profile.path("firstName")));
                objectNode.put("lastName", StringUtil.getNonEmptyValue(profile.path("lastName")));
                objectNode.put("email", StringUtil.getNonEmptyValue(profile.path("email")));// locale 값 변환 로직
                String locale = StringUtil.getNonEmptyValue(profile.get("locale"));
                objectNode.put("language", "en".equals(locale) ? "en_US" : "ko".equals(locale) ? "ko_KR" : locale);
                objectNode.put("country_code", StringUtil.getNonEmptyValue(profile.path("country")));
                objectNode.put("city", StringUtil.getNonEmptyValue(profile.path("city")));
                objectNode.put("state", StringUtil.getNonEmptyValue(profile.path("state")));

                ObjectNode profileNode = (ObjectNode) profile;
                List<Map<String, Object>> phones = objectMapper.convertValue(profileNode.get("phones"), List.class);

                if (phones != null && !phones.isEmpty()) {
                    for (Map<String, Object> phone : phones) {
                        String phoneNumber = phone.get("number").toString();

                        if (!phoneNumber.isEmpty()) {
                            // 공백으로 분리된 전화번호 확인
                            String[] phoneParts = phoneNumber.split(" ");

                            if (phone.get("type").equals("mobile_phone")) {
                                if (phoneParts.length > 1) {
                                    // "+"를 제거하고 코드와 번호를 저장
                                    objectNode.put("mobilePhoneCode", phoneParts[0].replace("+", ""));
                                    objectNode.put("mobilePhone", phoneParts[1]);
                                } else if (phoneNumber.startsWith("+")) {
                                    objectNode.put("mobilePhoneCode", phoneNumber.replace("+", ""));
                                    objectNode.put("mobilePhone", "");  // 번호가 없을 때 빈 값으로 처리
                                }
                            } else if (phone.get("type").equals("work_phone")) {
                                if (phoneParts.length > 1) {
                                    objectNode.put("workPhoneCode", phoneParts[0].replace("+", ""));
                                    objectNode.put("workPhone", phoneParts[1]);
                                } else if (phoneNumber.startsWith("+")) {
                                    objectNode.put("workPhoneCode", phoneNumber.replace("+", ""));
                                    objectNode.put("workPhone", "");
                                }
                            }
                        }
                    }
                }

                // 있으면 새로 추가 : data, loginIDs
                if (!data.isEmpty()) {
                    objectNode.put("jobtitle", StringUtil.getNonEmptyValue(data.path("jobtitle")));
                    objectNode.put("userDepartment", StringUtil.getNonEmptyValue(data.path("userDepartment")));
                    objectNode.put("salutation", StringUtil.getNonEmptyValue(data.path("salutation")));
                }

                if (!loginIDs.isEmpty()) {
                    objectNode.put("username", StringUtil.getNonEmptyValue(profile.path("loginIDs")));
                }

                // 공통코드 3. Work Phone : country_code_work / common_code > header = 'COUNTRY_CODE'
                List<CommonCode> countryCodeList = commonCodeRepository.findByHeader("COUNTRY_CODE");
                for (CommonCode countryCode : countryCodeList) {
                    Map<String, String> phoneMap = new HashMap<>();
                    phoneMap.put("name", "(+" + countryCode.getCode() + ") " + countryCode.getName());
                    phoneMap.put("value", countryCode.getCode());
                    phoneArray.add(phoneMap);
                }

                profile = (JsonNode) objectNode;
            }

            // String cdcUserJson = objectMapper.writeValueAsString(cdcUser);
            userDataResult = objectMapper.convertValue(profile, Map.class);
            log.warn("userDataResult : {}", userDataResult);
            // log.warn("cdcUser: {}, profile: {}", cdcUserJson, profileJson);
            // 각 필드를 String으로 변환
            String adminReason = work.path("description").asText();
            String workTitle = work.path("title").asText();
            String workLocation = work.path("location").asText();

        } catch (Exception e) {
            log.error("Error processing registration data", e);
            jsonResponse.put("error", "Exception occurred: " + e.getMessage());
        }

        ModelAndView modelAndView = new ModelAndView("myPage");
        String content = "fragments/myPage/personalInformation";
        String menu = "personalInformation";

        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("userDataResult", userDataResult);
        modelAndView.addObject("socialProviders", socialProviders);

        // 공통코드 1. Preferred Language : languages
        List<Map<String, String>> languages = new ArrayList<>();
        Map<String, String> koreanLanguageMap = new HashMap<>();
        koreanLanguageMap.put("name", "Korean");
        koreanLanguageMap.put("value", "ko_KR");
        languages.add(koreanLanguageMap);

        Map<String, String> englishLanguageMap = new HashMap<>();
        englishLanguageMap.put("name", "English");
        englishLanguageMap.put("value", "en_US");
        languages.add(englishLanguageMap);
        modelAndView.addObject("languages", languages); // languages

        // 공통코드 2. Departments : secDept / common_code > header = 'BIZ_WITH_DEPT'
        List<CommonCode> bizWithDeptList = commonCodeRepository.findByHeader("BIZ_WITH_DEPT");
        List<Map<String, String>> secDeptArr = new ArrayList<>();
        for (CommonCode bizWithDept : bizWithDeptList) {
            Map<String, String> secDeptMap = new HashMap<>();
            secDeptMap.put("name", bizWithDept.getName());
            secDeptMap.put("value", bizWithDept.getValue());
            secDeptArr.add(secDeptMap);
        }
        List<CommonCode> divisions = commonCodeRepository.findByHeader("BIZ_WITH_DEPT");
        modelAndView.addObject("divisions", divisions);
        modelAndView.addObject("secDept", secDeptArr); // Departments

        modelAndView.addObject("codes", commonCodeRepository.findByHeader("COUNTRY_CODE"));
        if (!phoneArray.isEmpty()) {
            modelAndView.addObject("country_code_work", phoneArray);
        }

        Map<String, Object> accountObject = new HashMap<>();
        Map<String, Object> regCompanyData = new HashMap<>();

        // 여기서 extractChannelAddFields 호출
        cdcTraitService.extractChannelAddFields(null, cdcUser, (String) session.getAttribute("session_channel"), accountObject, regCompanyData);

        modelAndView.addObject("accountObject", accountObject);

        Map<String, List<?>> fieldMap = cdcTraitService.generateFieldMap((String) session.getAttribute("session_channel"), objectMapper, false, true,true); // Company 데이터만 포함

        modelAndView.addObject("fieldMap", fieldMap);
        modelAndView.addObject("apiKey", BeansUtil.getApiKeyForChannel((String) session.getAttribute("session_channel")));

        return modelAndView;
    }

    public RedirectView savePersonalInformation(Map<String, String> payload, HttpSession session,
                                                HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Map<String, Object> jsonResponse = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        String channel = (String) session.getAttribute("session_channel");

        // 테스트 계정이라 예외처리 패스 (※ samsung.com 또는 partner.samsung.com 계정만 사용할때 주석제거할 것)
        // registrationService.validateInputs(payload, errors);

        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("showErrorMsg", errors.get("showErrorMsg"));
            return new RedirectView( request.getHeader("Referer"));
        }

        // 테스트 계정이라 예외처리 패스 (※ samsung.com 또는 partner.samsung.com 계정만 사용할때 주석제거할 것)
        // 7/31 isDomainBanned 함수 관련 소스는 일단 주석처리할 것 (mailinator.com 예외처리 안 되어있음)
        // registrationService.handleUserAffiliation(payload, session, request, errors);

        // 고정값인 email은 파라미터 추가 안함 : 이메일 안 넣으면 변경 안되기 때문
        try {
            Map<String, Object> profileFields = new HashMap<>();
            String languages = payload.get("languages");
            profileFields.put("locale", "en_US".equals(languages) ? "en" : "ko_KR".equals(languages) ? "ko" : languages);
            profileFields.put("firstName", payload.get("firstName"));
            profileFields.put("lastName", payload.get("lastName"));
            // 국가코드 2자리 (KR) : 수정 UI 없음
            // profileFields.put("country", payload.get("countryCode"));
            List<Map<String, String>> phoneArray = new ArrayList<>();
            Map<String, String> phoneMap = new HashMap<>();
            if (payload.get("mobilePhone") != null && !payload.get("mobilePhone").isEmpty()) {
                phoneMap.put("number", "+"+payload.get("mobilePhoneCode")+" "+payload.get("mobilePhone"));
                phoneMap.put("type", "mobile_phone");
                phoneArray.add(phoneMap);
            }
            if (payload.get("workPhone") != null && !payload.get("workPhone").isEmpty()) {
                phoneMap.put("number", "+"+payload.get("workPhoneCode")+" "+payload.get("workPhone"));
                phoneMap.put("type", "work_phone");
                phoneArray.add(phoneMap);
            }

            profileFields.put("phones", phoneArray);
            //profileFields.put("locale", payload.get("locale"));

            Map<String, Object> cdcParams = new HashMap<>();
            cdcParams.put("UID", payload.get("cdc_uid"));
            cdcParams.put("profile", objectMapper.writeValueAsString(profileFields));
            Map<String, Object> dataFields = new HashMap<>();
            dataFields.put("jobtitle", payload.get("jobtitle")); // Job Title
            dataFields.put("userDepartment", payload.get("userDepartment")); // Department
            dataFields.put("salutation", payload.get("salutation")); // Salutation

            Map<String, Object> accountParams = new HashMap<>();
            //Map<String, Object> companyParams = new HashMap<>();

            List<ChannelAddField> additionalFields = channelAddFieldRepository.selectAdditionalField(channel,"additional","account");
            List<ChannelAddField> specFields = channelAddFieldRepository.selectAdditionalField(channel,"spec","account");


            if(additionalFields.size()>0) {

                // 필드를 메모리에서 비교하며 매핑
                for (Map.Entry<String, String> entry : payload.entrySet()) {
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
                        String cdcDataField = matchingField.getCdcDataField().replace("$channelname", channel);

                        // 최종 필드 이름으로 accountParams에 값 설정
                        dataFields.put(cdcDataField, value);
                    }
                    //}
                }
            }
            if(specFields.size()>0) {

                // 필드를 메모리에서 비교하며 매핑
                for (Map.Entry<String, String> entry : payload.entrySet()) {
                    String elementId = entry.getKey(); // elementId를 가져옴
                    String value = entry.getValue();
                    // 해당 element_id와 일치하는 필드를 찾기
                    ChannelAddField matchingField = specFields.stream()
                            .filter(field -> field.getElementId().equals(elementId))
                            .findFirst()
                            .orElse(null);

                    if (matchingField != null) {
                        // cdc_data_field 값에 $channelname을 넣어서 최종 필드 이름을 생성
                        String cdcDataField = matchingField.getCdcDataField().replace("$channelname", channel);

                        // 최종 필드 이름으로 accountParams에 값 설정
                        dataFields.put(cdcDataField, value);
                    }
                }
            }

            cdcParams.put("data", objectMapper.writeValueAsString(dataFields));

            GSResponse response = gigyaService.executeRequest("default", "accounts.setAccountInfo", cdcParams);
            redirectAttributes.addFlashAttribute("responseErrorCode", response.getErrorCode());

            String channelType = channelRepository.selectChannelTypeSearch(channel);

            Optional<Channels> optionalChannelObj = channelRepository.selectByChannelName(channel);
            Channels channelObj = optionalChannelObj.get();
            Map<String, Object> configMap = channelObj.getConfigMap();

            if("CUSTOMER".equals(channelObj.getChannelType())) {
                cpiApiService.updateCpiContact("U",payload.get("cdc_uid"));
            }

            Boolean userProvisioning = configMap != null && configMap.containsKey("java_useprovisioning")
                    ? (Boolean) configMap.get("java_useprovisioning")
                    : false;

            if (userProvisioning) {
                cpiApiService.sendUidProvisioningNoConfigChecking(channel, "U",payload.get("cdc_uid"));
            }

            if (response.getErrorCode() == 0) {
                jsonResponse.put("errorCode", 0);
                jsonResponse.put("status", "ok");
                return new RedirectView("/myPage/personalInformation");
            } else {
                jsonResponse.put("errorCode", response.getErrorCode());
                jsonResponse.put("status", "Invalid password");
                return new RedirectView(request.getHeader("Referer"));
            }
        } catch (Exception e) {
            log.error("Error processing registration data", e);
            //jsonResponse.put("error", "Exception occurred: " + e.getMessage());
            return new RedirectView(request.getHeader("Referer"));
        }
    }
        
    public RedirectView deleteWithdrawUser(Map<String, String> payload, HttpSession session,
    HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // 1. API 호출 : accounts.getAccountInfo         
        try{
            // uid 하드코딩 dd4030f5c2384b23ae8ca401e8de64d1
            String hUid = "dd4030f5c2384b23ae8ca401e8de64d1";
            JsonNode cdcUser;

            if(session.getAttribute("cdc_uid") != null){
                cdcUser = cdcTraitService.getCdcUser((String) session.getAttribute("cdc_uid"), 0);
            } else {
                cdcUser = cdcTraitService.getCdcUser(hUid, 0);
            }
            log.warn("cdcUser : {}", cdcUser);

            // 2. 데이터 가져오기
            JsonNode profile = cdcUser.path("profile");
            JsonNode data = cdcUser.path("data");

            Map<String, Object> userData = null;

            ObjectNode objectNode = objectMapper.createObjectNode();

            String userEmail = StringUtil.getNonEmptyValue(profile.path("email"));
            String username = StringUtil.getNonEmptyValue(profile.path("username"));
            String firstName = StringUtil.getNonEmptyValue(profile.path("firstName"));
            String lastName = StringUtil.getNonEmptyValue(profile.path("lastName"));
            String contactID = StringUtil.getNonEmptyValue(profile.path("contactID"));

            // 현재 타임스탬프를 'Y-m-d\TH-i-s' 형식으로 포맷
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");
            String timestamp = LocalDateTime.now().format(formatter);
            // 이메일 주소를 '@' 기준으로 분리
            String[] emailParts = userEmail.split("@");
            // if (emailParts.length != 2) {
            //     throw new IllegalArgumentException("Invalid email address");
            // }
            String emailDomain = emailParts[1]; // '@' 뒤의 도메인 부분
            String newUsername = emailParts[0] + '-' + timestamp + '@' + emailDomain;
            String newFirstName = firstName + '-' + timestamp;
            String newLastName = lastName + '-' + timestamp;
            String newEmail = emailParts[0] + '-' + timestamp + '@' + emailDomain;
            
            // 데이터 직렬화 (JSON 형식으로 변환)
            ObjectMapper objectMapper = new ObjectMapper();
            
            // 직렬화된 JSON 문자열 얻기
            String dataArray = objectMapper.writeValueAsString(cdcUser);

            // 3. 삭제
            // 채널 예외처리는 보류
            
            Map<String, Object> profileUpdates = new HashMap<>();
            profileUpdates.put("username",newUsername);
            profileUpdates.put("firstName",newFirstName);
            profileUpdates.put("lastName",newLastName);
            profileUpdates.put("email",newEmail);
            profileUpdates.put("username",newEmail);
            Map<String, String> work = new HashMap<>();
            work.put("company", "");
            //work.put("compnayID", "");
            profileUpdates.put("work",work);
            
            Map<String, Object> cdcParams = new HashMap<>();
            cdcParams.put("UID", (String) session.getAttribute("cdc_uid"));
            cdcParams.put("isActive", "false");
            cdcParams.put("username", newEmail);
            cdcParams.put("profile", objectMapper.writeValueAsString(profileUpdates));

            Map<String, Object> accountData = new HashMap<>();
            accountData.put("userStatus", "disabled");
            accountData.put("accountID", "");
            accountData.put("channels", data.get("channels"));
            cdcParams.put("data", objectMapper.writeValueAsString(accountData));
            GSResponse response = gigyaService.executeRequest("default", "accounts.setAccountInfo", cdcParams);

            // channels 데이터를 가져와서 루프를 돌며 처리
            JsonNode channelsNode = data.get("channels");
            Iterator<Map.Entry<String, JsonNode>> fields = channelsNode.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String channelName = entry.getKey();

                // 채널 타입 조회
                String channelType = channelRepository.selectChannelTypeSearch(channelName);

                Optional<Channels> optionalChannelObj = channelRepository.selectByChannelName(channelName);
                Channels channelObj = optionalChannelObj.get();
                Map<String, Object> configMap = channelObj.getConfigMap();

                if("CUSTOMER".equals(channelType)) {
                    cpiApiService.updateCpiContact("D",(String) session.getAttribute("cdc_uid"));
                }

                Boolean userProvisioning = configMap != null && configMap.containsKey("java_useprovisioning")
                        ? (Boolean) configMap.get("java_useprovisioning")
                        : false;

                // "CUSTOMER"인 경우에만 cpiApiService 호출
                if (userProvisioning) {
                    cpiApiService.sendUidProvisioningNoConfigChecking(channelName, "D", (String) session.getAttribute("cdc_uid"));
                }
            }

            approvalAdminRepository.updateUidFalseStatus( (String) session.getAttribute("cdc_uid"));

            //cmdmApiService.withdrawAccount(request, "Context1", contactID, objectMapper.writeValueAsString(cdcUser));
            redirectAttributes.addFlashAttribute("responseErrorCode", response.getErrorCode());

            //Audit Log
            Map<String,String> param = new HashMap<String,String>();
            param.put("type", "Account_Deletion");
            param.put("action", "Deletion");
            param.put("items", "Deletion");
            auditLogService.addAuditLog(session, param);
            
            // 4. 화면 이동
            return new RedirectView("/sso/logout");
        } catch (Exception e) {
            log.error("Error processing registration data", e);
            //jsonResponse.put("error", "Exception occurred: " + e.getMessage());
            return new RedirectView(request.getHeader("Referer"));
        }
    }

    public Map<String,String> login(Map<String, String> payload, HttpSession session) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> emailSendParams = new HashMap<>();
        Map<String, String> responseData = new HashMap<>();

        params.put("loginID", payload.get("loginUserId"));
        params.put("password", payload.get("password"));

        GSResponse response = gigyaService.executeRequest("default", "accounts.login", params);

            //if (response.getErrorCode() == 0) { //pending상태 수정가능하게 수정(임시변경)
            if (response.getErrorCode() == 0 || response.getErrorCode() == 206006 || response.getErrorCode() == 206001) {
            responseData.put("loginSuccesYn","Y");
            emailSendParams.put("lang", LocaleContextHolder.getLocale().getLanguage());
            emailSendParams.put("email", payload.get("loginUserId"));
            emailSendParams.put("status", "active");

            GSResponse emailSendResponse = gigyaService.executeRequest("default", "accounts.otp.sendCode", emailSendParams);
            if (emailSendResponse.getErrorCode() == 0) {
                responseData.put("emailSendSuccesYn","Y");
                try {
                    JsonNode otpResponseJson = objectMapper.readTree(emailSendResponse.getResponseText());

                    Map<String, Object> data = new HashMap<>();
                    data.put("vToken", otpResponseJson.get("vToken"));
                    String jsonData = "";
                    jsonData = objectMapper.writeValueAsString(data);

                    // 암호화 수행
                    String encryptedToken = EncryptUtil.encryptData(jsonData);
                    session.setAttribute("vToken", encryptedToken);
                } catch (JsonProcessingException e) {
                    log.error("Failed to parse JSON response", e);
                }
            } else {
                responseData.put("emailSendSuccesYn","N");
            }
        } else {
            responseData.put("loginSuccesYn","N");
        }
        return responseData;
    }

    public String sendEmailCode(Map<String, String> payload, HttpSession session,RedirectAttributes redirectAttributes) {
        ObjectMapper objectMapper = new ObjectMapper();
        String successYn = "N";
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("lang", LocaleContextHolder.getLocale().getLanguage());
            params.put("email", payload.get("loginUserId"));
            params.put("status", "active");

            GSResponse response = gigyaService.executeRequest("default", "accounts.otp.sendCode", params);
            if (response.getErrorCode() == 0) {
                JsonNode otpResponseJson = objectMapper.readTree(response.getResponseText());

                Map<String, Object> data = new HashMap<>();
                data.put("vToken", otpResponseJson.get("vToken"));
                String jsonData = "";
                jsonData = objectMapper.writeValueAsString(data);

                // 암호화 수행
                String encryptedToken = EncryptUtil.encryptData(jsonData);
                session.setAttribute("vToken", encryptedToken);

                if (response.getErrorCode() == 0) {
                    successYn = "Y";
                } else {
                    successYn = "N";
                }
            } else {
                log.error("email send fall");
                redirectAttributes.addFlashAttribute("showErrorMsg", "email send fall");
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON response", e);
            redirectAttributes.addFlashAttribute("showErrorMsg", "emailSned Error");
        }
        return successYn;
    }

    public String emailVerified(Map<String, String> payload, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String successYn = "N";
        try {
            String vTokenEncrypted = (String) session.getAttribute("vToken");
            if (vTokenEncrypted == null) {
                redirectAttributes.addFlashAttribute("showErrorMsg", "Token not found in session");
                log.error("Token not found in session");
            } else {
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

                if (response.getErrorCode() == 0) {
                    session.removeAttribute("vToken"); // 세션에서 'vToken'을 제거합니다.
                    successYn = "Y";
                } else if (response.getErrorCode() == 400006 && "Token has been revoked".equals(response.getErrorDetails())){
                    successYn = "D";
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            redirectAttributes.addFlashAttribute("showErrorMsg", "Token expired or invalid, please request a new token");
        }
        return successYn;
    }

    public RedirectView selectedChannel(String channel,String channelDisplayName, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        session.setAttribute("session_channel",channel);
        session.setAttribute("session_display_channel",channelDisplayName);

        cdcTraitService.setAdminSession(session);

        return new RedirectView((String) session.getAttribute("selectedChannelRedirectUrl"));
    }

    public ModelAndView approvalList(HttpServletRequest request, HttpSession session, Model model) {
        ModelAndView modelAndView = new ModelAndView("myPage");
        String menu = "approvalList";
        String content = "fragments/myPage/" + menu;

        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);

        return modelAndView;
    }

    public ModelAndView consentManager(HttpServletRequest request, HttpSession session, Model model) {
        ModelAndView modelAndView = new ModelAndView("myPage");

        try {
            // uid 하드코딩 47835cb8b053490294937e333ad5ceb3
            String hUid = "47835cb8b053490294937e333ad5ceb3";
            String uid;

            // 1. 사용자 정보 가져오기
            @SuppressWarnings("unused")
            JsonNode cdcUser = null;
            if (session.getAttribute("cdc_uid") != null) {
                cdcUser = cdcTraitService.getCdcUser((String) session.getAttribute("cdc_uid"), 0);
                uid = (String) session.getAttribute("cdc_uid");
            } else {
                cdcUser = cdcTraitService.getCdcUser(hUid, 0);
                uid = hUid;
            }
            log.warn("uid ::: {}", uid);

            String channel = (String) session.getAttribute("session_channel");

            modelAndView.addObject("consents", getConsentManagerList(channel, "", ""));

            // common code
            // channel (channels 테이블 > value : channel_name / name : channel_display_name)
            modelAndView.addObject("channel", channelRepository.selectChannelTypeList(""));

            modelAndView.addObject("session_channel", channel);
            modelAndView.addObject("session_display_channel", session.getAttribute("session_display_channel"));
            
            boolean isChannelAdmin = Optional.ofNullable(session.getAttribute("cdc_channeladmin"))
                .map(attr -> (Boolean) attr)
                .orElse(false);

            boolean isCiamAdmin = Optional.ofNullable(session.getAttribute("cdc_ciamadmin"))
                .map(attr -> (Boolean) attr)
                .orElse(false);

            // testRole 2가지 : channel, ciam (이외에는 접근불가하며, 운영서버에서는 제거)
            String testRole =  (request.getParameter("testRole") != null) ? request.getParameter("testRole") : "";
            if(testRole.toLowerCase().contains("channel")) {
                modelAndView.addObject("role", "Channel Admin");
            } else if(testRole.toLowerCase().contains("ciam")) {
                modelAndView.addObject("role", "CIAM Admin");
            } else if (isChannelAdmin) {
                modelAndView.addObject("role", "Channel Admin");
            } else if(isCiamAdmin) {
                modelAndView.addObject("role", "CIAM Admin");
            } else {
                // role (테스트 데이터 포함 임시로 지정)
                modelAndView.addObject("role", session.getAttribute("btp_myrole"));
            }

            // type (consent_types 테이블 > value : id / name : name_en)
            modelAndView.addObject("type", consentTypesRepository.findAll());
            // log.warn("type : {}", consentTypesRepository.findAll());
            // location (cis_countries 테이블 > value : country_code / name : name_en)
            modelAndView.addObject("location", cisCountryRepository.findAllOrderedByNameEn());
            // log.warn("location : {}", cisCountryRepository.findAllOrderedByNameEn());

            // Create New Consent 팝업화면을 위한 데이터 2가지
            modelAndView.addObject("consentGroup", consentRepository.selectDistinctGroupId());
            log.warn("consentGroup : {}", consentRepository.selectDistinctGroupId());
            // 9/8 new subsidiary로 변경
            modelAndView.addObject("subsidiary", newSubsidiaryRepository.findByCompanyAbbreviationOrderByIdAsc());

        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error("Error consentManager processing failed", e);
        }

        String menu = "consentManager";
        String content = "fragments/myPage/" + menu;

        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);

        return modelAndView;
    }
    
    public RedirectView consentManager(HttpServletRequest request, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // ModelAndView modelAndView = new ModelAndView("myPage");

        try {
            // uid 하드코딩 47835cb8b053490294937e333ad5ceb3
            String hUid = "47835cb8b053490294937e333ad5ceb3";
            String uid;

            // 1. 사용자 정보 가져오기
            @SuppressWarnings("unused")
            JsonNode cdcUser = null;
            if (session.getAttribute("cdc_uid") != null) {
                cdcUser = cdcTraitService.getCdcUser((String) session.getAttribute("cdc_uid"), 0);
                uid = (String) session.getAttribute("cdc_uid");
            } else {
                cdcUser = cdcTraitService.getCdcUser(hUid, 0);
                uid = hUid;
            }
            log.warn("uid ::: {}", uid);

            String channel = (String) session.getAttribute("session_channel");

            redirectAttributes.addFlashAttribute("consents", getConsentManagerList(channel, "", ""));
            // modelAndView.addObject("consents", getConsentManagerList(channel, "", ""));

            // common code
            // channel (channels 테이블 > value : channel_name / name : channel_display_name)
            redirectAttributes.addFlashAttribute("channel", channelRepository.selectChannelTypeList(""));
            // modelAndView.addObject("channel", channelRepository.selectChannelTypeList(""));

            redirectAttributes.addFlashAttribute("session_channel", channel);
            // modelAndView.addObject("session_channel", channel);
            redirectAttributes.addFlashAttribute("session_display_channel", session.getAttribute("session_display_channel"));
            // modelAndView.addObject("session_display_channel", session.getAttribute("session_display_channel"));
            
            boolean isChannelAdmin = Optional.ofNullable(session.getAttribute("cdc_channeladmin"))
                .map(attr -> (Boolean) attr)
                .orElse(false);

            boolean isCiamAdmin = Optional.ofNullable(session.getAttribute("cdc_ciamadmin"))
                .map(attr -> (Boolean) attr)
                .orElse(false);

            // testRole 2가지 : channel, ciam (이외에는 접근불가하며, 운영서버에서는 제거)
            String testRole =  (request.getParameter("testRole") != null) ? request.getParameter("testRole") : "";
            if(testRole.toLowerCase().contains("channel")) {
                redirectAttributes.addFlashAttribute("role", "Channel Admin");
                // modelAndView.addObject("role", "Channel Admin");
            } else if(testRole.toLowerCase().contains("ciam")) {
                redirectAttributes.addFlashAttribute("role", "CIAM Admin");
                // modelAndView.addObject("role", "CIAM Admin");
            } else if (isChannelAdmin) {
                redirectAttributes.addFlashAttribute("role", "Channel Admin");
                // modelAndView.addObject("role", "Channel Admin");
            } else if(isCiamAdmin) {
                redirectAttributes.addFlashAttribute("role", "CIAM Admin");
                // modelAndView.addObject("role", "CIAM Admin");
            } else {
                // role (테스트 데이터 포함 임시로 지정)
                redirectAttributes.addFlashAttribute("role", session.getAttribute("btp_myrole"));
                // modelAndView.addObject("role", session.getAttribute("btp_myrole"));
            }

            // type (consent_types 테이블 > value : id / name : name_en)
            redirectAttributes.addFlashAttribute("type", consentTypesRepository.findAll());
            // modelAndView.addObject("type", consentTypesRepository.findAll());
            // log.warn("type : {}", consentTypesRepository.findAll());
            // location (cis_countries 테이블 > value : country_code / name : name_en)
            redirectAttributes.addFlashAttribute("location", cisCountryRepository.findAllOrderedByNameEn());
            // modelAndView.addObject("location", cisCountryRepository.findAllOrderedByNameEn());
            // log.warn("location : {}", cisCountryRepository.findAllOrderedByNameEn());

            // Create New Consent 팝업화면을 위한 데이터 2가지
            redirectAttributes.addFlashAttribute("consentGroup", consentRepository.selectDistinctGroupId());
            // modelAndView.addObject("consentGroup", consentRepository.selectDistinctGroupId());
            log.warn("consentGroup : {}", consentRepository.selectDistinctGroupId());
            // 9/25 new subsidiary로 변경
            redirectAttributes.addFlashAttribute("subsidiary", newSubsidiaryRepository.findByCompanyAbbreviationOrderByIdAsc());

        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error("Error consentManager processing failed", e);
        }

        String menu = "consentManager";
        // String content = "fragments/myPage/" + menu;

        // modelAndView.addObject("content", content);
        // modelAndView.addObject("menu", menu);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/myPage/consentManager"); // go to get
        // redirectAttributes.addFlashAttribute("content", content);
        // redirectAttributes.addFlashAttribute("menu", menu);
        return redirectView;
        // return modelAndView;
    }

    public ModelAndView approvalRequest(HttpServletRequest request, HttpSession session, Model model) {
        ModelAndView modelAndView = new ModelAndView("myPage");
        String menu = "approvalRequest";
        String content = "fragments/myPage/" + menu;

        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);

        return modelAndView;
    }

    public RedirectView consentDetail(Map<String, String> payload, HttpServletRequest request, HttpSession session,RedirectAttributes redirectAttributes) {
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/myPage/consentDetail"); // go to get
        redirectAttributes.addFlashAttribute("payloadData", payload);
        session.setAttribute("consentDetail", payload);
        return redirectView;
    }

    public ModelAndView consentDetail(Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        ModelAndView modelAndView = new ModelAndView("myPage");
        String menu = "consentManager";
        String content = "fragments/myPage/consentDetail";

        Integer consentId = Integer.parseInt(payload.get("selectedConsentId"));
        String groupId = consentRepository.getGroupId(consentId);
        List<Map<String, Object>> detailList = getConsentManagerDetailList(consentId, null);

        modelAndView.addObject("consents", detailList);
        if (groupId != null) {
            modelAndView.addObject("allConsents", getConsentManagerDetailList(consentId, groupId));
        } else {
            modelAndView.addObject("allConsents", detailList);
        }
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("payload", payload);

        modelAndView.addObject("consentLanguage", consentLanguagesRepository.findAll());
        log.warn("consentLanguage : {}", consentLanguagesRepository.findAll());

        return modelAndView;
    }

    public RedirectView consentHistory(Map<String, String> payload, HttpServletRequest request, HttpSession session,RedirectAttributes redirectAttributes) {
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/myPage/consentHistory"); // go to get
        redirectAttributes.addFlashAttribute("payloadData", payload);
        session.setAttribute("payloadData", payload);
        return redirectView;
    }

    // 9/24 consentHistory에도 allConsents 추가예정
    public ModelAndView consentHistory(Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        ModelAndView modelAndView = new ModelAndView("myPage");
        String menu = "consentManager";
        String content = "fragments/myPage/consentHistory";

        Integer consentId = Integer.parseInt(payload.get("selectedConsentId"));
        String groupId = consentRepository.getGroupId(consentId);
        List<Map<String, Object>> historyList = getConsentManagerHistoryList(consentId, null);

        // history 데이터로 변경
        modelAndView.addObject("consents", historyList);
        if (groupId != null) {
            modelAndView.addObject("allConsents", getConsentManagerHistoryList(consentId, groupId));
        } else {
            modelAndView.addObject("allConsents", historyList);
        }
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("payload", payload);

        return modelAndView;
    }

    public ModelAndView createNewConsent(HttpServletRequest request, HttpSession session, Model model) {
        ModelAndView modelAndView = new ModelAndView("myPage");
        String menu = "consentManager";
        String content = "fragments/myPage/createNewConsent";

        modelAndView.addObject("consent", session.getAttribute("consent"));
        modelAndView.addObject("payload", session.getAttribute("payload"));
        modelAndView.addObject("channel", session.getAttribute("channel"));
        modelAndView.addObject("type", session.getAttribute("type"));
        modelAndView.addObject("consentLanguage", session.getAttribute("consentLanguage"));

        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);

        return modelAndView;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getConsentManagerList(String channel, String type, String location) {
        String query = "/* UserProfileService.getConsentManagerList */ select id, a.coverage as channel, CASE WHEN (select channel_display_name from channels where channel_name = a.coverage) IS NULL then 'COMMON' ELSE (select channel_display_name from channels where channel_name = a.coverage) END AS channelNm, a.type_id as consentType, (select name_en from consent_types where id = a.type_id) as consentTypeNm,  a.countries as location," +
                       " (          SELECT string_agg(cc.name_en, ', ' ORDER BY array_position(string_to_array(a.countries, ',')::varchar[], cc.country_code)) FROM cis_countries cc WHERE cc.country_code IN (select unnest(string_to_array(regexp_replace(a.countries, ' +', '', 'g'), ',')))      ) AS locationNm, " + 
                       " a.subsidiary, a.group_id as consentGroup, a.created_at as createDate, a.updated_at as updateDate from consents a " + 
                       " where a.coverage = :channel and ('' = :type or a.type_id = :type) and ('' like '%' || :location || '%' or a.countries like '%' || :location || '%')" +
                       " order by a.id desc";

        List<Object[]> results = entityManager.createNativeQuery(query)
                                    .setParameter("channel", channel)
                                    .setParameter("type", type)
                                    .setParameter("location", location)
                                    .getResultList();        

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        List<Map<String, Object>> mappedResults = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> termsMap = new HashMap<>();
            termsMap.put("consentId", result[0]);
            termsMap.put("channel", result[1]);
            termsMap.put("channelNm", result[2]);
            termsMap.put("consentType", result[3]);
            termsMap.put("consentTypeNm", result[4]);
            termsMap.put("location", result[5]);
            termsMap.put("locationNm", result[6]);
            termsMap.put("subsidiary", result[7]);
            termsMap.put("consentGroup", result[8]);
            termsMap.put("createDate", result[9] != null ? dateFormatter.format((Date) result[9]) : "");
            termsMap.put("updateDate", result[10] != null ? dateFormatter.format((Date) result[10]) : "");
            mappedResults.add(termsMap);
        }
        
        return mappedResults;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getConsentManagerDetailList(Integer consentId, String groupId) {
        List<Object[]> results;
        String query = "/* UserProfileService.getConsentManagerDetailList */ select"
                        + " a.group_id as consentGroup,"
                        + " b.consent_id as consentId,"
                        + " a.coverage as channel, "
                        + " CASE WHEN (select channel_display_name from channels where channel_name = a.coverage) IS NULL then 'COMMON' "
                        + "     ELSE (select channel_display_name from channels where channel_name = a.coverage) "
                        + " END AS channelNm,"
                        + " a.type_id as consentType,"
                        + " (select name_en from consent_types where id = a.type_id) as consentTypeNm,"
                        + " a.countries as location, ( "
                        + "         SELECT  "
                        + "             string_agg(cc.name_en, ', ' ORDER BY array_position(string_to_array(a.countries, ',')::varchar[], cc.country_code)) "
                        + "         FROM  "
                        + "             cis_countries cc "
                        + "         WHERE  "
                        + "             cc.country_code IN (select unnest(string_to_array(regexp_replace(a.countries, '\s+', '', 'g'), ','))) "
                        + "     ) AS locationNm, "
                        + " b.version," 
                        + " a.subsidiary,"
                        + " (SELECT company_name FROM new_subsidiary WHERE company_abbreviation = a.subsidiary) as subsidiaryNm,"
                        + " language_id as language,"
                        + " (select name_en from consent_languages where id = b.language_id) as languageNm,"
                        + " b.created_at as createDate,"
                        + " b.released_at as releaseDate,"
                        + " b.status_id as status, (select name_en from consent_statuses where id = b.status_id) as statusNm, b.content, b.id"
                        + " from consents a join consent_contents b on a.id = b.consent_id "
                        + "where ";
                    if (groupId != null) {
                query += " a.group_id = :groupId";
                query += " and b.status_id in (select id from consent_statuses where is_editable = true) order by b.consent_id desc, b.version desc, b.id desc";
                results = entityManager.createNativeQuery(query)
                        .setParameter("groupId", groupId)
                        .getResultList();
                    } else {
                query += " b.consent_id = :consentId";
                query += " and b.status_id in (select id from consent_statuses where is_editable = true) order by b.consent_id desc, b.version desc, b.id desc";
                results = entityManager.createNativeQuery(query)
                        .setParameter("consentId", consentId)
                        .getResultList();
                    }

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        List<Map<String, Object>> mappedResults = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> termsMap = new HashMap<>();
            termsMap.put("consentGroup", result[0]);
            termsMap.put("consentId", result[1]);
            termsMap.put("channel", result[2]);
            termsMap.put("channelNm", result[3]);
            termsMap.put("consentType", result[4]);
            termsMap.put("consentTypeNm", result[5]);
            termsMap.put("location", result[6]);
            termsMap.put("locationNm", result[7]);
            termsMap.put("version", result[8]);
            termsMap.put("subsidiary", result[9]);
            termsMap.put("subsidiaryNm", result[10]);
            termsMap.put("language", result[11]);
            termsMap.put("languageNm", result[12]);
            termsMap.put("createDate", result[13] != null ? dateFormatter.format((Date) result[13]) : "");
            termsMap.put("updateDate", result[14] != null ? dateFormatter.format((Date) result[14]) : "");
            termsMap.put("status", result[15]);
            termsMap.put("statusNm", result[16]);
            termsMap.put("content", result[17]);
            termsMap.put("id", result[18]);
            mappedResults.add(termsMap);
        }

        return mappedResults;
    }

    public RedirectView createNewConsentContainGroup(Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/myPage/createNewConsent"); // go to get

        String consentGroup = payload.get("selectedConsentGroupCondition");
        log.warn("consentGroup : ", consentGroup);
        if(consentGroup != "") {
            session.setAttribute("consent", getConsentList(consentGroup, payload));
        } else {
            session.setAttribute("consent", getConsentList("", payload));
        }

        session.setAttribute("payload", payload);
        session.setAttribute("channel", channelRepository.selectChannelTypeList(""));
        session.setAttribute("type", consentTypesRepository.findAll());
        session.setAttribute("consentLanguage", consentLanguagesRepository.findAll());
        log.warn("consentLanguage : {}", consentLanguagesRepository.findAll());

        return redirectView;
    }

    // create consent에서 consent 기준으로 조회
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getConsentList(String groupId, Map<String, String> payload) {
        List<Object[]> results;
        String query = "/* UserProfileService.getConsentList */ select"
                        + " a.group_id as consentGroup,"
                        + " a.id as consentId,"
                        + " a.coverage as channel, "
                        + " CASE WHEN (select channel_display_name from channels where channel_name = a.coverage) IS NULL then 'COMMON' "
                        + "     ELSE (select channel_display_name from channels where channel_name = a.coverage) "
                        + " END AS channelNm,"
                        + " a.type_id as consentType,"
                        + " (select name_en from consent_types where id = a.type_id) as consentTypeNm,"
                        + " a.countries as location, ( "
                        + "         SELECT  "
                        + "             string_agg(cc.name_en, ', ' ORDER BY array_position(string_to_array(a.countries, ',')::varchar[], cc.country_code)) "
                        + "         FROM  "
                        + "             cis_countries cc "
                        + "         WHERE  "
                        + "             cc.country_code IN (select unnest(string_to_array(regexp_replace(a.countries, '\s+', '', 'g'), ','))) "
                        + "     ) AS locationNm, "
                        + " a.subsidiary,"
                        + " case when (subsidiary='ALL') then 'ALL' else (SELECT company_name FROM new_subsidiary WHERE company_abbreviation = a.subsidiary) end as subsidiaryNm,"        
                        + " a.default_language as defaultLanguage"
                        + " from consents a";
                if (groupId.equals("")){
                    query += " where a.group_id is null and a.coverage = :channel and a.type_id = :type and a.countries like '%'||:location||'%' and ('ALL' = :subsidiary or a.subsidiary = :subsidiary)";

                    results = entityManager.createNativeQuery(query)
                            .setParameter("channel", payload.get("selectedChannelCondition"))
                            .setParameter("type", payload.get("selectedTypeCondition"))
                            .setParameter("location", payload.get("selectedLocationCondition"))
                            .setParameter("subsidiary", payload.get("selectedSubsidiaryCondition"))
                            .getResultList();
                } else {
                    query += " where a.group_id = :groupId";

                    results = entityManager.createNativeQuery(query)
                            .setParameter("groupId", groupId)
                            .getResultList();
                }

        List<Map<String, Object>> mappedResults = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> termsMap = new HashMap<>();
            termsMap.put("consentGroup", result[0]);
            termsMap.put("consentId", result[1]);
            termsMap.put("channel", result[2]);
            termsMap.put("channelNm", result[3]);
            termsMap.put("consentType", result[4]);
            termsMap.put("consentTypeNm", result[5]);
            termsMap.put("location", result[6]);
            termsMap.put("locationNm", result[7]);
            termsMap.put("subsidiary", result[8]);
            termsMap.put("subsidiaryNm", result[9]);
            termsMap.put("defaultLanguage", result[10]);
            mappedResults.add(termsMap);
        }

        return mappedResults;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getConsentManagerHistoryList(Integer consentId, String groupId) {
        List<Object[]> results;
        String query = "/* UserProfileService.getConsentManagerHistoryList */ select"
                        + " a.group_id as consentGroup,"
                        + " b.consent_id as consentId,"
                        + " a.coverage as channel, "
                        + " CASE WHEN (select channel_display_name from channels where channel_name = a.coverage) IS NULL then 'COMMON' "
                        + "     ELSE (select channel_display_name from channels where channel_name = a.coverage) "
                        + " END AS channelNm,"
                        + " a.type_id as consentType,"
                        + " (select name_en from consent_types where id = a.type_id) as consentTypeNm,"
                        + " a.countries as location, ( "
                        + "         SELECT  "
                        + "             string_agg(cc.name_en, ', ' ORDER BY array_position(string_to_array(a.countries, ',')::varchar[], cc.country_code)) "
                        + "         FROM  "
                        + "             cis_countries cc "
                        + "         WHERE  "
                        + "             cc.country_code IN (select unnest(string_to_array(regexp_replace(a.countries, '\s+', '', 'g'), ','))) "
                        + "     ) AS locationNm, "
                        + " b.version," 
                        + " a.subsidiary,"
                        + " (SELECT company_name FROM new_subsidiary WHERE company_abbreviation = a.subsidiary) as subsidiaryNm,"
                        + " language_id as language,"
                        + " (select name_en from consent_languages where id = b.language_id) as languageNm,"
                        + " b.created_at as createDate,"
                        + " b.released_at as releaseDate,"
                        + " b.status_id as status, (select name_en from consent_statuses where id = b.status_id) as statusNm,"
                        + " b.content, b.id"
                        + " from consents a join consent_contents b on a.id = b.consent_id "
                        + "where ";
                        if (groupId != null) {
                    query += " a.group_id = :groupId";
                    query += " order by b.consent_id desc, b.version desc, b.id desc";
                    results = entityManager.createNativeQuery(query)
                            .setParameter("groupId", groupId)
                            .getResultList();
                        } else {
                    query += " b.consent_id = :consentId";
                    query += " order by b.consent_id desc, b.version desc, b.id desc";
                    results = entityManager.createNativeQuery(query)
                            .setParameter("consentId", consentId)
                            .getResultList();
                        }
                        
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        List<Map<String, Object>> mappedResults = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> termsMap = new HashMap<>();
            termsMap.put("consentGroup", result[0]);
            termsMap.put("consentId", result[1]);
            termsMap.put("channel", result[2]);
            termsMap.put("channelNm", result[3]);
            termsMap.put("consentType", result[4]);
            termsMap.put("consentTypeNm", result[5]);
            termsMap.put("location", result[6]);
            termsMap.put("locationNm", result[7]);
            termsMap.put("version", result[8]);
            termsMap.put("subsidiary", result[9]);
            termsMap.put("subsidiaryNm", result[10]);
            termsMap.put("language", result[11]);
            termsMap.put("languageNm", result[12]);
            termsMap.put("createDate", result[13] != null ? dateFormatter.format((Date) result[13]) : "");
            termsMap.put("updateDate", result[14] != null ? dateFormatter.format((Date) result[14]) : "");
            termsMap.put("status", result[15]);
            termsMap.put("statusNm", result[16]);
            termsMap.put("content", result[17]);
            termsMap.put("id", result[18]);
            mappedResults.add(termsMap);
        }

        return mappedResults;
    }

    public ModelAndView approvalConfiguration(Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        ModelAndView modelAndView = new ModelAndView("myPage");
        String menu = "approvalConfiguration";
        String content = "fragments/myPage/" + menu;

        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);

        String channel = (String) session.getAttribute("session_channel");

        // common code
        // channel (channels 테이블 > value : channel_name / name : channel_display_name)
        modelAndView.addObject("channel", channelRepository.selectChannelTypeList(""));

        modelAndView.addObject("session_channel", channel);
        modelAndView.addObject("session_display_channel", session.getAttribute("session_display_channel"));
        
        boolean isChannelAdmin = Optional.ofNullable(session.getAttribute("cdc_channeladmin"))
            .map(attr -> (Boolean) attr)
            .orElse(false);

        boolean isCiamAdmin = Optional.ofNullable(session.getAttribute("cdc_ciamadmin"))
            .map(attr -> (Boolean) attr)
            .orElse(false);

        // testRole 2가지 : channel, ciam (이외에는 접근불가하며, 운영서버에서는 제거)
        String testRole =  (request.getParameter("testRole") != null) ? request.getParameter("testRole") : "";
        // String channelCode;
        if(testRole.toLowerCase().contains("channel")) {
            modelAndView.addObject("role", "Channel Admin");
        } else if(testRole.toLowerCase().contains("ciam")) {
            modelAndView.addObject("role", "CIAM Admin");
        } else if (isChannelAdmin) {
            modelAndView.addObject("role", "Channel Admin");
        } else if (isCiamAdmin) {
            modelAndView.addObject("role", "CIAM Admin");
        } else {
            // role (테스트 데이터 포함 임시로 지정)
            modelAndView.addObject("role", session.getAttribute("btp_myrole"));
        } 
        
        modelAndView.addObject("location", cisCountryRepository.findAllOrderedByNameEn());
        modelAndView.addObject("executionCondition", commonCodeRepository.findByHeader("EXECUTION_CONDITION_CODE"));
        modelAndView.addObject("approver", commonCodeRepository.findByHeader("ROLE_CODE"));
        modelAndView.addObject("approverFormat", commonCodeRepository.findByHeader("APPROVE_FORMAT_CODE"));
        modelAndView.addObject("approvalCondition", commonCodeRepository.findByHeader("APPROVE_CONDITION_CODE"));
        modelAndView.addObject("requestType", commonCodeRepository.findByHeaderOrderBySortOrder("REQUEST_TYPE_CODE"));
        
        // 9/8 new subsidiary로 변경
        modelAndView.addObject("subsidiary", newSubsidiaryRepository.findByCompanyAbbreviationOrderByIdAsc());

        List<CommonCode> divisions = commonCodeRepository.findByHeader("DIVISION_CODE");
        modelAndView.addObject("divisions", divisions);

        modelAndView.addObject("approvalConfiguration", approvalConfigurationService.searchApprovalConfiguration(payload, session));
        modelAndView.addObject("stage", approvalConfigurationService.getApprovalRuleMasterStage(payload, session));
        
        return modelAndView;
    }

    public String deleteSsoAccess(String targetChannel, HttpSession session) {
        try {
            // CDC User Profile 가져오기
            String uid = (String) session.getAttribute("cdc_uid");
            JsonNode CDCUserProfile = cdcTraitService.getCdcUser(uid, 0);

            // channels 객체 가져오기
            ObjectNode channelsNode = (ObjectNode) CDCUserProfile.path("data").path("channels");

            // 대상 채널 데이터 가져오기
            ObjectNode channelNode = (ObjectNode) channelsNode.get(targetChannel);

            if (channelNode != null) {
                // approvalStatus를 "disabled"로 설정
                channelNode.put("approvalStatus", "disabled");

                // approvalStatus 필드를 제외한 모든 필드를 ""로 설정
                Iterator<String> fieldNames = channelNode.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    if (!"approvalStatus".equals(fieldName)) {
                        channelNode.put(fieldName, ""); // 필드 값을 ""로 설정
                    }
                }

                // 수정된 데이터로 dataFields 설정
                Map<String, Object> dataFields = new HashMap<>();
                dataFields.put("channels", channelsNode);

                // cdcParams 설정
                Map<String, Object> cdcParams = new HashMap<>();
                cdcParams.put("data", objectMapper.writeValueAsString(dataFields));
                cdcParams.put("UID", uid);

                // Gigya 서비스로 setAccountInfo 요청 실행
                GSResponse response = gigyaService.executeRequest("defaultChannel", "accounts.setAccountInfo", cdcParams);
                log.info("setAccountInfo response: {}", response.getResponseText());

                String channelType = channelRepository.selectChannelTypeSearch(targetChannel);

                Optional<Channels> optionalChannelObj = channelRepository.selectByChannelName(targetChannel);
                Channels channelObj = optionalChannelObj.get();
                Map<String, Object> configMap = channelObj.getConfigMap();

                Boolean userProvisioning = configMap != null && configMap.containsKey("java_useprovisioning")
                        ? (Boolean) configMap.get("java_useprovisioning")
                        : false;

                if (userProvisioning) {
                    cpiApiService.sendUidProvisioningNoConfigChecking(targetChannel, "D",uid);
                }

                if (response.getErrorCode() == 0) {
                    approvalAdminRepository.updateFalseStatus(uid,targetChannel);
                    return "Y";
                } else {
                    return "N";
                }
            } else {
                log.warn("Channel {} not found for user {}", targetChannel, uid);
                return "N";
            }

        } catch (Exception e) {
            log.error("Error updating account info", e);
            return "N";
        }
    }
}