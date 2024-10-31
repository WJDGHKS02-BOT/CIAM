package com.samsung.ciam.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.cpi.service.CpiApiService;
import com.samsung.ciam.common.gigya.service.GigyaService;
import com.samsung.ciam.models.*;
import com.samsung.ciam.repositories.*;
import com.samsung.ciam.utils.BeansUtil;
import com.samsung.ciam.utils.EncryptUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InvitationService {
    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private ChannelInvitationRepository channelInvitationRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private CisCountryRepository cisCountryRepository;

    @Autowired
    private AuthorizedSubsidiaryRepository authorizedSubsidiaryRepository;

    @Autowired
    private SecServingCountryRepository secServingCountryRepository;

    @Autowired
    private BtpAccountsRepository btpAccountsRepository;

    @Autowired
    private NewCompanyRepository newCompanyRepository;

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private EmailDomainsRepository emailDomainsRepository;

    @Autowired
    private CpiApiService cpiApiService;

    @Autowired
    private ApprovalAdminRepository approvalAdminRepository;

    @Autowired
    private NewSubsidiaryRepository newSubsidiaryRepository;

    public ModelAndView inviteUser(HttpSession session, Model model) {
        String admintype = "";
        String channelName = (String) session.getAttribute("session_channel");

        List<ChannelInvitation> invitations = new ArrayList<ChannelInvitation>();

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
        } else if (isChannelAdmin) {
            admintype = "Channel Admin";
        } else if (isCiamAdmin) {
            admintype = "CIAM Admin";
        } else if (isChannelBizAdmin) {
            admintype = "Channel biz Admin";
        } else if (isTempApprover) {
            admintype = "Temp Approver";
        } else {
            admintype = "General User";
        }

        List<Channels> channels;

        // Get channel list based on admin type
        switch (admintype) {
            case "Partner Admin":
                channels = channelRepository.selectChannelName(channelName);

                break;

            case "Channel biz Admin":
                channels = channelRepository.selectChannelName(channelName);

                break;

            case "Channel Admin":
                channels = channelRepository.selectChannelName(channelName);

                break;

            case "CIAM Admin":
                channels = channelRepository.findAll();

                break;

            default:
                channels = channelRepository.selectChannelName(channelName);

                break;
        }

        ModelAndView modelAndView = new ModelAndView("myPage");
        String content = "fragments/myPage/inviteUser";
        String menu = "inviteUser";
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("channels", channels);
        modelAndView.addObject("role", admintype);

        return modelAndView;
    }

    public Map<String, Object> inviteList(@RequestBody Map<String, String> allParams,HttpSession session) {
        Map<String, Object> gridData = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();

        String type = allParams.get("type");
        String searchText = allParams.get("searchText");
        String pageRow = allParams.get("pageRow");
        String resolvedChannel = allParams.get("channel");

        if (type == null || type.isEmpty()) {
            type = "all"; // Default value if param is not provideds
        }


        String admintype = "";
        String accountId = (String) session.getAttribute("cdc_companyid");
        String channelName = resolvedChannel;

        List<ChannelInvitation> invitations = new ArrayList<ChannelInvitation>();

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
        } else if (isChannelAdmin) {
            admintype = "Channel Admin";
        } else if (isCiamAdmin) {
            admintype = "CIAM Admin";
        } else if (isChannelBizAdmin) {
            admintype = "Channel biz Admin";
        } else if (isTempApprover) {
            admintype = "Temp Approver";
        } else {
            admintype = "General User";
        }

        // Get channel list based on admin type
        switch (admintype) {
            case "Partner Admin":
                invitations = channelInvitationRepository.selectPartnerAdminInvitationList(accountId,type,channelName,searchText,pageRow);

                break;

                //추후 subsidiary권한 추가되면 수정필요
            case "Channel biz Admin":
                String uid = (String) session.getAttribute("cdc_uid");
                JsonNode CDCUserProfile = cdcTraitService.getCdcUser(uid, 0);
                String subsidiary = approvalAdminRepository.selectApprovalAdminSubsidiary(uid,resolvedChannel);

                invitations = channelInvitationRepository.selectChannelBizAdminInvitationList(channelName,type,subsidiary,searchText,pageRow);

                break;

            case "Channel Admin":
                invitations = channelInvitationRepository.selectChannelAdminInvitationList(channelName,type,searchText,pageRow);

                break;

            case "CIAM Admin":
                invitations = channelInvitationRepository.selectCiamAdminAdminInvitationList(type,searchText,pageRow);

                break;

            default:
                invitations = null;

                break;
        }

        // 동적으로 헤더 목록 설정
        List<String> visibleHeaders = new ArrayList<>();

        switch (type) {
            case "pending":
                visibleHeaders.add("loginId");
                visibleHeaders.add("requestorId");
                visibleHeaders.add("companyName");
                visibleHeaders.add("createdAt");
                visibleHeaders.add("expiry");
                visibleHeaders.add("resend");
                visibleHeaders.add("delete");
                break;
            case "rejected":
                visibleHeaders.add("loginId");
                visibleHeaders.add("requestorId");
                visibleHeaders.add("companyName");
                visibleHeaders.add("createdAt");
                visibleHeaders.add("createdAt");
                visibleHeaders.add("rejectedDate");
                visibleHeaders.add("rejectedReason");
                visibleHeaders.add("rejectedId");
                break;
            case "approved":
                visibleHeaders.add("loginId");
                visibleHeaders.add("requestorId");
                visibleHeaders.add("companyName");
                visibleHeaders.add("createdAt");
                visibleHeaders.add("createdAt");
                visibleHeaders.add("approvedDate");
                visibleHeaders.add("approvedId");
                break;
            case "all":
                visibleHeaders.add("loginId");
                visibleHeaders.add("requestorId");
                visibleHeaders.add("companyName");
                visibleHeaders.add("status");
                visibleHeaders.add("createdAt");
                break;
            default:
                visibleHeaders.add("loginId");
                visibleHeaders.add("requestorId");
        }

        // ChannelInvitation 객체를 Map으로 변환하여 results 리스트에 추가
        if (invitations != null) {
            for (ChannelInvitation invitation : invitations) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", invitation.getId());
                map.put("loginId", invitation.getLoginId());
                map.put("loginUid", invitation.getLoginUid());
                map.put("requestorId", invitation.getRequestorId());
                map.put("requestorUid", invitation.getRequestorUid());
                map.put("bpid", invitation.getBpid());
                map.put("companySource", invitation.getCompanySource());
                map.put("channel", invitation.getChannel());
                map.put("token", invitation.getToken());
                map.put("expiry", invitation.getExpiry());
                map.put("status", invitation.getStatus());
                map.put("statusUpdated", invitation.getStatusUpdated());
                map.put("createdAt", invitation.getCreatedAt());
                map.put("updatedAt", invitation.getUpdatedAt());
                map.put("companyName", invitation.getCompanyName());
                map.put("rejectedDate", invitation.getRejectedDate());
                map.put("rejectedReason", invitation.getRejectedReason());
                map.put("rejectedId", invitation.getRejectedId());
                map.put("approvedDate", invitation.getUpdatedAt());
                map.put("approvedId", invitation.getApprovedId());

                results.add(map);
            }
        }


        gridData.put("result",results);
        gridData.put("visibleHeaders",visibleHeaders);

        return gridData;
    }

    //@Transactional
    public String resend(Map<String, String> payload, HttpSession session,RedirectAttributes redirectAttributes) {
        Map<String, Object> returnMsg = new HashMap<>();
        String successYn = "N";
        String channelType = "";

        ChannelInvitation channelInvitation = channelInvitationRepository.findById(Long.parseLong(payload.get("invitationId")))
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        // Check CDC user existence
        JsonNode CDCUser = cdcTraitService.getCdcUser(channelInvitation.getLoginUid(), 0);
        if (CDCUser.path("UID").isMissingNode()) {
            redirectAttributes.addFlashAttribute("showErrorMsg", "User is no longer valid, please cancel and re-create new invitation");
            return successYn;
        }

        Map<String, Object> paramArr = new HashMap<>();
        paramArr.put("template", "TEMPLET-NEW-005");
        paramArr.put("cdc_uid", channelInvitation.getLoginUid());
        paramArr.put("channel", channelInvitation.getChannel());
        paramArr.put("ch_inv_id", channelInvitation.getId());
        paramArr.put("firstName", CDCUser.get("profile").get("firstName") != null ? CDCUser.get("profile").get("firstName").asText() : "");
        paramArr.put("lastName", CDCUser.get("profile").get("lastName") != null ? CDCUser.get("profile").get("lastName").asText() : "");

        if("btp".equals(channelInvitation.getCompanySource())) {
            channelType="VENDOR";
        } else {
            channelType = "CUSTOMER";
        }
        paramArr.put("channelType", channelType); //g하드코딩

        returnMsg = mailService.sendMail(paramArr);
        if("Success".equals(returnMsg.get("rstMsg"))) {
            successYn="Y";
        }

        return successYn;
    }

    public String cancel(Map<String, String> payload,HttpSession session,RedirectAttributes redirectAttributes) {
        String successYn = "N";
        try {
            ChannelInvitation channelInvitation = channelInvitationRepository.findById(Long.parseLong(payload.get("invitationId")))
                    .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
                // Check CDC user existence
            JsonNode CDCUser = cdcTraitService.getCdcUser(channelInvitation.getLoginUid(), 0);
            String email = CDCUser.path("profile").path("email").asText("");
            LocalDateTime now = LocalDateTime.now();
            // LocalDateTime을 Timestamp로 변환
            Timestamp timestamp = Timestamp.valueOf(now);

            if (CDCUser.path("UID").isMissingNode()) {
                    redirectAttributes.addFlashAttribute("showErrorMsg", "User is no longer valid, please cancel and re-create new invitation");
                    return successYn;
            }
            // Validate code
            Map<String, Object> params = new HashMap<>();
            params.put("UID", channelInvitation.getLoginUid());

            GSResponse response = gigyaService.executeRequest("defaultChannel", "accounts.deleteAccount", params);

            if (response.getErrorCode() == 0) {
                channelInvitation.setStatus("rejected");
                channelInvitation.setRejectedDate(timestamp);
                channelInvitation.setRejectedReason("Invite Canceled");
                channelInvitation.setRejectedId(email);
                channelInvitationRepository.save(channelInvitation);

                successYn = "Y";
            }

            successYn = "Y";
        } catch (Exception e) {
            log.error(e.getMessage());
            redirectAttributes.addFlashAttribute("showErrorMsg", "Token expired or invalid, please request a new token");
        }
        return successYn;
    }

    public ModelAndView inviteInfomation(String channelName, HttpSession session) {
        // 관리자 유형 확인
        List<String> adminTypes = new ArrayList<>();
        if (Boolean.TRUE.equals(Optional.ofNullable(session.getAttribute("cdc_companyadmin")).orElse(false))) {
            adminTypes.add("partnerAdmin");
        }
        else if (Boolean.TRUE.equals(Optional.ofNullable(session.getAttribute("cdc_channeladmin")).orElse(false))) {
            adminTypes.add("channelAdmin");
        }
        else if (Boolean.TRUE.equals(Optional.ofNullable(session.getAttribute("cdc_ciamadmin")).orElse(false))) {
            adminTypes.add("ciamAdmin");
        }
        else if (Boolean.TRUE.equals(Optional.ofNullable(session.getAttribute("cdc_channeladminType")).orElse(false))) {
            adminTypes.add("ChannelBusinessAdmin");
        }
        else if (Boolean.TRUE.equals(Optional.ofNullable(session.getAttribute("cdc_tempApprover")).orElse(false))) {
            adminTypes.add("tempApprover");
        }
        else {
            adminTypes.add("generalUser");
        }

        // 관리자 유형에 따라 회사 검색 활성화 여부 결정
        boolean companySearchEnabled = adminTypes.contains("ciamAdmin") || adminTypes.contains("channelAdmin");

        Map<String, Object> companyData = new HashMap<>();
        if (adminTypes.contains("partnerAdmin")) {
            companySearchEnabled = false;
            JsonNode companyNode = cdcTraitService.getB2bOrg((String) session.getAttribute("cdc_companyid"));
            if (companyNode != null && companyNode.isObject()) {
                companyData = new ObjectMapper().convertValue(companyNode, new TypeReference<Map<String, Object>>() {});
            }
        } else {
            companySearchEnabled = true;
        }

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

        // 권한 확인
        boolean isAdmin = adminTypes.stream().anyMatch(adminTypes::contains);
        if (!isAdmin) {
            throw new RuntimeException("No permission");
        }

        // 국가 리스트 가져오기
        List<SecServingCountry> countries = getSecServingCountries(channelName);
        String uid = (String) session.getAttribute("cdc_uid");
        String channelType = Arrays.asList("sba", "mmp", "e2e", "ets", "edo").contains(channelName) ? "customer" : channelName;
        List<Channels> channels = channelRepository.selectByChannelNameContaining(channelName);
        // ChannelBusinessAdmin 역할 처리
        if ("ChannelBusinessAdmin".equals(adminTypes.get(0))) {
            countries.clear();
            JsonNode CDCUserProfile = cdcTraitService.getCdcUser(uid, 0);

            // subsidiary를 콤마로 구분된 값으로 가정하고 이를 리스트로 변환
            String subsidiary = approvalAdminRepository.selectApprovalAdminSubsidiary(uid, channelName);
            List<String> subsidiaryList = Arrays.asList(subsidiary.split(","));

            // subsidiary 리스트를 기반으로 IN 절을 사용하여 쿼리 실행
            //List<String> secCountries = secServingCountryRepository.selectCountryCodeList(channelName, subsidiaryList);
            countries = secServingCountryRepository.selectCountriesBySubsidiaryCodes(channelName, subsidiaryList);
        }

        boolean countryReadOnly = "CompanyAdmin".equals(Optional.ofNullable(session.getAttribute("btp_myrole")).orElse(""));

        // CMDM 회사 ID 확인
        if (Optional.ofNullable(session.getAttribute("cdc_companyid")).map(Object::toString).orElse("").contains("CMDM-")) {
            throw new RuntimeException("Invalid company assignment, please try again later");
        }

        String bpId = (String) session.getAttribute("cdc_companyid");

        Map<String,Object> companyObject = new HashMap<String,Object>();
        Map<String,Object> regCompanyData = new HashMap<String,Object>();

        ModelAndView modelAndView = new ModelAndView("myPage");
        String content="";
        String popupSearchCompany="";

        if(!"customer".equals(channelType)) {
            content = "fragments/myPage/invitationInformation";
            popupSearchCompany = "popups/searchCompany";
        } else {
            content = "fragments/myPage/customerInvitationInformation";
            popupSearchCompany = "popups/partnerSearchCompany";
        }
        modelAndView.addObject("content", content);
        String menu = "inviteUser";
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("searchCompanyModal", popupSearchCompany);
        modelAndView.addObject("channel",channelName);

        companyObject.put("countries", countries);

        if(adminTypes.contains("partnerAdmin")) {
            if(!"customer".equals(channelType)) {
                BtpAccounts companyInfo = btpAccountsRepository.selectByBpid(bpId);

                companyObject.put("bpid", bpId);
                companyObject.put("source",companyInfo.getSource());
                companyObject.put("type",companyInfo.getType());
                companyObject.put("validStatus",companyInfo.getValidStatus());
                companyObject.put("zip_code",companyInfo.getZipCode());
                companyObject.put("vendorcode",companyInfo.getVendorcode());

                regCompanyData.put("name",companyInfo.getName());
                regCompanyData.put("country",companyInfo.getCountry());
                regCompanyData.put("state",companyInfo.getState());
                //companyObject.put("region",companyInfo.getRepresentative());
                regCompanyData.put("city",companyInfo.getCity());
                regCompanyData.put("street_address",companyInfo.getStreetAddress());
                regCompanyData.put("phonenumber1",companyInfo.getPhonenumber1());
                regCompanyData.put("fax",companyInfo.getFaxno());
                regCompanyData.put("email",companyInfo.getEmail());
                regCompanyData.put("bizregno1",companyInfo.getBizregno1());
                regCompanyData.put("representative",companyInfo.getRepresentative());
                regCompanyData.put("vendorcode",companyInfo.getVendorcode());
                regCompanyData.put("zip_code",companyInfo.getValidStatus());
                regCompanyData.put("validStatus",companyInfo.getValidStatus());
                regCompanyData.put("type",companyInfo.getType());
                regCompanyData.put("source",companyInfo.getSource());
                regCompanyData.put("bpid",bpId);

                companyObject.put("registerCompany", regCompanyData);

                modelAndView.addObject("companyObject", companyObject);

            } else {
//            NewCompany newCompany = newCompanyRepository.selectByBpid(bpId);
//
//            companyObject.put("bpid", bpId);
//            companyObject.put("source",newCompany.getSource());
//            companyObject.put("type",newCompany.getType());
//            companyObject.put("validStatus",newCompany.getStatus());
//            companyObject.put("zip_code",newCompany.getZipCode());
//            companyObject.put("accountId",newCompany.getBpid());
//
//            regCompanyData.put("name",newCompany.getName());
//            regCompanyData.put("country",newCompany.getCountry());
//            regCompanyData.put("state",newCompany.getState());
//            //companyObject.put("region",companyInfo.getRepresentative());
//            regCompanyData.put("city",newCompany.getCity());
//            regCompanyData.put("street_address",newCompany.getStreetAddress());
//            regCompanyData.put("phonenumber1",newCompany.getPhoneNumber1());
//            regCompanyData.put("fax",newCompany.getFaxNo());
//            regCompanyData.put("email",newCompany.getEmail());
//            regCompanyData.put("bizregno1",newCompany.getBizRegNo1());
//            regCompanyData.put("representative",newCompany.getRepresentative());
//
//            companyObject.put("registerCompany", regCompanyData);
//
//            modelAndView.addObject("companyObject", companyObject);

                JsonNode companyNode = cdcTraitService.getB2bOrg((String) session.getAttribute("cdc_companyid"));

                JsonNode infoNode = companyNode.path("info");

                companyObject.put("bpid", companyNode.path("bpid").asText());
                companyObject.put("source", companyNode.path("source").asText());
                companyObject.put("type", companyNode.path("type").asText());
                companyObject.put("validStatus", companyNode.path("status").asText());
                companyObject.put("zip_code", infoNode.path("zip_code").path(0).asText(""));
                companyObject.put("vendorcode", companyNode.path("bpid").asText());

                regCompanyData.put("name", companyNode.path("orgName").asText());

                String country = infoNode.path("country").path(0).asText("");
                country = secServingCountryRepository.selectCountryName(channelName,country);

                regCompanyData.put("country", country);
                regCompanyData.put("state", infoNode.path("state").path(0).asText(""));
                regCompanyData.put("city", infoNode.path("city").path(0).asText(""));
                regCompanyData.put("street_address", infoNode.path("street_address").path(0).asText(""));
                regCompanyData.put("phonenumber1", infoNode.path("phonenumber1").path(0).asText(""));
                regCompanyData.put("fax", infoNode.path("fax").path(0).asText(""));
                regCompanyData.put("email", companyNode.path("email").asText(""));
                regCompanyData.put("bizregno1", infoNode.path("bizregno1").path(0).asText(""));
                regCompanyData.put("representative", infoNode.path("representative").path(0).asText(""));
                regCompanyData.put("vendorcode", companyNode.path("bpid").asText());
                regCompanyData.put("zip_code", infoNode.path("zip_code").path(0).asText(""));
                regCompanyData.put("validStatus", companyNode.path("status").asText(""));
                regCompanyData.put("type", companyNode.path("type").asText(""));
                regCompanyData.put("source", companyNode.path("source").asText(""));
                regCompanyData.put("bpid", companyNode.path("bpid").asText(""));

                companyObject.put("registerCompany", regCompanyData);

                modelAndView.addObject("companyObject", companyObject);
            }
        } else {
            companyObject.put("registerCompany", regCompanyData);

            modelAndView.addObject("companyObject", companyObject);
        }

        Map<String, Object> accountObject = new HashMap<>();
        List<Map<String, String>> languages = new ArrayList<>();
        List<String> subsidiarys = new ArrayList<String>();

        List<CommonCode> divisions = commonCodeRepository.findByHeader("BIZ_WITH_DEPT");//divisionRepository.findAllByOrderByNameEnAsc();
        List<SecServingCountry> secCountries = secServingCountryRepository.selectCountriesByChannelExcludingSubsidiary(channelName);
        List<CommonCode> codeList = commonCodeRepository.findByHeader("COUNTRY_CODE");

        String myRole = (String) session.getAttribute("btp_myrole");

        if("Channel biz Admin".equals(myRole)) {
            subsidiarys = approvalAdminRepository.selectBizAdminSubsidiary(uid,channelName);
        } else if ("Channel Admin".equals(myRole) || "CIAM Admin".equals(myRole)) {
            subsidiarys = newSubsidiaryRepository.findByCompanyAbbreviationOrderByIdAsc();
        } else if ("Partner Admin".equals(myRole)) {
            JsonNode CDCUserProfile = cdcTraitService.getCdcUser(uid, 0);
            String subsidiray = CDCUserProfile.path("data").path("subsidiary").asText("");

            subsidiarys.add(subsidiray);
        }

        List<EmailDomains> domains = emailDomainsRepository.selectBannedDomainsList(channelName);

        List<String> rejecteDomains = domains.stream()
                .map(EmailDomains::getDomain)  // 각 EmailDomains 객체에서 getDomains() 호출
                .collect(Collectors.toList());  // 결과를 List<String>으로 수집

        Map<String, String> koreanLanguageMap = new HashMap<>();
        koreanLanguageMap.put("name", "Korean");
        koreanLanguageMap.put("value", "ko_KR");
        languages.add(koreanLanguageMap);

        Map<String, String> englishLanguageMap = new HashMap<>();
        englishLanguageMap.put("name", "English");
        englishLanguageMap.put("value", "en_US");
        languages.add(englishLanguageMap);

        List<CommonCode> roleList = commonCodeRepository.selectRoleList("ROLE_CODE",adminTypes.get(0));

        accountObject.put("codes", codeList);
        accountObject.put("divisions", divisions);
        accountObject.put("secCountries", secCountries);
        accountObject.put("languages", languages);
        accountObject.put("roles", roleList);

        modelAndView.addObject("accountObject", accountObject);

//        accountObject.put("loginId", session.getAttribute("loginId"));
//        accountObject.put("presetEmail", emailParts[0]);
//        accountObject.put("subsidiaries", subsidiaries);
//        accountObject.put("accUser", accUser);
//        accountObject.put("domainArray", domainArray);
//        accountObject.put("mobilePhone", mobilePhone);
//        accountObject.put("mobilePhoneCode", mobilePhoneCode);
//        accountObject.put("workPhone", workPhone);
//        accountObject.put("workPhoneCode", workPhoneCode);
//        accountObject.put("workLocation", workLocation);
//        accountObject.put("salutation", salutation);
//        accountObject.put("subsidiary", subsidiary);
//        accountObject.put("division", division);
//        accountObject.put("secDept", secDept);
//        accountObject.put("language", accUser.get("profile").get("languages"));
//        accountObject.put("isConvertFlow", isConvertFlow);

        modelAndView.addObject("companySearchEnabled", companySearchEnabled);
        modelAndView.addObject("channelString", channelName);
        modelAndView.addObject("countries", countries);
        modelAndView.addObject("companyData", companyData);
        modelAndView.addObject("countryReadOnly", countryReadOnly);
        modelAndView.addObject("channels", channels);
        modelAndView.addObject("rejecteDomains", rejecteDomains);
        modelAndView.addObject("subsidiarys", subsidiarys);
        modelAndView.addObject("myRole", myRole);


        return modelAndView;
    }

    public List<SecServingCountry> getSecServingCountries(String channel) {
        return secServingCountryRepository.selectCountryCodeChannel(channel);
    }


    public String inviteCompanyAndAccountCreated(Map<String, String> requestParams, HttpSession session,RedirectAttributes redirectAttributes,HttpServletRequest request) {
        String successYn = "N";
        try {
            String companyDbSource;
            String usedBpid;
            String channel = requestParams.get("channel");

            String channelType = channelRepository.selectChannelTypeSearch(channel);
            // Step 1: Process Company Creation
            if (isNewCompany(requestParams)) {
                usedBpid = processNewCompany(requestParams,channelType,channel);
                companyDbSource = "newCompany";
            } else {
                usedBpid = requestParams.get("bpid");
                companyDbSource = determineCompanyDbSource(channel,channelType);
            }

            session.setAttribute("inviteBpid", usedBpid);
            session.setAttribute("inviteChannel", channel);
            session.setAttribute("inviteCompanyDb", companyDbSource);
            session.setAttribute("inviteCountry",requestParams.get("country"));

            // Step 2: Process Account Creation
            Map<String, Object> profileFields = prepareProfileFields(requestParams, session);
            Map<String, Object> dataFields = prepareDataFields(requestParams, channel,session);
            Map<String, Object> preferencesFields = new HashMap<>();

            preferencesFields = Map.of("provideInformation", Map.of("isConsentGranted", true),
                    "internal." + channel, Map.of("isConsentGranted", true));

            ObjectMapper objectMapper = new ObjectMapper();
            GSResponse response;
            response = gigyaService.executeRequest(channel, "accounts.register", Map.of(
                    "username", requestParams.get("loginUserId"),
                    "email", requestParams.get("loginUserId"),
                    "password", BeansUtil.getApplicationProperty("cdc.temp-reg-pwd"),
                    "regSource", channel,
                    "profile", objectMapper.writeValueAsString(profileFields),
                    "data", objectMapper.writeValueAsString(dataFields),
                    "preferences", objectMapper.writeValueAsString(preferencesFields)
            ));

            handleCdcResponse(response, requestParams, channel, session,redirectAttributes,channelType,usedBpid);

        } catch (Exception e) {
            log.error("Error updating account", e);
            redirectAttributes.addFlashAttribute("showErrorMsg", "An error occurred while updating the account");
            //return new RedirectView(request.getHeader("Referer"));
        }
        successYn = "Y";
        return successYn;
        //return new RedirectView("/myPage/inviteUser");
    }

    public boolean isNewCompany(Map<String, String> requestParams) {
        return "true".equals(requestParams.get("isNewCompany")) || requestParams.get("bpid") == null ||requestParams.get("bpid").isEmpty();
    }

    public String processNewCompany(Map<String, String> requestParams,String channelType,String channel) {
        String randomBpid = generateRandomBpid("vendor".equals(channelType) ? "NERP-" : "CMDM-", 100000, 999999);
        saveNewCompany(requestParams, randomBpid,channel);
        return randomBpid;
    }

    public void saveNewCompany(Map<String, String> requestParams, String randomBpid,String channel) {
        String bizregno1 = requestParams.get("bizregno1").isEmpty() ? "NOBIZREG-" + new Random().nextInt(10000) + '-' + System.currentTimeMillis() : requestParams.get("bizregno1");

        String subsidiary = secServingCountryRepository.selectSubsidiary(channel,requestParams.get("country"));
        String regch = channelRepository.selectChannelRegCh(channel);

        NewCompany newCompany = new NewCompany();
        newCompany.setBpid(randomBpid);
        newCompany.setName(requestParams.get("name"));
        newCompany.setCountry(requestParams.get("country"));
        newCompany.setCity(requestParams.get("city"));
        newCompany.setStreetAddress(requestParams.get("street_address"));
        newCompany.setZipCode(requestParams.get("zip_code"));
        newCompany.setPhoneNumber1(requestParams.get("phonenumber1"));
        newCompany.setBizRegNo1(bizregno1);
        newCompany.setVendorCode(requestParams.get("vendorcode"));
        newCompany.setRegCh(requestParams.get("regch"));
        newCompany.setOrgId(requestParams.get("orgId"));
        newCompany.setChannelType(requestParams.get("channelType"));
        newCompany.setType(requestParams.get("type"));
        newCompany.setProducts("");
        newCompany.setProductCategoryLists("");
        newCompany.setVatNo(requestParams.getOrDefault("vatno",""));
        newCompany.setDunsNo(requestParams.getOrDefault("dunsno",""));
        newCompany.setCisCode(requestParams.getOrDefault("ciscode",""));
        newCompany.setRepresentative(requestParams.getOrDefault("representative",""));
        newCompany.setAccountWebsite(requestParams.getOrDefault("website",""));
        newCompany.setEmail(requestParams.getOrDefault("email",""));
        newCompany.setFaxNo(requestParams.getOrDefault("fax",""));
        newCompany.setState(requestParams.getOrDefault("state",""));
        newCompany.setRegion(requestParams.getOrDefault("region",""));
        newCompany.setOrgId(requestParams.getOrDefault("orgId", ""));
        newCompany.setProductLists("");
        newCompany.setType("CMDM");
        newCompany.setSource("CMDM");
        newCompany.setSubsidiary(subsidiary);
        newCompany.setRegCh(regch);
        newCompany.setProducts(requestParams.getOrDefault("products",""));
        newCompany.setProductLists("");
        newCompany.setProductCategoryLists(requestParams.getOrDefault("productCategoryLists",""));
        newCompany.setMtLicense(requestParams.getOrDefault("magicinfo",""));
        newCompany.setMtStartDate(requestParams.getOrDefault("startDttm",""));
        newCompany.setMtEndDate(requestParams.getOrDefault("endDttm",""));

        newCompanyRepository.save(newCompany);

    }

    public String generateRandomBpid(String prefix, int min, int max) {
        Random random = new Random();
        int randomNumber = random.nextInt(max - min) + min;
        return prefix + randomNumber;
    }

    public String determineCompanyDbSource(String channelID,String channelType) {
        if ("VENDOR".equals(channelType)){
            return "btp";
        } else {
            return "cmdm";
        }
    }

    public Map<String, Object> prepareProfileFields(Map<String, String> requestParams, HttpSession session) {
        Map<String, Object> profileFields = new HashMap<>();
        profileFields.put("email", requestParams.getOrDefault("email",""));
        String language = requestParams.getOrDefault("language", "");
        profileFields.put("locale", "en_US".equals(language) ? "en" : "ko_KR".equals(language) ? "ko" : language);
        profileFields.put("firstName", requestParams.getOrDefault("firstName",""));
        profileFields.put("lastName", requestParams.getOrDefault("lastName",""));
        profileFields.put("address", "");
        profileFields.put("city", "");
        profileFields.put("country", session.getAttribute("inviteCountry"));

        String locale = (String) profileFields.get("locale");
        String languagesMap = "en".equals(locale) ? "en_US" : "ko".equals(locale) ? "ko_KR" : locale;

        profileFields.put("languages", "en_US");
        profileFields.put("state", "");
        profileFields.put("zip", requestParams.getOrDefault("postal_code",""));
        profileFields.put("username", requestParams.getOrDefault("loginUserId",""));
        List<Map<String, String>> phoneArray = new ArrayList<>();
        if (StringUtils.hasText(requestParams.get("mobile_phone"))) {
            phoneArray.add(Map.of("number", "+" + requestParams.get("country_code") + " " + requestParams.get("mobile_phone"), "type", "mobile_phone"));
        }
        if (StringUtils.hasText(requestParams.get("work_phone"))) {
            phoneArray.add(Map.of("number", "+" + requestParams.get("country_code_work") + " " + requestParams.get("work_phone"), "type", "work_phone"));
        }
        String companyName = (String) session.getAttribute("company_name");
        if (StringUtils.hasText(companyName)) {
            profileFields.put("work", Map.of("company", companyName));
        }
        profileFields.put("phones", phoneArray);

        return profileFields;
    }

    public Map<String, Object> prepareDataFields(Map<String, String> requestParams, String channel, HttpSession session) {
        Map<String, Object> dataFields = new HashMap<>();

        String secDept = requestParams.getOrDefault("secDept", "");
        String[] parts = secDept.split("%%%");
        String secDeptResult = parts[parts.length - 1];

        //String subsidiary = secServingCountryRepository.selectSubsidiary(channel,requestParams.get("country"));

        String subsidiary = requestParams.get("subsidiary");

        dataFields.put("userDepartment", secDeptResult);
        dataFields.put("salutation", requestParams.getOrDefault("salutation",""));
        dataFields.put("division", requestParams.getOrDefault("division",""));
        dataFields.put("isCompanyAdmin", false);

        // channels 맵을 초기화할 때 HashMap을 사용하여 수정 가능하도록 설정
        Map<String, Object> channelData = new HashMap<>();
        channelData.put("approvalStatus", "pending");
        channelData.put("channelUID", "");
        int adminType = 0;
        if (requestParams.get("role") != null) {
            switch (requestParams.get("role")) {
                case "0" -> adminType = 0; // General User
                case "1" -> adminType = 1; //ChannelSystemAdmin
                case "2" -> adminType = 2; //hannel Biz Admin
                case "3" -> adminType = 3; //Partner Admin
                case "4" -> adminType = 4; //CIAM Admin
                case "9" -> adminType = 9; //TempApprover
            }
            channelData.put("adminType", adminType);  // 기본값으로 0 설정, 필요에 따라 이후에 변경
        }
        dataFields.put("channels", Map.of(channel, channelData));

        dataFields.put("jobtitle", requestParams.getOrDefault("job_title",""));
        dataFields.put("reasonCompanyAdmin", requestParams.getOrDefault("adminReason",""));
        dataFields.put("accountID", session.getAttribute("inviteBpid"));
        dataFields.put("userStatus", "inviteUser");
        dataFields.put("subsidiary", subsidiary);

        //handleChannelAdminRoles(requestParams, channel, dataFields);

        return dataFields;
    }

    public Map<String, Map<String, Object>> prepareChannelMap(String channelID) {
        return Map.of(channelID, Map.of("approvalStatus", "pending"));
    }

    public void handleChannelAdminRoles(Map<String, String> requestParams, String channel, Map<String, Object> dataFields) {
        if (requestParams.get("role") != null) {
            switch (requestParams.get("role")) {
                case "1" -> setChannelAdminType(dataFields, channel, 1); //Channel Biz Admin
                case "2" -> setChannelAdminType(dataFields, channel, 2); //ChannelSystemAdmin
                case "3" -> setChannelAdminType(dataFields, channel, 3); //Partner Admin
                case "4" -> setChannelAdminType(dataFields, channel, 4); //CIAM Admin
                case "0" -> setChannelAdminType(dataFields, channel, 0); //General User
            }
        }
    }

    public void setChannelAdminType(Map<String, Object> dataFields, String channelID, int adminType) {
        Map<String, Object> channels = (Map<String, Object>) dataFields.get("channels");
        Map<String, Object> channelData = (Map<String, Object>) channels.get(channelID);

        if (channelData == null) {
            // channelData가 null일 경우 새 HashMap을 생성하여 초기화
            channelData = new HashMap<>();
        }
        channelData.put("adminType", adminType);
        channels.put(channelID, channelData);
        dataFields.put("channels", channels);
    }

    public void handleCdcResponse(GSResponse response, Map<String, String> requestParams, String channelID, HttpSession session,RedirectAttributes redirectAttributes,String channelType,String usedBpid) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseJson = objectMapper.readTree(response.getResponseText());
            if (response.getErrorCode() == 206006 || response.getErrorCode() == 206001) {
                //updateAuthorizedSubsidiaries(responseJson, requestParams, channelID); //Subsidiary관련 완료되면 수정필요?

                SecServingCountry secServingCountry = secServingCountryRepository.selectCountryByChannelAndCountryCode(channelID,requestParams.get("secCountry")).orElse(null);;

//                String subsidiary = Optional.ofNullable(secServingCountry)
//                        .map(SecServingCountry::getSubsidiary)
//                        .orElse("");
                String uid = responseJson.get("UID").asText("");
                JsonNode CDCUserProfile = cdcTraitService.getCdcUser(uid, 0);
                String invitationUid = (String) session.getAttribute("cdc_uid");
                //String subsidiary = approvalAdminRepository.selectApprovalAdminSubsidiary(invitationUid,channelID);

                String subsidiary = requestParams.get("subsidiary");

                ChannelInvitation savedInvitation = saveChannelInvitation(responseJson, requestParams, channelID, session,subsidiary);
                sendInviteEmail(responseJson,channelID,savedInvitation,channelType,CDCUserProfile);

                if (isNewCompany(requestParams)) {
                    NewCompany newCompany = newCompanyRepository.selectByBpid(usedBpid);
                    cdcTraitService.registerOrganization(newCompany, requestParams,session);

                    JsonNode companyNode = cdcTraitService.getB2bOrg(newCompany.getBpid());

                    Map<String, Object> createdAccount = cpiApiService.createAccount(channelID, "Context1", objectMapper.convertValue(companyNode, Map.class), session,newCompany);

                    Map<String, Object> accountMap = (Map<String, Object>) createdAccount.get("account");
                    Map<String, Object> wfobj = (Map<String, Object>) createdAccount.get("wfobj");  // 단일 Map으로 처리

                    if (accountMap != null && !accountMap.isEmpty()) {
                        String accountId = (String) accountMap.get("acctid");
                        newCompany.setBpidInCdc(accountId);
                        newCompanyRepository.save(newCompany);

                        cdcTraitService.updateAccountID(uid, newCompany.getBpidInCdc());
                        JsonNode accountNode = cdcTraitService.getCdcUser(uid, 0);

                        Map<String, Object> orgInfo = new HashMap<>();
                        orgInfo.put("bpid", accountId);
                        orgInfo.put("wfstate", wfobj.get("wfstate"));  // 단일 Map으로 처리
                        orgInfo.put("identifystatus", accountNode.get("identifystatus"));
                        orgInfo.put("industry_type", newCompany.getIndustryType());
                        orgInfo.put("products", newCompany.getProducts());
                        orgInfo.put("channeltype", newCompany.getChannelType());

                        // Update organization in CDC
                        cdcTraitService.updateOrganization(newCompany.getBpid(), orgInfo);
                    }
                }


            } else {
                redirectAttributes.addFlashAttribute("showErrorMsg", "invite failed");
                //handleInviteFailure();
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON response", e);
            redirectAttributes.addFlashAttribute("showErrorMsg", "Error processing registration response.");
        }
    }

    public void updateAuthorizedSubsidiaries(JsonNode response, Map<String, String> requestParams, String channel) {
        if (requestParams.get("role") != null && "2".equals(requestParams.get("role"))) {
            AuthorizedSubsidiary authorizedSubsidiary = authorizedSubsidiaryRepository.selectAutoSubsidiary(
                    channel,
                    response.get("UID").asText(),
                    String.join(",", requestParams.get("subsidiaries")
            ));
            if(authorizedSubsidiary!=null) {
                authorizedSubsidiaryRepository.save(authorizedSubsidiary);
            }
        }
    }

    public ChannelInvitation saveChannelInvitation(JsonNode response, Map<String, String> requestParams, String channel, HttpSession session,String subsidiary) {
        ChannelInvitation invitation = new ChannelInvitation();
        invitation.setLoginId(requestParams.get("loginUserId"));
        invitation.setLoginUid(response.get("UID").asText());
        invitation.setRequestorId((String) session.getAttribute("cdc_email"));
        invitation.setRequestorUid((String) session.getAttribute("cdc_uid"));
        invitation.setBpid((String) session.getAttribute("inviteBpid"));
        invitation.setCompanySource((String) session.getAttribute("inviteCompanyDb"));
        invitation.setChannel(channel);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setExpiry(Timestamp.valueOf(LocalDateTime.now().plusHours(168))); //하드코딩부분
        invitation.setStatus("pending");
        invitation.setStatusUpdated(Timestamp.valueOf(LocalDateTime.now()));
        invitation.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        invitation.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        invitation.setSubsidiary(subsidiary);
        invitation.setCompanyName(requestParams.get("name"));
        ChannelInvitation savedInvitation = channelInvitationRepository.save(invitation);
        return savedInvitation;
    }

    public void sendInviteEmail(JsonNode response  ,String channel,ChannelInvitation savedInvitation,String channelType,JsonNode CDCUserProfile) {
        Map<String, Object> returnMsg = new HashMap<>();

        Map<String, Object> paramArr = new HashMap<>();
        //paramArr.put("template", "TEMPLET-032");
        paramArr.put("template", "TEMPLET-NEW-005");
        paramArr.put("cdc_uid", response.get("UID").asText());
        paramArr.put("channel", channel);
        paramArr.put("ch_inv_id", savedInvitation.getId()); //g하드코딩
        paramArr.put("channelType", channelType); //g하드코딩
        paramArr.put("firstName", CDCUserProfile.get("profile").get("firstName") != null ? CDCUserProfile.get("profile").get("firstName").asText() : "");
        paramArr.put("lastName", CDCUserProfile.get("profile").get("lastName") != null ? CDCUserProfile.get("profile").get("lastName").asText() : "");

        returnMsg = mailService.sendMail(paramArr);
    }

//    public void redirectToInvitationIndex(String channelID) {
//        // Logic to redirect to invitation index page
//    }
//
//    public void handleInviteFailure() {
//        // Logic to handle invitation failure
//    }

}
