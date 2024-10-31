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
import com.samsung.ciam.services.*;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
public class EmpVerificationService {
    private static final ObjectMapper objectMapper = new ObjectMapper();
        //1. wf id 생성
    // 날짜 포맷을 지정합니다 (예: 20240826)
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 매일 시퀀스를 초기화하기 위해 AtomicInteger를 사용합니다.
    private static final AtomicInteger sequence = new AtomicInteger(1);

    // 현재 날짜를 저장하여 날짜가 변경되면 시퀀스를 리셋할 수 있게 합니다.
    private static LocalDate currentDay = LocalDate.now();


    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private ConsentJConsentContentsRepository consentJConsentContentsRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private CisCountryRepository cisCountryRepository;

    @Autowired
    private VerificationDomainRepository verificationDomainRepository;

    @Autowired
    private VerificationEmployRepository verificationEmployRepository;

    @Autowired
    private AuditLogService auditLogService; 

    @Autowired
    private WfIdGeneratorService wfIdGeneratorService;

    @Autowired
    private WFCreateService wfCreateService;

    @Autowired
    private SecServingCountryRepository secServingCountryRepository;

    @Autowired
    private ApprovalAdminRepository approvalAdminRepository;

    @Autowired
    private MailService mailService;

    public RedirectView selectedChannel(String channel,String channelDisplayName, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        session.setAttribute("session_channel",channel);
        session.setAttribute("session_display_channel",channelDisplayName);
        cdcTraitService.setAdminSession(session);
        return new RedirectView((String) session.getAttribute("selectedChannelRedirectUrl"));
    }

    public ModelAndView approvalRequest(HttpServletRequest request, HttpSession session, Model model) {
        ModelAndView modelAndView = new ModelAndView("myPage");
        Map<String, Object> cdcParams = new HashMap<>();
        Map<String, Object> companyDataResult = null;
        
        try {
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
            String channel = (String) session.getAttribute("session_channel");

            // common code
            // channel (channels 테이블 > value : channel_name / name : channel_display_name)
            modelAndView.addObject("channel", channelRepository.selectChannelTypeList(""));

            modelAndView.addObject("session_channel", channel);
            modelAndView.addObject("session_display_channel", session.getAttribute("session_display_channel"));
            // log.warn("channel : {}", channelRepository.selectChannelTypeList(""));
            // log.warn("session_channel : {}", channelRepository.selectChannelTypeList(""));
            // log.warn("session_display_channel : {}", channelRepository.selectChannelTypeList(""));
            // role (정해지면 프론트에서 추가)
            modelAndView.addObject("role", session.getAttribute("btp_myrole"));
            // modelAndView.addObject("role", "Partner Admin"); // kimjy Partner Admin 테스트용!!!
            log.warn("role rolerole: {}", session.getAttribute("btp_myrole"));
            
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
            
            

            // 국가 리스트 가져오기
            String channelName = channel;
            String role = (String)session.getAttribute("btp_myrole");
            List<SecServingCountry> countries = getSecServingCountries(channelName);
            
            String channelType = Arrays.asList("sba", "mmp", "e2e", "ets", "edo").contains(channelName) ? "customer" : channelName;
            List<Channels> channels = channelRepository.selectByChannelNameContaining(channelName);
            // ChannelBusinessAdmin 역할 처리
            if ("Channel biz Admin".equals(role)) {
                countries.clear();
                JsonNode CDCUserProfile = cdcTraitService.getCdcUser(uid, 0);
                String subsidiary = approvalAdminRepository.selectApprovalAdminSubsidiary(uid,channelName);
                List<String> secCountries = secServingCountryRepository.selectCountryCodeList(channelName,subsidiary);
                countries = secServingCountryRepository.selectCountriesBySubsidiaryCodes(channelName,subsidiary);
            }
            modelAndView.addObject("countries", countries);

             
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error("Error approvalRequest processing failed", e);
        }

        String menu = "approvalRequest";
        String content = "fragments/myPage/" + menu;

        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("companyDataResult", companyDataResult);

        return modelAndView;

    }

    public List<SecServingCountry> getSecServingCountries(String channel) {
        return secServingCountryRepository.selectCountryCodeChannel(channel);
    }

