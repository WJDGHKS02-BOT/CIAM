package com.samsung.ciam.controllers;

import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 1. 파일명   : ChannelSSOController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : 채널 프록시, 로그인, 로그아웃 및 오류 처리 관련 SSO 기능을 관리하는 컨트롤러
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
@RequestMapping("/signin")
public class ChannelSSOController {

  /*
   * 1. 메소드명: proxy
   * 2. 클래스명: ChannelSSOController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    특정 채널의 프록시 페이지로 이동하여 API 키와 URL 정보를 모델에 설정
   * 2. 사용법
   *    /signin/{channel}/proxy 경로로 GET 요청 시 채널의 프록시 페이지로 이동
   * 3. 예시 데이터
   *    - Input: 채널 이름과 파라미터 정보
   *    - Output: "registration" 페이지에 필요한 정보 추가 후 이동
   * </PRE>
   * @param channel 채널 이름
   * @param servletRequest Servlet 요청 객체
   * @param request HTTP 요청 객체
   * @param params 요청 파라미터
   * @param model 모델 객체
   * @return 프록시 페이지 ("registration")
   */
  @GetMapping("/{channel}/proxy")
  public String proxy(
      @PathVariable String channel,
      ServletRequest servletRequest,
      HttpServletRequest request,
      @RequestParam Map<String, String> params,
      Model model
  ) {
    String fullURL = request.getRequestURL().toString();
    String hostURL = fullURL.split("signin")[0];


    String samlContext = params.get("samlContext");
    String spName = params.get("spName");

    //login url조회
    String loginURL = hostURL + "signin/" + channel + "/login?spName=" + spName;
    String logoutURL = hostURL + "signin/" + channel + "/logout?spName=" + spName;

    //model 전달
    model.addAttribute("url", hostURL);
    model.addAttribute("channel", channel);
    model.addAttribute("loginURL", loginURL);
    model.addAttribute("logoutURL", logoutURL);
    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));

    String content = "fragments/signin/proxy";
    model.addAttribute("content", content);

    return "registration";
  }

  /*
   * 1. 메소드명: login
   * 2. 클래스명: ChannelSSOController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    로그인 페이지로 이동하며 채널에 따라 API 키와 URL 설정을 포함
   * 2. 사용법
   *    /signin/{channel}/login 경로로 GET 요청 시 로그인 페이지로 이동
   * 3. 예시 데이터
   *    - Input: 채널 이름과 파라미터 정보
   *    - Output: "registration" 페이지에 필요한 로그인 API 설정 추가 후 이동
   * </PRE>
   * @param channel 채널 이름
   * @param servletRequest Servlet 요청 객체
   * @param params 요청 파라미터
   * @param model 모델 객체
   * @return 로그인 페이지 ("registration")
   */
  @GetMapping("/{channel}/login")
  public String login(
      @PathVariable String channel,
      ServletRequest servletRequest,
      @RequestParam Map<String, String> params,
      Model model
  ) {
    String apiKey = BeansUtil.getApiKeyForChannel(channel);

    String channelLoginAPIURL = "https://cdns.au1.gigya.com/js/gigya.js?apikey=";
    String btpLoginAPIURL = "https://cdns.gigya.com/js/gigya.js?apikey=";
    
    String loginAPISrc = "";

    Boolean isBTPLogin;

    // 로그인 API URL src 설정
    if ("btp".equals(channel)) {
      isBTPLogin = true;
      loginAPISrc = btpLoginAPIURL;
    } else {
      isBTPLogin = false;
      loginAPISrc = channelLoginAPIURL;
    }

    model.addAttribute("apiKey", apiKey);
    model.addAttribute("channel", channel);
    model.addAttribute("isBTPLogin", isBTPLogin);
    model.addAttribute("loginAPISrc", loginAPISrc);

    String content = "fragments/signin/login";
    String headScript = "fragments/signin/headScript";
    model.addAttribute("content", content);
    model.addAttribute("headScript", headScript);

    return "registration";
  }

  /*
   * 1. 메소드명: samlError
   * 2. 클래스명: ChannelSSOController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    SAML 인증 혹은 채널 로그인 관련 오류가 발생했을 때 오류 메시지를 표시하는 페이지로 이동
   * 2. 사용법
   *    /signin/{channel}/error 경로로 GET 요청 시 오류 페이지로 이동
   * 3. 예시 데이터
   *    - Input: 채널 이름과 오류 메시지
   *    - Output: 오류 페이지 ("channelError")
   * </PRE>
   * @param channel 채널 이름
   * @param error 오류 메시지
   * @param model 모델 객체
   * @return 오류 페이지 ("channelError")
   */
  @GetMapping("/{channel}/error")
  public String samlError(@PathVariable("channel") String channel, @RequestParam("error") String error, Model model) {

    model.addAttribute("channel", channel);
    model.addAttribute("error", error);
    model.addAttribute("loginPage", BeansUtil.getLoginPageForChannel(channel));
    return "channelError";
  }

  /*
   * 1. 메소드명: channelSamlAd
   * 2. 클래스명: ChannelSSOController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    특정 채널의 SAML AD 페이지로 이동하며 CDC API 키를 설정
   * 2. 사용법
   *    /signin/{channel}/saml-ad 경로로 GET 요청 시 SAML AD 페이지로 이동
   * 3. 예시 데이터
   *    - Input: 채널 이름
   *    - Output: SAML AD 페이지 ("channelSamlAd")와 CDC API 키 추가
   * </PRE>
   * @param model 모델 객체
   * @param channel 채널 이름
   * @return SAML AD 페이지 ("channelSamlAd")
   */
  @GetMapping("/{channel}/saml-ad")
  public String channelSamlAd(Model model, @PathVariable String channel) {
    String cdcKey = BeansUtil.getApiKeyForChannel(channel);
    model.addAttribute("gigyaApiKey", cdcKey);
    return "channelSamlAd";
  }

  /*
   * 1. 메소드명: loginError
   * 2. 클래스명: ChannelSSOController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    로그인 오류 시 SAML AD 페이지로 이동하며 CDC API 키를 설정
   * 2. 사용법
   *    /signin/login-error 경로로 GET 요청 시 SAML AD 페이지로 이동
   * 3. 예시 데이터
   *    - Input: 채널 이름
   *    - Output: SAML AD 페이지 ("channelSamlAd")와 CDC API 키 추가
   * </PRE>
   * @param model 모델 객체
   * @param channel 채널 이름
   * @return SAML AD 페이지 ("channelSamlAd")
   */
  @GetMapping("/login-error")
  public String loginError(Model model, @PathVariable String channel) {
    String cdcKey = BeansUtil.getApiKeyForChannel(channel);
    model.addAttribute("gigyaApiKey", cdcKey);
    return "channelSamlAd";
  }

  /*
   * 1. 메소드명: logout
   * 2. 클래스명: ChannelSSOController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    특정 채널의 로그아웃 페이지로 이동하며 로그아웃 후 리디렉션할 URL 설정
   * 2. 사용법
   *    /signin/{channel}/logout 경로로 GET 요청 시 로그아웃 페이지로 이동
   * 3. 예시 데이터
   *    - Input: 채널 이름
   *    - Output: 로그아웃 페이지 ("channelLogout")와 리디렉션 URL 추가
   * </PRE>
   * @param channel 채널 이름
   * @param model 모델 객체
   * @return 로그아웃 페이지 ("channelLogout")
   */
  @GetMapping("/{channel}/logout")
  public String logout(@PathVariable("channel") String channel, Model model) {
    model.addAttribute("redirectTo", BeansUtil.getLoginPageForChannel(channel));
    model.addAttribute("cdcKey", BeansUtil.getApiKeyForChannel(channel));
    return "channelLogout";
  }

  /*
   * 1. 메소드명: newChannelEntryPoint
   * 2. 클래스명: ChannelSSOController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    신규 채널 등록 페이지로 이동하며 채널 정보를 설정
   * 2. 사용법
   *    /signin/newChannelEntryPoint/{channel} 경로로 GET 요청 시 신규 채널 페이지로 이동
   * 3. 예시 데이터
   *    - Input: 채널 이름
   *    - Output: 신규 채널 페이지 ("newChannelEntryPoint")와 채널 정보 추가
   * </PRE>
   * @param channel 채널 이름
   * @param model 모델 객체
   * @return 신규 채널 페이지 ("newChannelEntryPoint")
   */
  @GetMapping("/newChannelEntryPoint/{channel}")
  public String newChannelEntryPoint(@PathVariable String channel, Model model) {
    model.addAttribute("channel", channel);
    return "newChannelEntryPoint";
  }

  /*
   * 1. 메소드명: newChannelEntryPointSubmit
   * 2. 클래스명: ChannelSSOController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    신규 채널 등록 폼 제출 시 채널과 등록 토큰을 처리
   * 2. 사용법
   *    /signin/newChannelEntryPointSubmit/{channel} 경로로 POST 요청 시 등록 폼 제출 처리
   * 3. 예시 데이터
   *    - Input: 채널 이름과 등록 토큰
   *    - Output: 결과 페이지 ("resultPage")와 채널 및 등록 토큰 정보 추가
   * </PRE>
   * @param channel 채널 이름
   * @param regToken 등록 토큰
   * @param model 모델 객체
   * @return 결과 페이지 ("resultPage")
   */
  @PostMapping("/newChannelEntryPointSubmit/{channel}")
  public String newChannelEntryPointSubmit(@PathVariable String channel, @RequestParam String regToken, Model model) {
    model.addAttribute("channel", channel);
    model.addAttribute("regToken", regToken);
    return "resultPage"; // 결과를 보여줄 페이지
  }

  /*
   * 1. 메소드명: consentUpdate
   * 2. 클래스명: ChannelSSOController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    약관 업데이트 완료 페이지로 이동하며 로그인 페이지 URL을 설정
   * 2. 사용법
   *    /signin/consentUpdate/{channel} 경로로 GET 요청 시 약관 업데이트 페이지로 이동
   * 3. 예시 데이터
   *    - Input: 채널 이름
   *    - Output: 약관 업데이트 완료 페이지 ("consentUpdatedThankYou")와 로그인 페이지 URL 추가
   * </PRE>
   * @param channel 채널 이름
   * @param model 모델 객체
   * @return 약관 업데이트 완료 페이지 ("consentUpdatedThankYou")
   */
  @GetMapping("/consentUpdate/{channel}")
  public String consentUpdate(@PathVariable String channel, Model model) {
    model.addAttribute("loginPage", BeansUtil.getLoginPageForChannel(channel));
    return "consentUpdatedThankYou";
  }
}