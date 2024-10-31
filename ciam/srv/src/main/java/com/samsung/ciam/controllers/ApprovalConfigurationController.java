package com.samsung.ciam.controllers;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.samsung.ciam.models.ApprovalRule;
import com.samsung.ciam.models.ConsentJConsentContents;
import com.samsung.ciam.services.ApprovalAdminService;
import com.samsung.ciam.services.ApprovalConfigurationService;
import com.samsung.ciam.services.AuditLogService;
import com.samsung.ciam.services.SystemContentsService;
import com.samsung.ciam.services.SystemService;
import com.samsung.ciam.services.UserProfileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/approvalConfiguration")
public class ApprovalConfigurationController {

    @Autowired
    private ApprovalConfigurationService approvalConfigurationService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private ApprovalAdminService approvalAdminService;

    @Autowired
    private AuditLogService auditLogService; 
    
    @PostMapping("/approvalConfigurationList")
    public List<Map<String, Object>> searchApprovalConfiguration(@RequestBody Map<String, String> payload, HttpSession session) {
        List<Map<String, Object>> resultList = approvalConfigurationService.searchApprovalConfiguration(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "ListView"); // 6가지 : ListView, DetailedView, View, Search, Creation, Modification
        param.put("condition", String.valueOf(payload));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", String.valueOf(resultList.size()));
        auditLogService.addAuditLog(session, param);
        return resultList;
    }
    
    @PostMapping("/newApprovalConfigurationList")
    public Map<String, Object> newSearchApprovalConfiguration(@RequestBody Map<String, String> payload, HttpSession session) {
        List<Map<String, Object>> resultList = approvalConfigurationService.searchApprovalConfiguration(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Search");
        param.put("condition", String.valueOf(payload));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", String.valueOf(resultList.size()));
        auditLogService.addAuditLog(session, param);

        Map<String, Object> results = new HashMap<>();
        results.put("result", resultList);
        results.put("stage", approvalConfigurationService.getApprovalRuleMasterStage(payload, session));
        results.put("approveFormat", approvalConfigurationService.getApprovalRuleMasterApproveFormat(payload, session));
        results.put("ruleLevel", approvalConfigurationService.getPossibleApprovalRuleList(payload, session));
        return results;
    }

    @PostMapping("/insertApprovalRule")
    public Integer insertApprovalRule(@RequestBody Map<String, String> payload, HttpSession session) {
        Integer resultCount = approvalConfigurationService.insertApprovalRule(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Creation");
        param.put("condition", String.valueOf(payload));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", resultCount.toString());
        auditLogService.addAuditLog(session, param);

        return resultCount;
    }

    @PostMapping("/updateApprovalRule")
    public Integer updateApprovalRule(@RequestBody Map<String, String> payload, HttpSession session) {
        Integer resultCount = approvalConfigurationService.updateApprovalRule(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Modification");
        param.put("condition", String.valueOf(payload));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", resultCount.toString());
        auditLogService.addAuditLog(session, param);

        return resultCount;
    }

    @PostMapping("/deleteApprovalRule")
    public Integer deleteApprovalRule(@RequestBody Map<String, String> payload, HttpSession session) {
        Integer resultCount = approvalConfigurationService.deleteApprovalRule(Integer.parseInt(payload.get("id")));
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Deletion");
        param.put("condition", payload.get("id"));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", resultCount.toString());
        auditLogService.addAuditLog(session, param);

        return resultCount;
    }

    @GetMapping("/searchUserManagement")
    public Map<String, Object> searchUserManagment(@RequestParam("userId") String userId, HttpSession session, Model model) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        Map<String, Object> userManagement = approvalConfigurationService.searchUserManagement(params,session);
        List<Object> resultList = (List<Object>) userManagement.get("result");

         // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Search");
        param.put("condition", params.get("userId").toString());
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", String.valueOf(resultList.size()));
        auditLogService.addAuditLog(session, param);

        Map<String, Object> results = new HashMap<>();
        int count = resultList.size();
        results.put("count", count);
        if(count > 0){
            results.put("result", resultList.get(0));
            results.put("message", "데이터 가져오기 성공");
        } else {
            results.put("message", "데이터가 없습니다.");
        }
        log.warn("results : {}", results);
        return results;
    }

    @PostMapping("/insertApprovalAdmin")
    public Integer insertApprovalAdmin(@RequestBody Map<String, String> payload, HttpSession session) {
        Integer resultCount = approvalConfigurationService.insertApprovalAdmin(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Creation");
        param.put("condition", payload.get("id"));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", resultCount.toString());
        auditLogService.addAuditLog(session, param);

        return resultCount;
    }

    @PostMapping("/deleteApprovalAdmin")
    public Integer deleteApprovalAdmin(@RequestBody Map<String, String> payload, HttpSession session) {
        Integer resultCount = approvalAdminService.deleteApprovalAdmin(payload);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Modification");
        param.put("condition", payload.get("id"));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", resultCount.toString());
        auditLogService.addAuditLog(session, param);

        return resultCount;
    }

    @PostMapping("/saveApprovalRuleMasterStage")
    public Integer saveApprovalRuleMasterStage(@RequestBody Map<String, String> payload, HttpSession session) {
        Integer resultCount = approvalConfigurationService.saveApprovalRuleMasterStage(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Deletion");
        param.put("condition", payload.get("id"));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", resultCount.toString());
        auditLogService.addAuditLog(session, param);

        return resultCount;
    }

    @PostMapping("/selectApprovalAdminList")
    public List<Map<String, Object>> selectApprovalAdminList(@RequestBody Map<String, String> payload, HttpSession session) {
        List<Map<String, Object>> resultList = approvalAdminService.selectApprovalAdminList(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "ListView"); // 6가지 : ListView, DetailedView, View, Search, Creation, Modification
        param.put("condition", String.valueOf(payload));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", String.valueOf(resultList.size()));
        auditLogService.addAuditLog(session, param);
        return resultList;
    }
}