    public List<SecServingCountry> searchBusinessLocationList(Map<String, String> allParams, HttpSession session) {
        return secServingCountryRepository.selectCountryCodeChannel(allParams.get("channel"));
    }

    //  Role 변경 
    public RedirectView requestRoleSubmit(Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        Map<String, Object> registerMapper = new HashMap<>();
        Map<String, Object> jsonResponse = new HashMap<>();
        log.warn("requestRoleSubmit");
        String UID = (String) session.getAttribute("cdc_uid");
        String cdc_email = (String) session.getAttribute("cdc_email");

        String channel = payload.getOrDefault("channel", "sba");
        String currentRole = payload.getOrDefault("currentRole", "abc.com");
        String change_role = payload.getOrDefault("changeRole", "abc.com");
        String bpid = payload.getOrDefault("bpid", "NERP-FA58S0");
        String bp_name = payload.getOrDefault("bp_name", "no bp_name");
        String requestor_uid = UID;//payload.getOrDefault(UID, "");
        String requestor_email = cdc_email;//payload.getOrDefault(cdc_email, "");
        String requestor_role = payload.getOrDefault("requestor_role", "");
        try {

            //2024.08.26 wf 생성. kimjy
            String wf_id = "";
            wf_id = wfIdGeneratorService.generateNewWfId();
            log.warn("Role WF_ID real:?? {}", wf_id);
            String wf_code = "W05";

            Map<String,String> wf_param = new HashMap<String,String>();
            wf_param.put("channel",channel);
            wf_param.put("workflow_code",wf_code);
            wf_param.put("currentRole",currentRole);
            wf_param.put("change_role",change_role);
            wf_param.put("bpid",bpid);
            wf_param.put("bp_name",bp_name);
            wf_param.put("requestor_uid",requestor_uid);
            wf_param.put("requestor_email",requestor_email);
            wf_param.put("requestor_role",requestor_role);
            //지울것!!!
           // wf_param.put("country","NL");
           // wf_param.put("subsidiary","SEACE");
        //     wf_param.put("division","Network");
        //   wf_param.put("reg_channel","sba");
        //   wf_param.put("target_channel","gmap");
        //   wf_param.put("target_channeltype","Vendor"); //접근권한 확장 대상 채널 타입(Vendor or Customer)			

            // 새로만든 결재 로직 
            wfCreateService.wfCreate(session, wf_param) ; 


            //- 기존 로직은 Stop!! kimjy 2024.09.04
           // verificationDomainRepository.insert_wf_master_role(wf_id, channel, change_role, bpid, bp_name,requestor_uid,requestor_email,requestor_role); 

            //Audit Log
            Map<String,String> param = new HashMap<String,String>();
            param.put("type", "Approval_Request");
            param.put("action", "Creation");
            param.put("items", "channel, changeRole, bpid, bp_name, requestor_uid, requestor_email, requestor_role");
            param.put("condition", channel+" "+ change_role+" "+ bpid+" "+ bp_name+" "+requestor_uid+" "+requestor_email+" "+requestor_role);
            auditLogService.addAuditLog(session, param);

            redirectAttributes.addFlashAttribute("responseErrorCode", 0);   // 성공코드 반환
        
            return new RedirectView(request.getHeader("Referer"));
        } catch (Exception e) {
            log.error("Error processing registration data", e);
            redirectAttributes.addFlashAttribute("responseErrorCode", 500);
            //jsonResponse.put("error", "Exception occurred: " + e.getMessage());
            return new RedirectView(request.getHeader("Referer"));
        }
    }



