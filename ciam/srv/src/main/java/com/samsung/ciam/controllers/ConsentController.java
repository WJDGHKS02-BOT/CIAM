package com.samsung.ciam.controllers;

import com.samsung.ciam.repositories.ChannelRepository;
import com.samsung.ciam.repositories.ConsentRepository;
import com.samsung.ciam.services.*;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * 1. 파일명   : ConsentController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : 약관 조회 및 관리를 위한 API를 제공하는 컨트롤러
 * 4. 작성자   : 한국민
 * 5. 작성일자 : 2024. 11. 04.
 * 6. 히스토리 :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * 날짜         | 이름         | 설명
 * <p>
 * -------------|--------------|------------------------------------
 * <p>
 * 2024. 11. 04 | 한국민       | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@RestController
@RequestMapping("/consent")
public class ConsentController {

    @Autowired
    private ConsentService consentService;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private ConsentRepository consentRepository;

    /*
     * 1. 메소드명: consentsView
     * 2. 클래스명: ConsentController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    특정 채널의 약관 뷰 페이지로 이동하여 관련 데이터를 보여줍니다.
     * 2. 사용법
     *    GET 요청을 통해 채널별 약관 데이터를 조회
     * 3. 예시 데이터
     *    - Input: 채널명
     *    - Output: 약관 데이터가 포함된 뷰 페이지
     * </PRE>
     * @param channel 채널 이름
     * @param servletRequest 서블릿 요청 객체
     * @param request HTTP 요청 객체
     * @param model 모델 객체
     * @param session 세션 객체
     * @return ModelAndView 약관 뷰 페이지
     */
    @GetMapping("/{channel}")
    public ModelAndView consentsView(
        @PathVariable String channel,
        ServletRequest servletRequest,
        HttpServletRequest request,
        Model model,
        HttpSession session
    ) {

        ModelAndView modelAndView = new ModelAndView("fragments/consentView");

        // common code
        // channel (channels 테이블 > value : channel_name / name : channel_display_name)
        // modelAndView.addObject("channel", channelRepository.selectChannelTypeList(""));
        String channelDisplayName = channelRepository.selectChannelDisplayName(channel);
        modelAndView.addObject("session_channel",channel);
        modelAndView.addObject("session_display_channel",channelDisplayName);
        modelAndView.addObject("contentCnt", consentRepository.getContentCntByChannel(channel));
        return modelAndView;
    }
        // return consentService.consentsView(session);

    /*
     * 1. 메소드명: getConsentTypeList
     * 2. 클래스명: ConsentController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관 타입 리스트를 반환
     * 2. 사용법
     *    POST 요청으로 약관 타입 리스트를 조회
     * 3. 예시 데이터
     *    - Input: 요청 데이터(payload)
     *    - Output: 약관 타입 리스트
     * </PRE>
     * @param payload 요청 데이터
     * @param request HTTP 요청 객체
     * @param session 세션 객체
     * @param model 모델 객체
     * @return 약관 타입 리스트
     */
    @PostMapping("/getConsentTypeList")
    public List<Map<String, Object>> getConsentTypeList(@RequestBody Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        return consentService.getConsentTypeList(payload);
    }

    /*
     * 1. 메소드명: getCountryList
     * 2. 클래스명: ConsentController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관에 따른 국가 리스트를 반환
     * 2. 사용법
     *    POST 요청으로 국가 리스트를 조회
     * 3. 예시 데이터
     *    - Input: 요청 데이터(payload)
     *    - Output: 국가 리스트
     * </PRE>
     * @param payload 요청 데이터
     * @param request HTTP 요청 객체
     * @param session 세션 객체
     * @param model 모델 객체
     * @return 국가 리스트
     */
    @PostMapping("/getCountryList")
    public List<Map<String, Object>> getCountryList(@RequestBody Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        return consentService.getCountryList(payload);
    }

    /*
     * 1. 메소드명: getLanguageList
     * 2. 클래스명: ConsentController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관에 따른 언어 리스트를 반환
     * 2. 사용법
     *    POST 요청으로 언어 리스트를 조회
     * 3. 예시 데이터
     *    - Input: 요청 데이터(payload)
     *    - Output: 언어 리스트
     * </PRE>
     * @param payload 요청 데이터
     * @param request HTTP 요청 객체
     * @param session 세션 객체
     * @param model 모델 객체
     * @return 언어 리스트
     */
    @PostMapping("/getLanguageList")
    public List<String> getLanguageList(@RequestBody Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        return consentService.getLanguageList(payload);
    }

    /*
     * 1. 메소드명: getVersionList
     * 2. 클래스명: ConsentController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관에 따른 버전 리스트를 반환
     * 2. 사용법
     *    POST 요청으로 버전 리스트를 조회
     * 3. 예시 데이터
     *    - Input: 요청 데이터(payload)
     *    - Output: 버전 리스트
     * </PRE>
     * @param payload 요청 데이터
     * @param request HTTP 요청 객체
     * @param session 세션 객체
     * @param model 모델 객체
     * @return 버전 리스트
     */
    @PostMapping("/getVersionList")
    public List<Map<String, Object>> getVersionList(@RequestBody Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        return consentService.getVersionList(payload);
    }

    /*
     * 1. 메소드명: getConsentContent
     * 2. 클래스명: ConsentController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    특정 약관의 내용을 반환
     * 2. 사용법
     *    POST 요청으로 약관 내용을 조회
     * 3. 예시 데이터
     *    - Input: 요청 데이터(payload)
     *    - Output: 약관 내용
     * </PRE>
     * @param payload 요청 데이터
     * @param request HTTP 요청 객체
     * @param session 세션 객체
     * @param model 모델 객체
     * @return 약관 내용이 포함된 맵
     */
    @PostMapping("/getConsentContent")
    public Map<String,String> getConsentContent(@RequestBody Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        Map<String,String> param = new HashMap<String,String>();
        param.put("content", consentService.getConsentContent(payload));
        return param;
    }
}