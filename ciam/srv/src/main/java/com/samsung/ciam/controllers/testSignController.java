package com.samsung.ciam.controllers;

import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 1. 파일명   : testSignController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : 테스트용 로그인 및 이중 인증 페이지 처리를 위한 컨트롤러
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
@RequestMapping("/test/sign-in")
public class testSignController {

  /*
   * 1. 메소드명: signIn
   * 2. 클래스명: testSignController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    테스트용 로그인 페이지로 이동하며 필요한 URL 및 채널 정보를 모델에 설정
   * 2. 사용법
   *    GET 요청으로 채널 정보와 함께 로그인 페이지로 이동
   * 3. 예시 데이터
   *    - Input: URL에 포함된 채널 정보 및 추가 매개변수
   *    - Output: 로그인 페이지 뷰 이름을 반환
   * </PRE>
   * @param channel 채널 정보
   * @param servletRequest 서블릿 요청 객체
   * @param request HTTP 요청 객체
   * @param params 요청 파라미터
   * @param model 모델 객체
   * @return 로그인 페이지 뷰 이름
   */
  @GetMapping("/{channel}")
  public String signIn(
      @PathVariable String channel,
      ServletRequest servletRequest,
      HttpServletRequest request,
      @RequestParam Map<String, String> params,
      Model model
  ) {
    String fullURL = request.getRequestURL().toString();
    String hostURL = fullURL.split("signin")[0];

    String spName = params.get("spName");

    String loginURL = hostURL + "signin/" + channel + "/login?spName=" + spName;
    String logoutURL = hostURL + "signin/" + channel + "/logout?spName=" + spName;

    model.addAttribute("url", hostURL);
    model.addAttribute("channel", channel);
    model.addAttribute("loginURL", loginURL);
    model.addAttribute("logoutURL", logoutURL);
    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));

    return "@pages/login/signIn";
  }

  /*
   * 1. 메소드명: email
   * 2. 클래스명: testSignController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    이메일을 통한 MFA 인증 페이지로 이동하며 필요한 URL 및 채널 정보를 모델에 설정
   * 2. 사용법
   *    GET 요청으로 채널 정보와 함께 이중 인증 페이지로 이동
   * 3. 예시 데이터
   *    - Input: URL에 포함된 채널 정보 및 추가 매개변수
   *    - Output: 이메일 이중 인증 페이지 뷰 이름을 반환
   * </PRE>
   * @param channel 채널 정보
   * @param servletRequest 서블릿 요청 객체
   * @param request HTTP 요청 객체
   * @param params 요청 파라미터
   * @param model 모델 객체
   * @return 이메일 이중 인증 페이지 뷰 이름
   */
  @GetMapping("/{channel}/two-factor-auth/email")
  public String email(
      @PathVariable String channel,
      ServletRequest servletRequest,
      HttpServletRequest request,
      @RequestParam Map<String, String> params,
      Model model
  ) {
    String fullURL = request.getRequestURL().toString();
    String hostURL = fullURL.split("signin")[0];

    String spName = params.get("spName");

    String loginURL = hostURL + "signin/" + channel + "/login?spName=" + spName;
    String logoutURL = hostURL + "signin/" + channel + "/logout?spName=" + spName;

    model.addAttribute("url", hostURL);
    model.addAttribute("channel", channel);
    model.addAttribute("loginURL", loginURL);
    model.addAttribute("logoutURL", logoutURL);
    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));

    return "@pages/login/email";
  }


}