    // Domain 추가 
    public RedirectView requestDomainSubmit(Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        log.info("payload: " + payload);
        Map<String, Object> registerMapper = new HashMap<>();
        Map<String, Object> jsonResponse = new HashMap<>();
        log.warn("requestDomainSubmit");
        String UID = (String) session.getAttribute("cdc_uid");
        String cdc_email = (String) session.getAttribute("cdc_email");

        String channel = payload.getOrDefault("channel", "sba");
        String email_domain = payload.getOrDefault("email_domain", "abc.com");
        String bpid = payload.getOrDefault("bpid", "NERP-FA58S0");
        String bp_name = payload.getOrDefault("bp_name", "no bp_name");
        String requestor_uid = UID;//payload.getOrDefault(UID, "");
        String requestor_email = cdc_email;//payload.getOrDefault(cdc_email, "");
        String requestor_role = payload.getOrDefault("requestor_role", "");

        try {
            // 1. Black List 확인
            boolean blackList = verificationDomainRepository.isEmailDomainBlacklisted(email_domain);
            //2. 같은 Email Domain 있는지 확인
            boolean SameEmailDomain = verificationDomainRepository.isSameEmailDomain(bpid, email_domain);
            //3. 신청중인 Email Domain 있는지 확인
            boolean isPendingEmailDomain = verificationDomainRepository.isPendingEmailDomain(bpid, email_domain);
            if(blackList){//black list에 domain이 걸리면 
                log.warn("Black List Status: {}", blackList);
                // 사용할수 없는 도메인 입니다.
                redirectAttributes.addFlashAttribute("responseErrorCode", 50001);
            } else if(SameEmailDomain){
                log.warn("SameEmailDomain: {}", SameEmailDomain);
                // 이미 존재하는 도메인 입니다.
                redirectAttributes.addFlashAttribute("responseErrorCode", 50002);
            } else if(isPendingEmailDomain){
                log.warn("isPendingEmailDomain real:?? {}", isPendingEmailDomain);
                // 이미 신청된 도메인 입니다.
                redirectAttributes.addFlashAttribute("responseErrorCode", 50003);
            } else {
                //2024.08.26 wf 생성. kimjy
                String wf_id = "";
                wf_id = wfIdGeneratorService.generateNewWfId();
                log.warn("WF_ID real:?? {}", wf_id);
                log.info("email_domain: " + email_domain);
                String wf_code = "W07";
                //requestor_role = CIAM Admin 이면 자동승인 
                if(requestor_role.equals("CIAM Admin")){
                    verificationDomainRepository.insert_Domain_CIAMAdmin(wf_id, channel, email_domain, bpid, bp_name,requestor_uid,requestor_email,requestor_role); 
                    
                    //자동 승인 후 verification_employ 찾아서 Update 해줘야 한다. CDC Update Logic


                }else{
                    //Data Insert 
                    Map<String,String> wparam = new HashMap<String,String>();
                    wparam.put("channel", channel);
                    wparam.put("email_domain", email_domain);
                    wparam.put("bpid", bpid);
                    wparam.put("bp_name", bp_name);
                    wparam.put("requestor_uid", requestor_uid);
                    wparam.put("requestor_email", requestor_email);
                    wparam.put("requestor_role", requestor_role);
                    wparam.put("workflow_code", wf_code);

                    //verificationDomainRepository.insert_wf_master_domain(wf_id, channel, email_domain, bpid, bp_name,requestor_uid,requestor_email,requestor_role,wf_code); 
                    wfCreateService.wfCreate(session, wparam );

                }

                //Audit Log
                Map<String,String> param = new HashMap<String,String>();
                param.put("type", "Approval_Request");
                param.put("action", "Creation");
                param.put("items", "channel, email_domain, bpid, bp_name, requestor_uid, requestor_email, requestor_role");
                param.put("condition", channel+" "+ email_domain+" "+ bpid+" "+ bp_name+" "+requestor_uid+" "+requestor_email+" "+requestor_role);
                auditLogService.addAuditLog(session, param);

                redirectAttributes.addFlashAttribute("responseErrorCode", 0);   // 성공코드 반환
            }
            return new RedirectView(request.getHeader("Referer"));
        } catch (Exception e) {
            log.error("Error processing registration data", e);
            redirectAttributes.addFlashAttribute("responseErrorCode", 500);
            //jsonResponse.put("error", "Exception occurred: " + e.getMessage());
            return new RedirectView(request.getHeader("Referer"));
        }
    }
    //재직인증 리스트  - VerificationEmpoy 테이블에서 Data 가져옴
    public List<Map<String,Object>> searchempVerification(Map<String, String> payload, HttpSession session) {
        log.info("payload: " + payload);
        String uid = "";
        if (session.getAttribute("cdc_uid") != null) {
            uid = (String) session.getAttribute("cdc_uid");
        }
        String role = (String)session.getAttribute("btp_myrole");
        String cdc_companyid = (String)session.getAttribute("cdc_companyid");
        String channel = payload.get("channel");
        String type = payload.get("type").toLowerCase(); // 소문자로 변환
        List<Map<String, Object>> resultList = null;
        // role = "Channel biz Admin"; test
        if(role.equals("Partner Admin")){ // PA 이면 회사코드를 넘겨서 같은 회사코드인 Data를 조회
            resultList = verificationEmployRepository.searchVerificationEmploy(channel, type, cdc_companyid);

        }else if(role.equals("Channel biz Admin")){ // subsidiary 로 조회
            resultList = verificationEmployRepository.searchVerificationEmployCB(channel, type, uid);

        }else { // Channel Admin, CIAM Admin 채널로 조회 
            resultList = verificationEmployRepository.searchVerificationEmploy(channel, type);
        }

        //Audit Log
        Map<String,String> param = new HashMap<String,String>();
            param.put("type", "Employment_Verification");
            param.put("action", "ListView");
            param.put("condition", payload.get("channel"));
                // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
            param.put("result_count", String.valueOf(resultList.size()));
        auditLogService.addAuditLog(session, param);
        
        return resultList;
    }

