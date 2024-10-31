package com.samsung.ciam.services;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import com.gigya.socialize.GSResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import com.samsung.ciam.models.*;
import com.samsung.ciam.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samsung.ciam.common.gigya.service.GigyaService;

import com.samsung.ciam.utils.StringUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class ApprovalService {
    private String channelName;
    private String approvalType;
    private String requestorCDCUID;
    private boolean isSamsungEmployee;

    private JsonNode requestorCDCUser;

    private ObjectMapper objMapper = new ObjectMapper();

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

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private ChannelApprovalStatusesRepository channelApprovalStatusesRepository;

    @Autowired
    private UserOnboardHistoryRepository userOnboardHistoryRepository;

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private MailService mailService;

    @Autowired
    private AuthorizedSubsidiaryService authorizedSubsidiaryService;

    @Autowired
    private ChannelInvitationRepository channelInvitationRepository;

    @Autowired
    private WfIdGeneratorService wfIdGeneratorService;

    @Autowired
    private WfMasterRepository wfMasterRepository;

    @Autowired
    private WfListRepository wfListRepository;

    @Autowired
    private WFCreateService wfCreateService;


    public boolean approvalConfig(String channelName, String approvalType, String requestorUid, boolean isSamsungEmployee){

        this.prepareConfigData(channelName, approvalType, requestorUid, isSamsungEmployee);

        Optional<Channels> channelinfo = channelRepository.findByChannelName(channelName);
        Channels channelObj = !channelinfo.isEmpty() ? channelinfo.get() : null;
        Map<String,Object> configMap = channelObj.getConfigMap();
        this.setRequestor(requestorUid);

        Map<String,Object> approvalTypeMap = (Map<String,Object>)configMap.get(approvalType);
        boolean approvalConfig = (Boolean)configMap.get("approval_configuration");
//        if(approvalConfig){
//            return false;
//        }

        if(!approvalTypeMap.isEmpty()){
            if(approvalTypeMap.get("auto_approve")!=null && (Boolean)approvalTypeMap.get("auto_approve")==false){
                if(!StringUtil.isEmpty(approvalTypeMap.get("approver_admin_type"))){
                    this.createApprovalRequest(channelObj, approvalType, (String)approvalTypeMap.get("approver_admin_type"));
                } else if(approvalTypeMap.get("approval_admin_java")!=null && (Boolean)approvalTypeMap.get("approval_admin_java")==true) {
                    this.createApprovalJavaRequest(channelObj, approvalType, (String)approvalTypeMap.get("approver_admin_type"));
                }
            }else{
                this.handleAutoApproval(channelObj, approvalType);
            }

            //after processing, if theres any default admin type needs tobe set.
            if(!StringUtil.isEmpty(approvalTypeMap.get("admin_type"))){
                this.setAdminType(channelObj, (String)approvalTypeMap.get("admin_type"));
            }
        }

        return true;
    }

    public void createApprovalRequest(Channels channelObj, String approvalType, String approverRole){
        this.createApprovalRequest(channelObj, approvalType, approverRole, this.requestorCDCUser.get("UID").asText());
    }

    public void createApprovalJavaRequest(Channels channelObj, String approvalType, String approverRole){
        this.createApprovalJavaRequest(channelObj, approvalType, approverRole, this.requestorCDCUser.get("UID").asText());
    }

    public void createApprovalRequest(Channels channelObj, String approvalType, String approverRole, String requestorUid){
        if(!StringUtil.isEmpty(requestorUid)){
            this.setRequestor(requestorUid);
        }

        if(StringUtil.isEmpty(this.channelName)){
            this.prepareConfigData(channelObj.getChannelName(), approvalType, requestorUid, false);
        }

        String accountID;
        JsonNode userData = this.requestorCDCUser.get("data");
        if(userData!=null && userData.get("accountID")!=null){
            accountID = userData.get("accountID").asText();
        }else{ 
            accountID = "";
        }

// approverRole :=> CompanyAdmin / ChannelBusinessAdmin / ChannelSystemAdmin / CIAMAdmin 
        List<Map<String,Object>> userList = this.getUserListBasedOnRole(approverRole, channelObj, accountID);
        if (userList.size() > 0) {
            this.handleUserApproval(userList, channelObj, approvalType, approverRole);
        } else {
            this.handleNoUsersFound(channelObj, approvalType, approverRole);
        }
    }

    public void createApprovalJavaRequest(Channels channelObj, String approvalType, String approverRole, String requestorUid){
        if(!StringUtil.isEmpty(requestorUid)){
            this.setRequestor(requestorUid);
        }

        if(StringUtil.isEmpty(this.channelName)){
            this.prepareConfigData(channelObj.getChannelName(), approvalType, requestorUid, false);
        }

        String accountID;
        JsonNode userData = this.requestorCDCUser.get("data");
        if(userData!=null && userData.get("accountID")!=null){
            accountID = userData.get("accountID").asText();
        }else{
            accountID = "";
        }

        HttpServletRequest requestServlet = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpSession session = requestServlet.getSession();

        String wf_id = "";
        String wf_code = "";
        String loginId = (String) session.getAttribute("loginId") != null ? (String) session.getAttribute("loginId") : "";
        String country = (String) session.getAttribute("secCountry") != null ? (String) session.getAttribute("secCountry") : "";
        String subsidiary = (String) session.getAttribute("secSubsidiary") != null ? (String) session.getAttribute("secSubsidiary") : "";
        String division = "";
        String companyName = (String) session.getAttribute("company_name") != null ? (String) session.getAttribute("company_name") : "";

        if(session.getAttribute("approval_division")!=null && session.getAttribute("approval_division")!="") {
            division = (String) session.getAttribute("approval_division");
        }


        //2024.09.06 kimjy procedure -> api 호출로 변경
        // wfMasterRepository.insert_wf_master_new_user(wf_id,channelObj.getChannelName(),country,subsidiary,division, accountID,"",requestorUid,
        //         loginId,"","W01");

//        if (session.getAttribute("regType") != null && "invitation".equals(regType)) {
//            approvalType = "invitation";
//        } else if (session.getAttribute("channelUID") != null) {
//            approvalType = "conversion";
//        } else {
//            approvalType = "registration";
//        }

        if("invitation".equals(approvalType)) {
            wf_code = "W03";
        } else if("conversion".equals(approvalType)) {
            wf_code = "W02";
        } else if("registration".equals(approvalType)) {
            wf_code = "W01";
        } else if("ssoAccess".equals(approvalType)) {
            wf_code = "W06";
        } else if("adRegistration".equals(approvalType)) {
            wf_code = "W04";
        } else {
            wf_code = "W01";
        }

        Map<String,String> wf_param = new HashMap<String,String>();
        wf_param.put("workflow_code",wf_code);
        wf_param.put("channel",channelObj.getChannelName());
        wf_param.put("country",country);
        wf_param.put("subsidiary",subsidiary);
        wf_param.put("division",division);
        wf_param.put("bpid",accountID);
        wf_param.put("requestor_uid",requestorUid);
        wf_param.put("requestor_email",loginId);
        wf_param.put("bp_name",companyName);

        if("W06".equals(wf_code)) {
            String reg_channel = this.requestorCDCUser.path("regSource").asText("");
            String target_channel = channelObj.getChannelName();
            String target_channeltype = channelObj.getChannelType();

            wf_param.put("reg_channel",reg_channel);
            wf_param.put("target_channel",target_channel);
            wf_param.put("target_channeltype",target_channeltype);
        }

        if("W03".equals(wf_code)) {
            String invitaion_sender_id = "";
            String invitaion_sender_email = "";
            Object invitationIdObj = session.getAttribute("invitation_id");

            if (invitationIdObj != null) {

                Long invitationId = (Long) invitationIdObj;
                Optional<ChannelInvitation> channelInvitationOptional = channelInvitationRepository.findById(invitationId);

                if (channelInvitationOptional.isPresent()) {
                    ChannelInvitation channelInvitation = channelInvitationOptional.get();
                    invitaion_sender_id = channelInvitation.getRequestorUid();
                    invitaion_sender_email = channelInvitation.getRequestorId();
                }

            }
            wf_param.put("invitaion_sender_id",invitaion_sender_id);
            wf_param.put("invitaion_sender_email",invitaion_sender_email);
        }

        // 새로만든 결재로직 호출
        wf_id = wfCreateService.wfCreate(session, wf_param) ;

        
        //승인쪽 wf_id값으로 전달 수정
        WfMaster wmaster = wfMasterRepository.selectWfMaster(wf_id);
        List<WfList> wfList = wfListRepository.selectWfList(wf_id);
        if(wmaster!=null) {
            if("approved".equals(wmaster.getStatus())) {
                cdcTraitService.newJavaApproveUser(
                        requestorUid,
                        accountID,
                        channelObj.getChannelName(),
                        "",
                        approvalType,
                        0
                );
            } else if("pending".equals(wmaster.getStatus())){
                if (wfList.size() > 0) {
                    for(WfList wf : wfList){
                        Map<String, Object> paramArr = new HashMap<>();
                        JsonNode adminUser = cdcTraitService.getCdcUser(wf.getApproverId(), 0);
                        JsonNode requestUser = cdcTraitService.getCdcUser(wmaster.getRequestorId(), 0);
                        String firstName ="";
                        String approverName = "";
                        String lastName="";
                        if (adminUser.get("profile") != null) {
                            // firstName과 lastName 각각에 대해 null 확인
                            firstName = adminUser.get("profile").get("firstName") != null ? adminUser.get("profile").get("firstName").asText() : "";
                            lastName = adminUser.get("profile").get("lastName") != null ? adminUser.get("profile").get("lastName").asText() : "";

                            // firstName과 lastName을 합쳐서 approverName 설정
                            approverName = firstName + " " + lastName;
                        }

                        paramArr.put("template", "TEMPLET-NEW-002");
                        //paramArr.put("template", "TEMPLET-002");
                        paramArr.put("cdc_uid", wf.getApproverId());
                        if (channelObj != null && channelObj.getChannelName() != null) {
                            paramArr.put("channel", channelObj.getChannelName());
                        } else {
                            paramArr.put("channel", "");  // 또는 적절한 기본값
                        }
                       // paramArr.put("approverName", approverName);
                        paramArr.put("firstName", firstName);
                        paramArr.put("lastName", lastName);
                        // requestUser가 존재하고, profile과 email이 null이 아닌지 확인
                        if (requestUser != null && requestUser.get("profile") != null && requestUser.get("profile").get("email") != null) {
                            paramArr.put("approvalRequestEmail", requestUser.get("profile").get("email").asText());
                        } else {
                            paramArr.put("approvalRequestEmail", "");  // 기본값 설정
                        }
                        mailService.sendMail(paramArr);
                    }

                    //this.handleAddtionalApproverAdminType(channelObj, approvalType, approverRole);
                    this.saveOnboardHistory(true);
                    //this.handleUserJavaApproval(wfList, channelObj, approvalType, approverRole);
                } else {
                    this.handleNoUsersFound(channelObj, approvalType, approverRole);
                }
            }

        }

    }

    public List<Map<String,Object>> getUserListBasedOnRole(String approverRole, Channels channelObj, String accountID){
        String query = null;
        List<Map<String,Object>> resultUserList = null;
        switch (approverRole) {
            case "CompanyAdmin":
                query = "SELECT UID, profile.firstName,profile.lastName,profile.email FROM accounts "
                             + "WHERE data.isCompanyAdmin = false AND data.accountID = '" + accountID + "' AND data.userStatus = 'regEmailVerified' "
                             + "ORDER BY lastLogin DESC ";
                break;
            case "ChannelBusinessAdmin":
                String subidiary = this.requestorCDCUser.path("data").path("subsidiary").asText("");
                int limit = channelObj.getConfigMap().get("approver_find_limit")!=null ? (Integer)channelObj.getConfigMap().get("approver_find_limit") : 0;
                String cdcUidConditions = "";
                List<String> cdcUidList = authorizedSubsidiaryService.getCdcUidList(channelObj.getChannelName(), subidiary, limit);
                if (!cdcUidList.isEmpty()) {
                    cdcUidConditions += "AND UID in('" + String.join("','", cdcUidList) + "') ";
                }

                query = "SELECT UID, profile.email FROM accounts "
                      + "WHERE data.channels." + channelObj.getChannelName() + ".approvalStatus = 'approved' "
                      + "AND data.channels." + channelObj.getChannelName() + ".adminType = '2' AND data.userStatus = 'active' "
                      + cdcUidConditions
                      + "AND profile.username = 'BIZADMINNOTFOUND' "
                      + "ORDER BY lastLogin DESC";
                break;
            case "ChannelSystemAdmin":
                query = "SELECT UID, profile.email FROM accounts "
                      + "WHERE data.channels." + channelObj.getChannelName() + ".approvalStatus = 'approved' "
                      + "AND data.channels." + channelObj.getChannelName() + ".adminType = '1' AND data.userStatus = 'active' "
                      + "ORDER BY lastLogin DESC";
                break;
            case "CIAMAdmin":
                query = "SELECT UID, profile.email FROM accounts "
                      + "WHERE data.isCIAMAdmin = true AND data.userStatus = 'active' "
                      + "ORDER BY lastLogin DESC ";
                break;
            default:
                // no action
        }

        if(!StringUtil.isEmpty(query)){
            if(channelObj.getConfigMap().get("approver_find_limit")!=null){
                query += " LIMIT " + channelObj.getConfigMap().get("approver_find_limit");
            }

            GSResponse response = gigyaService.executeRequest("default", "accounts.search", query);
            try {
                Map<String,Object> mapVal = objMapper.readValue(response.getResponseText(), Map.class);
                resultUserList = (List<Map<String,Object>>)mapVal.get("results");
            }catch(Exception e){
                log.error("Error getUserListBasedOnRole processing failed", e);
            }
        }

        return resultUserList!=null ? resultUserList : new ArrayList<>();
    }

    public void saveOnboardHistory(String channelName,String approvalType, String requestorUid, boolean pendingApproval){

        this.setRequestor(requestorUid);
        String emailAddress = this.requestorCDCUser.get("profile").get("email").asText();
        if(emailAddress.indexOf("samsung.com") > -1){
            this.isSamsungEmployee = true;
        }else{
            this.isSamsungEmployee = false;
        }

        Map<String,Object> valArr = this.validateOnboardingStatus(channelName, isSamsungEmployee);
        updateOnboardHistory(
                approvalType,
                requestorUid,
                emailAddress,
                channelName,
                isSamsungEmployee,
                pendingApproval,
                (String)valArr.get("validation_status"),
                (String)valArr.get("validation_detail"),
                (Boolean)valArr.get("is_retry_required")
        );
    }

    private void prepareConfigData(String channelName, String approvalType, String requestorCDCUID, boolean isSamsungEmployee){
        this.channelName = channelName;
        this.approvalType = approvalType;
        this.requestorCDCUID = requestorCDCUID;
        this.isSamsungEmployee = isSamsungEmployee;
    }

    private void setAdminType(Channels channelObj, String adminType){
        // switch(adminType){
        //     case "CompanyAdmin":
        //         $accountArray["main"]["UID"] = $this->requestorCDCUser->UID;
        //         $accountArray["data"]["isCompanyAdmin"] = true;
        //         $this->CDCService->updateAccount($accountArray);
        //         break;

        //     case "CIAMAdmin":
        //         $accountArray["main"]["UID"] = $this->requestorCDCUser->UID;
        //         $accountArray["data"]["isCIAMAdmin"] = true;
        //         $this->CDCService->updateAccount($accountArray);
        //         break;

        //     case "ChannelBusinessAdmin":
        //         $accountArray["main"]["UID"] = $this->requestorCDCUser->UID;
        //         $accountArray["data"]["channelObj"][$channel->channel_name]["adminType"] = "2";
        //         $this->CDCService->updateAccount($accountArray);

        //         $conditions = [
        //             'channel' => $channel->channel_name,
        //             'cdc_uid' => $this->requestorCDCUser->UID,
        //         ];
        //         $countrySubsidiaries = (new SecServingCountry())->sdfcSubsidiaries();
        //         $subsidiaryNames = array_column($countrySubsidiaries->toArray(), 'subsidiary');
        //         $subsidiaryString = implode(',', $subsidiaryNames);
        //         $values = [
        //             'authorized_subsidiary_list' => implode(",", $subsidiaryString),
        //         ];
        //         $authorizedSubsidiary = AuthorizedSubsidiary::updateOrCreate($conditions, $values);

        //         break;

        //     case "ChannelSystemAdmin":
        //         $accountArray["main"]["UID"] = $this->requestorCDCUser->UID;
        //         $accountArray["data"]["channelObj"][$channel->channel_name]["adminType"] = "1";
        //         $this->CDCService->updateAccount($accountArray);
        //         break;
        // }  // end of switch
    }

    private void setRequestor(String requestorUid){
        if(this.requestorCDCUser==null){
            this.requestorCDCUser = cdcTraitService.getCdcUser(requestorUid, 0);
            if(this.requestorCDCUser.get("profile")==null){
                this.requestorCDCUser = cdcTraitService.getCdcUser(requestorUid, 0);
            }
        }else{
            String uid = this.requestorCDCUser.get("UID").asText();
            if(!uid.equals(requestorUid)){
                this.requestorCDCUser = cdcTraitService.getCdcUser(requestorUid, 0);
                if(this.requestorCDCUser.get("profile")==null){
                    this.requestorCDCUser = cdcTraitService.getCdcUser(requestorUid, 0);
                }
            }
        }
    }

    private void handleUserApproval(List<Map<String,Object>> userList, Channels channelObj, String approvalType, String approverRole){
        for(Map<String,Object> user : userList){
            this.createChannelApprovalRecord(objMapper.valueToTree(user), channelObj, approvalType);
        }

        this.handleAddtionalApproverAdminType(channelObj, approvalType, approverRole);
        this.saveOnboardHistory(true);
    }

    private void createChannelApprovalRecord(JsonNode adminUser, Channels channelObj, String approvalType){
        String approverName = (adminUser.get("profile").get("firstName")!=null ? adminUser.get("profile").get("firstName").asText() : "")
                            + " "
                            + (adminUser.get("profile").get("lastName")!=null ? adminUser.get("profile").get("lastName").asText() : "");

        Map<String, Object> paramArr = new HashMap<>();
        switch(approvalType){
            case "role_request":
                paramArr.put("template", "TEMPLET-029");

                break;
            default:
                /**
                 * create next approval request
                 */
                channelApprovalStatusesRepository.save(
                    new ChannelApprovalStatuses(
                        this.requestorCDCUser.get("profile").get("username").asText(),  // login_id
                        this.requestorCDCUser.get("UID").asText(),  // login_uid
                        channelObj.getChannelName(),  // channel
                        0l,  // channel_approver_id
                        null,  // approval_line
                        "any",  // match_type
                        approvalType,  // request_type
                        "pending",  // status
                        approverName,  // approver_name
                        adminUser.get("profile").get("email").asText(),  // approver_email
                        adminUser.get("UID").asText(),  // approver_uid
                        this.requestorCDCUser.path("data").path("subsidiary").asText("")  // subsidiary
                    )
                );

                paramArr.put("template", "TEMPLET-002");
        }  // end of switch

        // additional mail parameters
        paramArr.put("cdc_uid", adminUser.get("UID").asText());
        paramArr.put("channel", channelObj.getChannelName());
        paramArr.put("approverName", approverName);

        mailService.sendMail(paramArr);
    }

    private void handleAutoApproval(Channels channelObj, String approvalType){
        switch(approvalType){
            case "role_request":
                // abort(500,"role_request should not be auto approving!");
                break;
            default:
                this.createAutoApproveRecord(channelObj, approvalType);
                cdcTraitService.approveUser(
                        this.requestorCDCUser.get("UID").asText(),
                        this.requestorCDCUser.path("data").path("accountID").asText(""),
                        channelObj.getChannelName(),
                        "",
                        approvalType
                );
                this.saveOnboardHistory(false);
        }

    }


    private void handleAddtionalApproverAdminType(Channels channelObj, String approvalType, String approverRole){
        Map<String,Object> configMap = channelObj.getConfigMap();
        if(configMap.get(approvalType)!=null){
            Map<String,Object> approvalTypeInfo = (Map<String,Object>)configMap.get(approvalType);
            String additionalApproverAdminType = (String)approvalTypeInfo.get("additional_approver_admin_type");
            if(additionalApproverAdminType!=null){
                //check if the current role is not the additional role that was escalated to.
                if(approverRole!=null && approverRole.equals(additionalApproverAdminType)){
                    //compare current vs additional which is the higher role, if the current is higher than additional, do not process
                    int currentRoleLevel = ROLE_LEVEL.valueOf(approverRole).value();
                    int additionalRoleLevel = ROLE_LEVEL.valueOf(additionalApproverAdminType).value();
                    if(currentRoleLevel > additionalRoleLevel){
                        this.createApprovalRequest(channelObj, approvalType, additionalApproverAdminType);
                    }
                }
            }
        }
    }

    private void saveOnboardHistory(boolean pendingApproval){
        if(!StringUtil.isEmpty(this.approvalType) && !"role_request".equals(this.approvalType)){
            this.setRequestor(this.requestorCDCUID);
            Map<String,Object> valArr = this.validateOnboardingStatus(this.channelName, this.isSamsungEmployee);
            updateOnboardHistory(
                    this.approvalType,
                    this.requestorCDCUID,
                    this.requestorCDCUser.get("profile").get("email").asText(),
                    this.channelName,
                    this.isSamsungEmployee,
                    pendingApproval,
                    (String)valArr.get("validation_status"),
                    (String)valArr.get("validation_detail"),
                    (Boolean)valArr.get("is_retry_required")
            );
        }
    }

    private void updateOnboardHistory(String approvalType, String requestorUid, String email, String channelName, boolean isSamsungEmployee, boolean pendingApproval, String validationStatus, String validationDetail, boolean isRetryRequired){
        userOnboardHistoryRepository.save(
            new UserOnboardHistory(
                approvalType,  // onboard_type
                requestorUid,  // cdc_uid
                email,  // email
                channelName,  // channel
                isSamsungEmployee,  // is_employee
                this.requestorCDCUser.toString(),  // cdc_data
                pendingApproval,  // pending_approval
                validationStatus,  // validation_status
                validationDetail,  // validation_detail
                isRetryRequired  // is_retry_required
            )
        );
    }

    private Map<String,Object> validateOnboardingStatus(String channelName, boolean isSamsungEmployee){
        Optional<Channels> channelinfo = channelRepository.findByChannelName(channelName);
        Channels channelObj = !channelinfo.isEmpty() ? channelinfo.get() : null;
        Map<String,Object> configMap = channelObj.getConfigMap();

        // Initialize validation result
        Map<String,Object> validationResult = new HashMap<>();
        validationResult.put("validation_status", "passed");
        validationResult.put("validation_detail", "{\"issues\":[]}"); // initial value : {"issues":[]}
        validationResult.put("is_retry_required", false);

        return validationResult;
    }

    private void createAutoApproveRecord(Channels channelObj, String approvalType){
        channelApprovalStatusesRepository.save(
            new ChannelApprovalStatuses(
                this.requestorCDCUser.get("profile").get("username").asText(),  // login_id
                this.requestorCDCUser.get("UID").asText(),  // login_uid
                channelObj.getChannelName(),  // channel
                0l,  // channel_approver_id
                null,  // approval_line
                "catchAll",  // match_type
                approvalType,  // request_type
                "approved",  // status
                "Auto Approved",  // approver_name
                "",  // approver_email
                "",  // approver_uid
                this.requestorCDCUser.path("data").path("subsidiary").asText("")  // subsidiary
            )
        );
        if("invitation".equals(approvalType)) {

            HttpServletRequest requestServlet = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            HttpSession session = requestServlet.getSession();

            // 초대 ID가 세션에 있을 경우 초대 상태 업데이트
            Object invitationIdObj = session.getAttribute("invitation_id");
            if (invitationIdObj != null) {
                Long invitationId = (Long) invitationIdObj;
                Optional<ChannelInvitation> channelInvitationOptional = channelInvitationRepository.findById(invitationId);

                if (channelInvitationOptional.isPresent()) {
                    ChannelInvitation channelInvitation = channelInvitationOptional.get();
                    channelInvitation.setStatus("approved");
                    channelInvitation.setApprovedId("auto");
                    channelInvitation.setApprovedDate(Timestamp.valueOf(LocalDateTime.now()));
                    channelInvitation.setStatusUpdated(Timestamp.valueOf(LocalDateTime.now()));
                    channelInvitationRepository.save(channelInvitation);
                }
            }
        }

    }

    private void handleNoUsersFound(Channels channelObj, String approvalType, String approverRole){
        // Here, check for approval escalation or proceed to auto-approve and user approval.
        if(this.shouldEscalate(channelObj, approvalType)){
            String nextApprovalAdminType = this.getNextApprovalAdminType(approverRole);
            if(nextApprovalAdminType != null){
                this.createApprovalRequest(channelObj, approvalType, nextApprovalAdminType);
            }else{
                //most probably won't happen
                this.handleAutoApproval(channelObj, approvalType);
            }
        } else {
            this.handleAutoApproval(channelObj, approvalType);
        }
    }

    private boolean shouldEscalate(Channels channelObj, String approvalType){
        boolean result = false;
        Map<String,Object> channelConfig = channelObj.getConfigMap();

        // Implement logic to determine if escalation is needed
        if(channelConfig.get(approvalType)!=null){
            Map<String,Object> approvalTypeMap = (Map<String,Object>)channelConfig.get(approvalType);
            if(approvalTypeMap.get("approval_escalation")!=null && (Boolean)approvalTypeMap.get("approval_escalation")==true){
                result = true;
            }
        }
        return result;
    }

    private String getNextApprovalAdminType(String approverRole){
        String nextApproverRole = null;
        switch(approverRole){
            case "CompanyAdmin":
                nextApproverRole = "ChannelBusinessAdmin";
                break;
            case "ChannelBusinessAdmin":
                nextApproverRole = "ChannelSystemAdmin";
                break;
            case "ChannelSystemAdmin":
                nextApproverRole = "CIAMAdmin";
                break;
            default:
                // no action
        }
        return nextApproverRole;
    }

}
