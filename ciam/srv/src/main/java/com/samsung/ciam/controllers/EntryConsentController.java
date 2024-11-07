package com.samsung.ciam.controllers;

import com.samsung.ciam.services.EntryPointsService;
import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

/**
 * 1. 파일명   : EntryConsentController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : 새로운 약관 동의및 약관버전 업데이트 체크, 약관 조회를 위한 엔드포인트를 제공하는 컨트롤러
 * 4. 작성자   : 서정환
 * 5. 작성일자 : 2024. 11. 04.
 * 6. 히스토리 :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * 날짜         | 이름         | 설명
 * <p>
 * -------------|--------------|------------------------------------
 * <p>
 * 2024. 11. 04 | 서정환       | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@Controller
public class EntryConsentController {

    @Autowired
    private EntryPointsService entryPointsService;

    /*
     * 1. 메소드명: newConsent
     * 2. 클래스명: EntryConsentController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    새로운 약관 동의 화면을 제공
     * 2. 사용법
     *    POST 요청으로 사용자 UID와 key(채널)을 파라미터로 전달
     * 3. 예시 데이터
     *    - Input: key, uid
     *    - Output: ModelAndView 약관 동의 페이지
     * </PRE>
     * @param key 동의 대상 key
     * @param uid 사용자 ID
     * @param session 사용자 세션
     * @return 약관 동의 화면을 반환하는 ModelAndView 객체
     */
    @PostMapping("/consent-update/{key}")
    public ModelAndView newConsent(@PathVariable("key") String key,
                                   @RequestParam("uid") String uid,
                                   HttpSession session) {
        // 파라미터와 세션을 사용하여 처리 로직 수행
        return entryPointsService.newConsent(uid, key, session);
    }

    /*
     * 1. 메소드명: consentVersionCheck
     * 2. 클래스명: EntryConsentController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관 버전이 최신인지 확인하는 기능 -> 로그인시 호출
     * 2. 사용법
     *    POST 요청으로 약관 데이터를 전달하여 확인
     * 3. 예시 데이터
     *    - Input: payload (약관 정보가 포함된 Map)
     *    - Output: 약관 버전 확인 결과 문자열
     * </PRE>
     * @param payload 요청 데이터
     * @param session 세션 객체
     * @param redirectAttributes 리다이렉트 시 전달할 속성
     * @return 버전 확인 결과 문자열
     */
    @ResponseBody
    @PostMapping("/new-consent/consentVersionCheck")
    public String consentVersionCheck(@RequestBody Map<String, String> payload, HttpSession session, RedirectAttributes redirectAttributes) {
        return entryPointsService.consentVersionCheck(payload,session,redirectAttributes);
    }

    /*
     * 1. 메소드명: consentSubmit
     * 2. 클래스명: EntryConsentController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관 버전 업데이트 필요하여 새로운 약관 동의를 제출하는 기능
     * 2. 사용법
     *    POST 요청으로 채널과 약관 동의 데이터를 전달하여 동의 제출
     * 3. 예시 데이터
     *    - Input: channel, payload (약관 동의 데이터)
     *    - Output: RedirectView 리다이렉트 객체
     * </PRE>
     * @param channel 동의 대상 채널
     * @param payload 요청 데이터
     * @param session 세션 객체
     * @param request 요청 객체
     * @param redirectAttributes 리다이렉트 속성
     * @return 약관 동의 결과 페이지로 리다이렉트
     */
    @PostMapping("/new-consent/consentSubmit")
    public RedirectView consentSubmit(@RequestParam(value = "channel") String channel,
                                      @RequestParam Map<String, String> payload,
                                      HttpSession session,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {
        return entryPointsService.newConsentSubmit(payload, channel, session,redirectAttributes,request);
    }

    /*
     * 1. 메소드명: consentUpdatedThankYou
     * 2. 클래스명: EntryConsentController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관 갱신 완료 화면을 표시
     * 2. 사용법
     *    GET 요청으로 param 파라미터 전달
     * 3. 예시 데이터
     *    - Input: param
     *    - Output: ModelAndView 동의 갱신 완료 페이지
     * </PRE>
     * @param param 추가 파라미터
     * @return 갱신 완료 페이지를 반환하는 ModelAndView 객체
     */
    @GetMapping("/new-consent/newConsentComplete")
    public ModelAndView consentUpdatedThankYou(@RequestParam("param") String param) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/newconsent/consentUpdatedThankYou";

        modelAndView.addObject("content", content);

        return modelAndView;
    }

    /*
     * 1. 메소드명: consentView
     * 2. 클래스명: EntryConsentController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관 보기 페이지를 제공
     * 2. 사용법
     *    GET 요청으로 약관 관련 정보를 설정하여 페이지를 반환
     * 3. 예시 데이터
     *    - Input: 요청 파라미터
     *    - Output: 약관 보기 페이지
     * </PRE>
     * @param request 요청 객체
     * @param params 요청 파라미터 맵
     * @param model 모델 객체
     * @return 약관 보기 페이지의 뷰 이름
     */
    @GetMapping("/consent-view")
    public String consentView(HttpServletRequest request,
                              @RequestParam Map<String, String> params,
                              Model model
    ) {
        String scheme = request.getScheme(); // http 또는 https
        String serverName = request.getServerName(); // localhost 또는 도메인명
        int serverPort = request.getServerPort(); // 8080 같은 포트 번호

        String hostURL = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);

        String samlContext = params.get("samlContext");
        String spName = params.get("spName");
        String channel = params.get("channel");

        String loginURL = hostURL + "/sign-in?channel=" + channel;
        String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;

        model.addAttribute("channel", channel);
        model.addAttribute("loginURL", loginURL);
        model.addAttribute("logoutURL", logoutURL);
        model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));

        return "_pages/consent-view/consent-view";
    }

}