    //재직인증
    public ModelAndView employVerification(HttpServletRequest request, HttpSession session, Model model) {
        ModelAndView modelAndView = new ModelAndView("myPage");
        Map<String, Object> cdcParams = new HashMap<>();
        Map<String, Object> companyDataResult = null;
        Map<String, Object> userData = null;
        
        try {
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
            String channel = (String) session.getAttribute("session_channel");

            // common code
            // channel (channels 테이블 > value : channel_name / name : channel_display_name)
            modelAndView.addObject("channel", channelRepository.selectChannelTypeList(""));
            modelAndView.addObject("session_channel", channel);
            modelAndView.addObject("session_display_channel", session.getAttribute("session_display_channel"));
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
            log.error("Error employVerification processing failed", e);
        }

        String menu = "employmentVerification";
        String content = "fragments/myPage/" + menu;

        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("companyDataResult", companyDataResult);
        
        return modelAndView;

    }

    // 재직인증 Batch Job : Expired date 가 되면 pending -> reject로 변경한다. 
    public void employVerificationExpire(){
        // 오늘 날짜를 LocalDateTime 형식으로 가져오기
        LocalDateTime today = LocalDateTime.now().plusDays(1);
        
        // 날짜 형식에 맞게 변환 (DB와의 비교를 위해 형식을 맞춰줌)
        String formattedToday = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // 만료된 requestor_uid와 channel 리스트를 반환
        List<Map<String, Object>> rejectedDataList = verificationEmployRepository.expireVerificationEmploy(formattedToday);

        // 각 UID와 채널에 대해 CDC 상태를 'emp_pending'으로 업데이트 .. disabled로 하면 매일 batch가 돌아가면서 실제로 삭제를 해버린다. 
        for (Map<String, Object> rejectedData : rejectedDataList) {
            String uid = (String) rejectedData.get("requestor_uid");
            String requestor_email = (String) rejectedData.get("requestor_email");
            String channel = (String) rejectedData.get("channel");
            String bpid = (String) rejectedData.get("bpid");
            String subsidiary = (String) rejectedData.get("subsidiary");
            String reject_reason ="deadline for re-verification has passed";
            String cdc_email = "";
            log.info(" formattedToday: " + formattedToday +" uid:"+uid + " channel:" + channel);
            // CDC API 호출
            try {
                cdcUserUpdate(uid, channel, "emp_pending");
                log.info("Successfully updated CDC status for UID: " + uid + ", Channel: " + channel);
            } catch (Exception e) {
                log.error("Failed to update CDC status for UID: " + uid + ", Channel: " + channel);
            }

            //재직인증 기한 마감 Mail 발송
            JsonNode CDCUser = cdcTraitService.getCdcUser(uid, 0);
            
            List<Map<String, Object>> RejectorList = null;

            // PA 검색
            RejectorList = verificationEmployRepository.searchVerificationEND_PA(channel, bpid);
            log.info("PA?? :::  "+ RejectorList);
                
            if (RejectorList == null || RejectorList.isEmpty()) {
                // PA 없으면 CB 검색
                RejectorList = verificationEmployRepository.searchVerificationEND_CB(channel, subsidiary);

                if (RejectorList == null || RejectorList.isEmpty()) {
                    // CB 없으면 CA 검색
                    RejectorList = verificationEmployRepository.searchVerificationEND_CA(channel);
                }
            }
            
            if (RejectorList != null && !RejectorList.isEmpty()) {
                StringBuilder emailBuilder = new StringBuilder();
            
                for (Map<String, Object> rejectorEmail : RejectorList) {
                    String email = (String) rejectorEmail.get("email");
                    if (email != null) {
                        if (emailBuilder.length() > 0) {
                            emailBuilder.append(", ");
                        }
                        emailBuilder.append(email);
                    }
                }
            
                // 최종적으로 cdc_email에 모든 이메일을 쉼표로 구분하여 저장
                cdc_email = emailBuilder.toString();
                // cdc_email 출력 또는 로직 사용
                log.info("cdc_email :::  "+cdc_email);
            }

            Map<String, Object> paramArr = new HashMap<>();
            paramArr.put("template", "TEMPLET-NEW-009");
            paramArr.put("cdc_uid", uid);
            paramArr.put("channel", channel );
            paramArr.put("EmployeeVerificationRejector", cdc_email );
            paramArr.put("EmployeeVerificationRejectReason", reject_reason );
            paramArr.put("firstName", CDCUser.get("profile").get("firstName") != null ? CDCUser.get("profile").get("firstName").asText() : "");
            paramArr.put("lastName", CDCUser.get("profile").get("lastName") != null ? CDCUser.get("profile").get("lastName").asText() : "");
            // Mail 발송 
            log.info("Mail Send :::  "+cdc_email + " cdc_uid:"+ uid +  " bpid:"+ bpid + " requestor_email::"+requestor_email );
            mailService.sendMail(paramArr);


        }

    }

