package com.samsung.ciam.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSResponse;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import java.util.*;


@Slf4j
@Service
public class CompanyProfileService {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private CisCountryRepository cisCountryRepository;
    
    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private ChannelAddFieldRepository channelAddFieldRepository;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ChannelRepository channelRepository;


    // 24.07.24 kimjy 추가 - personalInformation 화면 예비 URL
    @SuppressWarnings("unchecked")
    public ModelAndView companyInformation(HttpServletRequest request, HttpSession session) {
        Map<String, Object> jsonResponse = new HashMap<>();
        Map<String, Object> cdcParams = new HashMap<>();
        Map<String, Object> companyDataResult = null;
        Map<String, Object> companyObject = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        List<CisCountry> countries = getCountries();
        JsonNode responseData = null;
        JsonNode cdcUser = null;
        JsonNode info = null;
        String type="";
        String socialProviders ="";

        /*
        for (CisCountry country : countries) {
            log.warn("Country: {}", country);
        }
        */
        //$bpid = session()->get('cdc_companyid');
        //String bpid = "NERP-FA48HR";
        //bpid로 Company 정보를 조회해서 가져온다. 
        String bpid = (String) session.getAttribute("cdc_companyid");
        cdcParams.put("bpid", bpid);

        try{
            log.warn("Start");
            GSResponse response = gigyaService.executeRequest("default", "accounts.b2b.getOrganizationInfo", cdcParams);
            responseData = objectMapper.readTree(response.getResponseText());
            type = responseData.path("type").asText("");
            String companyJson = objectMapper.writeValueAsString(responseData);
            cdcUser = cdcTraitService.getCdcUser((String) session.getAttribute("cdc_uid"), 0);
            socialProviders =  StringUtil.getNonEmptyValue(cdcUser.path("socialProviders"));
            log.warn("companyDataResult : {}", companyJson);
            // "info" 노드 추출
            info = responseData.path("info");
            // "info" 노드가 ObjectNode인지 확인하고 변환
            ObjectNode objectNode = objectMapper.createObjectNode();
            String channel = (String) session.getAttribute("session_channel");
            String channelType = channelRepository.selectChannelTypeSearch(channel);

            if (info.isObject()) {
                ObjectNode infoNode = (ObjectNode) info;
                objectNode.put("orgName", getNonEmptyValue(responseData.path("orgName")));
                objectNode.put("bpid", getNonEmptyValue(responseData.path("bpid")));

                objectNode.put("country", getNonEmptyValues(info.path("country")));
                objectNode.put("products", getNonEmptyValues(info.path("products")));
                objectNode.put("zip_code", getNonEmptyValues(info.path("zip_code")));
                objectNode.put("bizregno1", getNonEmptyValues(info.path("bizregno1")));
                if("VENDOR".equals(channelType)) {
                    objectNode.put("vendorcode", getNonEmptyValues(info.path("vendorcode")));
                } else {
                    objectNode.put("vendorcode", getNonEmptyValue(responseData.path("bpid")));
                }
                objectNode.put("channeltype	", getNonEmptyValues(info.path("channeltype	")));
                objectNode.put("name_search", getNonEmptyValues(info.path("name_search")));
                objectNode.put("phonenumber1", getNonEmptyValues(info.path("phonenumber1")));
                objectNode.put("street_address", getNonEmptyValues(info.path("street_address")));
            }

            // ObjectNode를 Map으로 변환
            companyDataResult = objectMapper.convertValue(objectNode, Map.class);
            companyDataResult.put("countries", countries);

            // 추가 필드 추출
            Map<String, Object> accountObject = new HashMap<>();
            Map<String, Object> regCompanyData = new HashMap<>();
            cdcTraitService.extractChannelAddFields(info, cdcUser, (String) session.getAttribute("session_channel"), accountObject, regCompanyData);

            String industryTypeValue = String.valueOf(info.path("industry_type"));
            industryTypeValue = industryTypeValue.replaceAll("[\\[\\]\"\\\\]", "");
            regCompanyData.put("industryType", industryTypeValue);

            // companyObject에 데이터를 넣어줌
            companyObject.put("registerCompany", regCompanyData);

            // 변환된 Map을 로그에 출력
            //log.warn("companyDataResult Map: {}", companyDataResult);

            //Audit Log
            Map<String,String> param = new HashMap<String,String>();
            param.put("type", "Company_Profile");
            param.put("action", "ListView");
            param.put("items", "General Information");
            auditLogService.addAuditLog(session, param);

        }catch (Exception e) {
            log.error("Error processing registration data", e);
            jsonResponse.put("error", "Exception occurred: " + e.getMessage());
        }


        ModelAndView modelAndView = new ModelAndView("myPage");
        String content = "fragments/myPage/companyProfile";
        String menu = "companyProfile";
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("companyDataResult", companyDataResult);
        modelAndView.addObject("companyObject", companyObject);
        modelAndView.addObject("companyObject", companyObject);
        modelAndView.addObject("socialProviders", socialProviders);

        Map<String, List<?>> fieldMap = cdcTraitService.generateFieldMap((String) session.getAttribute("session_channel"), objectMapper, true, false,true); // Company 데이터만 포함

        modelAndView.addObject("fieldMap", fieldMap);
        modelAndView.addObject("type", type);


        return modelAndView;

    }
    // Business Location 가져오기
    public List<CisCountry> getCountries() {
        return cisCountryRepository.findAllOrderedByNameEn();
    }

  
    public ModelAndView passwordReset(HttpServletRequest request, HttpSession session) {
        JsonNode cdcUser = null;
        String socialProviders ="";
        cdcUser = cdcTraitService.getCdcUser((String) session.getAttribute("cdc_uid"), 0);
        socialProviders =  StringUtil.getNonEmptyValue(cdcUser.path("socialProviders"));

        ModelAndView modelAndView = new ModelAndView("myPage");
        String content = "fragments/myPage/passwordReset";
        String menu = "passwordReset";
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("socialProviders", socialProviders);
        modelAndView.addObject("apiKey", BeansUtil.getApiKeyForChannel((String) session.getAttribute("session_channel")));

        return modelAndView;

    }
    // 기존 PW 확인
    public Map<String, Object> checkOldPassword(Map<String, String> payload,  HttpSession session) {
        Map<String, Object> registerMapper = new HashMap<>();
        Map<String, Object> jsonResponse = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        // 세션에서 이메일 값 가져오기
        String loginUserId = (String) session.getAttribute("cdc_email");
        // String loginUserId = "dragon02@yopmail.com"; // 테스트용
        registerMapper.put("loginID", loginUserId);
        registerMapper.put("password", payload.get("oldPwd"));
        try {
            GSResponse response = gigyaService.executeRequest("default", "accounts.login", registerMapper);
            if (response.getErrorCode() == 0) {
                jsonResponse.put("errorCode", 0);
                jsonResponse.put("status", "ok");
            } else {
                jsonResponse.put("errorCode", response.getErrorCode());
                jsonResponse.put("status", "Invalid password");
            }
        } catch (Exception e) {
            log.error("Error processing registration data", e);
            jsonResponse.put("error", "Exception occurred: " + e.getMessage());
        }

        return jsonResponse;
    }

