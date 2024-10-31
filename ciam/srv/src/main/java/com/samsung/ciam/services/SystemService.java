package com.samsung.ciam.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.gigya.service.GigyaService;
import com.samsung.ciam.models.*;
import com.samsung.ciam.repositories.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class SystemService {

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private CisCountryRepository cisCountryRepository;

    @Autowired
    private SecServingCountryRepository secServingCountryRepository;

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private ChannelAddFieldRepository channelAddFieldRepository;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private ApprovalAdminRepository approvalAdminRepository;

    @Autowired
    private NewSubsidiaryRepository newSubsidiaryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ModelAndView userManagment(HttpServletRequest request, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("myPage");
        String admintype = "";
        String channel = (String) session.getAttribute("session_channel");
        List<Channels> channels;

        String content = "fragments/myPage/userManagment";
        String menu = "userManagment";
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);

        // Get user type from session with null check
        boolean isCompanyAdmin = Optional.ofNullable(session.getAttribute("cdc_companyadmin"))
                .map(attr -> (Boolean) attr)
                .orElse(false);

        boolean isChannelAdmin = Optional.ofNullable(session.getAttribute("cdc_channeladmin"))
                .map(attr -> (Boolean) attr)
                .orElse(false);

        boolean isCiamAdmin = Optional.ofNullable(session.getAttribute("cdc_ciamadmin"))
                .map(attr -> (Boolean) attr)
                .orElse(false);

        boolean isChannelBizAdmin = Optional.ofNullable(session.getAttribute("cdc_channeladminType"))
                .map(attr -> (Boolean) attr)
                .orElse(false);

        boolean isTempApprover = Optional.ofNullable(session.getAttribute("cdc_tempApprover"))
                .map(attr -> (Boolean) attr)
                .orElse(false);

        if (isCompanyAdmin) {
            admintype = "Partner Admin";
            channels = channelRepository.selectChannelName(channel);
            modelAndView.addObject("channels", channels);

            JsonNode CDCUser = cdcTraitService.getCdcUser(session.getAttribute("cdc_uid").toString(), 0);
            String companyName = CDCUser.path("profile").path("work").path("company").asText("");
            String companyId = CDCUser.path("data").path("accountID").asText("");
            modelAndView.addObject("companyName", companyName);
            modelAndView.addObject("companyId", companyId);
        } else if (isChannelAdmin) {
            admintype = "Channel Admin";
            channels = channelRepository.selectChannelName(channel);
            modelAndView.addObject("channels", channels);
        } else if (isCiamAdmin) {
            admintype = "CIAM Admin";
            channels = channelRepository.findAll();
            modelAndView.addObject("channels", channels);
        } else if (isChannelBizAdmin) {
            admintype = "Channel biz Admin";
            channels = channelRepository.selectChannelName(channel);
            modelAndView.addObject("channels", channels);
        } else if (isTempApprover) {
            admintype = "Temp Approver";
            channels = channelRepository.selectChannelName(channel);
            modelAndView.addObject("channels", channels);
        } else {
            admintype = "General User";
        }

        modelAndView.addObject("role", admintype);
        return modelAndView;

    }

    public Map<String, Object> searchUserManagment(Map<String, String> allParams, HttpSession session) {
        List<Map<String, Object>> results = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> gridData = new HashMap<>();
        List<Map<String,Object>> resultUserList = new ArrayList<>();

        String channel = allParams.getOrDefault("channel",null);
        String companyName = allParams.getOrDefault("companyName",null);
        String userId = allParams.getOrDefault("userId",null);
        String companyId = allParams.getOrDefault("companyId",null);
        String firstName = allParams.getOrDefault("firstName",null);
        String lastName = allParams.getOrDefault("lastName",null);
//        String domain = allParams.getOrDefault("domain",null);


        StringBuilder query = new StringBuilder();
        query.append("SELECT * ")
                //.append(channel)  // 'channel'은 사용자 입력 값
                .append(" FROM accounts")
                .append(" WHERE data.userStatus IN ('active','inactive')");

        if (companyName != null && !companyName.isEmpty()) {
            query.append(" and groups.organizations.orgName LIKE '%").append(companyName).append("%'");
        }

        if (userId != null && !userId.isEmpty()) {
            query.append(" AND profile.username = '").append(userId).append("'");
        }

        if (companyId != null && !companyId.isEmpty()) {
            query.append(" AND data.accountID = '").append(companyId).append("'");
        }

        if (firstName != null && !firstName.isEmpty()) {
            query.append(" AND profile.firstName = '").append(firstName).append("'");
        }

        if (lastName != null && !lastName.isEmpty()) {
            query.append(" AND profile.lastName = '").append(lastName).append("'");
        }

//        if (domain != null && !domain.isEmpty() && domain.startsWith("@")) {
//            query.append(" AND profile.username LIKE '%").append(domain).append("'");
//            query.append(" AND profile.username LIKE '%").append(domain).append("'");
//        }

        boolean isChannelBizAdmin = Optional.ofNullable(session.getAttribute("cdc_channeladminType"))
                .map(attr -> (Boolean) attr)
                .orElse(false);

        if (isChannelBizAdmin) {
            String uid = (String) session.getAttribute("cdc_uid");
            JsonNode CDCUserProfile = cdcTraitService.getCdcUser(uid, 0);
            String country = approvalAdminRepository.selectApprovalAdminCountry(uid, channel);
            String subsidiary = approvalAdminRepository.selectApprovalAdminSubsidiary(uid, channel);

            boolean hasSubsidiaryCondition = subsidiary != null && !"ALL".equals(subsidiary);
            boolean hasCountryCondition = country != null && !"ALL".equals(country);

            // 조건이 있을 경우에만 AND 추가
            if (hasSubsidiaryCondition || hasCountryCondition) {
                query.append(" AND (");

                // subsidiary 조건 처리
                if (hasSubsidiaryCondition) {
                    String[] subsidiaries = subsidiary.split(",");

                    if (subsidiaries.length == 1) {
                        query.append("data.subsidiary = '").append(subsidiaries[0].trim()).append("'");
                    } else {
                        query.append("data.subsidiary IN (");
                        for (int i = 0; i < subsidiaries.length; i++) {
                            if (i > 0) {
                                query.append(", ");
                            }
                            query.append("'").append(subsidiaries[i].trim()).append("'");
                        }
                        query.append(")");
                    }
                }

                // country 조건 처리
                if (hasCountryCondition) {
                    if (hasSubsidiaryCondition) {
                        query.append(" OR ");
                    }

                    String[] countrys = country.split(",");

                    if (countrys.length == 1) {
                        query.append("profile.country = '").append(countrys[0].trim()).append("'");
                    } else {
                        query.append("profile.country IN (");
                        for (int i = 0; i < countrys.length; i++) {
                            if (i > 0) {
                                query.append(", ");
                            }
                            query.append("'").append(countrys[i].trim()).append("'");
                        }
                        query.append(")");
                    }
                }

                query.append(")");  // OR 조건 닫기
            }
        }
        query.append(" LIMIT 10000 ");

//        if (approvalStatus != null && !approvalStatus.isEmpty()) {
//            query.append(" AND data.channels.")
//                    .append(channel)
//                    .append(".approvalStatus = '").append(approvalStatus).append("'");
//        }

        // StringBuilder를 String으로 변환
        String finalQuery = query.toString();

        GSResponse response = gigyaService.executeRequest("default", "accounts.search", finalQuery);

        try {
            Map<String,Object> mapVal = objectMapper.readValue(response.getResponseText(), Map.class);
            resultUserList = (List<Map<String,Object>>)mapVal.get("results");

            if (resultUserList != null) {
                List<CommonCode> commonCodeList = commonCodeRepository.findByHeader("ROLE_CODE");
                List<Channels> channelsList = channelRepository.findAllBy();

                for (Map<String, Object> user : resultUserList) {
                    // 안전한 null 체크 후 profile.username 값을 가져옴
                    String uid = (String) user.get("UID");
                    Map<String, Object> profile = (Map<String, Object>) user.get("profile");
                    String loginId = "";
                    String companyNameValue = "";

                    if (profile != null) {
                        loginId = (String) profile.getOrDefault("username", "");

                        Map<String, Object> work = (Map<String, Object>) profile.get("work");
                        if (work != null) {
                            companyNameValue = (String) work.getOrDefault("company", "");
                        }
                    }

                    // 안전한 null 체크 후 data.accountID 값을 가져옴
                    Map<String, Object> data = (Map<String, Object>) user.get("data");
                    if (data != null) {
                        String companyIdValue = (String) data.getOrDefault("accountID", "");

                        Map<String, Object> channels = (Map<String, Object>) data.get("channels");

                        if (channel != null && !channel.isEmpty()) {
                            // channel 값이 주어진 경우 해당 채널만 체크
                            if (channels != null && channels.containsKey(channel)) {
                                Map<String, Object> channelData = (Map<String, Object>) channels.get(channel);
                                String approvalStatus = (String) channelData.getOrDefault("approvalStatus", "");

                                if ("approved".equals(approvalStatus)) {
                                    Map<String, Object> resultMap = new HashMap<>();
                                    resultMap.put("LoginId", loginId);
                                    resultMap.put("uid", uid);
                                    resultMap.put("companyName", companyNameValue);
                                    resultMap.put("companyId", companyIdValue);

                                    Boolean isCIAMAdmin = Optional.ofNullable((Boolean) data.get("isCIAMAdmin")).orElse(false);

                                    if (isCIAMAdmin) {
                                        resultMap.put("role", "CIAM Admin");
                                        resultMap.put("lastLoginDate", channelData.get("lastLogin"));
                                        resultMap.put("createDate", channelData.get("approvalStatusDate"));
                                    } else {
                                        populateResultMap(resultMap, channelData, commonCodeList);
                                    }
                                    resultMap.put("channel", channel);
                                    String channelDisplayName = channelsList.stream()
                                            .filter(ch -> ch.getChannelName().equals(channel))
                                            .map(Channels::getChannelDisplayName)
                                            .findFirst()
                                            .orElse(channel);

                                    resultMap.put("channelDisplayName", channelDisplayName);
                                    results.add(resultMap);
                                }
                            }
                        } else {
                            // channel 값이 주어지지 않은 경우 모든 채널을 체크
                            if (channels != null) {
                                for (String channelName : channels.keySet()) {
                                    Map<String, Object> channelData = (Map<String, Object>) channels.get(channelName);
                                    String approvalStatus = (String) channelData.getOrDefault("approvalStatus", "");

                                    if ("approved".equals(approvalStatus)) {
                                        Map<String, Object> resultMap = new HashMap<>();
                                        resultMap.put("LoginId", loginId);
                                        resultMap.put("uid", uid);
                                        resultMap.put("companyName", companyNameValue);
                                        resultMap.put("companyId", companyIdValue);
                                        resultMap.put("channel", channelName);

                                        String channelDisplayName = channelsList.stream()
                                                .filter(ch -> ch.getChannelName().equals(channelName))
                                                .map(Channels::getChannelDisplayName)
                                                .findFirst()
                                                .orElse(channelName);
                                        resultMap.put("channelDisplayName", channelDisplayName);


                                        Boolean isCIAMAdmin = Optional.ofNullable((Boolean) data.get("isCIAMAdmin")).orElse(false);

                                        if (isCIAMAdmin) {
                                            resultMap.put("role", "CIAM Admin");
                                            resultMap.put("lastLoginDate", channelData.get("lastLogin"));
                                            resultMap.put("createDate", channelData.get("approvalStatusDate"));
                                        } else {
                                            populateResultMap(resultMap, channelData, commonCodeList);
                                        }

                                        results.add(resultMap);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        gridData.put("result", results);
        return gridData;
    }

    public void populateResultMap(Map<String, Object> resultMap, Map<String, Object> channelData, List<CommonCode> commonCodeList) {
        // adminType 값을 가져옴
        Object adminTypeObj = channelData.get("adminType");
        String roleName = "";  // 기본값 설정

        if (adminTypeObj != null) {
            String adminType = adminTypeObj.toString();

            // commonCodeList에서 adminType 값에 맞는 name을 찾음
            for (CommonCode code : commonCodeList) {
                if (code.getCode().equals(adminType)) {
                    roleName = code.getName();
                    break;
                }
            }
        }
        if(roleName==null || roleName.isEmpty()) {
            roleName = "General User";
        }

        resultMap.put("role", roleName);
        resultMap.put("lastLoginDate", channelData.get("lastLogin"));
        resultMap.put("createDate", channelData.get("approvalStatusDate"));
    }

    public ModelAndView userManagmentDetail(HttpServletRequest request, HttpSession session, Map<String, String> payload) {
        ModelAndView modelAndView = new ModelAndView("myPage");
        ObjectMapper objectMapper = new ObjectMapper();
        List<Channels> channels;
        List<CommonCode> roleList;
        JsonNode CDCUserProfile;
        Map<String,Object> companyObject = new HashMap<String,Object>();
        Map<String,Object> regCompanyData = new HashMap<String,Object>();
        Map<String,Object> accountObject = new HashMap<String,Object>();
        List<Map<String, String>> languages = new ArrayList<>();

        List<CisCountry> countries = cisCountryRepository.findAllOrderedByNameEn();
        List<NewSubsidiary> subsidiarys = newSubsidiaryRepository.findAll();
        List<CommonCode> approvalDivisions = commonCodeRepository.findByHeader("DIVISION_CODE");

        String content = "fragments/myPage/userManagmentDetail";
        String menu = "userManagment";
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT division, subsidiary, country ")
                .append("FROM approval_admins ")
                .append("WHERE uid = :uid ")
                .append("AND channel = :channel");

        List<Object[]> results = entityManager.createNativeQuery(queryBuilder.toString())
                .setParameter("uid", payload.get("uid"))
                .setParameter("channel", payload.get("channel"))
                .getResultList();

        for (Object[] result : results) {
            Map<String, Object> resultMap = new HashMap<>();
            // 처리
            companyObject.put("approvalDivision", result[0]);
            companyObject.put("approvalSubsidiary", result[1]);
            companyObject.put("approvalCountry", result[2]);
        }

        companyObject.put("countries", countries);
        companyObject.put("subsidiarys", subsidiarys);
        companyObject.put("approvalDivisions", approvalDivisions);

        channels = channelRepository.selectChannelName(payload.get("channel"));
        roleList = commonCodeRepository.selectRoleNameList("ROLE_CODE",payload.get("role"));
        modelAndView.addObject("channels", channels);
        CDCUserProfile = cdcTraitService.getCdcUser(payload.get("uid"), 0);

        //회사정보 세팅
        JsonNode companyNode = cdcTraitService.getB2bOrg(payload.get("bpid"));
        JsonNode infoNode = companyNode.path("info");
        String type = companyNode.path("type").asText("");

        companyObject.put("bpid", companyNode.path("bpid").asText(""));
        companyObject.put("source", companyNode.path("source").asText(""));
        companyObject.put("type", companyNode.path("type").asText(""));
        companyObject.put("validStatus", companyNode.path("status").asText(""));
        companyObject.put("zip_code", infoNode.path("zip_code").path(0).asText(""));
        companyObject.put("vendorcode", companyNode.path("bpid").asText(""));

        regCompanyData.put("name", companyNode.path("orgName").asText(""));
        regCompanyData.put("country", infoNode.path("country").path(0).asText(""));
        regCompanyData.put("state", infoNode.path("state").path(0).asText(""));
        regCompanyData.put("city", infoNode.path("city").path(0).asText(""));
        regCompanyData.put("street_address", infoNode.path("street_address").path(0).asText(""));
        regCompanyData.put("phonenumber1", infoNode.path("phonenumber1").path(0).asText(""));
        regCompanyData.put("fax", infoNode.path("fax").path(0).asText(""));
        regCompanyData.put("email", companyNode.path("email").asText(""));
        regCompanyData.put("bizregno1", infoNode.path("bizregno1").path(0).asText(""));
        regCompanyData.put("representative", infoNode.path("representative").path(0).asText(""));
        regCompanyData.put("vendorcode", companyNode.path("bpid").asText(""));
        regCompanyData.put("zip_code", infoNode.path("zip_code").path(0).asText(""));
        regCompanyData.put("validStatus", companyNode.path("status").asText(""));
        regCompanyData.put("type", companyNode.path("type").asText(""));
        regCompanyData.put("source", companyNode.path("source").asText(""));
        regCompanyData.put("bpid", companyNode.path("bpid").asText(""));


        modelAndView.addObject("type", type);
        modelAndView.addObject("uid", payload.get("uid"));

        String salutation = CDCUserProfile.path("salutation").asText("");
        String language = CDCUserProfile.path("profile").path("locale").asText("");
        String firstName = CDCUserProfile.path("profile").path("firstName").asText("");
        String lastName = CDCUserProfile.path("profile").path("lastName").asText("");
        JsonNode phonesNode = CDCUserProfile.path("profile").path("phones");
        String workPhone = null;
        if (phonesNode.isArray()) {
            for (JsonNode phoneNode : phonesNode) {
                if ("work_phone".equals(phoneNode.path("type").asText())) {
                    workPhone = phoneNode.path("number").asText("");
                    break;
                }
            }
        }

        // 첫 번째 부분에서 "+" 기호를 제거하고 숫자만 남김
        String countryCode = "";
        String phoneNumber = "";
        if (workPhone != null) {
            String[] parts = workPhone.split(" ");

            // 국가 코드와 전화번호 분리
            for (String part : parts) {
                if (part.startsWith("+")) {
                    countryCode = part.replace("+", "").replaceAll("\\D", "");
                } else if (!part.isEmpty()) {
                    phoneNumber = part;
                }
            }
        }

        Map<String, String> koreanLanguageMap = new HashMap<>();
        koreanLanguageMap.put("name", "Korean");
        koreanLanguageMap.put("value", "ko_KR");
        languages.add(koreanLanguageMap);

        Map<String, String> englishLanguageMap = new HashMap<>();
        englishLanguageMap.put("name", "English");
        englishLanguageMap.put("value", "en_US");
        languages.add(englishLanguageMap);

        String secDept = CDCUserProfile.path("data").path("userDepartment").asText("");
        String job_title = CDCUserProfile.path("data").path("jobtitle").asText("");

        List<CommonCode> codeList = commonCodeRepository.findByHeader("COUNTRY_CODE");
        List<CommonCode> divisions = commonCodeRepository.findByHeader("BIZ_WITH_DEPT");//divisionRepository.findAllByOrderByNameEnAsc();

        accountObject.put("salutation",salutation);
        accountObject.put("language", "en".equals(language) ? "en_US" : "ko".equals(language) ? "ko_KR" : language);
        accountObject.put("firstName",firstName);
        accountObject.put("lastName",lastName);

        accountObject.put("country_code_work",countryCode);
        accountObject.put("work_phone",phoneNumber);
        accountObject.put("secDept",secDept);
        accountObject.put("job_title",job_title);
        accountObject.put("languages", languages);
        accountObject.put("roles", roleList);
        accountObject.put("divisions", divisions);
        accountObject.put("codes", codeList);
        accountObject.put("loginId", payload.get("userId"));
        accountObject.put("currentAdminRole", payload.get("role"));

        ObjectNode channelsNode = (ObjectNode) CDCUserProfile.path("data").path("channels");
        Map<String, Object> channelsMap = objectMapper.convertValue(channelsNode, Map.class);
        Map<String, Object> channelData = (Map<String, Object>) channelsMap.get(payload.get("channel"));

        boolean isCiamAdmin = CDCUserProfile.path("data").path("isCIAMAdmin").asBoolean(false);
        Object adminTypeObject;
        if (isCiamAdmin) {
            adminTypeObject = 4;
        } else {
            adminTypeObject = channelData.getOrDefault("adminType", "0");
        }
        int userRole;

        if (adminTypeObject instanceof String) {
            userRole = Integer.parseInt((String) adminTypeObject);
        } else if (adminTypeObject instanceof Integer) {
            userRole = (Integer) adminTypeObject;
        } else {
            userRole = 0; // 예상치 못한 경우에 대비한 기본값
        }
        accountObject.put("userRole", String.valueOf(userRole));

        Map<String, List<?>> fieldMap = cdcTraitService.generateFieldMap(payload.get("channel"), objectMapper, true, true,true); // Company와 User 데이터 모두 포함

        modelAndView.addObject("fieldMap", fieldMap);

        cdcTraitService.extractChannelAddFields(infoNode,CDCUserProfile, payload.get("channel"), accountObject,regCompanyData);

        String industryTypeValue = String.valueOf(infoNode.path("industry_type"));
        industryTypeValue = industryTypeValue.replaceAll("[\\[\\]\"\\\\]", "");
        regCompanyData.put("industryType", industryTypeValue);

        companyObject.put("registerCompany", regCompanyData);
        modelAndView.addObject("companyObject", companyObject);
        modelAndView.addObject("accountObject", accountObject);
        return modelAndView;

    }

    public String adminTypeEdit(Map<String, String> payload, HttpSession session, RedirectAttributes redirectAttributes) {
        String successYn = "N";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> channelData = new HashMap<>();
            Map<String, Object> cdcParams = new HashMap<>();
            Map<String, Object> dataFields = new HashMap<>();

            JsonNode CDCUser = cdcTraitService.getCdcUser(payload.get("uid"), 0);

            if (CDCUser.path("UID").isMissingNode()) {
                redirectAttributes.addFlashAttribute("showErrorMsg", "User is no longer valid, please cancel and re-create new invitation");
                return successYn;
            }
            int adminType = 0;
            if (payload.get("role") != null) {
                switch (payload.get("role")) {
                    case "0" -> adminType = 0; // General User
                    case "1" -> adminType = 1; //ChannelSystemAdmin
                    case "2" -> adminType = 2; //Channel Biz Admin
                    case "3" -> adminType = 3; //Partner Admin
                    case "4" -> adminType = 4; //CIAM Admin
                    case "9" -> adminType = 9; // Temp Appvoer
                }
            }
            // adminType이 4가 아닐 때만 adminType을 channelData에 추가
            if (adminType != 4) {
                channelData.put("adminType", adminType);
            } else {
                dataFields.put("isCIAMAdmin", true);
            }
            dataFields.put("channels", Map.of(payload.get("channel"), channelData));

            cdcParams.put("data", objectMapper.writeValueAsString(dataFields));
            cdcParams.put("UID", payload.get("uid"));
            GSResponse response = gigyaService.executeRequest("defaultChannel", "accounts.setAccountInfo", cdcParams);

            if (response.getErrorCode() == 0) {
                String uid = (String) session.getAttribute("cdc_uid");
                String channel = payload.get("channel");
                // payload에서 값이 있으면 우선적으로 사용, 없으면 CDCUser에서 값 사용
                // payload에서 값이 있으면 우선적으로 사용, 없으면 CDCUser에서 값 사용
                String country = payload.get("country") != null ? payload.get("country") : CDCUser.path("profile").path("country").asText("");
                String subsidiary = payload.get("subsidiary") != null ? payload.get("subsidiary") : (!CDCUser.path("data").path("subsidiary").asText().isEmpty()
                        ? CDCUser.path("data").path("subsidiary").asText()
                        : "ALL");

                ObjectMapper mapper = new ObjectMapper();

                // 데이터 변환
                Map<String, Object> data = mapper.convertValue(CDCUser.get("data"), Map.class);
                Map<String, Object> channels = mapper.convertValue(data.get("channels"), Map.class);
                Map<String, Object> channelMapData = mapper.convertValue(channels.get(channel), Map.class);

                String divisionDataMapId = channelAddFieldRepository.selectDivisionYnDataMapId(payload.get("channel")); // ex: "customInfoTest"
                String divisionElement_id = channelAddFieldRepository.selectDivisionYnField(payload.get("channel")); // ex: "customInfoTest.gbm1"
                Map<String, Object> customInfoTestMap = mapper.convertValue(channelMapData.get(divisionDataMapId), Map.class);

                String division = payload.get("division") != null
                        ? payload.get("division")
                        : (customInfoTestMap != null && customInfoTestMap.get(divisionElement_id) != null)
                        ? (String) customInfoTestMap.get(divisionElement_id)
                        : "ALL";

                String companyCode= CDCUser.path("data").path("accountID").asText("");
                String requestor_uid= CDCUser.get("UID").asText("");
                String requestor_email = CDCUser.path("profile").path("email").asText("");
                String StringRole =Integer.toString(adminType);
                String adminTypeShort = commonCodeRepository.selectAttribute3("ROLE_CODE",StringRole);
                String approval_email = (String) session.getAttribute("cdc_email");

                approvalAdminRepository.updateFalseStatus(requestor_uid,channel);

                if(adminType > 0) {
                    approvalAdminRepository.insertApprovalAdmin(requestor_uid , requestor_email , adminTypeShort, channel, country, subsidiary,division, companyCode,  approval_email);
                }
                successYn = "Y";
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            redirectAttributes.addFlashAttribute("showErrorMsg", "Token expired or invalid, please request a new token");
        }
        return successYn;
    }
}