    // 재직인증 Batch Job : CDC에서 Data를 가져와서 DB에 넣는 I/F API
    public void employVerificationTableInsert(){
        //1) API가 호출되면 지난달 Data view_status 를 close로 변경시킨다. - 일단 모든 Data를 close. 
        verificationEmployRepository.updateViewStatus();

        List<Map<String,Object>> resultUserList = null;
        String query = null;
        //2) data.accountID != 'C100' 삼성임직원 제외로직
        // 자동인증 이메일이 등록된 회사도 제외 -- cdc data가 1만개만 return 할수 있어서.. 
        // 자동인증 이므로 user_status를 approved로 가져오면 된다. 
        //
        List<Map<String, String>> domainList = verificationDomainRepository.EmailDomainList();

        // domainList에서 "bpid" 값만 추출하고 쉼표로 구분하여 문자열로 변환
        String domains = domainList.stream()
                                .map(domain -> "'" + domain.get("bpid") + "'")
                                .collect(Collectors.joining(", "));

        log.info(" domains: " + domains );

        query = "SELECT UID, profile.username, data.subsidiary, data.division,  data.channels, groups.organizations.orgName, data.accountID "
                + "FROM accounts WHERE data.accountID != 'C100' AND (data.userStatus= 'active' OR data.userStatus= 'inactive') "
                + "AND data.accountID IN (" + domains + ") " 
                + " LIMIT 400";

        GSResponse response = gigyaService.executeRequest("default", "accounts.search", query);
        try {
            Map<String,Object> mapVal = objectMapper.readValue(response.getResponseText(), Map.class);
            resultUserList = (List<Map<String,Object>>)mapVal.get("results");
            log.warn("employVerificationTableInsert List: {}", resultUserList);
    
            // DB에 Insert 후 완료 여부를 확인
            int processResult = processAndInsertVerificationData(resultUserList);
            if(processResult > 0){
                String NotInQuery = "SELECT UID, profile.username, data.subsidiary, data.division, data.channels, groups.organizations.orgName, data.accountID "
                + "FROM accounts WHERE data.accountID != 'C100' AND (data.userStatus= 'active' OR data.userStatus= 'inactive') "
                + "AND data.accountID Not IN (" + domains + ") "  // 제외할 도메인 추가
                + " LIMIT 200";                
                GSResponse response1 = gigyaService.executeRequest("default", "accounts.search", NotInQuery);
                mapVal = null; 
                resultUserList = null;
                mapVal = objectMapper.readValue(response1.getResponseText(), Map.class);
                resultUserList = (List<Map<String,Object>>)mapVal.get("results");
                log.warn("employVerificationTableInsert List2: {}", resultUserList);
                processAndInsertVerificationData(resultUserList);

            }

            // Insert 완료 한 후 메일발송
            List<Map<String, Object>> resultList = verificationEmployRepository.insertBatchMail();
            
            for (Map<String, Object> mailUID : resultList) {
                String uid = (String) mailUID.get("uid");
                String mail_channel = (String) mailUID.get("channel");
                JsonNode CDCUser = cdcTraitService.getCdcUser(uid, 0);
                log.info("Mail Send " + CDCUser + " channel::");

                Map<String, Object> paramArr = new HashMap<>();
                paramArr.put("template", "TEMPLET-NEW-006");
                paramArr.put("cdc_uid", uid);
                paramArr.put("channel", mail_channel );
                paramArr.put("firstName", CDCUser.get("profile").get("firstName") != null ? CDCUser.get("profile").get("firstName").asText() : "");
                paramArr.put("lastName", CDCUser.get("profile").get("lastName") != null ? CDCUser.get("profile").get("lastName").asText() : "");
                // Mail 발송 
                mailService.sendMail(paramArr);
            }

        }catch(Exception e){
            log.error("Error employVerificationTableInsert processing failed", e);
        }

        //3) 자동인증 등록된 회사 User를 조회 해서 DB 등록, 허용된 도메인에 대해서는 user_status를 approved로 자동승인해준다. 

    }
    