    // PW 변경
    public RedirectView changePasswordSubmit(Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        Map<String, Object> registerMapper = new HashMap<>();
        Map<String, Object> jsonResponse = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        
        String UID = (String) session.getAttribute("cdc_uid");
        registerMapper.put("UID", UID);
        registerMapper.put("password", payload.get("oldPwd"));
        registerMapper.put("newPassword", payload.get("newPwd"));
        
        try {
            GSResponse response = gigyaService.executeRequest("default", "accounts.setAccountInfo", registerMapper);
            redirectAttributes.addFlashAttribute("responseErrorCode", response.getErrorCode());
            redirectAttributes.addFlashAttribute("responseErrorDetails", response.getErrorDetails());
            if (response.getErrorCode() == 0) {
                jsonResponse.put("errorCode", 0);
                jsonResponse.put("status", "ok");
                        //Audit Log
                Map<String,String> param = new HashMap<String,String>();
                param.put("type", "Password_Reset");
                param.put("action", "Modification");
                param.put("items", "Password Reset");
                auditLogService.addAuditLog(session, param);

                return new RedirectView(request.getHeader("Referer"));
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


    //MFA Setting 
    public ModelAndView mfaSetting(HttpServletRequest request, HttpSession session) {
        Map<String, Object> jsonResponse = new HashMap<>();
        Map<String, Object> mfaDataResult = null;
        String hUid = (String) session.getAttribute("cdc_uid");
        String socialProviders ="";

        try{
            JsonNode cdcUser = cdcTraitService.getCdcUser(hUid, 0);
            JsonNode data = cdcUser.path("data");
            JsonNode tfaMethods = data.path("tfaMethods");
            socialProviders =  StringUtil.getNonEmptyValue(cdcUser.path("socialProviders"));

            ObjectNode objectNode = objectMapper.createObjectNode();
            log.warn("data : {}", data);
            log.warn("data : {}", data.path("tfaMethods"));
            boolean gigyaEmail = tfaMethods.path("gigyaEmail").asBoolean();
            boolean gigyaTotp = tfaMethods.path("gigyaTotp").asBoolean();
            // Log the value
            log.warn("gigyaEmail value: {}", gigyaEmail);
            log.warn("gigyaTotp value: {}", gigyaTotp);
            if (data.isObject()) {
                objectNode.put("gigyaEmail", gigyaEmail );
                objectNode.put("gigyaTotp", gigyaTotp );
                mfaDataResult = objectMapper.convertValue(objectNode, Map.class);
            }else{


            }
            log.warn("mfaDataResult Map: {}", mfaDataResult);
    
        }catch (Exception e) {
            log.error("Error processing registration data", e);
            jsonResponse.put("error", "Exception occurred: " + e.getMessage());
        }

        //Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "MFA_Setting");
        param.put("action", "ListView");
        param.put("items", "MFA Setting");
        auditLogService.addAuditLog(session, param);

        ModelAndView modelAndView = new ModelAndView("myPage");
        String content = "fragments/myPage/mfaSetting";
        String menu = "mfaSetting";
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("mfaDataResult", mfaDataResult);
        modelAndView.addObject("socialProviders", socialProviders);
        modelAndView.addObject("apiKey", BeansUtil.getApiKeyForChannel((String) session.getAttribute("session_channel")));

        return modelAndView;
    }

    public RedirectView mfaSubmit(  Map<String, String> payload, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // HttpSession session = request.getSession();
        Map<String, Object> dataFields = new HashMap<>();
        Map<String, Boolean> tfaMethods = new HashMap<>();
        
        tfaMethods.put("gigyaEmail", "mfaEmail".equals(payload.get("mfa")));
        tfaMethods.put("gigyaTotp", "mfaTfa".equals(payload.get("mfa")));

        dataFields.put("tfaMethods", tfaMethods);
        Map<String, Object> params = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            params.put("data", objectMapper.writeValueAsString(dataFields));
            params.put("UID", (String) session.getAttribute("cdc_uid"));
            GSResponse response = gigyaService.executeRequest("default", "accounts.setAccountInfo", params);
            redirectAttributes.addFlashAttribute("responseErrorCode", response.getErrorCode());
            redirectAttributes.addFlashAttribute("responseErrorDetails", response.getErrorDetails());

            if (response.getErrorCode() == 0) { // 성공일때 
                redirectAttributes.addFlashAttribute("showErrorMsg", "MFA changed successfully");
                        //Audit Log
                Map<String,String> param = new HashMap<String,String>();
                param.put("type", "MFA_Setting");
                param.put("action", "Modification");
                param.put("items", "MFA Setting");
                auditLogService.addAuditLog(session, param);

                return new RedirectView(request.getHeader("Referer"));

            } else {
                redirectAttributes.addFlashAttribute("showErrorMsg", "MFA change failed");
                return new RedirectView(request.getHeader("Referer"));
            }

        }catch  (JsonProcessingException e) {
            redirectAttributes.addFlashAttribute("showErrorMsg", e.getMessage());
            return new RedirectView(request.getHeader("Referer"));
        }

    }



    //MFA Setting 
    public ModelAndView withdrawUser(HttpServletRequest request, HttpSession session) {
        JsonNode cdcUser = null;
        String socialProviders ="";
        cdcUser = cdcTraitService.getCdcUser((String) session.getAttribute("cdc_uid"), 0);
        socialProviders =  StringUtil.getNonEmptyValue(cdcUser.path("socialProviders"));

        ModelAndView modelAndView = new ModelAndView("myPage");
        String content = "fragments/myPage/withdrawUser";
        String menu = "withdrawUser";
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("socialProviders", socialProviders);
        modelAndView.addObject("apiKey", BeansUtil.getApiKeyForChannel((String) session.getAttribute("session_channel")));

        return modelAndView;

    }




    // JSON 배열에서 빈 문자열이 아닌 값만 추출하는 헬퍼 메서드
    private static String getNonEmptyValues(JsonNode jsonNode) {
        StringBuilder result = new StringBuilder();
        if (jsonNode.isArray()) {
            Iterator<JsonNode> elements = jsonNode.elements();
            while (elements.hasNext()) {
                String value = elements.next().asText();
                if (!value.isEmpty()) {
                    if (result.length() > 0) {
                        result.append(", ");
                    }
                    result.append(value);
                }
            }
        }
        return result.toString();
    }   
    // JSON 값에서 빈 문자열이 아닌 값을 추출하는 헬퍼 메서드
    private static String getNonEmptyValue(JsonNode jsonNode) {
        String value = jsonNode.asText();
        return value.isEmpty() ? null : value;
    }

    public ModelAndView selectChannel(HttpServletRequest request, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("myPage");
        String content = "fragments/myPage/selectChannel";
        modelAndView.addObject("content", content);
        modelAndView.addObject("channels", session.getAttribute("channels"));
       // modelAndView.addObject("channel_displayName", session.getAttribute("session_display_channel"));


        return modelAndView;

    }
}
