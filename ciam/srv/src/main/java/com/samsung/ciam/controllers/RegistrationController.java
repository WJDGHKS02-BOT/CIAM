package com.samsung.ciam.controllers;

import com.samsung.ciam.services.RegistrationService;
import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;

@Controller
@RequestMapping("/registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;


    @GetMapping("/{key}")
    public ModelAndView signup(@PathVariable String key, @RequestParam Map<String, Object> allParams, HttpServletRequest request) {
        return registrationService.signup(key, allParams, request);
    }

    @PostMapping("/signupSubmit")
    public RedirectView signupSubmit(@RequestParam(value = "channel") String channel,@RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // 사용자의 회원가입을 처리하는 서비스 메소드 호출
        if(session.getAttribute("ssoAccessYn")!=null) {
            return registrationService.processSsoSignup(channel, payload, session, request,redirectAttributes);
        }
        if(session.getAttribute("convertYn")!=null) {
            return registrationService.processConvertSingup(channel, payload, session, request,redirectAttributes);
        }
        else {
            return registrationService.processSignup(channel, payload, session, request,redirectAttributes);
        }
    }

    @ResponseBody
    @PostMapping("/checkUserId")
    public Map<String, Object> checkUserId(@RequestBody Map<String, String> payload) {
        return registrationService.checkUserId(payload);
    }

    @ResponseBody
    @PostMapping("/checkPwd")
    public Map<String, Object> checkPwd(@RequestBody Map<String, String> payload) {
        return registrationService.checkPwd(payload);
    }

    @ResponseBody
    @PostMapping("/resendEmailCode")
    public Map<String, Object> resendEmailCode(@RequestBody Map<String, String> payload,HttpSession session) {
        Map<String, Object> resendEmail = new HashMap<>();
        resendEmail= registrationService.resendEmailCode(payload,session);
        return resendEmail;
    }

    @GetMapping("/signupVerify")
    // public String signupVerify(@RequestParam("param") String param, HttpSession session, Model model) {
    public ModelAndView signupVerify(@RequestParam("param") String param, HttpSession session) {
        // 세션에서 이메일 값 가져오기
        String loginUserId = (String) session.getAttribute("loginId");
        ModelAndView modelAndView = new ModelAndView("registration");
        // content 경로 설정
        String content = "fragments/registration/mailVerifyContent"; // src/main/resources/templates/thymeleaf/fragments/signupVerify.html

        // 모델에 값 추가
        modelAndView.addObject("email", loginUserId);
        modelAndView.addObject("loginUserId", loginUserId);
        modelAndView.addObject("channel", param);
        modelAndView.addObject("content", content);

        // 뷰 이름 반환
        return modelAndView;
    }

    @PostMapping("/signupVerified")
    public RedirectView signupVerified(@RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        return registrationService.signupVerified(payload,session, request,redirectAttributes);
    }

    @GetMapping("/company")
    public ModelAndView company(@RequestParam("param") String param, @RequestParam(required = false) String t, @RequestParam(required = false) String channelType,HttpSession session,HttpServletRequest request,Model model,RedirectAttributes redirectAttributes) {
        ModelAndView modelAndView = new ModelAndView();
        if(channelType != "" && channelType!=null) {
            session.setAttribute("channelCompanyType", channelType);
        }

        //파트너 타입
        if("CUSTOMER".equals(session.getAttribute("channelCompanyType"))) {
            modelAndView = registrationService.partnerCompany(param, t, session,channelType);
            if(!"error/403".equals(modelAndView.getViewName())) {
                modelAndView = registrationService.partnerAccount(param,t,channelType, session, request, modelAndView,redirectAttributes);
            }
            return modelAndView;
        } else {
            modelAndView = registrationService.company(param, t, session,channelType);
            if(!"error/403".equals(modelAndView.getViewName())) {
                modelAndView = registrationService.account(param, t, channelType, session, request, modelAndView, redirectAttributes);
            }
            return modelAndView;
        }
    }

    @PostMapping("/companySubmit")
    public RedirectView companySubmit(@RequestParam(value = "channel") String channel,
                                      @RequestParam Map<String, String> payload,
                                      HttpSession session,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {

        registrationService.companySubmit(payload, channel, session, request, redirectAttributes);

        if (redirectAttributes.getFlashAttributes().get("showErrorMsg") == null) {
            return registrationService.accountSubmit(payload, channel, session, request, redirectAttributes);
        }

        String referer = request.getHeader("Referer");
        return new RedirectView(referer != null ? referer : "/");

    }

    @PostMapping("/customerCompanySubmit")
    public RedirectView customerCompanySubmit(@RequestParam(value = "channel") String channel,
                                      @RequestParam Map<String, String> payload,
                                      HttpSession session,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {

        registrationService.customerCompanySubmit(payload, channel, session, request, redirectAttributes);

        if (redirectAttributes.getFlashAttributes().get("showErrorMsg") == null) {
            return registrationService.customerAccountSubmit(payload, channel, session, request, redirectAttributes);
        }

        String referer = request.getHeader("Referer");
        return new RedirectView(referer != null ? referer : "/");

    }
    
    // 24.07.08 홍정인 추가 - 회원가입 email 화면 예비 URL
    @GetMapping("/{key}/inProgress")
    public ModelAndView inProgress(@PathVariable String key, @RequestParam Map<String, Object> allParams, HttpServletRequest request) {
        return registrationService.inProgress(key, allParams, request);
    }

    @GetMapping("/changeEmailVerify")
    public ModelAndView changeEmailVerify(@RequestParam("param") String param, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("registration");
        // content 경로 설정
        String content = "fragments/registration/mailVerifyContent"; // src/main/resources/templates/thymeleaf/fragments/signupVerify.html

        // 모델에 값 추가
        modelAndView.addObject("email", session.getAttribute("changedEmail"));
        modelAndView.addObject("loginUserId", session.getAttribute("changedEmail"));
        modelAndView.addObject("channel", param);
        modelAndView.addObject("content", content);

        // 뷰 이름 반환
        return modelAndView;
    }

    @GetMapping("/consent")
    public ModelAndView consent(@RequestParam("param") String param, HttpSession session) {
        return registrationService.consent(param, session);
    }

    @PostMapping("/consentSubmit")
    public RedirectView consentSubmit(@RequestParam(value = "channel") String channel,
                                      @RequestParam Map<String, String> payload,
                                      HttpSession session,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {
        return registrationService.consentSubmit(payload, channel, session,redirectAttributes,request);
    }

    @GetMapping("/authVerify/{param}")
    public ModelAndView authVerify(@PathVariable String param) {
        return registrationService.authVerify(param);
    }

    @PostMapping("/authVerifySubmit/{param}")
    public RedirectView authVerifySubmit(@PathVariable String param, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return registrationService.authVerifySubmit(param, request, redirectAttributes);
    }

    @GetMapping("/mfa")
    public ModelAndView mfa(@RequestParam("param") String param, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/registration/multiFactorAuthentication";
        modelAndView.addObject("content", content);
        modelAndView.addObject("channel", param);

        return modelAndView;
    }

    @PostMapping("/mfaSubmit")
    public RedirectView mfaSubmit(@RequestParam(value = "channel") String param,
                                      @RequestParam Map<String, String> payload,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {
        return registrationService.mfaSubmit(param, request, payload,redirectAttributes);
    }

    // 24.07.04 홍정인 추가 - 회원가입 Complete 화면 예비 URL
    @GetMapping("/registerComplete")
    public ModelAndView registerComplete(@RequestParam("param") String param, @RequestParam Map<String, Object> allParams, HttpServletRequest request,HttpSession session,Model model) {
        model.addAttribute("loginPage", BeansUtil.getLoginPageForChannel(param));
        return registrationService.signupComplete(param, allParams, request,session);
    }

    // 24.07.12 홍정인 추가 - 회원가입 Consent 화면 Direct 접근 URL
    // HOSTURL/registration/{channelName}/consent
    @GetMapping("/{key}/consent")
    public ModelAndView signupConsentTest(@RequestParam Map<String, Object> allParams, HttpServletRequest request, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/registration/consent";
        modelAndView.addObject("content", content);

        return modelAndView;
        // return registrationService.signupConsentTest(allParams, request);
    }

    // 24.07.12 홍정인 추가 - 회원가입 MFA 화면 Direct 접근 URL
    // HOSTURL/registration/{channelName}/mfa
    @GetMapping("/{key}/mfa")
    public ModelAndView mfa(@RequestParam Map<String, Object> allParams, HttpServletRequest request, HttpSession session) {
    // public ModelAndView mfa(@RequestParam("param") String param, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/registration/multiFactorAuthentication";
        modelAndView.addObject("channel", "gmapvd");
        modelAndView.addObject("content", content);
        // modelAndView.addObject("channel", param);

        return modelAndView;
    }

    // 24.07.18 홍정인 추가 - 회원가입 registrationInProgress 화면 Direct 접근 URL
    @GetMapping("/progress")
    public ModelAndView progress(@RequestParam Map<String, Object> allParams, HttpServletRequest request, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/registration/registrationInProgress";
        modelAndView.addObject("content", content);

        return modelAndView;
    }

    // 24.08.07 서정환 - 회원가입 Information 입력 화면 예비 URL
    @GetMapping("/{key}/information")
    public ModelAndView signupInformation(@PathVariable String key, @RequestParam Map<String, Object> allParams, HttpServletRequest request) {
        return registrationService.signupInformation(key, allParams, request);
    }

    @PostMapping("/delete-session")
    public ResponseEntity<Void> deleteSession(HttpSession session) {
        session.invalidate(); // 세션 무효화
        return ResponseEntity.ok().build();
    }

    @GetMapping("/adLoginProcessing/{key}")
    public ModelAndView adLoginProcessing(@PathVariable String key,HttpServletRequest request) {
        return registrationService.adLoginProcessing(key , request);
    }
}