    // DB 저장
    private int processAndInsertVerificationData(List<Map<String, Object>> resultUserList) {
        for (Map<String, Object> user : resultUserList) {
            // user가 null인지 체크
            if (user == null) {
                log.warn("User data is null, skipping this entry.");
                continue;
            }

            String uid = (String) user.getOrDefault("UID", null);
            Map<String, Object> data = (Map<String, Object>) user.getOrDefault("data", null);
            String accountId = data != null ? (String) data.getOrDefault("accountID", null) : null;
            // accountId가 null인지 체크하고, null이면 해당 유저 데이터 건너뛰기
            if (accountId == null) {
                log.warn("Account ID is null for UID: {}, skipping this entry.", uid);
                continue;
            }
            String division = data != null ? (String) data.getOrDefault("division", null) : null;
            String subsidiary = data != null ? (String) data.getOrDefault("subsidiary", null) : null;

            Map<String, Object> channels = data != null ? (Map<String, Object>) data.getOrDefault("channels", null) : null;
            Map<String, Object> profile = (Map<String, Object>) user.getOrDefault("profile", null);
            String username = profile != null ? (String) profile.getOrDefault("username", null) : null;
            List<Map<String, Object>> organizations = (List<Map<String, Object>>) ((Map<String, Object>) user.getOrDefault("groups", new HashMap<>())).getOrDefault("organizations", null);
            String orgName = (organizations != null && !organizations.isEmpty()) ? (String) organizations.get(0).getOrDefault("orgName", null) : null;
    
            // channels가 null인지 체크
            if (channels == null) {
                log.warn("Channels data is null for UID: {}", uid);
                continue;
            }

            // 각 채널 데이터를 반복 처리
            for (Map.Entry<String, Object> channelEntry : channels.entrySet()) {
                String channelName = channelEntry.getKey(); // 채널 이름 (예: edo, partnerhub)
                Map<String, Object> channelData = (Map<String, Object>) channelEntry.getValue();
                String cdc_status = channelData != null ? (String) channelData.getOrDefault("approvalStatus", null) : null;

                if (channelData != null && channelData.containsKey("approvalStatus") && cdc_status.equals("approved") && channelName.equals("sba") ) { 
                    //20240926 sba만 open 하기 때문에 sba만 hardcoding. 

                    String lastLogin = channelData != null ? (String) channelData.getOrDefault("lastLogin", null) : null;
                    String approvalStatusDate = channelData != null ? (String) channelData.getOrDefault("approvalStatusDate", null) : null;

                    Object adminTypeObj = channelData.getOrDefault("adminType", null);
                    Integer adminType = null;

                    if (adminTypeObj instanceof String) {
                        String adminTypeStr = (String) adminTypeObj;
                        if (!adminTypeStr.isEmpty()) {
                            try {
                                adminType = Integer.parseInt(adminTypeStr);  // String -> Integer로 변환
                            } catch (NumberFormatException e) {
                                log.error("Invalid number format for adminType: " + adminTypeStr);
                            }
                        } else {
                            log.info("adminType is an empty string");
                        }
                    } else if (adminTypeObj instanceof Integer) {
                        adminType = (Integer) adminTypeObj;
                    }

                    // 기존의 role 할당 코드
                    String role = "General User";
                    if (adminType != null) {
                        switch (adminType) {
                            case 0 -> role = "General User";
                            case 1 -> role = "Channel Admin";
                            case 2 -> role = "Channel biz Admin";
                            case 3 -> role = "Partner Admin";
                            case 4 -> role = "CIAM Admin";
                            case 9 -> role = "Temp Approver";
                            default -> role = "General User";
                        }
                    }else{
                        role = "General User";
                    }

                    String status ="pending";
                    log.info("username:"+username+ " cdc_status: " + cdc_status + " adminType: "+ adminType + " role: "+ role);
                    if (adminType == null || adminType == 0 ) {// General User 나 Partner Admin 일때만 DB 입력
                        // 데이터베이스에 INSERT
                        verificationEmployRepository.insertVerificationEmploy(
                            uid,            // UID
                            accountId,      // accountID 회사코드
                            channelName,    // channel (예: edo, partnerhub)
                            username,       // Email
                            orgName,        // 회사명
                            role, 
                            cdc_status, // CDC에서 가져온 User 상태값
                            subsidiary, division
                        );
                    }else if(adminType == 3){ //Partner Admin 일때는 무조건 Pending
                        verificationEmployRepository.insertVerificationEmployPA(
                            uid,            // UID
                            accountId,      // accountID 회사코드
                            channelName,    // channel (예: edo, partnerhub)
                            username,       // Email
                            orgName,        // 회사명
                            role, 
                            cdc_status, // CDC에서 가져온 User 상태값
                            subsidiary, division
                        );
                        
                    }
                }
            }

        }
        return 1;
    }

