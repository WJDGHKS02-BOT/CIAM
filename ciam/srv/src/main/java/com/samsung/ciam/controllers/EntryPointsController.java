package com.samsung.ciam.controllers;

import com.samsung.ciam.services.EntryPointsService;
import com.samsung.ciam.utils.BeansUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

/**
 * 1. 파일명   : EntryPointsController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : AD 가입을 위한 신규 채널 등록 및 관련 인증 엔드포인트를 처리하는 컨트롤러
 * 4. 작성자   : 서정환
 * 5. 작성일자 : 2024. 11. 04.
 * 6. 히스토리 :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * 날짜        | 이름          | 설명
 * <p>
 * -------------|--------------|------------------------------------
 * <p>
 * 2024. 11. 04 | 서정환       | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@Controller
@RequestMapping("/new-channel")
public class EntryPointsController {

    @Autowired
    private EntryPointsService entryPointsService;

    /**
     * <PRE>
     * 1. 설명
     *    GET 요청으로 특정 채널을 기반으로 신규 AD 가입 등록 페이지로 이동
     * 2. 사용법
     *    URL: /new-channel/register/{key}
     * 3. 예시 데이터
     *    - Input: /new-channel/register/ABC123
     *    - Output: 삼성 AD 가입 페이지로 이동
     * </PRE>
     * @param param 등록 채널 키
     * @param model 모델 객체
     * @return 뷰 이름
     */
    @GetMapping("/register/{key}")
    public String newChannelEntryPoint(@PathVariable("key") String param, Model model) {
        model.addAttribute("channel", param);
        return "samsungAdRegistration/newChannelEntryPoint";
    }

    /**
     * <PRE>
     * 1. 설명
     *    POST 요청으로 신규 AD 가입 등록 정보를 제출하고 세션에 저장된 정보를 바탕으로 다음 경로로 리다이렉트
     * 2. 사용법
     *    URL: /new-channel/register/{key}
     * 3. 예시 데이터
     *    - Input: /new-channel/register/ABC123, regToken, convertLoginId, adCdcUid, convertUid 등
     *    - Output: 세션에 정보 저장 후 다음 페이지로 리다이렉트
     * </PRE>
     * @param param 등록 채널 키
     * @param regToken 등록 토큰
     * @param convertLoginId 선택적 로그인 ID
     * @param adCdcUid AD 사용자 식별자
     * @param convertUid 선택적 변환 사용자 식별자
     * @param session HTTP 세션 객체
     * @param redirectAttributes 리다이렉트 속성
     * @return 리다이렉트 URL
     */
    @PostMapping("/register/{key}")
    public String newChannelEntryPointSubmit(@PathVariable("key") String param,
                                             @RequestParam(value = "regToken", required = false) String regToken,
                                             @RequestParam(value = "convertLoginId", required = false) String convertLoginId,
                                             @RequestParam(value = "adCdcUid", required = false) String adCdcUid,
                                             @RequestParam(value = "convertUid", required = false) String convertUid,
                                             HttpSession session,
                                             RedirectAttributes redirectAttributes) {
        // 세션에 값 저장
        session.setAttribute("newChannel", param);
        session.setAttribute("regToken", regToken);

        if (convertLoginId != null) {
            session.setAttribute("convertLoginId", convertLoginId);
        }

        if (adCdcUid != null) {
            session.setAttribute("adCdcUid", adCdcUid);
        }

        if (convertUid != null && !convertUid.isEmpty()) {
            session.setAttribute("convertUid", convertUid);
        }

        // 다음 경로로 리다이렉트
        redirectAttributes.addFlashAttribute("channel", param);
        return "redirect:/new-channel/account/" + param;
    }

    /**
     * <PRE>
     * 1. 설명
     *    GET 요청으로 신규 채널에 대한 계정 페이지로 이동
     * 2. 사용법
     *    URL: /new-channel/account/{key}
     * 3. 예시 데이터
     *    - Input: /new-channel/account/sba
     *    - Output: 계정 페이지
     * </PRE>
     * @param key 채널 키
     * @param session HTTP 세션 객체
     * @param model 모델 객체
     * @return ModelAndView 객체
     */
    @GetMapping("/account/{key}")
    public ModelAndView newChannelAccount(@PathVariable String key, HttpSession session, Model model) {
        return entryPointsService.newChannelAccount(key, session);
    }

    /**
     * <PRE>
     * 1. 설명
     *    POST 요청으로 신규 채널 계정 정보를 제출하여 처리 후 리다이렉트
     * 2. 사용법
     *    URL: /new-channel/account/{param}
     * 3. 예시 데이터
     *    - Input: 요청 파라미터 (param, session 등)
     *    - Output: 제출 후 리다이렉트 뷰
     * </PRE>
     * @param param 채널 키
     * @param requestParams 요청 파라미터
     * @param session HTTP 세션 객체
     * @param request HTTP 요청 객체
     * @param redirectAttributes 리다이렉트 속성
     * @return RedirectView 객체
     */
    @PostMapping("/account/{param}")
    public RedirectView newChannelAccountSubmit(@PathVariable String param,
                                                @RequestParam Map<String, String> requestParams,
                                                HttpSession session,
                                                HttpServletRequest request,
                                                RedirectAttributes redirectAttributes) {
        return entryPointsService.newChannelAccountSubmit(requestParams, param, session, request, redirectAttributes);
    }

    /**
     * <PRE>
     * 1. 설명
     *    GET 요청으로 사용자 약관 동의 페이지로 이동
     * 2. 사용법
     *    URL: /new-channel/consent
     * 3. 예시 데이터
     *    - Input: /new-channel/consent?param=채널키
     *    - Output: 동의 페이지
     * </PRE>
     * @param param 채널 키
     * @param session HTTP 세션 객체
     * @return ModelAndView 객체
     */
    @GetMapping("/consent")
    public ModelAndView consent(@RequestParam("param") String param, HttpSession session) {
        return entryPointsService.consent(param, session);
    }

    /**
     * <PRE>
     * 1. 설명
     *    POST 요청으로 약관 동의 제출을 처리 후 리다이렉트
     * 2. 사용법
     *    URL: /new-channel/consentSubmit
     * 3. 예시 데이터
     *    - Input: 요청 파라미터 (채널 키, 페이로드 등)
     *    - Output: 동의 제출 후 리다이렉트 뷰
     * </PRE>
     * @param channel 채널 키
     * @param payload 요청 파라미터
     * @param session HTTP 세션 객체
     * @param request HTTP 요청 객체
     * @param redirectAttributes 리다이렉트 속성
     * @return RedirectView 객체
     */
    @PostMapping("/consentSubmit")
    public RedirectView consentSubmit(@RequestParam(value = "channel") String channel,
                                      @RequestParam Map<String, String> payload,
                                      HttpSession session,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {
        return entryPointsService.consentSubmit(payload, channel, session,redirectAttributes,request);
    }

    /**
     * <PRE>
     * 1. 설명
     *    GET 요청으로 MFA 인증 페이지로 이동
     * 2. 사용법
     *    URL: /new-channel/mfa
     * 3. 예시 데이터
     *    - Input: /new-channel/mfa?param=채널키
     *    - Output: 다중 인증 페이지
     * </PRE>
     * @param param 채널 키
     * @param session HTTP 세션 객체
     * @return ModelAndView 객체
     */
    @GetMapping("/mfa")
    public ModelAndView mfa(@RequestParam("param") String param, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/samsungAdRegistration/multiFactorAuthentication";
        modelAndView.addObject("content", content);
        modelAndView.addObject("channel", param);

        return modelAndView;
    }

    /**
     * <PRE>
     * 1. 설명
     *    POST 요청으로 MFA 인증 제출을 처리하고 리다이렉트
     * 2. 사용법
     *    URL: /new-channel/mfaSubmit
     * 3. 예시 데이터
     *    - Input: 요청 파라미터 (채널 키, 페이로드 등)
     *    - Output: 다중 인증 처리 후 리다이렉트 뷰
     * </PRE>
     * @param param 채널 키
     * @param payload 요청 파라미터
     * @param request HTTP 요청 객체
     * @param redirectAttributes 리다이렉트 속성
     * @return RedirectView 객체
     */
    @PostMapping("/mfaSubmit")
    public RedirectView mfaSubmit(@RequestParam(value = "channel") String param,
                                  @RequestParam Map<String, String> payload,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        return entryPointsService.mfaSubmit(param, request, payload,redirectAttributes);
    }

    /**
     * <PRE>
     * 1. 설명
     *    POST 요청으로 SSO 접근(일반계정 채널 확장)을 위한 리다이렉트 처리
     * 2. 사용법
     *    URL: /new-channel/ssoAccess
     * 3. 예시 데이터
     *    - Input: targetChannel, cdc_uid
     *    - Output: SSO 접근 처리 후 리다이렉트 뷰
     * </PRE>
     * @param targetChannel 대상 채널
     * @param cdcUid 사용자 식별자
     * @param session HTTP 세션 객체
     * @return RedirectView 객체
     */
    @PostMapping("/ssoAccess")
    public RedirectView ssoAccess(@RequestParam("targetChannel") String targetChannel,
                                        @RequestParam("cdc_uid") String cdcUid, // Receive `cdc_uid` as a parameter
                                        HttpSession session) {
        return entryPointsService.ssoAccess(targetChannel, cdcUid, session);
    }


}