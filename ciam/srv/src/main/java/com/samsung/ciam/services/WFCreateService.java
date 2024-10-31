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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
public class WFCreateService {

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private WfIdGeneratorService wfIdGeneratorService;

    @Autowired
    private WfMasterRepository wfMasterRepository;

    @Autowired
    private ChannelRepository channelRepository;

// W01	신규 가입승인
// W02	전환 가입승인
// W03	초대 가입승인
// W04	AD 가입승인
// W05	Role Management 승인
// W06	SSO Access 승인
// W07	Company Domain 승인

    public String wfCreate(HttpSession session, Map<String, String> wfData){
        ObjectMapper objectMapper = new ObjectMapper();
        String wf_id = "";
        if(wfData!=null){
            log.warn("WFCreate !!!!!!! Start {}", wfData );
            wf_id = wfIdGeneratorService.generateNewWfId();
            log.warn("WF_ID real:?? {}", wf_id);
            String UID = (String) session.getAttribute("cdc_uid");
            String cdc_email = (String) session.getAttribute("cdc_email");
            String workflow_code = StringUtil.getStringValue(wfData.get("workflow_code"), "W01");
            String channel =    StringUtil.getStringValue(wfData.get("channel"), "");
            String currentRole =StringUtil.getStringValue(wfData.get("currentRole"), "General User");
            String change_role =StringUtil.getStringValue(wfData.get("change_role"), "");
            String company_code =       StringUtil.getStringValue(wfData.get("bpid"), "");
            String bp_name =    StringUtil.getStringValue(wfData.get("bp_name"), "");
            String country =    StringUtil.getStringValue(wfData.get("country"), "");
            String subsidiary = StringUtil.getStringValue(wfData.get("subsidiary"), "");
            String division =   StringUtil.getStringValue(wfData.get("division"), "");
            String rule_master_id = "";
            String approveFormat = "auto"; // auto로 한번 고정되면 바꾸지말것! 전체 auto or 전체 self

            // domain request에서 사용하는 파라메터 
            String requestor_uid =   StringUtil.getStringValue(wfData.get("requestor_uid"), "");
            String requestor_email =   StringUtil.getStringValue(wfData.get("requestor_email"), "");
            String requestor_role =   StringUtil.getStringValue(wfData.get("requestor_role"), "");
            String email_domain =   StringUtil.getStringValue(wfData.get("email_domain"), "");
            //W03 초대에서 사용하는 파라메터
            //invitaion_sender_id, invitaion_sender_email
            String invitaion_sender_id =   StringUtil.getStringValue(wfData.get("invitaion_sender_id"), "");
            String invitaion_sender_email =   StringUtil.getStringValue(wfData.get("invitaion_sender_email"), "");

            //reg_channel, target_channel, target_channeltype W06 SSO 에서 사용하는 파라메터
            String reg_channel =   StringUtil.getStringValue(wfData.get("reg_channel"), "");
            String target_channel =   StringUtil.getStringValue(wfData.get("target_channel"), "");
            String target_channeltype =   StringUtil.getStringValue(wfData.get("target_channeltype"), "");

            if(UID == null || UID.length()<1){
                UID = requestor_uid;
            }
            if(cdc_email == null || cdc_email.length()<1){
                cdc_email = requestor_email;
            }
            
            int stage = 1; // 최대 Stage
            int wf_max_level = 1; // 최대 결재 레벨
            int wf_level = 1; // 현재 결재 레벨
        
            log.info("This is row data: channel " + channel + "  country:"+ country);
            List<Map<String, Object>> RuleList = new ArrayList<>(); // 빈 ArrayList로 초기화
            List<Map<String, Object>> wfList = new ArrayList<>(); // WF List
            List<Map<String, Object>> TempwfList = new ArrayList<>(); // Temporary WF List
            wf_max_level = 1; // wf_max_level 초기값 설정
            
            // 1. approval_rule_master 테이블과 approval_rule을 join해서 rule_master_id, stage, role ~~ 가져온다.
            try {
                RuleList = wfMasterRepository.searchRule(channel, workflow_code, country, subsidiary, division);

                Map<String, Object> stage_result = RuleList.get(0);
                log.info("stage_result: {}", stage_result );
                for (Map.Entry<String, Object> entry : stage_result.entrySet()) {
                    log.info("Key: {}, Value: {}", entry.getKey(), entry.getValue());
                }
                Integer Tstage = (Integer) stage_result.get("stage");
                // stage가 null이 아니고 0보다 큰 경우에만 값 할당
                if (Tstage != null && Tstage > 0) {
                    stage = Tstage;
                } else {
                }
               // approveFormat = (String) stage_result.get("approve_format");
               approveFormat = (String)  stage_result.getOrDefault("approve_format", "self");
               //channel = (String)  stage_result.getOrDefault("channel", channel);
               country = (String)  stage_result.getOrDefault("country", "ALL");
               subsidiary = (String)  stage_result.getOrDefault("subsidiary", "ALL");
               division = (String)  stage_result.getOrDefault("division", "ALL");
              //  String currentRole = payload.getOrDefault("currentRole", "abc.com");
              log.info("stage_result ::::."+ stage_result + " channel" +channel + " country ::"+country);

              log.info("approveFormat ::::."+ approveFormat + " channel" +channel + " country ::"+country);

                int now_stage = 0; // 현재 Stage 값 switch에 걸리는 Data가 있을때만 +1을 해준다. 
                for (int i = 0; i < RuleList.size(); i++) {
                    Map<String, Object> result = RuleList.get(i);
            
                    String rule_level = (String) result.get("rule_level");
                    rule_master_id = (String) result.get("rule_master_id");
                    String role = (String) result.get("role");
                    log.info("Record " + (i + 1) + " - role: " + role + ", stage: " + stage + ", rule_level: " + rule_level +
                            ", Approve Format: " + approveFormat + ", rule_master_id: " + rule_master_id);


                    // 2. stage가 1인 경우 wf_max_level = 1로 변경하고 Rule Lv 순서대로 결재자 찾기. 자동승인 고려
                    switch (role) {
                        case "PA": // Partner Admin
                        TempwfList = wfMasterRepository.searchPA(channel, company_code, rule_master_id, rule_level);
                            log.info("PA -> channel: " + channel + ", company_code: " + company_code +
                                               ", rule_master_id: " + rule_master_id + ", rule_level: " + rule_level);
                            break;
            
                        case "CB": // Channel Biz Admin
                        TempwfList = wfMasterRepository.searchCB(channel, country, subsidiary, division, rule_master_id, rule_level);
                            log.info("CB -> channel: " + channel + ", country: " + country +", subsidiary: " + subsidiary + ", division: " + division + ", rule_master_id: " + rule_master_id + ", rule_level: " + rule_level);
                            break;
            
                        case "AM": // CIAM Admin
                        TempwfList = wfMasterRepository.searchAM();
                            
                            break;
            
                        case "CA": // Channel Admin
                        TempwfList = wfMasterRepository.searchCA(channel, rule_master_id, rule_level);
                            log.info("CA -> channel: " + channel + ", rule_master_id: " + rule_master_id + ", rule_level: " + rule_level);
                            break;
            
                        default:
                            log.info("No matching role found.");
                    }
                    // TempwfList에서 필요한 데이터만 추출하여 wfList에 추가
                    if (TempwfList != null && !TempwfList.isEmpty()) {
                        now_stage++; // 걸린 Data가 있다면 stage를 1개씩 늘려준다. 
                        for (Map<String, Object> entry : TempwfList) {
                            // 필요한 데이터만 추출
                            Map<String, Object> filteredEntry = new HashMap<>();
                            filteredEntry.put("uid", entry.get("uid"));
                            filteredEntry.put("email", entry.get("email"));
                            filteredEntry.put("type", entry.get("type"));
                            filteredEntry.put("channel", entry.get("channel"));
                            filteredEntry.put("company_code", entry.get("company_code"));
                            filteredEntry.put("rule_level", rule_level);
                            filteredEntry.put("stage", stage);
                            filteredEntry.put("now_stage", now_stage);
                            filteredEntry.put("approveFormat", approveFormat);
                            // wfList에 추가
                            wfList.add(filteredEntry);
                        }
                        if(now_stage == stage) {
                            break; //stage가 같다면 for문을 정지시킨다. 
                        }
                    }
   
                }
                // For 문장 끝난 이후, 결재 Data가 들어왔으면 결재선을 만들어준다. 
                //now_stage = wf_max_level : stage의 최대값임.
                switch (workflow_code) {
                    case "W01": // W01 신규유저가입
                    case "W04": // W04 AD유저가입
                        log.info("insertWFMaster: " + wf_id + channel+ "  WorkFlow::: "+workflow_code+ UID+cdc_email+ currentRole+ "pending"+ now_stage + bp_name+company_code + " rule_master_id :"+ rule_master_id);
                        // 자동승인인 경우
                        if(approveFormat.equals("auto")){
                            wfMasterRepository.insertWFMaster(wf_id, channel, workflow_code, UID,cdc_email, currentRole, company_code, bp_name,  "approved", rule_master_id , now_stage);
                        }else{ // 수동승인인 경우
                            wfMasterRepository.insertWFMaster(wf_id, channel, workflow_code, UID,cdc_email, currentRole, company_code, bp_name,  "pending", rule_master_id , now_stage);
                        }
                        
                        for (Map<String, Object> entry : wfList) {
                            try {
                                // Map을 JSON 문자열로 변환
                                String jsonString = objectMapper.writeValueAsString(entry);
                                String approver_id =   StringUtil.getStringValue(entry.get("uid"), "");
                                String approver_email = StringUtil.getStringValue(entry.get("email"), "");
                                //String approval_format= StringUtil.getStringValue(entry.get("approveFormat"), "");
                                String approver_role =   StringUtil.getStringValue(entry.get("type"), "");
                                // Object를 Integer로 캐스팅하여 int로 변환
                                int set_wf_level = entry.get("now_stage") != null ? (Integer) entry.get("now_stage") : 0;
                                // 자동승인인 경우
                                if(approveFormat.equals("auto")){
                                    // WF List를 만들어준다. 
                                    wfMasterRepository.insertWFList(wf_id, workflow_code, set_wf_level, "approved", approveFormat, approver_id, approver_email, approver_role);
                                    //requestor_id
                                    log.info("cdcUserUpdate -> UID: " + UID + ", channel: " + channel );
                                    String cdcUpdate = cdcUserUpdate(UID, channel, "approved" );// 최종결재가 완료되면 CDC Data를 Update 해준다. 
                                    //redirectAttributes.addFlashAttribute("cdcUserUpdate", cdcUpdate);

                                }else{
                                    // 수동승인인 경우 WF List를 만들어준다. 
                                    wfMasterRepository.insertWFList(wf_id, workflow_code, set_wf_level, "pending", approveFormat, approver_id, approver_email, approver_role);
                                }
                            } catch (Exception e) {
                                log.error("Error W04 processing failed", e);
                            }
                        }
                        break;

                        case "W02": // W02 전환유저가입
                            log.info("insertWFMaster: W02 " + wf_id + channel+ " workflow_code :"+workflow_code+" UID: "+ UID+cdc_email+ currentRole+ "pending"+ now_stage + bp_name+company_code + " rule_master_id :"+ rule_master_id);
                        // 자동승인인 경우
                        if(approveFormat.equals("auto")){
                            wfMasterRepository.insertWFMaster(wf_id, channel, workflow_code, UID,cdc_email, currentRole, company_code, bp_name,  "approved", rule_master_id , now_stage);
                        }else{ // 수동승인인 경우
                            wfMasterRepository.insertWFMaster(wf_id, channel, workflow_code, UID,cdc_email, currentRole, company_code, bp_name,  "pending", rule_master_id , now_stage);
                        }
                        
                        for (Map<String, Object> entry : wfList) {
                            try {
                                // Map을 JSON 문자열로 변환
                                String jsonString = objectMapper.writeValueAsString(entry);
                                String approver_id =   StringUtil.getStringValue(entry.get("uid"), "");
                                String approver_email = StringUtil.getStringValue(entry.get("email"), "");
                               // String approval_format= StringUtil.getStringValue(entry.get("approveFormat"), "");
                                String approver_role =   StringUtil.getStringValue(entry.get("type"), "");
                                // Object를 Integer로 캐스팅하여 int로 변환
                                int set_wf_level = entry.get("now_stage") != null ? (Integer) entry.get("now_stage") : 0;
                                // 자동승인인 경우
                                if(approveFormat.equals("auto")){
                                    // WF List를 만들어준다. 
                                    wfMasterRepository.insertWFList(wf_id, workflow_code, set_wf_level, "approved", approveFormat, approver_id, approver_email, approver_role);
                                    //requestor_id
                                    log.info("cdcUserUpdate -> UID: " + UID + ", channel: " + channel );
                                    String cdcUpdate = cdcUserUpdate(UID, channel, "approved" );// 최종결재가 완료되면 CDC Data를 Update 해준다. 
                                    //redirectAttributes.addFlashAttribute("cdcUserUpdate", cdcUpdate);

                                }else{
                                    // 수동승인인 경우 WF List를 만들어준다. 
                                    wfMasterRepository.insertWFList(wf_id, workflow_code, set_wf_level, "pending", approveFormat, approver_id, approver_email, approver_role);
                                }
                            } catch (Exception e) {
                                log.error("Error processing failed", e);
                            }
                        }
                        break;


                        case "W03": // W03 초대 가입승인
                            log.info("insertWFMaster: W03 " + wf_id + channel+ " workflow_code :"+workflow_code+" UID: "+ UID+cdc_email+ currentRole+ "pending"+ now_stage + bp_name+company_code + " rule_master_id :"+ rule_master_id + "invitaion_sender_email::"+invitaion_sender_email);
                        // 자동승인인 경우
                        if(approveFormat.equals("auto")){
                            wfMasterRepository.insertWFMasterW03(wf_id, channel, workflow_code, UID,cdc_email, currentRole, company_code, bp_name,  "approved", rule_master_id , now_stage, invitaion_sender_id, invitaion_sender_email );
                        }else{ // 수동승인인 경우
                            wfMasterRepository.insertWFMasterW03(wf_id, channel, workflow_code, UID,cdc_email, currentRole, company_code, bp_name,  "pending", rule_master_id , now_stage, invitaion_sender_id, invitaion_sender_email );
                        }
                        
                        for (Map<String, Object> entry : wfList) {
                            try {
                                // Map을 JSON 문자열로 변환
                                String jsonString = objectMapper.writeValueAsString(entry);
                                String approver_id =   StringUtil.getStringValue(entry.get("uid"), "");
                                String approver_email = StringUtil.getStringValue(entry.get("email"), "");
                                String approver_role =   StringUtil.getStringValue(entry.get("type"), "");
                                // Object를 Integer로 캐스팅하여 int로 변환
                                int set_wf_level = entry.get("now_stage") != null ? (Integer) entry.get("now_stage") : 0;
                                // 자동승인인 경우
                                if(approveFormat.equals("auto")){
                                    // WF List를 만들어준다. 
                                    wfMasterRepository.insertWFList(wf_id, workflow_code, set_wf_level, "approved", approveFormat, approver_id, approver_email, approver_role);
                                    //requestor_id
                                    log.info("cdcUserUpdate -> UID: " + UID + ", channel: " + channel );
                                    String cdcUpdate = cdcUserUpdate(UID, channel, "approved" );// 최종결재가 완료되면 CDC Data를 Update 해준다. 
                                    //redirectAttributes.addFlashAttribute("cdcUserUpdate", cdcUpdate);

                                }else{
                                    // 수동승인인 경우 WF List를 만들어준다. 
                                    wfMasterRepository.insertWFList(wf_id, workflow_code, set_wf_level, "pending", approveFormat, approver_id, approver_email, approver_role);
                                }
                            } catch (Exception e) {
                                log.error("Error W03 processing failed", e);
                            }
                        }
                        break;

                        case "W05": // W05 Role 수동승인
                            log.info("W05 Role: " + wf_id + channel+ workflow_code+ UID+cdc_email+ currentRole+ "pending"+ now_stage + bp_name+company_code + " rule_master_id :"+ rule_master_id);
                        wfMasterRepository.insertWFMaster(wf_id, channel, workflow_code, UID,cdc_email, currentRole, company_code, bp_name,  "pending", rule_master_id , now_stage);
                        wfMasterRepository.insertRoleManagement(wf_id, channel, change_role, "pending", company_code, bp_name, UID, cdc_email, currentRole );

                        for (Map<String, Object> entry : wfList) {
                            try {
                                String approver_id =   StringUtil.getStringValue(entry.get("uid"), "");
                                String approver_email = StringUtil.getStringValue(entry.get("email"), "");
                              //  String approval_format= StringUtil.getStringValue(entry.get("approveFormat"), "");
                                String approver_role =   StringUtil.getStringValue(entry.get("type"), "");
                                // Object를 Integer로 캐스팅하여 int로 변환
                                int set_wf_level = entry.get("now_stage") != null ? (Integer) entry.get("now_stage") : 0;
                                wfMasterRepository.insertWFList(wf_id, workflow_code, set_wf_level, "pending", approveFormat, approver_id, approver_email, approver_role);

                            } catch (Exception e) {
                                log.error("Error W05 processing failed", e);
                            }
                        }
                        break;


                        case "W06": // W06 SSO Access 승인
                            log.info("insertWFMaster: W06 " + wf_id + channel+ " workflow_code :"+workflow_code+" UID: "+ UID+cdc_email+ currentRole+ "pending"+ now_stage + bp_name+company_code + " rule_master_id :"+ rule_master_id);
                        // 자동승인인 경우
                        if(approveFormat.equals("auto")){
                            wfMasterRepository.insertWFMaster(wf_id, channel, workflow_code, UID,cdc_email, currentRole, company_code, bp_name,  "approved", rule_master_id , now_stage, reg_channel, target_channel, target_channeltype );
                        }else{ // 수동승인인 경우
                            wfMasterRepository.insertWFMaster(wf_id, channel, workflow_code, UID,cdc_email, currentRole, company_code, bp_name,  "pending", rule_master_id , now_stage, reg_channel, target_channel, target_channeltype);
                        }
                        
                        for (Map<String, Object> entry : wfList) {
                            try {
                                // Map을 JSON 문자열로 변환
                                String jsonString = objectMapper.writeValueAsString(entry);
                                String approver_id =   StringUtil.getStringValue(entry.get("uid"), "");
                                String approver_email = StringUtil.getStringValue(entry.get("email"), "");
                                String approver_role =   StringUtil.getStringValue(entry.get("type"), "");
                                // Object를 Integer로 캐스팅하여 int로 변환
                                int set_wf_level = entry.get("now_stage") != null ? (Integer) entry.get("now_stage") : 0;
                                // 자동승인인 경우
                                if(approveFormat.equals("auto")){
                                    // WF List를 만들어준다. 
                                    wfMasterRepository.insertWFList(wf_id, workflow_code, set_wf_level, "approved", approveFormat, approver_id, approver_email, approver_role);
                                    //requestor_id
                                    log.info("cdcUserUpdate -> UID: " + UID + ", channel: " + channel );
                                    String cdcUpdate = cdcUserUpdate(UID, channel, "approved" );// 최종결재가 완료되면 CDC Data를 Update 해준다. 
                                    //redirectAttributes.addFlashAttribute("cdcUserUpdate", cdcUpdate);

                                }else{
                                    // 수동승인인 경우 WF List를 만들어준다. 
                                    wfMasterRepository.insertWFList(wf_id, workflow_code, set_wf_level, "pending", approveFormat, approver_id, approver_email, approver_role);
                                }
                            } catch (Exception e) {
                                log.error("Error processing failed", e);
                            }
                        }
                        break;



                        case "W07": // W05 Domain 수동승인
                            log.info("W07 Domain: " + wf_id + channel+ workflow_code+ UID+cdc_email+ currentRole+ "pending"+ now_stage + bp_name+company_code + " rule_master_id :"+ rule_master_id);
                        wfMasterRepository.insertWFMaster(wf_id, channel, workflow_code, UID,cdc_email, currentRole, company_code, bp_name,  "pending", rule_master_id , now_stage);
                        wfMasterRepository.insertDomain(wf_id, channel, email_domain, company_code, bp_name, UID, cdc_email, currentRole );

                        for (Map<String, Object> entry : wfList) {
                            try {
                                String approver_id =   StringUtil.getStringValue(entry.get("uid"), "");
                                String approver_email = StringUtil.getStringValue(entry.get("email"), "");
                             //   String approval_format= StringUtil.getStringValue(entry.get("approveFormat"), "");
                                String approver_role =   StringUtil.getStringValue(entry.get("type"), "");
                                // Object를 Integer로 캐스팅하여 int로 변환
                                int set_wf_level = entry.get("now_stage") != null ? (Integer) entry.get("now_stage") : 0;
                                wfMasterRepository.insertWFList(wf_id, workflow_code, set_wf_level, "pending", approveFormat, approver_id, approver_email, approver_role);

                            } catch (Exception e) {
                                log.error("Error W07 processing failed", e);
                            }
                        }
                        break;

                }
                // Jackson ObjectMapper 생성
                for (Map<String, Object> entry : wfList) {
                    try {
                        // Map을 JSON 문자열로 변환
                        String jsonString = objectMapper.writeValueAsString(entry);
                    } catch (Exception e) {
                        log.error("Error wfCreate processing failed", e);
                    }
                }

            } catch (Exception e) {
                log.error("Error wfCreate processing failed", e);
            }
        }
        return wf_id;
    }

    private String cdcUserUpdate(String uid,  String channel, String approvalStatus){
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


}
