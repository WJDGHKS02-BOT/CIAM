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
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class ApprovalListService {
    private String channelName;
    private String approvalType;
    private String requestorCDCUID;
    private boolean isSamsungEmployee;
    private String hUid = "47835cb8b053490294937e333ad5ceb3";
    private String uid;
    private String cdc_email =null;
    private String btp_myrole =null;
    @Autowired
    private SecServingCountryRepository secServingCountryRepository;

    enum ROLE_LEVEL{
        CIAMAdmin(0),
        ChannelSystemAdmin(1),
        ChannelBusinessAdmin(2),
        CompanyAdmin(3),
        GeneralUser(4);

        private int index;

        ROLE_LEVEL(int index){
            this.index = index;
        }

        public int value(){
           return index;
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private CisCountryRepository cisCountryRepository;

    @Autowired
    private WfMasterRepository wfMasterRepository;

    @Autowired
    private AuditLogService auditLogService; 

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private ApprovalAdminRepository approvalAdminRepository;

    @Autowired
    private NewSubsidiaryRepository newSubsidiaryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MailService mailService;

    // 신규 가입승인 조회 W01~W06까지 모두 같은 Api
    public List<Map<String, Object>> searchW01(Map<String, String> payload, HttpSession session) {
        // type 값을 소문자로 변환
        log.info("searchW01 payload: " + payload);
        String status = payload.get("type").toLowerCase(); // 소문자로 변환
        String workflow_code =    StringUtil.getStringValue(payload.get("requestType"), "W01");
        String channel = payload.get("channel");
        String menu_type = "New Registration";
        List<Map<String, Object>> resultList = new ArrayList<>(); // 빈 ArrayList로 초기화
 
        // 1. 사용자 정보 가져오기
        @SuppressWarnings("unused")
        JsonNode cdcUser = null;
        if (session.getAttribute("cdc_uid") != null) {
            cdcUser = cdcTraitService.getCdcUser((String) session.getAttribute("cdc_uid"), 0);
            uid = (String) session.getAttribute("cdc_uid");
            cdc_email = (String) session.getAttribute("cdc_email");
            btp_myrole  = (String) session.getAttribute("btp_myrole");

        } else {
            cdcUser = cdcTraitService.getCdcUser(hUid, 0);
            uid = hUid;
        }
        log.info("uid: " + uid + " cdc_email:" + cdc_email +" btp_myrole:"+ btp_myrole + " workflow_code:"+workflow_code);
        try {
            if(status.equals("approved")){  // 승인완료일때 
                resultList = wfMasterRepository.searchW01Appr(uid, status , workflow_code, channel);
            }else{
                resultList = wfMasterRepository.searchW01(uid, status, workflow_code, channel);
            }
            
            switch (workflow_code) {
                case "W01" -> {    menu_type = "New Registration";    }
                case "W02" -> {    menu_type = "Conversion Registration";    }
                case "W03" -> {    menu_type = "Invite Registration";    }
                case "W04" -> {    menu_type = "AD Registration";    }
                case "W06" -> {    menu_type = "SSO Acess";    }
                default -> {    menu_type = "New Registration";    }
            }
            log.info("menu_type: " + menu_type );

            // Audit Log
            Map<String, String> param = new HashMap<>();
            param.put("type", "Approval_List");
            param.put("menu_type", menu_type);
            param.put("action", "ListView");
            param.put("condition", channel);
            // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
            param.put("result_count", String.valueOf(resultList.size()));
            auditLogService.addAuditLog(session, param);
    
        } catch (Exception e) {
            log.error("Error searchW01 processing failed", e);
        }
        return resultList;
    }
    // Role W05 결재 리스트 Search
    public List<Map<String, Object>> searchW05(Map<String, String> payload, HttpSession session) {
        // type 값을 소문자로 변환
        log.info("payload: " + payload);
        String status = payload.get("type").toLowerCase(); // 소문자로 변환
        String channel = payload.get("channel");
        List<Map<String, Object>> resultList = new ArrayList<>(); // 빈 ArrayList로 초기화
 
        // 1. 사용자 정보 가져오기
        @SuppressWarnings("unused")
        JsonNode cdcUser = null;
        if (session.getAttribute("cdc_uid") != null) {
            cdcUser = cdcTraitService.getCdcUser((String) session.getAttribute("cdc_uid"), 0);
            uid = (String) session.getAttribute("cdc_uid");
            cdc_email = (String) session.getAttribute("cdc_email");
            btp_myrole  = (String) session.getAttribute("btp_myrole");

        } else {
            cdcUser = cdcTraitService.getCdcUser(hUid, 0);
            uid = hUid;
        }
        log.info("uid: " + uid + " cdc_email:" + cdc_email +" btp_myrole:"+ btp_myrole);
        try {
            resultList = wfMasterRepository.rolePending(uid,status, channel);

            // Audit Log
            Map<String, String> param = new HashMap<>();
            param.put("type", "Approval_List");
            param.put("menu_type", "Role Management");
            param.put("action", "ListView");
            param.put("condition", channel);
            // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
            param.put("result_count", String.valueOf(resultList.size()));
            auditLogService.addAuditLog(session, param);
    
        } catch (Exception e) {
            log.error("Error searchW05 processing failed", e);
        }
        return resultList;
    }
    

    // Domain 결재 리스트 Search
    public List<Map<String, Object>> searchW07(Map<String, String> payload, HttpSession session) {
        // type 값을 소문자로 변환
        log.info( " Domain payload: " + payload);
        String status = payload.get("type").toLowerCase(); // 소문자로 변환
        String channel = payload.get("channel");
        List<Map<String, Object>> resultList = new ArrayList<>(); // 빈 ArrayList로 초기화
 
        // 1. 사용자 정보 가져오기
        @SuppressWarnings("unused")
        JsonNode cdcUser = null;
        if (session.getAttribute("cdc_uid") != null) {
            cdcUser = cdcTraitService.getCdcUser((String) session.getAttribute("cdc_uid"), 0);
            uid = (String) session.getAttribute("cdc_uid");
            cdc_email = (String) session.getAttribute("cdc_email");
            btp_myrole  = (String) session.getAttribute("btp_myrole");

        } else {
            cdcUser = cdcTraitService.getCdcUser(hUid, 0);
            uid = hUid;
        }
        log.info("uid: " + uid + " cdc_email:" + cdc_email +" btp_myrole:"+ btp_myrole);
        try {
            resultList = wfMasterRepository.domainPending(uid,status, channel);
            // 1. type = pending 일때
            // if (status.equals("pending")) {
            //     resultList = wfMasterRepository.domainPending(uid,status );
            // }else if(status.equals("reject")) {
            //     resultList = wfMasterRepository.domainPending(uid,status);
            // }else if(status.equals("approved")) {
            //     resultList = wfMasterRepository.domainApproved(uid,status);
            // }
            
            // 로그를 통해 resultList의 각 항목을 출력
            // for (Map<String, Object> result : resultList) {
            //     log.warn("wf Record:");
            //     for (Map.Entry<String, Object> entry : result.entrySet()) {
            //         log.warn("{} : {}", entry.getKey(), entry.getValue());
            //     }
            // }
    
            // Audit Log
            Map<String, String> param = new HashMap<>();
            param.put("type", "Approval_List");
            param.put("action", "ListView");
            param.put("menu_type", "Company Domain");
            param.put("condition", channel);
            // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
            param.put("result_count", String.valueOf(resultList.size()));
            auditLogService.addAuditLog(session, param);
    
        } catch (Exception e) {
            log.error("Error searchW07 processing failed", e);
        }
        return resultList;
    }
    


    public ModelAndView approvalList(HttpServletRequest request, HttpSession session, Model model) {
        ModelAndView modelAndView = new ModelAndView("myPage");
        Map<String, Object> cdcParams = new HashMap<>();
        Map<String, Object> companyDataResult = null;
        
        try {
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
            String channel = (String) session.getAttribute("session_channel");

            // common code
            // channel (channels 테이블 > value : channel_name / name : channel_display_name)
            modelAndView.addObject("channel", channelRepository.selectChannelTypeList(""));

            modelAndView.addObject("session_channel", channel);
            modelAndView.addObject("session_display_channel", session.getAttribute("session_display_channel"));
            // log.warn("channel : {}", channelRepository.selectChannelTypeList(""));
            // log.warn("session_channel : {}", channelRepository.selectChannelTypeList(""));
            modelAndView.addObject("role", session.getAttribute("btp_myrole"));
            // log.warn("role : {}", session.getAttribute("btp_myrole"));
            
            String bpid = (String) session.getAttribute("cdc_companyid");
            cdcParams.put("bpid", bpid);
            //회사정보를 가져오는 로직
            GSResponse response = gigyaService.executeRequest("default", "accounts.b2b.getOrganizationInfo", cdcParams);
            JsonNode responseData = objectMapper.readTree(response.getResponseText());
            String companyJson = objectMapper.writeValueAsString(responseData);
            log.warn("companyDataResult : {}", companyJson);
            JsonNode info = responseData.path("info");
            ObjectNode objectNode = objectMapper.createObjectNode();
            if (info.isObject()) {
                ObjectNode infoNode = (ObjectNode) info;
                objectNode.put("orgName", getNonEmptyValue(responseData.path("orgName")));
                objectNode.put("bpid", getNonEmptyValue(responseData.path("bpid")));
            }
            companyDataResult = objectMapper.convertValue(objectNode, Map.class);

        } catch (Exception e) {
            log.error("Error approvalList processing failed", e);
        }
        String menu = "approvalList";
        String content = "fragments/myPage/" + menu;
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("companyDataResult", companyDataResult);
        modelAndView.addObject("requestType", commonCodeRepository.findByHeaderOrderBySortOrder("REQUEST_TYPE_CODE"));
        return modelAndView;
    }


    // JSON 값에서 빈 문자열이 아닌 값을 추출하는 헬퍼 메서드
    private static String getNonEmptyValue(JsonNode jsonNode) {
        String value = jsonNode.asText();
        return value.isEmpty() ? null : value;
    }

    public RedirectView approvalListPendingSubmit(Map<String, String> payload, HttpServletRequest request, HttpSession session,RedirectAttributes redirectAttributes) {
        log.info("Approval List Pending Submit Payload::: " + payload);  // 데이터 확인
        Map<String, Object> selectedRowData = new HashMap<>();
        redirectAttributes.addFlashAttribute("payloadData", payload);

        // 1. 사용자 정보 가져오기
        JsonNode cdcUser = null;
        if (session.getAttribute("cdc_uid") != null) {
            cdcUser     = cdcTraitService.getCdcUser((String) session.getAttribute("cdc_uid"), 0);
            uid         = (String) session.getAttribute("cdc_uid");
            cdc_email = (String) session.getAttribute("cdc_email");
            btp_myrole  = (String) session.getAttribute("btp_myrole");
            
        } else {
            cdcUser = cdcTraitService.getCdcUser(hUid, 0);
            uid = hUid;
        }
        log.info("uid: " + uid + " cdc_email:" + cdc_email +" btp_myrole:"+ btp_myrole);
        // 2. payload에서 wf_id 추출
        String wf_id = "";
        String menu_type = "";
        String reject_reason = "";
        String requestor_id = "", requestor_email = "",  requestor_uid = "" , requestor_company_code ="", requestor_role ="", W01change_role ="" ;
        String country = "", subsidiary = "", division = "", companyCode = "";
        String reg_channel = "", target_channel = "", target_channeltype = ""   ;
        String channel = ""; 
        reject_reason = (String) payload.get("rejectReason");
        String selectedRowDataString = (String) payload.get("selectedRowData");
        String requestType = (String) payload.get("requestType"); //companyDomain, roleManagement ...
        String statusUpdate = payload.get("statusUpdate"); // approve, reject
        String status = null;
        String change_role ="";
        int wf_max_level = 1;
        int wf_level = 1;
        int adminType = 1;
        String adminTypeShort ="";

        if (selectedRowDataString != null) {
            try {
                // JSON 문자열을 Map으로 파싱
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> selectedRowDataMap = mapper.readValue(selectedRowDataString, new TypeReference<Map<String, Object>>() {});
                if (selectedRowDataMap.containsKey("wf_id")) {
                    wf_id = (String) selectedRowDataMap.get("wf_id");
                }
                if (selectedRowDataMap.containsKey("requestor_id")) {
                    requestor_id = (String) selectedRowDataMap.get("requestor_id");
                }
                if (selectedRowDataMap.containsKey("requestor_uid")) {
                    requestor_uid = (String) selectedRowDataMap.get("requestor_uid");
                }
                if (selectedRowDataMap.containsKey("requestor_company_code")) {   requestor_company_code = (String) selectedRowDataMap.get("requestor_company_code");    }
                if (selectedRowDataMap.containsKey("channel"))              {   channel = (String) selectedRowDataMap.get("channel");    }
                if (selectedRowDataMap.containsKey("requestor_email"))      {   requestor_email = (String) selectedRowDataMap.get("requestor_email");    }
                if (selectedRowDataMap.containsKey("requestor_company_code")) {   requestor_company_code = (String) selectedRowDataMap.get("requestor_company_code");    }

                String vendorCode = payload.get("vendorcode");
                String bpId = payload.get("bpid");

                //회사 변경한경우 cdc계정 accountID 및 wf_master변경
                if(bpId!=null && !bpId.equals(requestor_company_code)) {
                    Map<String, Object> cdcParams = new HashMap<>();
                    Map<String, Object> dataFields = new HashMap<>();
                    requestor_company_code = bpId;
                    String channelType = channelRepository.selectChannelTypeSearch(channel);

                    cdcParams.put("UID", requestor_id);

                    if("VENDOR".equals(channelType)) {
                        dataFields.put("vendorCode", vendorCode);
                        dataFields.put("accountID", requestor_company_code);
                    } else {
                        dataFields.put("accountID", requestor_company_code);
                    }
                    try {
                        cdcParams.put("data", objectMapper.writeValueAsString(dataFields));
                    } catch (Exception e) {
                        log.error("Error processing data fields", e);
                    }
                    GSResponse setAccountResponse = gigyaService.executeRequest("default", "accounts.setAccountInfo", cdcParams);
                    wfMasterRepository.updateWfMasterCompanyCode(wf_id,requestor_company_code,payload.get("name"));
                }

                //W06 SSO
                if (selectedRowDataMap.containsKey("reg_channel"))          {   reg_channel = (String) selectedRowDataMap.get("reg_channel");    }
                if (selectedRowDataMap.containsKey("target_channel"))       {   target_channel = (String) selectedRowDataMap.get("target_channel");    }
                if (selectedRowDataMap.containsKey("target_channeltype"))   {   target_channeltype = (String) selectedRowDataMap.get("target_channeltype");    }
                
                if (selectedRowDataMap.containsKey("wf_max_level")) {
                    wf_max_level = (int) selectedRowDataMap.get("wf_max_level");
                }
                if (selectedRowDataMap.containsKey("wf_level")) {
                    wf_level = (int) selectedRowDataMap.get("wf_level");
                }
                if (selectedRowDataMap.containsKey("channel")) {
                    channel = (String) selectedRowDataMap.get("channel");
                }
                if (selectedRowDataMap.containsKey("change_role")) {
                    change_role = (String) selectedRowDataMap.get("change_role");
                        if (change_role != null) {
                            switch (change_role) {
                                case "General User" -> {
                                    adminType = 0; // General User
                                    adminTypeShort = "US";
                                }
                                case "Channel Admin" -> {
                                    adminType = 1; // ChannelSystemAdmin
                                    adminTypeShort = "CA";
                                }
                                case "Channel biz Admin" -> {
                                    adminType = 2; // Channel Biz Admin
                                    adminTypeShort = "CB";
                                }
                                case "Partner Admin" -> {
                                    adminType = 3; // Partner Admin
                                    adminTypeShort = "PA";
                                }
                                case "CIAM Admin" -> {
                                    adminType = 4; // CIAM Admin
                                    adminTypeShort = "AM";
                                }
                                case "Temp Appvoer" -> {
                                    adminType = 9; // Temp Appvoer
                                    adminTypeShort = "TA";
                                }
                                default -> {
                                    adminType = 0; // Default to General User
                                    adminTypeShort = "US";
                                }
                            }
                        }
                }
                log.info(" payload.get::::change_role:: " + payload.get("change_role"));

                W01change_role = (String) payload.get("change_role");
                if (W01change_role != null) {
                    switch (W01change_role) {
                        case "General User" -> {
                            adminType = 0; // General User
                            adminTypeShort = "US";
                        }
                        case "Channel Admin" -> {
                            adminType = 1; // ChannelSystemAdmin
                            adminTypeShort = "CA";
                        }
                        case "Channel biz Admin" -> {
                            adminType = 2; // Channel Biz Admin
                            adminTypeShort = "CB";
                        }
                        case "Partner Admin" -> {
                            adminType = 3; // Partner Admin
                            adminTypeShort = "PA";
                        }
                        case "CIAM Admin" -> {
                            adminType = 4; // CIAM Admin
                            adminTypeShort = "AM";
                        }
                        case "Temp Appvoer" -> {
                            adminType = 9; // Temp Appvoer
                            adminTypeShort = "TA";
                        }
                        default -> {
                            adminType = 0; // Default to General User
                            adminTypeShort = "US";
                        }
                    }
                }

                   
                
            } catch (JsonProcessingException e) {
                log.error("Error approvalListPendingSubmit processing failed", e);
            }
        }
        // wf_id 출력
        log.info("wf_id: " + wf_id + "rejectReason" + reject_reason + "  status "+statusUpdate + " change_role: "+ change_role + " adminType:"+adminType);
        // 3. companyDomain 일때 
        if (requestType.equals("W07") ) {
            // approve , rejected 일때 
            if(statusUpdate.equals("approved") || statusUpdate.equals("rejected") ){
                status = statusUpdate; //"approved";
                try {
                    // wf_list 상태 업데이트
                    wfMasterRepository.updateWfListStatus(wf_id,status);
        
                    // wf_master 상태 업데이트
                    wfMasterRepository.updateWfMasterStatus(uid, wf_id, cdc_email, status, reject_reason);

                    //updateVerification_domains
                    wfMasterRepository.updateVerificationDomains(uid, wf_id, cdc_email, status, reject_reason);
                    
                    //2024.09.12 kimjy CompanyDomain 승인되면 verification_employ Update 해주고 CDC도 Approved로 변경해야 한다.
                    wfMasterRepository.updateVerificationEmploy(uid, wf_id, cdc_email );

                    redirectAttributes.addFlashAttribute("responseErrorCode", 0);   // 성공코드 반환

                } catch (Exception e) {
                    log.error("Error approvalListPendingSubmit processing failed", e);
                }
            }
        } 
        // roleManagement 승인일때 
        else if(requestType.equals("W05") ){
            // approve , rejected 일때 
            if(statusUpdate.equals("approved") || statusUpdate.equals("rejected") ){
                status = statusUpdate; //"approved";
                try {
                    // wf_list 상태 업데이트
                    wfMasterRepository.updateWfListStatus(wf_id,status);
        
                    // wf_master 상태 업데이트
                    wfMasterRepository.updateWfMasterStatus(uid, wf_id, cdc_email, status, reject_reason);

                    //update RoleManagement
                    wfMasterRepository.updateRoleManagement(uid, wf_id, cdc_email, status, reject_reason);
                    String cdcUpdate = cdcRoleUpdate(requestor_uid, channel, adminType );// 최종결재가 완료되면 CDC Data를 Update 해준다. 

                    if(statusUpdate.equals("rejected")){
                        //반려일 경우 요청자에게 메일발송
                        JsonNode CDCUser = cdcTraitService.getCdcUser(requestor_uid, 0);

                        Map<String, Object> paramArr = new HashMap<>();
                        paramArr.put("template", "TEMPLET-NEW-012");
                        paramArr.put("cdc_uid", requestor_uid);
                        paramArr.put("channel", channel );
                        paramArr.put("RoleManagerRejector", cdc_email ); // 담당자
                        paramArr.put("RoleManagerRejectReason", reject_reason ); // 사유
                        paramArr.put("firstName", CDCUser.get("profile").get("firstName") != null ? CDCUser.get("profile").get("firstName").asText() : "");
                        paramArr.put("lastName", CDCUser.get("profile").get("lastName") != null ? CDCUser.get("profile").get("lastName").asText() : "");
                        // 반려인 경우 Mail 발송 
                        mailService.sendMail(paramArr);
               

                    }

                    redirectAttributes.addFlashAttribute("responseErrorCode", 0);   // 성공코드 반환

                } catch (Exception e) {
                    log.error("Error approvalListPendingSubmit processing failed", e);
                }
            }
        }
        
        
        // 신규가입 승인일때 
        else if(requestType.equals("W01") || requestType.equals("W02") || requestType.equals("W03")  || requestType.equals("W04")  || requestType.equals("W06")  ){
            // approve , rejected 일때 
            log.info("registrationApproval: Comming!!! requestor_role :: "+W01change_role );
            if(statusUpdate.equals("approved") ){ 
                status = statusUpdate; //"approved";
                try {
                    // wf_list 상태 업데이트
                    wfMasterRepository.updateW01WfList(wf_id,status, uid, cdc_email, wf_level);
                    if(wf_max_level > wf_level ){
                        status = "inprogress"; // 결재중
                    }
                    // wf_master 상태 업데이트
                    wfMasterRepository.updateW01WfMaster(uid, wf_id, cdc_email, W01change_role, status, reject_reason, wf_level);
                    if(wf_max_level == wf_level ){ // 최종결재 완료일때 CDC를 Update 한다. 
                        log.info("Last Approval: requestor_id " + requestor_id + "    adminType:"+adminType + "adminTypeShort :"+ adminTypeShort+ " channel :"+ channel+ "companyCode :"+ requestor_company_code);
                        //Admin일 경우 approval_Admin 테이블에  Data 입력
                        if(adminType>0){
                            country= (String) payload.get("channelBizAdminLocationRole");  
                            subsidiary = (String) payload.get("channelBizAdminSubsidiaryRole");
                            division = (String) payload.get("channelBizAdminDivisionRole"); 
                            approvalAdminRepository.insertApprovalAdmin(requestor_id , requestor_email , adminTypeShort, channel, country, subsidiary,division, requestor_company_code,  cdc_email);
                            log.info("insertApprovalAdmin " + requestor_id + "    adminType:"+adminType + "adminTypeShort :"+ adminTypeShort+ " channel :"+ channel+ "companyCode :"+ requestor_company_code);
                        }

                        //String cdcUpdate = cdcUserUpdate(requestor_id, channel, status, adminType );// 최종결재가 완료되면 CDC Data를 Update 해준다.
                        if ("W03".equals(requestType)) {
                            approvalType = "invitation";
                        } else if ("W02".equals(requestType)) {
                            approvalType = "conversion";
                        } else if ("W01".equals(requestType)) {
                            approvalType = "registration";
                        } else if ("W06".equals(requestType)) {
                            approvalType = "ssoAccess";
                        } else if ("W04".equals(requestType)) {
                            approvalType = "adRegistration";
                        } else {
                            approvalType = "registration";
                        }
                        JsonNode CDCUserProfile = cdcTraitService.getCdcUser(requestor_id, 0);
                        String accountID = CDCUserProfile.path("data").path("accountID").asText(null);
                        cdcTraitService.newJavaApproveUser(
                                requestor_id,
                                accountID,
                                channel,
                                "",
                                approvalType,
                                adminType
                        );
                        redirectAttributes.addFlashAttribute("cdcUserUpdate", "ok");
                    }
                    redirectAttributes.addFlashAttribute("responseErrorCode", 0);   // 성공코드 반환

                } catch (Exception e) {
                    log.error("Error approvalListPendingSubmit processing failed", e);
                }
            }else if(statusUpdate.equals("rejected") ){ // 반려일때 
                log.info("registrationApproval: Reject!!!" );
                status = statusUpdate; //"rejected";
                // wf_list 상태 업데이트
                try{
                    wfMasterRepository.updateW01WfList(wf_id,status, uid, cdc_email, wf_level);
                    // wf_master 상태 업데이트
                    wfMasterRepository.updateW01WfMaster(uid, wf_id, cdc_email, W01change_role,  status, reject_reason, wf_level);
                    status = "disabled"; //CDC disabled : 승인 요청 반려 시

                    log.info("reject mail: requestor_uid"+requestor_uid +" requestor_id:"+requestor_id );

                    //반려일 경우 요청자에게 메일발송
                    JsonNode CDCUser = cdcTraitService.getCdcUser(requestor_id, 0);

                    Map<String, Object> paramArr = new HashMap<>();
                    paramArr.put("template", "TEMPLET-NEW-003");
                    paramArr.put("cdc_uid", requestor_id);
                    paramArr.put("channel", channel );
                    paramArr.put("ApprovalRequestRejector", cdc_email ); // 담당자
                    paramArr.put("ApprovalRequestRejectReason", reject_reason ); // 사유
                    paramArr.put("firstName", CDCUser.get("profile").get("firstName") != null ? CDCUser.get("profile").get("firstName").asText() : "");
                    paramArr.put("lastName", CDCUser.get("profile").get("lastName") != null ? CDCUser.get("profile").get("lastName").asText() : "");
                    // 반려인 경우 Mail 발송 
                    mailService.sendMail(paramArr);

                    String cdcUpdate = cdcUserUpdate(requestor_id,requestor_email, channel, status , adminType );// CDC Data를 Update 해준다.

                    redirectAttributes.addFlashAttribute("cdcUserUpdate", cdcUpdate);
                    redirectAttributes.addFlashAttribute("responseErrorCode", 0);   // 성공코드 반환

                }catch (Exception e) {
                    log.error("Error approvalListPendingSubmit processing failed", e);
                }
            }
        }
    

        RedirectView redirectView = new RedirectView();

        // Request Type이 Company Doamin or Role Management 일 때
        // Redirect URL을 메인 페이지(Approval List)로 설정
        if (requestType.equals("W07") || requestType.equals("W05")) {
            redirectView.setUrl("/myPage/approvalList");

            /* 데이터 처리 */

        } 
        // 나머지는 상세 페이지(Approval Information)로 설정
        else {
           // redirectView.setUrl("/myPage/approvalInformation"); // go to get ??
            redirectView.setUrl("/myPage/approvalList"); // go to get
        }
       
        try {
            // Audit Log
            switch (requestType) {
                case "W01" -> {    menu_type = "New Registration";    }
                case "W02" -> {    menu_type = "Conversion Registration";    }
                case "W03" -> {    menu_type = "Invite Registration";    }
                case "W04" -> {    menu_type = "AD Registration";    }
                case "W05" -> {    menu_type = "Role Management";    }
                case "W06" -> {    menu_type = "SSO Acess";    }
                case "W07" -> {    menu_type = "Company Domain";    }
                default -> {    menu_type = "New Registration";    }
            }
            log.info("menu_type: " + menu_type );

            Map<String, String> param = new HashMap<>();
            param.put("type", "Approval_List");
            param.put("menu_type", menu_type);
            param.put("action", "Creation");
            param.put("condition", payload.get("channel"));
            // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
            param.put("result_count", "0");
            auditLogService.addAuditLog(session, param);
    
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error("Error approvalListPendingSubmit processing failed", e);
        }

        return redirectView;
    }
    
    private String cdcUserUpdate(String uid, String email, String channel, String approvalStatus, Integer adminType){
        ObjectMapper objectMapper = new ObjectMapper();
        Optional<Channels> optionalChannelObj = channelRepository.selectByChannelName(channel);
        Map<String, Object> dataFields = new HashMap<>();
        Map<String, Object> profileFields = new HashMap<>();
        Map<String, Object> cdcParams = new HashMap<>();
        Map<String, Object> loginIDs = new HashMap<>();

        String[] emailSplit = email.split("@");
        String emailId = emailSplit[0];
        String domain = emailSplit[1];

        long timestamp = System.currentTimeMillis();
        String newEmail = emailId + "-" + timestamp + "@" + domain;

        profileFields.put("email",newEmail);
        profileFields.put("username",newEmail);

        cdcParams.put("UID", uid);
        cdcParams.put("username", newEmail);
        cdcParams.put("isActive", "false");
        dataFields.put("channels", Map.of(
            channel, Map.of(
                    "adminType", adminType,
                    "approvalStatus", approvalStatus,
                    "approvalStatusDate", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    "lastLogin", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            )
        ));
        try {
            cdcParams.put("data", objectMapper.writeValueAsString(dataFields));
            cdcParams.put("profile", objectMapper.writeValueAsString(profileFields));
        } catch (Exception e) {
            log.error("Error processing data fields", e);
        }
        GSResponse setAccountResponse = gigyaService.executeRequest("default", "accounts.setAccountInfo", cdcParams);
        log.info("setAccountInfo: {}", setAccountResponse.getResponseText());
        if (setAccountResponse.getErrorCode() == 0) {
            return "ok";
        } else {
            return "failed";
        }
    }

    private String cdcRoleUpdate(String uid,  String channel, Integer adminType){
        log.info("cdcRoleUpdate: " + uid + "     channel:" + channel + "     adminType: "+adminType);

        ObjectMapper objectMapper = new ObjectMapper();
        Optional<Channels> optionalChannelObj = channelRepository.selectByChannelName(channel);
        Map<String, Object> dataFields = new HashMap<>();
        Map<String, Object> cdcParams = new HashMap<>();
        cdcParams.put("UID", uid);
        dataFields.put("channels", Map.of(
            channel, Map.of(
                    "adminType", adminType,
                    //"approvalStatus", "approved",
                    "approvalStatusDate", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    //"lastLogin", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            )
        ));
        try {
            cdcParams.put("data", objectMapper.writeValueAsString(dataFields));
        } catch (Exception e) {
            log.error("Error processing data fields", e);
        }
        GSResponse setAccountResponse = gigyaService.executeRequest("default", "accounts.setAccountInfo", cdcParams);
        log.info("setAccountInfo: {}", setAccountResponse.getResponseText());
        if (setAccountResponse.getErrorCode() == 0) {
            return "ok";
        } else {
            return "failed";
        }
    }



    public RedirectView userListDetail(Map<String, String> payload, HttpServletRequest request, HttpSession session,RedirectAttributes redirectAttributes) {
        log.info("user List Detail Payload::: " + payload);  // 데이터 확인
        Map<String, Object> selectedRowData = new HashMap<>();
        redirectAttributes.addFlashAttribute("payloadData", payload);
       // 2. payload에서 wf_id 추출
        String wf_id = "";
        String requestor_id = ""; // 선택한 요청자 ID
        String requestor_email = ""; // 선택한 요청자 ID
        String channel ="";
        String requestor_company_code = "";
        String requestType = (String) payload.get("requestType"); //companyDomain, roleManagement ...
        String status = null;
        String selectedRowDataString = (String) payload.get("selectedRowData");
        String socialProviders ="";
        String socialProvidersYn ="N";

        ObjectMapper objectMapper = new ObjectMapper();
        List<Channels> channels;
        List<CommonCode> roleList;
        JsonNode CDCUserProfile;
        Map<String,Object> companyObject = new HashMap<String,Object>();
        Map<String,Object> regCompanyData = new HashMap<String,Object>();
        Map<String,Object> accountObject = new HashMap<String,Object>();
        List<Map<String, String>> languages = new ArrayList<>();


        if (selectedRowDataString != null) {
            try {
                // JSON 문자열을 Map으로 파싱
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> selectedRowDataMap = mapper.readValue(selectedRowDataString, new TypeReference<Map<String, Object>>() {});
                if (selectedRowDataMap.containsKey("wf_id")) {
                    wf_id = (String) selectedRowDataMap.get("wf_id");
                }
                if (selectedRowDataMap.containsKey("requestor_id")) {
                    requestor_id = (String) selectedRowDataMap.get("requestor_id");
                }
                if (selectedRowDataMap.containsKey("requestor_email")) {
                    requestor_email = (String) selectedRowDataMap.get("requestor_email");
                }
                if (selectedRowDataMap.containsKey("channel")) {
                    channel = (String) selectedRowDataMap.get("channel");
                }
                if (selectedRowDataMap.containsKey("requestor_company_code")) {
                    requestor_company_code = (String) selectedRowDataMap.get("requestor_company_code");
                }
                //requestor_company_code
                
            } catch (JsonProcessingException e) {
                // 예외 처리: JSON 파싱 실패 시 로그를 남기거나 사용자에게 오류 메시지 반환
                log.error("Error userListDetail processing failed", e);
            }
        }

        List<CisCountry> countries = cisCountryRepository.findAllOrderedByNameEn();
        companyObject.put("countries", countries);
        List<SecServingCountry> secCountries = secServingCountryRepository.selectCountryCodeChannel(channel);

        log.info("requestor_id::: " + requestor_id);  // 데이터 확인
        log.info("channel::: " + channel);  // 데이터 확인

        channels = channelRepository.selectChannelName(channel);
        String channelType = channelRepository.selectChannelTypeSearch(channel);
        roleList = commonCodeRepository.selectRoleNameList("ROLE_CODE",payload.get("role"));
        //CDCUserProfile = cdcTraitService.getCdcUser(payload.get("uid"), 0);
        CDCUserProfile = cdcTraitService.getCdcUser(requestor_id, 0);
        socialProviders =  StringUtil.getNonEmptyValue(CDCUserProfile.path("socialProviders"));

        //회사정보 세팅
        JsonNode companyNode = cdcTraitService.getB2bOrg(requestor_company_code);
        JsonNode infoNode = companyNode.path("info");
        String type = companyNode.path("type").asText("");

        List<NewSubsidiary> subsidiarys = newSubsidiaryRepository.findAll();
        List<CommonCode> approvalDivisions = commonCodeRepository.findByHeader("DIVISION_CODE");

        companyObject.put("subsidiarys", subsidiarys);
        companyObject.put("approvalDivisions", approvalDivisions);
        companyObject.put("bpid", companyNode.path("bpid").asText(""));
        companyObject.put("source", companyNode.path("source").asText(""));
        companyObject.put("type", companyNode.path("type").asText(""));
        companyObject.put("validStatus", companyNode.path("status").asText(""));
        companyObject.put("zip_code", infoNode.path("zip_code").path(0).asText(""));
        if("CUSTOMER".equals(channelType)) {
            companyObject.put("vendorcode", companyNode.path("bpid").asText(""));
        } else {
            companyObject.put("vendorcode", infoNode.path("vendorcode").path(0).asText(""));
        }
        companyObject.put("countries", countries);
        companyObject.put("secCountries", secCountries);

        regCompanyData.put("name", companyNode.path("orgName").asText(""));

        String country = infoNode.path("country").path(0).asText("");
        //country = secServingCountryRepository.selectCountryName(channel,country);

        regCompanyData.put("country", country);
        regCompanyData.put("state", infoNode.path("state").path(0).asText(""));
        regCompanyData.put("city", infoNode.path("city").path(0).asText(""));
        regCompanyData.put("street_address", infoNode.path("street_address").path(0).asText(""));
        regCompanyData.put("phonenumber1", infoNode.path("phonenumber1").path(0).asText(""));
        regCompanyData.put("fax", infoNode.path("fax").path(0).asText(""));
        regCompanyData.put("email", companyNode.path("email").asText(""));
        regCompanyData.put("bizregno1", infoNode.path("bizregno1").path(0).asText(""));
        regCompanyData.put("representative", infoNode.path("representative").path(0).asText(""));
        if("CUSTOMER".equals(channelType)) {
            regCompanyData.put("vendorcode", companyNode.path("bpid").asText(""));
        } else {
            regCompanyData.put("vendorcode", infoNode.path("vendorcode").path(0).asText(""));
        }
        regCompanyData.put("zip_code", infoNode.path("zip_code").path(0).asText(""));
        regCompanyData.put("validStatus", companyNode.path("status").asText(""));
        regCompanyData.put("type", companyNode.path("type").asText(""));
        regCompanyData.put("source", companyNode.path("source").asText(""));
        regCompanyData.put("bpid", companyNode.path("bpid").asText(""));



        String salutation = CDCUserProfile.path("salutation").asText("");
        String language = CDCUserProfile.path("profile").path("languages").asText("");
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
        ObjectNode channelDataNode = (ObjectNode) CDCUserProfile.path("data").path("channels").path(channel);
        Map<String, Object> channelData = objectMapper.convertValue(channelDataNode, Map.class);
        Object adminTypeObj = channelData.get("adminType");
        String adminType = String.valueOf(adminTypeObj);
        String role = commonCodeRepository.selectRoleSearch("ROLE_CODE",adminType);


        List<CommonCode> codeList = commonCodeRepository.findByHeader("COUNTRY_CODE");
        List<CommonCode> divisions = commonCodeRepository.findByHeader("BIZ_WITH_DEPT");//divisionRepository.findAllByOrderByNameEnAsc();

        accountObject.put("salutation",salutation);
        accountObject.put("language",language);
        accountObject.put("firstName",firstName);
        accountObject.put("lastName",lastName);

        if (requestor_email.matches("^[A-Za-z0-9._%+-]+@(samsung\\.com|[A-Za-z0-9.-]+\\.samsung\\.com)$")) {
            socialProvidersYn = "Y";
        }

        accountObject.put("socialProvidersYn",socialProvidersYn);

        accountObject.put("country_code_work",countryCode);
        accountObject.put("work_phone",phoneNumber);
        accountObject.put("secDept",secDept);
        accountObject.put("job_title",job_title);
        accountObject.put("languages", languages);
        accountObject.put("roles", roleList);
        accountObject.put("role", role);
        accountObject.put("divisions", divisions);
        accountObject.put("codes", codeList);
        accountObject.put("loginId", requestor_email);
        accountObject.put("currentAdminRole", payload.get("role"));

        String popupSearchCompany = "";
        if("CUSTOMER".equals(channelType)) {
            popupSearchCompany = "popups/partnerApprovalSearchCompany";
        } else {
            popupSearchCompany = "popups/searchApprovalCompany";
        }

        Map<String, List<?>> fieldMap = cdcTraitService.generateFieldMap(channel, objectMapper, true, true,true); // Company와 User 데이터 모두 포함

        cdcTraitService.extractChannelAddFields(infoNode,CDCUserProfile, channel, accountObject,regCompanyData);

        // industry_type 값이 존재하고 배열일 경우 첫 번째 값을 추출하여 regCompanyData에 넣기
        String industryTypeValue = String.valueOf(infoNode.path("industry_type"));
        industryTypeValue = industryTypeValue.replaceAll("[\\[\\]\"\\\\]", "");
        regCompanyData.put("industryType", industryTypeValue);
        companyObject.put("registerCompany", regCompanyData);
        redirectAttributes.addFlashAttribute("type", type);
        redirectAttributes.addFlashAttribute("channels", channels);
        redirectAttributes.addFlashAttribute("accountObject", accountObject);
        redirectAttributes.addFlashAttribute("companyObject", companyObject);
        redirectAttributes.addFlashAttribute("ApprovalSearchCompanyModal", popupSearchCompany);
        redirectAttributes.addFlashAttribute("fieldMap", fieldMap);
        RedirectView redirectView = new RedirectView();

        // Request Type이 Company Doamin or Role Management 일 때
        // Redirect URL을 메인 (approvalInformation)로 설정

            redirectView.setUrl("/myPage/approvalInformation"); // go to get


        try {
            // Audit Log
            Map<String, String> param = new HashMap<>();
            param.put("type", "Approval_List");
            param.put("action", "Creation");
            param.put("condition", payload.get("channel"));
            // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
            param.put("result_count", "0");
            auditLogService.addAuditLog(session, param);
    
        } catch (Exception e) {
            log.error("Error userListDetail processing failed", e);
        }

        return redirectView;
    }
    

    public ModelAndView approvalInformation(Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        String menu = "approvalList";
        String content = "fragments/myPage/approvalInformation";

        ModelAndView modelAndView = new ModelAndView("myPage");

        modelAndView.addObject("payload", payload);
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        // modelAndView.addObject("companyDataResult", companyDataResult);
        return modelAndView;
    }


    // 도메인삭제 submit
    public RedirectView deleteDomain(Map<String, String> payload, HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes) {
        // Payload 및 rowData 확인용 println
        String uid = (String) session.getAttribute("cdc_uid");
        String cdc_email = (String) session.getAttribute("cdc_email");

        log.info("delete payload: " + payload);
        log.info("This is selectedRowData data: " + payload.get("selectedRowData"));
        String selectedRowDataString = (String) payload.get("selectedRowData");

        Map<String, Object> registerMapper = new HashMap<>();
        Map<String, Object> jsonResponse = new HashMap<>();
        int id = 1 ; //selected row id
        String requestor_uid = "";// payload.getOrDefault("requestor_uid", "");
        String new_status = "rejected"; // delete는 wf_master, list에 reject로 표시한다. 
        String email_domain = "", wf_id="", reject_reason = "delete";

        if (selectedRowDataString != null) {
            try {
                // JSON 문자열을 Map으로 파싱
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> selectedRowDataMap = mapper.readValue(selectedRowDataString, new TypeReference<Map<String, Object>>() {});
                        // id와 requestor_uid를 읽어옴
                if (selectedRowDataMap.containsKey("id"))           {   id = (int) selectedRowDataMap.get("id");                    }
                if (selectedRowDataMap.containsKey("wf_id"))        {   wf_id = (String) selectedRowDataMap.get("wf_id");    }
                if (selectedRowDataMap.containsKey("email_domain")) {   email_domain = (String) selectedRowDataMap.get("email_domain");    }
                if (selectedRowDataMap.containsKey("requestor_uid")){   requestor_uid = (String) selectedRowDataMap.get("requestor_uid");    }

            }catch (Exception e) {
                log.error("Error processing registration data", e);
                //jsonResponse.put("error", "Exception occurred: " + e.getMessage());
                return new RedirectView(request.getHeader("Referer"));
            }
        }
        log.info("updateUserStatus: wf_id " + wf_id + " new_status:"+ new_status + " id:"+id + " UID"+ uid);
        try {
            // wf_list 상태 업데이트
            wfMasterRepository.updateWfListStatus(wf_id, new_status);

            // wf_master 상태 업데이트
            wfMasterRepository.updateWfMasterStatus(uid, wf_id, cdc_email, new_status, reject_reason);

            //updateVerification_domains
            wfMasterRepository.updateVerificationDomains(uid, wf_id, cdc_email, "delete", reject_reason);
            
            //redirectAttributes.addFlashAttribute("responseErrorCode", updateUserStatus.toString());
            return new RedirectView(request.getHeader("Referer"));
        } catch (Exception e) {
            log.error("Error processing registration data", e);
            //jsonResponse.put("error", "Exception occurred: " + e.getMessage());
            return new RedirectView(request.getHeader("Referer"));
        }

        //return new RedirectView(request.getHeader("Referer"));
    }

    public ModelAndView ciamApprovalList(HttpServletRequest request, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("myPage");
        //String channel = (String) session.getAttribute("session_channel");
        List<Channels> channels  = new ArrayList<Channels>();

//        Channels channel = new Channels();
//        channel.setChannelName("ALL");
//        channel.setChannelDisplayName("ALL");
//
//        channels.add(channel);

        channels = channelRepository.findAll();

        modelAndView.addObject("channels", channels);

        String content = "fragments/myPage/ciamApprovalList";
        String menu = "ciamApprovalList";
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("requestType", commonCodeRepository.findByHeaderOrderBySortOrder("REQUEST_TYPE_CODE"));
        modelAndView.addObject("role", session.getAttribute("btp_myrole"));
        modelAndView.addObject("session_channel", session.getAttribute("session_channel"));
        return modelAndView;

    }

    public Map<String, Object> getApprovalList(Map<String, String> allParams, HttpSession session) {
        Map<String, Object> gridData = new HashMap<>();
        StringBuilder queryBuilder = new StringBuilder("SELECT channel, cc.\"name\", requestor_email, requestor_company_name, requestor_company_code, TO_CHAR(requested_date, 'YYYY-MM-DD HH24:MI:SS') as requested_date,wf_id,requestor_role,T1.status ");
        queryBuilder.append("FROM wf_master T1 ");
        queryBuilder.append("LEFT JOIN common_code cc ON cc.\"code\" = T1.workflow_code AND cc.\"header\" = 'REQUEST_TYPE_CODE' ");
        queryBuilder.append("WHERE 1=1 ");

        // 동적 조건 추가
        if (!"all".equals(allParams.get("channels"))) {
            queryBuilder.append("AND T1.channel = :channel ");
        }
        if (!"all".equals(allParams.get("requestType"))) {
            queryBuilder.append("AND cc.\"code\" = :requestType ");
        }
        if (!"all".equals(allParams.get("status"))) {
            queryBuilder.append("AND T1.status = :status ");
        }
        if (allParams.get("email") != null && !allParams.get("email").isEmpty()) {
            queryBuilder.append("AND T1.requestor_email LIKE :email ");
        }
        if (allParams.get("startDate") != null) {
            queryBuilder.append("AND T1.requested_date >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS') ");
        }
        if (allParams.get("endDate") != null) {
            queryBuilder.append("AND T1.requested_date <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS') ");
        }

        queryBuilder.append("ORDER BY requested_date DESC");

        Query query = entityManager.createNativeQuery(queryBuilder.toString());

        // 파라미터 바인딩
        if (!"all".equals(allParams.get("channels"))) {
            query.setParameter("channel", allParams.get("channels"));
        }
        if (!"all".equals(allParams.get("requestType"))) {
            query.setParameter("requestType", allParams.get("requestType"));
        }
        if (!"all".equals(allParams.get("status"))) {
            query.setParameter("status", allParams.get("status"));
        }
        if (allParams.get("email") != null && !allParams.get("email").isEmpty()) {
            query.setParameter("email", "%" + allParams.get("email") + "%"); // 이메일 검색시 LIKE로 처리
        }
        if (allParams.get("startDate") != null) {
            // String 날짜를 타임스탬프로 변환 (날짜 형식이 올바른지 확인하고 시간 부분 추가)
            String startDateString = allParams.get("startDate") + " 00:00:00";  // 시간 부분 추가
            Timestamp startDate = Timestamp.valueOf(startDateString);
            query.setParameter("startDate", startDate);
        }

        if (allParams.get("endDate") != null) {
            // String 날짜를 타임스탬프로 변환 (날짜 형식이 올바른지 확인하고 시간 부분 추가)
            String endDateString = allParams.get("endDate") + " 23:59:59";  // 시간 부분 추가
            Timestamp endDate = Timestamp.valueOf(endDateString);
            query.setParameter("endDate", endDate);
        }

        List<Object[]> wfMasterList = query.getResultList();

        List<Map<String, Object>> approvalData = new ArrayList<>();

        for (Object[] wfMaster : wfMasterList) {
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("channel", wfMaster[0]);
            rowData.put("requestType", wfMaster[1]);
            rowData.put("requestorId", wfMaster[2]);
            rowData.put("Company", wfMaster[3]);
            rowData.put("CompanyCode", wfMaster[4]);
            rowData.put("requestedDate", wfMaster[5]);
            rowData.put("wfId", wfMaster[6]);
            rowData.put("requestorRole", wfMaster[7]);
            rowData.put("status", wfMaster[8]);
            approvalData.add(rowData);
        }

        gridData.put("result", approvalData);

        return gridData;
    }


    public RedirectView ciamApprovalDetail(Map<String, String> payload, HttpServletRequest request, HttpSession session,RedirectAttributes redirectAttributes) {
        Map<String, Object> selectedRowData = new HashMap<>();
        session.setAttribute("ciamApprovalDetail", payload);
        // 2. payload에서 wf_id 추출
        String wf_id = "";
        String requestor_id = ""; // 선택한 요청자 ID
        String requestor_email = ""; // 선택한 요청자 ID
        String channel ="";
        String requestor_company_code = "";

        ObjectMapper objectMapper = new ObjectMapper();
        List<Channels> channels;
        List<CommonCode> roleList;
        JsonNode CDCUserProfile;
        Map<String,Object> companyObject = new HashMap<String,Object>();
        Map<String,Object> regCompanyData = new HashMap<String,Object>();
        Map<String,Object> accountObject = new HashMap<String,Object>();
        List<Map<String, String>> languages = new ArrayList<>();

        wf_id =payload.get("wfId");
        WfMaster wfmaster = wfMasterRepository.selectWfMaster(wf_id);

        requestor_id =wfmaster.getRequestorId();
        requestor_email =wfmaster.getRequestorEmail();
        channel =wfmaster.getChannel();
        requestor_company_code =wfmaster.getRequestorCompanyCode();

        List<CisCountry> countries = cisCountryRepository.findAllOrderedByNameEn();
        companyObject.put("countries", countries);

        channels = channelRepository.selectChannelName(channel);
        String channelType = channelRepository.selectChannelTypeSearch(channel);
        roleList = commonCodeRepository.selectRoleNameList("ROLE_CODE",payload.get("role"));
        CDCUserProfile = cdcTraitService.getCdcUser(requestor_id, 0);


        //회사정보 세팅
        JsonNode companyNode = cdcTraitService.getB2bOrg(requestor_company_code);
        JsonNode infoNode = companyNode.path("info");
        String type = companyNode.path("type").asText("");

        List<NewSubsidiary> subsidiarys = newSubsidiaryRepository.findAll();
        List<CommonCode> approvalDivisions = commonCodeRepository.findByHeader("DIVISION_CODE");

        companyObject.put("subsidiarys", subsidiarys);
        companyObject.put("approvalDivisions", approvalDivisions);
        companyObject.put("bpid", companyNode.path("bpid").asText(""));
        companyObject.put("source", companyNode.path("source").asText(""));
        companyObject.put("type", companyNode.path("type").asText(""));
        companyObject.put("validStatus", companyNode.path("status").asText(""));
        companyObject.put("zip_code", infoNode.path("zip_code").path(0).asText(""));
        companyObject.put("vendorcode", companyNode.path("bpid").asText(""));

        regCompanyData.put("name", companyNode.path("orgName").asText(""));

        String country = infoNode.path("country").path(0).asText("");
        country = secServingCountryRepository.selectCountryName(channel,country);

        regCompanyData.put("country", country);
        regCompanyData.put("state", infoNode.path("state").path(0).asText(""));
        regCompanyData.put("city", infoNode.path("city").path(0).asText(""));
        regCompanyData.put("street_address", infoNode.path("street_address").path(0).asText(""));
        regCompanyData.put("phonenumber1", infoNode.path("phonenumber1").path(0).asText(""));
        regCompanyData.put("fax", infoNode.path("fax").path(0).asText(""));
        regCompanyData.put("email", companyNode.path("email").asText(""));
        regCompanyData.put("bizregno1", infoNode.path("bizregno1").path(0).asText(""));
        regCompanyData.put("representative", infoNode.path("representative").path(0).asText(""));
        if("CUSTOMER".equals(channelType)) {
            regCompanyData.put("vendorcode", companyNode.path("bpid").asText(""));
        } else {
            regCompanyData.put("vendorcode", infoNode.path("vendorcode").path(0).asText(""));
        }
        regCompanyData.put("zip_code", infoNode.path("zip_code").path(0).asText(""));
        regCompanyData.put("validStatus", companyNode.path("status").asText(""));
        regCompanyData.put("type", companyNode.path("type").asText(""));
        regCompanyData.put("source", companyNode.path("source").asText(""));
        regCompanyData.put("bpid", companyNode.path("bpid").asText(""));

        String salutation = CDCUserProfile.path("salutation").asText("");
        String language = CDCUserProfile.path("profile").path("languages").asText("");
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
        accountObject.put("language",language);
        accountObject.put("firstName",firstName);
        accountObject.put("lastName",lastName);

        accountObject.put("country_code_work",countryCode);
        accountObject.put("work_phone",phoneNumber);
        accountObject.put("secDept",secDept);
        accountObject.put("job_title",job_title);
        accountObject.put("languages", languages);
        accountObject.put("roles", roleList);
        accountObject.put("role", payload.get("role"));
        accountObject.put("divisions", divisions);
        accountObject.put("codes", codeList);
        accountObject.put("loginId", requestor_email);
        accountObject.put("currentAdminRole", payload.get("role"));

        Map<String, List<?>> fieldMap = cdcTraitService.generateFieldMap(channel, objectMapper, true, true,true); // Company와 User 데이터 모두 포함

        cdcTraitService.extractChannelAddFields(infoNode,CDCUserProfile, channel, accountObject,regCompanyData);

        // industry_type 값이 존재하고 배열일 경우 첫 번째 값을 추출하여 regCompanyData에 넣기
        String industryTypeValue = String.valueOf(infoNode.path("industry_type"));
        industryTypeValue = industryTypeValue.replaceAll("[\\[\\]\"\\\\]", "");
        regCompanyData.put("industryType", industryTypeValue);
        companyObject.put("registerCompany", regCompanyData);
        redirectAttributes.addFlashAttribute("type", type);
        redirectAttributes.addFlashAttribute("channels", channels);
        redirectAttributes.addFlashAttribute("accountObject", accountObject);
        redirectAttributes.addFlashAttribute("companyObject", companyObject);
        redirectAttributes.addFlashAttribute("fieldMap", fieldMap);
        RedirectView redirectView = new RedirectView();


        Map<String, Object> dataContainer = new HashMap<>();
        dataContainer.put("type", type);
        dataContainer.put("channels", channels);
        dataContainer.put("accountObject", accountObject);
        dataContainer.put("companyObject", companyObject);
        dataContainer.put("fieldMap", fieldMap);

        // 세션에 통합 데이터 저장
        session.setAttribute("dataContainer", dataContainer);

        redirectView.setUrl("/myPage/ciamApprovalDetail"); // go to get


        try {
            // Audit Log
            Map<String, String> param = new HashMap<>();
            param.put("type", "Ciam_Approval_List");
            param.put("action", "Creation");
            param.put("condition", payload.get("channel"));
            // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
            param.put("result_count", "0");
            auditLogService.addAuditLog(session, param);

        } catch (Exception e) {
            log.error("Error ciamApprovalDetail processing failed", e);
        }

        return redirectView;
    }

    public ModelAndView ciamApprovalDetail(Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        Map<String, Object> dataContainer = (Map<String, Object>) session.getAttribute("dataContainer");

        if (dataContainer != null) {
            String type = (String) dataContainer.get("type");
            List<Channels> channels = (List<Channels>) dataContainer.get("channels");
            Map<String, Object> accountObject = (Map<String, Object>) dataContainer.get("accountObject");
            Map<String, Object> companyObject = (Map<String, Object>) dataContainer.get("companyObject");
            Map<String, List<?>> fieldMap = (Map<String, List<?>>) dataContainer.get("fieldMap");

            // 모델에 데이터 추가
            model.addAttribute("type", type);
            model.addAttribute("channels", channels);
            model.addAttribute("accountObject", accountObject);
            model.addAttribute("companyObject", companyObject);
            model.addAttribute("fieldMap", fieldMap);
        }

        String menu = "ciamApprovalList";
        String content = "fragments/myPage/ciamApprovalDetail";

        ModelAndView modelAndView = new ModelAndView("myPage");

        modelAndView.addObject("payload", payload);
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        return modelAndView;
    }

    public Map<String, Object> getApprovalDetailList(Map<String, String> allParams, HttpSession session) {
        Map<String, Object> gridData = new HashMap<>();

        // 쿼리를 wf_level, approved_email, status 항목으로 수정
        StringBuilder queryBuilder = new StringBuilder("SELECT wf_level, approver_email, status, approved_email,TO_CHAR(approved_date, 'YYYY-MM-DD HH24:MI:SS') as approved_date ");
        queryBuilder.append("FROM wf_list wl ");
        queryBuilder.append("WHERE wl.wf_id = :wfId ");

        // 쿼리 실행
        Query query = entityManager.createNativeQuery(queryBuilder.toString());
        query.setParameter("wfId", allParams.get("wfId"));

        List<Object[]> wfList = query.getResultList();
        List<Map<String, Object>> wfListMap = new ArrayList<>();

        for (Object[] wfMaster : wfList) {
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("wfLevel", wfMaster[0]); // wf_level
            rowData.put("approverEmail", wfMaster[1]); // approved_email
            rowData.put("status", wfMaster[2]); // status
            rowData.put("approvedEmail", wfMaster[3]); // status
            rowData.put("approvedDate", wfMaster[4]); // status
            wfListMap.add(rowData);
        }

        gridData.put("result", wfListMap);
        return gridData;
    }

}
