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

/**
 * 1. FileName	: RegistrationController.java
 * 2. Package	: com.samsung.ciam.controllers
 * 3. Comments	: 사용자 회원가입과 관련된 요청을 처리하는 컨트롤러로, 회원가입, 인증, 이메일 코드 재전송, 채널 및 계정 정보 관리 등을 수행
 * 4. Author	: 서정환
 * 5. DateTime	: 2024. 11. 04.
 * 6. History	:
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * Date		 |	Name			|	Comment
 * <p>
 * -------------  -----------------   ------------------------------
 * <p>
 * 2024. 11. 04.		 | 서정환			|	최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@Controller
@RequestMapping("/registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    /*
     * 1. 메소드명: signup
     * 2. 클래스명: RegistrationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    주어진 키(채널) 값을 사용하여 첫 회원가입 페이지를 반환하는 메소드
     * 2. 사용법
     *    URL에서 {key} 값을 받아 회원가입 페이지를 호출
     * 3. 예시 데이터
     *    - Input: key = "userKey123", allParams = {param1=value1, ...}
     *    - Output: ModelAndView (회원가입 화면)
     * </PRE>
     * @param key 사용자 키 값
     * @param allParams 모든 요청 파라미터
     * @param request HTTP 요청 객체
     * @return ModelAndView 회원가입 페이지
     */
    @GetMapping("/{key}")
    public ModelAndView signup(@PathVariable String key, @RequestParam Map<String, Object> allParams, HttpServletRequest request) {
        return registrationService.signup(key, allParams, request);
    }

    /*
     * 1. 메소드명: signupSubmit
     * 2. 클래스명: RegistrationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    사용자의 회원가입 데이터(이메일,국가)를 제출하여 처리하는 메소드
     * 2. 사용법
     *    POST 요청으로 회원가입 정보와 세션 상태를 통해 CDC 임시계정 생성 절차 진행
     * 3. 예시 데이터
     *    - Input: channel = "web", payload = {username: "user", password: "pass123"}
     *    - Output: RedirectView (회원가입 완료 후 리다이렉트)
     * </PRE>
     * @param channel 요청된 가입 채널
     * @param payload 회원가입 정보
     * @param session HTTP 세션 객체
     * @param request HTTP 요청 객체
     * @param redirectAttributes 리다이렉트 속성 객체
     * @return RedirectView 리다이렉트 URL
     */
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

    /*
     * 1. 메소드명: checkUserId
     * 2. 클래스명: RegistrationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    사용자 이메일의 CDC 중복 여부를 확인하는 메소드
     * 2. 사용법
     *    POST 요청으로 payload에 사용자 ID가 포함되어 있어야 함
     * 3. 예시 데이터
     *    - Input: payload = {userId: "user123"}
     *    - Output: {isAvailable: true/false}
     * </PRE>
     * @param payload 사용자 ID 정보
     * @return Map<String, Object> 중복 확인 결과
     */
    @ResponseBody
    @PostMapping("/checkUserId")
    public Map<String, Object> checkUserId(@RequestBody Map<String, String> payload) {
        return registrationService.checkUserId(payload);
    }

    /*
     * 1. 메소드명: checkPwd
     * 2. 클래스명: RegistrationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    사용자 비밀번호의 유효성을 확인하는 메소드
     * 2. 사용법
     *    POST 요청으로 payload에 비밀번호 정보가 포함되어 있어야 함
     * 3. 예시 데이터
     *    - Input: payload = {pwd: "password123"}
     *    - Output: {isValid: true/false}
     * </PRE>
     * @param payload 비밀번호 정보
     * @return Map<String, Object> 비밀번호 유효성 확인 결과
     */
    @ResponseBody
    @PostMapping("/checkPwd")
    public Map<String, Object> checkPwd(@RequestBody Map<String, String> payload) {
        return registrationService.checkPwd(payload);
    }

    /*
     * 1. 메소드명: resendEmailCode
     * 2. 클래스명: RegistrationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    사용자에게 이메일 인증 코드를 다시 전송하는 메소드
     * 2. 사용법
     *    POST 요청으로 payload에 이메일 정보가 포함되어 있어야 함
     * 3. 예시 데이터
     *    - Input: payload = {email: "user@example.com"}
     *    - Output: {status: "success"}
     * </PRE>
     * @param payload 이메일 정보
     * @param session HTTP 세션 객체
     * @return Map<String, Object> 이메일 코드 재전송 결과
     */
    @ResponseBody
    @PostMapping("/resendEmailCode")
    public Map<String, Object> resendEmailCode(@RequestBody Map<String, String> payload,HttpSession session) {
        Map<String, Object> resendEmail = new HashMap<>();
        resendEmail= registrationService.resendEmailCode(payload,session);
        return resendEmail;
    }

    /*
     * 1. 메소드명: signupVerify
     * 2. 클래스명: RegistrationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    사용자의 이메일 인증코드 입력화면으로 반환하는 메소드
     * 2. 사용법
     *    GET 요청으로 세션의 로그인 ID를 사용하여 이메일 검증 페이지를 호출
     * 3. 예시 데이터
     *    - Input: param = "channel"
     *    - Output: ModelAndView (이메일 인증 페이지)
     * </PRE>
     * @param param 채널 정보
     * @param session HTTP 세션 객체
     * @return ModelAndView 이메일 인증 페이지
     */
    @GetMapping("/signupVerify")
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

    /*
     * 1. 메소드명: signupVerified
     * 2. 클래스명: RegistrationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    이메일 인증 검증 절차를 진행하고 이후 회사,유저정보 화면으로 리다이렉트하는 메소드
     * 2. 사용법
     *    POST 요청으로 인증 정보를 수행
     * 3. 예시 데이터
     *    - Input: payload = {userId: "user123", verificationCode: "abc123"}
     *    - Output: RedirectView (회원가입 완료 후 리다이렉트)
     * </PRE>
     * @param payload 이메일 인증코드
     * @param session HTTP 세션 객체
     * @param request HTTP 요청 객체
     * @param redirectAttributes 리다이렉트 속성 객체
     * @return RedirectView 리다이렉트 URL
     */
    @PostMapping("/signupVerified")
    public RedirectView signupVerified(@RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        return registrationService.signupVerified(payload,session, request,redirectAttributes);
    }

    /*
     * 1. 메소드명: company
     * 2. 클래스명: RegistrationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    회사 관련 정보를 조회하고 사용자 유형에 따라 적절한 VENDOR/CUSTOMER 화면을 반환하는 메소드
     * 2. 사용법
     *    GET 요청으로 회사 정보를 받아 필요한 데이터를 설정
     * 3. 예시 데이터
     *    - Input: param = "company", channelType = "CUSTOMER"
     *    - Output: ModelAndView (회사/파트너 페이지)
     * </PRE>
     */
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

    /*
     * 1. 메소드명: companySubmit
     * 2. 클래스명: RegistrationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    VENDOR 채널 유형으로 회사 정보 및 유저정보를 저장하고 이후 약관동의 화면으로 리다이렉트하는 메소드
     * 2. 사용법
     *    POST 요청으로 회사 정보와 사용자 채널 데이터를 제출
     * 3. 예시 데이터
     *    - Input: payload = {name: "CompanyName"}, channel = "web"
     *    - Output: RedirectView (계정 설정 완료 후 리다이렉트)
     * </PRE>
     */
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

    /*
     * 1. 메소드명: customerCompanySubmit
     * 2. 클래스명: RegistrationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CUSTOMER 채널 유형으로 회사 정보 및 유저정보를 저장하고 이후 약관동의 화면으로 리다이렉트하는 메소드
     * 2. 사용법
     *    POST 요청으로 회사 정보와 사용자 채널 데이터를 제출
     * 3. 예시 데이터
     *    - Input: payload = {name: "CustomerCompany"}, channel = "mobile"
     *    - Output: RedirectView (계정 설정 완료 후 리다이렉트)
     * </PRE>
     */
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

    /*
     * 1. 메소드명: inProgress
     * 2. 클래스명: RegistrationController
     * 3. 작성자명: 홍정인
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    회원가입 email 화면 예비 URL 랜더링
     * 2. 사용법
     *    GET 요청으로 사용자의 진행 상황 페이지 호출
     * 3. 예시 데이터
     *    - Input: key = "user123", allParams = {param1=value1, ...}
     *    - Output: ModelAndView (진행 상황 화면)
     * </PRE>
     */
    @GetMapping("/{key}/inProgress")
    public ModelAndView inProgress(@PathVariable String key, @RequestParam Map<String, Object> allParams, HttpServletRequest request) {
        return registrationService.inProgress(key, allParams, request);
    }

    /*
     * 1. 메소드명: changeEmailVerify
     * 2. 클래스명: RegistrationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    사용자가 이메일을 변경할 때 인증을 수행하는 화면을 반환하는 메소드
     * 2. 사용법
     *    GET 요청으로 변경된 이메일 정보를 보여주는 페이지 호출
     * 3. 예시 데이터
     *    - Input: param = "newChannel"
     *    - Output: ModelAndView (이메일 인증 화면)
     * </PRE>
     */
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

    /*
     * 1. 메소드명: consent
     * 2. 클래스명: RegistrationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    사용자의 약관 동의 화면을 제공하는 메소드
     * 2. 사용법
     *    GET 요청으로 동의 화면 호출
     * 3. 예시 데이터
     *    - Input: param = "userConsent"
     *    - Output: ModelAndView (동의 화면)
     * </PRE>
     */
    @GetMapping("/consent")
    public ModelAndView consent(@RequestParam("param") String param, HttpSession session) {
        return registrationService.consent(param, session);
    }

    /*
     * 1. 메소드명: consentSubmit
     * 2. 클래스명: RegistrationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    사용자가 약관 동의를 제출할 때 처리하는 메소드
     * 2. 사용법
     *    POST 요청으로 동의 데이터를 제출하여 저장
     * 3. 예시 데이터
     *    - Input: payload = {consent: "agree"}, channel = "web"
     *    - Output: RedirectView (동의 완료 후 리다이렉트)
     * </PRE>
     */
    @PostMapping("/consentSubmit")
    public RedirectView consentSubmit(@RequestParam(value = "channel") String channel,
                                      @RequestParam Map<String, String> payload,
                                      HttpSession session,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {
        return registrationService.consentSubmit(payload, channel, session,redirectAttributes,request);
    }

    /**
     * 1. 메소드명 : authVerify
     * 2. 클래스명 : RegistrationController
     * 3. 작성자   : 서정환
     * 4. 작성일자 : 2024. 11. 06.
     * <PRE>
     * 1. 설명
     *    인증 확인 화면을 반환하는 메소드(테스트용)
     * 2. 사용법
     *    GET 요청으로 인증 확인 화면 호출
     * 3. 예시 데이터
     *    - Input: param = "authKey"
     *    - Output: ModelAndView (인증 확인 화면)
     * </PRE>
     */
    @GetMapping("/authVerify/{param}")
    public ModelAndView authVerify(@PathVariable String param) {
        return registrationService.authVerify(param);
    }

    /**
     * 1. 메소드명 : authVerifySubmit
     * 2. 클래스명 : RegistrationController
     * 3. 작성자   : 서정환
     * 4. 작성일자 : 2024. 11. 06.
     * <PRE>
     * 1. 설명
     *    인증 확인 후 제출을 처리하고 리다이렉트하는 메소드(테스트용)
     * 2. 사용법
     *    POST 요청으로 인증 확인을 제출
     * 3. 예시 데이터
     *    - Input: param = "authKey"
     *    - Output: RedirectView (인증 확인 후 리다이렉트)
     * </PRE>
     */
    @PostMapping("/authVerifySubmit/{param}")
    public RedirectView authVerifySubmit(@PathVariable String param, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return registrationService.authVerifySubmit(param, request, redirectAttributes);
    }

    /**
     * 1. 메소드명 : mfa
     * 2. 클래스명 : RegistrationController
     * 3. 작성자   : 서정환
     * 4. 작성일자 : 2024. 11. 06.
     * <PRE>
     * 1. 설명
     *    2차 인증(MFA) 선택 화면을 반환하는 메소드(테스트용)
     * 2. 사용법
     *    GET 요청으로 MFA 설정 화면 호출
     * 3. 예시 데이터
     *    - Input: param = "mfaChannel"
     *    - Output: ModelAndView (MFA 설정 화면)
     * </PRE>
     */
    @GetMapping("/mfa")
    public ModelAndView mfa(@RequestParam("param") String param, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/registration/multiFactorAuthentication";
        modelAndView.addObject("content", content);
        modelAndView.addObject("channel", param);

        return modelAndView;
    }

    /**
     * 1. 메소드명 : mfaSubmit
     * 2. 클래스명 : RegistrationController
     * 3. 작성자   : 서정환
     * 4. 작성일자 : 2024. 11. 06.
     * <PRE>
     * 1. 설명
     *    2차 인증 설정을 제출하는 메소드
     * 2. 사용법
     *    POST 요청으로 MFA 설정 제출
     * 3. 예시 데이터
     *    - Input: payload = {mfaData: "data"}, param = "channel"
     *    - Output: RedirectView (MFA 설정 완료 후 리다이렉트)
     * </PRE>
     */
    @PostMapping("/mfaSubmit")
    public RedirectView mfaSubmit(@RequestParam(value = "channel") String param,
                                  @RequestParam Map<String, String> payload,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        return registrationService.mfaSubmit(param, request, payload,redirectAttributes);
    }

    /**
     * 1. 메소드명 : registerComplete
     * 2. 클래스명 : RegistrationController
     * 3. 작성자   : 서정환
     * 4. 작성일자 : 2024. 07. 04.
     * <PRE>
     * 1. 설명
     *    회원가입 완료 화면을 반환하는 메소드
     * 2. 사용법
     *    GET 요청으로 회원가입 완료 페이지 호출
     * 3. 예시 데이터
     *    - Input: param = "completeChannel"
     *    - Output: ModelAndView (회원가입 완료 화면)
     * </PRE>
     */
    @GetMapping("/registerComplete")
    public ModelAndView registerComplete(@RequestParam("param") String param, @RequestParam Map<String, Object> allParams, HttpServletRequest request,HttpSession session,Model model) {
        model.addAttribute("loginPage", BeansUtil.getLoginPageForChannel(param));
        return registrationService.signupComplete(param, allParams, request,session);
    }

    /**
     * 1. 메소드명 : signupConsentTest
     * 2. 클래스명 : RegistrationController
     * 3. 작성자   : 홍정인
     * 4. 작성일자 : 2024. 07. 12.
     * <PRE>
     * 1. 설명
     *    회원가입 동의 화면을 다이렉트로 접근하여 호출하는 메소드(테스트용)
     * 2. 사용법
     *    GET 요청으로 동의 화면 호출
     * 3. 예시 데이터
     *    - Input: key = "userChannel"
     *    - Output: ModelAndView (동의 화면)
     * </PRE>
     */
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

    /**
     * 1. 메소드명 : mfa (direct access)
     * 2. 클래스명 : RegistrationController
     * 3. 작성자   : 홍정인
     * 4. 작성일자 : 2024. 07. 12.
     * <PRE>
     * 1. 설명
     *    2차 인증 설정 화면을 다이렉트로 접근하여 호출하는 메소드(테스트용)
     * 2. 사용법
     *    GET 요청으로 MFA 설정 페이지 호출
     * 3. 예시 데이터
     *    - Input: key = "userChannel"
     *    - Output: ModelAndView (MFA 설정 화면)
     * </PRE>
     */
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

    /**
     * 1. 메소드명 : progress
     * 2. 클래스명 : RegistrationController
     * 3. 작성자   : 홍정인
     * 4. 작성일자 : 2024. 07. 18.
     * <PRE>
     * 1. 설명
     *    회원가입 진행 상황을 보여주는 화면을 반환하는 메소드(테스트용)
     * 2. 사용법
     *    GET 요청으로 진행 상황 페이지 호출
     * 3. 예시 데이터
     *    - Input: allParams = {status: "inProgress"}
     *    - Output: ModelAndView (진행 상황 화면)
     * </PRE>
     */
    // 24.07.18 홍정인 추가 - 회원가입 registrationInProgress 화면 Direct 접근 URL
    @GetMapping("/progress")
    public ModelAndView progress(@RequestParam Map<String, Object> allParams, HttpServletRequest request, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/registration/registrationInProgress";
        modelAndView.addObject("content", content);

        return modelAndView;
    }

    /**
     * 1. 메소드명 : signupInformation
     * 2. 클래스명 : RegistrationController
     * 3. 작성자   : 서정환
     * 4. 작성일자 : 2024. 08. 07.
     * <PRE>
     * 1. 설명
     *    회원가입 정보 입력 화면을 다이렉트로 반환하는 메소드(테스트용)
     * 2. 사용법
     *    GET 요청으로 회원가입 정보 입력 페이지 호출
     * 3. 예시 데이터
     *    - Input: key = "userKey"
     *    - Output: ModelAndView (회원가입 정보 입력 화면)
     * </PRE>
     */
    // 24.08.07 서정환 - 회원가입 Information 입력 화면 예비 URL
    @GetMapping("/{key}/information")
    public ModelAndView signupInformation(@PathVariable String key, @RequestParam Map<String, Object> allParams, HttpServletRequest request) {
        return registrationService.signupInformation(key, allParams, request);
    }

    /**
     * 1. 메소드명 : deleteSession
     * 2. 클래스명 : RegistrationController
     * 3. 작성자   : 서정환
     * 4. 작성일자 : 2024. 11. 06.
     * <PRE>
     * 1. 설명
     *    세션 삭제를 수행하는 메소드(회원가입 완료후 작동)
     * 2. 사용법
     *    POST 요청으로 세션을 종료
     * 3. 예시 데이터
     *    - Input: 세션 정보
     *    - Output: 성공 응답
     * </PRE>
     */
    @PostMapping("/delete-session")
    public ResponseEntity<Void> deleteSession(HttpSession session) {
        session.invalidate(); // 세션 무효화
        return ResponseEntity.ok().build();
    }

    /**
     * 1. 메소드명 : adLoginProcessing
     * 2. 클래스명 : RegistrationController
     * 3. 작성자   : 서정환
     * 4. 작성일자 : 2024. 11. 06.
     * <PRE>
     * 1. 설명
     *    회원가입 AD계정 입력후 AD가입페이지로 이동하기전 대기 페이지 VIEW 랜더링
     * 2. 사용법
     *    GET 요청으로 AD가입 대기 처리
     * 3. 예시 데이터
     *    - Input: key = "adKey"
     *    - Output: ModelAndView (AD가입 대기 화면)
     * </PRE>
     */
    @GetMapping("/adLoginProcessing/{key}")
    public ModelAndView adLoginProcessing(@PathVariable String key,HttpServletRequest request) {
        return registrationService.adLoginProcessing(key , request);
    }
}
