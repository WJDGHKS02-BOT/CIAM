package com.samsung.ciam.controllers;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.samsung.ciam.models.ApprovalRule;
import com.samsung.ciam.models.ConsentJConsentContents;
import com.samsung.ciam.repositories.ConsentContentRepository;
import com.samsung.ciam.services.ApprovalConfigurationService;
import com.samsung.ciam.services.SystemContentsService;
import com.samsung.ciam.services.UserProfileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/systemConsents")
public class SystemConsentsController {
    
    @Autowired
    private SystemContentsService systemContentsService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private ApprovalConfigurationService approvalConfigurationService;

    @Autowired
    private ConsentContentRepository consentContentRepository;
    
    @PostMapping("/consentManagementList")
    public List<Map<String, Object>> searchConsentManagement(@RequestBody Map<String, String> payload, HttpSession session) {
        return systemContentsService.searchConsentManagement(payload, session);
    }
    
    @PostMapping("/insertConsentManagement")
    public RedirectView insertConsentManagement(HttpServletRequest request,HttpSession session,@RequestParam Map<String, String> payload, Model model, RedirectAttributes redirectAttributes) throws ParseException {
        systemContentsService.insertConsentManagement(payload, session, request);
        return userProfileService.consentManager(request, session, model, redirectAttributes);
    }
    
    @PostMapping("/updateConsentManagement")
    public RedirectView updateConsentManagement(HttpServletRequest request,HttpSession session,@RequestParam Map<String, String> payload, Model model, RedirectAttributes redirectAttributes) throws ParseException {
        if(consentContentRepository.getStatusId(Integer.parseInt(payload.get("id"))).equals("published")) {
            systemContentsService.insertConsentManagement(payload, session, request);
        } else {
            systemContentsService.updateConsentManagement(payload, session, request);
        }
        return userProfileService.consentManager(request, session, model, redirectAttributes);
    }

    @PostMapping("/duplicationConsentCheck")
    public Map<String, Object> duplicationConsentCheck(@RequestBody Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        return systemContentsService.duplicationConsentCheck(payload, session, request, redirectAttributes);
    }
    
    @PostMapping("/approvalConfigurationList")
    public List<Map<String, Object>> searchApprovalConfiguration(@RequestBody Map<String, String> payload, HttpSession session) {
        return approvalConfigurationService.searchApprovalConfiguration(payload, session);
    }
}
