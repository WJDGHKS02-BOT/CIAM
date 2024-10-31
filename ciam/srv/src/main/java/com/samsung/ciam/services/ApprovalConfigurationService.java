package com.samsung.ciam.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApprovalConfigurationService {

    @Autowired
    private SystemService systemService;

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private ApprovalRuleRepository approvalRuleRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private ApprovalRuleMasterRepository approvalRuleMasterRepository;

    @Autowired
    private ApprovalAdminRepository approvalAdminRepository;

    public List<Map<String, Object>> searchApprovalConfiguration(Map<String, String> payload, HttpSession session) {

        List<Map<String, Object>> mappedResults = new ArrayList<>();
        List<ApprovalRule> results = approvalRuleRepository.selectApprovalRuleList(payload.get("selectedChannel"),payload.get("selectedType"),payload.get("selectedLocation"),payload.get("selectedSubsidiary"),payload.get("selectedDivision"));        

        for (ApprovalRule result : results) {
            Map<String, Object> termsMap = new HashMap<>();
            termsMap.put("id", result.getId());
            termsMap.put("ruleLevel", result.getRuleLevel());
            termsMap.put("requestType", result.getWorkflowCode());
            termsMap.put("requestTypeNm", result.getWorkflowCodeNm());
            termsMap.put("approver", result.getRole());
            termsMap.put("approverNm", result.getRoleNm());
            termsMap.put("approveFormat", result.getApproveFormat());
            termsMap.put("approveFormatNm", result.getApproveFormatNm());
            termsMap.put("approveConditions", result.getApproveCondition());
            termsMap.put("approveConditionsNm", result.getApproveConditionNm());
            termsMap.put("lastModifiedDate", result.getLastModifiedDate());
            termsMap.put("channel", result.getChannel());
            termsMap.put("country", result.getCountry());
            termsMap.put("subsidiary", result.getSubsidiary());
            termsMap.put("division", result.getDivision());
            termsMap.put("ruleMasterId", result.getRuleMasterId());
            termsMap.put("status", result.getStatus());
            mappedResults.add(termsMap);
        }
        log.warn("approvalConfiguration : {} ", mappedResults);

        return mappedResults;
    }

    public Integer insertApprovalRule(Map<String, String> payload, HttpSession session) {
        // 1. rule_master_id 검색
        int ruleMasterCnt = approvalRuleMasterRepository.selectApprovalRuleMasterCount(payload.get("workflowCode"), payload.get("channel"), payload.get("country"), payload.get("subsidiary"), payload.get("division"));

        if (ruleMasterCnt == 0) {
            log.info("ruleMasterId is null");
            approvalRuleMasterRepository.insertApprovalRuleMaster(payload.get("workflowCode"), payload.get("channel"), payload.get("country"), payload.get("subsidiary"), payload.get("division"));
        }
        
        String ruleMasterId = approvalRuleMasterRepository.selectApprovalRuleMasterId(payload.get("workflowCode"), payload.get("channel"), payload.get("country"), payload.get("subsidiary"), payload.get("division"));
        
        // 2. rule_master_id 토대로 insert
        // 9/8 approval_rule 테이블의 approve_format 컬럼 미사용
        return approvalRuleRepository.insertApprovalRule(ruleMasterId, payload.get("ruleLevel"), payload.get("approveCondition"), payload.get("role"));
        // return approvalRuleRepository.insertApprovalRule(ruleMasterId, payload.get("ruleLevel"), payload.get("approveFormat"), payload.get("approveCondition"), payload.get("role"));
    }

    public Integer updateApprovalRule(Map<String, String> payload, HttpSession session) {
        // 9/8 approval_rule 테이블의 approve_format 컬럼 미사용
        return approvalRuleRepository.updateApprovalRule(payload.get("ruleLevel"), payload.get("role"), Integer.parseInt(payload.get("id")));
        // return approvalRuleRepository.updateApprovalRule(payload.get("ruleLevel"), payload.get("approveFormat"), payload.get("role"), Integer.parseInt(payload.get("id")));
    }
    
    public Integer deleteApprovalRule(int id) {
        int deleteApprovalRuleCnt = 1;
        // rule 비활성화
        // 1. id로 개수 가져와서 rule 개수 1이하면 
        if (approvalRuleRepository.selectApprovalRuleCnt(id) <= 1) {
            // 1-1. ruleMasterId 가져오기 
            String ruleMasterId = approvalRuleRepository.selectApprovalRuleMasterId(id);

            // 1-2. rule/master 삭제
            if (!ruleMasterId.equals("")) {
                deleteApprovalRuleCnt = approvalRuleRepository.deleteDataApprovalRule(ruleMasterId);
                approvalRuleMasterRepository.deleteDataApprovalRuleMaster(ruleMasterId);
            }
        } else {
            // 2. 아니면 상태변경
            deleteApprovalRuleCnt = approvalRuleRepository.deleteApprovalRule(id);
        }
        return deleteApprovalRuleCnt;
    }
    
    // systemService.searchUserManagment에 firstName, lastName 추가
    public Map<String, Object> searchUserManagement(Map<String, String> allParams, HttpSession session) {
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


        StringBuilder query = new StringBuilder();
        query.append("SELECT * ")
                //.append(channel)  // 'channel'은 사용자 입력 값
                .append(" FROM accounts")
                .append(" WHERE data.userStatus IN ('active','inactive')");

        if (userId != null && !userId.isEmpty()) {
            query.append(" AND profile.username = '").append(userId).append("'");
        }

        // query.append(" LIMIT 5000 ");

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
                        log.warn("profile : {}", profile);
                        loginId = (String) profile.getOrDefault("username", "");

                        Map<String, Object> work = (Map<String, Object>) profile.get("work");
                        if (work != null) {
                            companyNameValue = (String) work.getOrDefault("company", "");
                        }
                    }

                    // 안전한 null 체크 후 data.accountID 값을 가져옴
                    Map<String, Object> data = (Map<String, Object>) user.get("data");
                    if (data != null) {
                        log.warn("data : {}", data);
                        String companyIdValue = (String) data.getOrDefault("accountID", "");

                        Map<String, Object> channels = (Map<String, Object>) data.get("channels");

                        if (channel != null && !channel.isEmpty()) {
                            // channel 값이 주어진 경우 해당 채널만 체크
                            if (channels != null && channels.containsKey(channel)) {
                                Map<String, Object> channelData = (Map<String, Object>) channels.get(channel);
                                String approvalStatus = (String) channelData.getOrDefault("approvalStatus", "");

                                if ("approved".equals(approvalStatus)) {
                                    Map<String, Object> resultMap = new HashMap<>();
                                    resultMap.put("userId", loginId);
                                    resultMap.put("uid", uid);
                                    resultMap.put("companyName", companyNameValue);
                                    resultMap.put("companyCode", companyIdValue);
                                    systemService.populateResultMap(resultMap, channelData, commonCodeList);
                                    resultMap.put("channel", channel);
                                    String channelDisplayName = channelsList.stream()
                                            .filter(ch -> ch.getChannelName().equals(channel))
                                            .map(Channels::getChannelDisplayName)
                                            .findFirst()
                                            .orElse(channel);

                                    resultMap.put("channelDisplayName", channelDisplayName);
                                    resultMap.put("firstName", profile.get("firstName"));
                                    resultMap.put("lastName", profile.get("lastName"));
                                    resultMap.put("department", data.get("userDepartment"));
                                    resultMap.put("creatorId", session.getAttribute("cdc_email"));
                                    resultMap.put("country", profile.get("country"));
                                    resultMap.put("subsidiary", data.get("subsidiary"));
                                    resultMap.put("division", data.get("division"));
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
                                        resultMap.put("userId", loginId);
                                        resultMap.put("uid", uid);
                                        resultMap.put("companyName", companyNameValue);
                                        resultMap.put("companyCode", companyIdValue);
                                        resultMap.put("channel", channelName);

                                        String channelDisplayName = channelsList.stream()
                                                .filter(ch -> ch.getChannelName().equals(channelName))
                                                .map(Channels::getChannelDisplayName)
                                                .findFirst()
                                                .orElse(channelName);
                                        resultMap.put("channelDisplayName", channelDisplayName);
                                        resultMap.put("firstName", profile.get("firstName"));
                                        resultMap.put("lastName", profile.get("lastName"));
                                        resultMap.put("department", data.get("userDepartment"));
                                        resultMap.put("creatorId", session.getAttribute("cdc_email"));
                                        resultMap.put("country", profile.get("country"));
                                        resultMap.put("subsidiary", data.get("subsidiary"));
                                        resultMap.put("division", data.get("division"));

                                        systemService.populateResultMap(resultMap, channelData, commonCodeList);

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

    public Integer insertApprovalAdmin(Map<String, String> payload, HttpSession session) {
        String type = payload.get("type");

        // 파라미터 : uid/userId/없음/channel/country/subsidiary/division/companyCode/creatorId
        // 테이블 : uid/email/type/channel/country/subsidiary/division/companyCode/approvalUser
        // 변경 후
        int approvalAdminCnt = approvalAdminRepository.selectDuplicateApprovalAdmin(payload.get("uid"), type, payload.get("channel"), payload.get("country"), payload.get("subsidiary"), payload.get("division"));
        int insertApprovalAdminCnt = 0;
        if(approvalAdminCnt == 0) {
        // 9/30 무조건 Temp Approver로 저장 및 삭제되도록 수정
        //    if(type.equals("TA")) {
            insertApprovalAdminCnt = approvalAdminRepository.insertApprovalAdmin(payload.get("uid"), payload.get("userId"), type, payload.get("channel"), payload.get("country"), payload.get("subsidiary"), payload.get("division"), payload.get("companyCode"), payload.get("creatorId"), payload.get("ruleMasterId"), payload.get("ruleLevel"));
        //    } else {
        //     insertApprovalAdminCnt = approvalAdminRepository.insertApprovalAdmin(payload.get("uid"), payload.get("userId"), type, payload.get("channel"), payload.get("country"), payload.get("subsidiary"), payload.get("division"), payload.get("companyCode"), payload.get("creatorId"));
        //    }
        } else {
           // 비활성화 데이터 있으면 지움
           approvalAdminRepository.deleteDisabledApprovalAdmin(payload.get("userId"), type, payload.get("channel"), payload.get("country"), payload.get("subsidiary"), payload.get("division"));
        }

        return insertApprovalAdminCnt;
    }

    public Integer saveApprovalRuleMasterStage(Map<String, String> payload, HttpSession session) {
        String ruleMasterId = approvalRuleMasterRepository.getApprovalRuleMasterId(payload.get("selectedType"),payload.get("selectedChannel"),payload.get("selectedLocation"),payload.get("selectedSubsidiary"),payload.get("selectedDivision"));
        // 데이터 있으면 update, 없으면 insert
        return approvalRuleMasterRepository.saveApprovalRuleMasterStage(ruleMasterId, payload.get("selectedType"),payload.get("selectedChannel"),payload.get("selectedLocation"),payload.get("selectedSubsidiary"),payload.get("selectedDivision"), Integer.parseInt(payload.get("selectedStage")), payload.get("selectedApproveFormat"));
    }

    public Integer getApprovalRuleMasterStage(Map<String, String> payload, HttpSession session) {
        return approvalRuleMasterRepository.getApprovalRuleMasterStage(payload.get("selectedType"),payload.get("selectedChannel"),payload.get("selectedLocation"),payload.get("selectedSubsidiary"),payload.get("selectedDivision"));
    }

    public String getApprovalRuleMasterApproveFormat(Map<String, String> payload, HttpSession session) {
        return approvalRuleMasterRepository.getApprovalRuleMasterApproveFormat(payload.get("selectedType"),payload.get("selectedChannel"),payload.get("selectedLocation"),payload.get("selectedSubsidiary"),payload.get("selectedDivision"));
    }    

    public List<Integer> getPossibleApprovalRuleList(Map<String, String> payload, HttpSession session) {
        return approvalRuleMasterRepository.getPossibleApprovalRuleList(payload.get("selectedType"),payload.get("selectedChannel"),payload.get("selectedLocation"),payload.get("selectedSubsidiary"),payload.get("selectedDivision"));
    }

    
}