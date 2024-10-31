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
public class ApprovalAdminService {

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private ApprovalAdminRepository approvalAdminRepository;

    public List<Map<String, Object>> selectApprovalAdminList(Map<String, String> payload, HttpSession session) {
        List<Map<String, Object>> mappedResults = new ArrayList<>();
        String approver = payload.get("approver");

        List<ApprovalAdmin> results; 
        if("PA".equals(approver) || "CA".equals(approver)) {
            results = approvalAdminRepository.selectApprovalAdminList(payload.get("approver"),payload.get("channel"),payload.get("ruleMasterId"),payload.get("ruleLevel"));
        } else if ("AM".equals(approver)) {
            results = approvalAdminRepository.selectApprovalAdminList(payload.get("approver"),payload.get("ruleMasterId"),payload.get("ruleLevel"));
        } else {
            results = approvalAdminRepository.selectApprovalAdminList(payload.get("approver"),payload.get("channel"),payload.get("country"),payload.get("subsidiary"),payload.get("division"),payload.get("ruleMasterId"),payload.get("ruleLevel"));
        }

        for (ApprovalAdmin result : results) {
            Map<String, Object> termsMap = new HashMap<>();
            termsMap.put("id", result.getId());
            termsMap.put("uid", result.getUid());
            termsMap.put("email", result.getEmail());
            termsMap.put("type", result.getType());
            termsMap.put("typeNm", result.getTypeNm());
            termsMap.put("channel", result.getChannel());
            termsMap.put("country", result.getCountry());
            termsMap.put("subsidiary", result.getSubsidiary());
            termsMap.put("division", result.getDivision());
            termsMap.put("companyCode", result.getCompanyCode());
            termsMap.put("companyName", cdcTraitService.getB2bOrg(result.getCompanyCode()).path("orgName").asText(""));

            termsMap.put("active", result.getActive());
            termsMap.put("approvalDate", result.getApprovalDate());

            termsMap.put("tempapprover", result.getTempapprover());
            termsMap.put("tempapprovalrule", result.getTempapprovalrule());
            termsMap.put("tempapprovalrulelevel", result.getTempapprovalrulelevel());
            termsMap.put("disabled", result.getDisabled());
            termsMap.put("disabledrule", result.getDisabledrule());
            termsMap.put("disabledrulelevel", result.getDisabledrulelevel());

            termsMap.put("updateuser", result.getUpdateUser());
            termsMap.put("creatorId", result.getApprovalUser()); // id를 email값으로 출력해달라는 요청
            termsMap.put("createdDate", result.getCreatedAt());
            termsMap.put("lastAccessTime", result.getUpdatedAt());
            mappedResults.add(termsMap);
        }
        log.warn("ApproverList : {}", mappedResults.toString());

        return mappedResults;
    }
    
    public Integer deleteApprovalAdmin(Map<String, String> payload) {
        // disabled는 해당 조건만
        return approvalAdminRepository.insertDisabledApprovalAdmin(Integer.parseInt(payload.get("id")), payload.get("ruleMasterId"), payload.get("ruleLevel"), payload.get("creatorId"));
    }
}