    // EmploymentVerification 재직인증 Submit 
    public RedirectView employmentVerificationSubmit(Map<String, String> payload, HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes) {
        // Payload 및 rowData 확인용 println
        String UID = (String) session.getAttribute("cdc_uid");
        String cdc_email = (String) session.getAttribute("cdc_email");

        log.info("payload: " + payload);
        log.info("This is row data: " + payload.get("rowData"));      // 이사님 rowData는 JSON string이라 JSON으로 변환하셔서 사용하시면 됩니다. - 홍정인
        String selectedRowDataString = (String) payload.get("rowData");

        Map<String, Object> registerMapper = new HashMap<>();
        Map<String, Object> jsonResponse = new HashMap<>();
        int id = 1 ; //selected row id
        String requestor_uid = "";// payload.getOrDefault("requestor_uid", "");
        String new_status = payload.getOrDefault("requestType", "");
        String reject_reason = payload.getOrDefault("rejectReason", "");
        String channel ="", reject_id ="";
        if (selectedRowDataString != null) {
            try {
                // JSON 문자열을 Map으로 파싱
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> selectedRowDataMap = mapper.readValue(selectedRowDataString, new TypeReference<Map<String, Object>>() {});
                        // id와 requestor_uid를 읽어옴
                if (selectedRowDataMap.containsKey("id")) {  id = (int) selectedRowDataMap.get("id");       }
                if (selectedRowDataMap.containsKey("requestor_uid")) {   requestor_uid = (String) selectedRowDataMap.get("requestor_uid");    }
                if (selectedRowDataMap.containsKey("channel")) {   channel = (String) selectedRowDataMap.get("channel");    }
                if (selectedRowDataMap.containsKey("reject_id")) {   reject_id = (String) selectedRowDataMap.get("reject_id");    }

            }catch (Exception e) {
                log.error("Error processing registration data", e);
                //jsonResponse.put("error", "Exception occurred: " + e.getMessage());
                return new RedirectView(request.getHeader("Referer"));
            }
        }
        log.info("updateUserStatus: requestor_uid " + requestor_uid + " new_status:"+ new_status + " id:"+id + " UID"+ UID + " reject_id:"+reject_id);
        try {
            // 데이터베이스에 INSERT
           int val = verificationEmployRepository.updateUserStatus(
                new_status,        // new_status (예: approved, reject)
                reject_reason,     // reject_reason
                requestor_uid,     // requestor_uid
                id,           // 화면 ID(row)
                UID,
                cdc_email
            );
            //  rejected 인 경우 cdc disabled

            if(new_status.equals("rejected")){
                log.info("rejected: Auto " );
                cdcUserUpdate(requestor_uid, channel, "disabled" );

                // Check CDC user existence
                JsonNode CDCUser = cdcTraitService.getCdcUser(requestor_uid, 0);
                log.info("rejected: CDCUser 1 " + CDCUser + " channel::"+channel);

                Map<String, Object> paramArr = new HashMap<>();
                paramArr.put("template", "TEMPLET-NEW-008");
                paramArr.put("cdc_uid", requestor_uid);
                paramArr.put("channel", channel );
                paramArr.put("EmployeeVerificationRejector", cdc_email );
                paramArr.put("EmployeeVerificationRejectReason", reject_reason );
                paramArr.put("firstName", CDCUser.get("profile").get("firstName") != null ? CDCUser.get("profile").get("firstName").asText() : "");
                paramArr.put("lastName", CDCUser.get("profile").get("lastName") != null ? CDCUser.get("profile").get("lastName").asText() : "");
                // 반려인 경우 Mail 발송 
                mailService.sendMail(paramArr);
        
            }
            //  auto reject이 되서 수동으로 승인 한 경우 cdc approved로 update 해준다. 
            if(new_status.equals("approved") && reject_id != null && reject_id.equals("auto") ){
                log.info("Auto Reject appr Change" );
                cdcUserUpdate(requestor_uid, channel, "approved" );
            }
            redirectAttributes.addFlashAttribute("responseErrorCode", 0); // 성공
            return new RedirectView(request.getHeader("Referer"));
        } catch (Exception e) {
            log.error("Error processing registration data", e);
            //jsonResponse.put("error", "Exception occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("responseErrorCode", 1); // 실패
            return new RedirectView(request.getHeader("Referer"));
        }
   }

