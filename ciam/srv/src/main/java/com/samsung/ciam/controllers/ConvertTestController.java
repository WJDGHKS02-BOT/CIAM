package com.samsung.ciam.controllers;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * 1. 파일명   : ConvertTestController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : 채널 전환 테스트 페이지로 사용자가 입력한 값을 확인할 수 있도록 하는 컨트롤러
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
@Profile({"local", "dev"})
@RequestMapping("/converttest")
public class ConvertTestController {

    /*
     * 1. 메소드명: convert
     * 2. 클래스명: ConvertTestController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    GET 요청으로 채널 전환 테스트 페이지를 호출하며, 해당 채널명을 뷰에 전달
     * 2. 사용법
     *    특정 채널을 선택하여 전환 테스트 페이지로 이동
     * 3. 예시 데이터
     *    - Input: 채널 이름
     *    - Output: 전환 테스트 페이지로 이동
     * </PRE>
     * @param channel 채널 이름
     * @param servletRequest 서블릿 요청 객체
     * @param request HTTP 요청 객체
     * @param params 요청 파라미터 맵
     * @param model 모델 객체
     * @return ModelAndView 전환 테스트 페이지 뷰
     */
    @GetMapping("/{channel}")
    public ModelAndView convert(
            @PathVariable String channel,
            ServletRequest servletRequest,
            HttpServletRequest request,
            @RequestParam Map<String, String> params,
            Model model
    ) {

        ModelAndView modelAndView = new ModelAndView("converttest");
        modelAndView.addObject("channel", channel);

        return modelAndView;
    }

    /*
     * 1. 메소드명: convert
     * 2. 클래스명: ConvertTestController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    POST 요청으로 전환 테스트 페이지에서 사용자 정보를 확인하고, 해당 정보를 뷰에 전달
     * 2. 사용법
     *    채널 전환 테스트 페이지에서 입력한 사용자 정보를 전송하면 뷰로 전달
     * 3. 예시 데이터
     *    - Input: 사용자 정보 맵
     *    - Output: 사용자 정보가 포함된 변환 확인 페이지
     * </PRE>
     * @param channel 채널 이름
     * @param payload 사용자 정보가 담긴 맵
     * @param session 세션 객체
     * @param request HTTP 요청 객체
     * @param redirectAttributes 리디렉션 속성 객체
     * @return ModelAndView 사용자 정보가 담긴 뷰 페이지
     */
    @PostMapping("/{channel}")
    public ModelAndView convert(@PathVariable String channel, @RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        ModelAndView modelAndView = new ModelAndView("convert");
        modelAndView.addObject("firstName",payload.get("firstName"));
        modelAndView.addObject("lastName",payload.get("lastName"));
        modelAndView.addObject("loginID",payload.get("loginID"));
        modelAndView.addObject("cmdmAccountID",payload.get("cmdmAccountID"));
        modelAndView.addObject("department",payload.get("department"));
        modelAndView.addObject("userPhone",payload.get("userPhone"));
        modelAndView.addObject("languages",payload.get("languages"));
        modelAndView.addObject("countryCode",payload.get("countryCode"));
        modelAndView.addObject("channelType",payload.get("channelType"));
        modelAndView.addObject("industryType",payload.get("industryType"));
        modelAndView.addObject("gbm1",payload.get("gbm1"));
        modelAndView.addObject("gbm2",payload.get("gbm2"));
        modelAndView.addObject("gbm3",payload.get("gbm3"));
        modelAndView.addObject("gbm4",payload.get("gbm4"));
        modelAndView.addObject("channelUID",payload.get("channelUID"));
        modelAndView.addObject("subsidiaryName",payload.get("subsidiaryName"));
        modelAndView.addObject("userType",payload.get("userType"));
        return modelAndView;

    }

}