    private String cdcUserUpdate(String uid,  String channel, String approvalStatus){
        log.info("cdcUserUpdate -> UID: " + uid + ", approvalStatus: " + approvalStatus );
        ObjectMapper objectMapper = new ObjectMapper();
        Optional<Channels> optionalChannelObj = channelRepository.selectByChannelName(channel);
        Map<String, Object> dataFields = new HashMap<>();
        Map<String, Object> cdcParams = new HashMap<>();
        cdcParams.put("UID", uid);
        dataFields.put("channels", Map.of(
            channel, Map.of(
                    "approvalStatus", approvalStatus,
                    "approvalStatusDate", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    "lastLogin", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            )
        ));
        try {
            cdcParams.put("data", objectMapper.writeValueAsString(dataFields));
        } catch (Exception e) {
            log.error("Error processing data fields", e);
        }
        GSResponse setAccountResponse = gigyaService.executeRequest("default", "accounts.setAccountInfo", cdcParams);
        log.info("CDCsetAccountInfo: {}", setAccountResponse.getResponseText());
        if (setAccountResponse.getErrorCode() == 0) {
            return "ok";
        } else {
            return "failed";
        }
    }


    // JSON 값에서 빈 문자열이 아닌 값을 추출하는 헬퍼 메서드
    private static String getNonEmptyValue(JsonNode jsonNode) {
        String value = jsonNode.asText();
        return value.isEmpty() ? null : value;
    }
    
}