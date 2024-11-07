package com.samsung.ciam.controllers;

import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Locale;
import java.util.Map;

/**
 * 1. 파일명   : LoginController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : 로그인 및 비밀번호 관리와 관련된 기능을 제공하는 컨트롤러
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
public class LoginController {

  /*
   * 1. 메소드명: addLocaleToModel
   * 2. 클래스명: LoginController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 09. 10.
   */
  /**
   * <PRE>
   * 1. 설명
   *    모델에 사용자 언어(Locale) 정보를 추가
   * 2. 사용법
   *    모든 요청에 대해 자동으로 호출되어 모델에 Locale 정보를 설정
   * </PRE>
   * @param request 요청 객체로 클라이언트의 Locale 정보 포함
   * @param model 모델 객체로, Locale 정보를 추가하는 데 사용
   */
  @ModelAttribute
  public void addLocaleToModel(HttpServletRequest request, Model model) {
    Locale locale = request.getLocale(); // 브라우저의 locale 설정
    String language = locale.toString();  // 기본적으로 en_US 또는 ko_KR 등의 형식일 수 있음

    if (language.contains("_")) {
      language = language.split("_")[0];  // 언더스코어(_)로 분리하고 첫 번째 값만 사용
    }
    model.addAttribute("langLocale", language); // model에 locale 정보를 추가
  }

  /*
   * 1. 메소드명: rootUrl
   * 2. 클래스명: LoginController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 09. 10.
   */
  /**
   * <PRE>
   * 1. 설명
   *    루트 URL 요청 시 기본 페이지로 리다이렉트
   * 2. 사용법
   *    '/' 경로로 접근 시 호출되어 개인 정보 페이지로 이동
   * </PRE>
   * @return 개인 정보 페이지로 리다이렉트
   */
  @GetMapping("/")
  public RedirectView rootUrl() {
    return new RedirectView("/myPage/personalInformation");
  }

  /*
   * 1. 메소드명: loginError
   * 2. 클래스명: LoginController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 09. 10.
   */
  /**
   * <PRE>
   * 1. 설명
   *    로그인 실패 시 오류 페이지를 반환
   * 2. 사용법
   *    '/login-error' 경로로 접근 시 오류 정보를 모델에 추가하고, 오류 페이지로 이동
   * </PRE>
   * @param apiKey 필수 API 키
   * @param regToken (선택) 등록 토큰
   * @param convertLoginId (선택) 로그인 ID 변환 정보
   * @param newADLogin (선택) AD 로그인 상태
   * @param model 모델 객체
   * @return ModelAndView 오류 페이지로 이동
   */
  @GetMapping("/login-error")
  public ModelAndView loginError(
      @RequestParam String apiKey,
      @RequestParam(required = false) String regToken,
      @RequestParam(required = false) String convertLoginId,
      @RequestParam(required = false) String newADLogin,
      Model model) {

    String content = "fragments/loginErrorContent";

    // 데이터를 모델에 추가하고 뷰를 반환합니다.
    ModelAndView modelAndView = new ModelAndView("registration");
    modelAndView.addObject("content", content);
    // 모델에 값 추가
    model.addAttribute("apiKey", apiKey);
    model.addAttribute("regToken", regToken);

    if (convertLoginId != null) {
      model.addAttribute("convertLoginId", convertLoginId);
    }

    if (newADLogin != null) {
      model.addAttribute("newADLogin", newADLogin);
    }

    return modelAndView;
  }

  /*
   * 1. 메소드명: newSignIn
   * 2. 클래스명: LoginController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 09. 10.
   */
  /**
   * <PRE>
   * 1. 설명
   *    로그인 페이지를 렌더링하고 필요한 URL 정보를 설정
   * 2. 사용법
   *    '/sign-in' 경로로 접근 시 로그인 페이지 렌더링
   * </PRE>
   * @param request 요청 객체
   * @param params 요청 파라미터
   * @param model 모델 객체
   * @return 로그인 페이지로 이동
   */
  @GetMapping("/sign-in")
  public String newSignIn(HttpServletRequest request,
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

    String loginURL = hostURL + "/sign-in?channel=" + channel + "&spName=" + spName;
    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;

    model.addAttribute("channel", channel);
    model.addAttribute("loginURL", loginURL);
    model.addAttribute("logoutURL", logoutURL);
    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
    model.addAttribute("parentKeys", BeansUtil.getAllApiKeyForChannel("btp"));
    model.addAttribute("hostUrl", BeansUtil.getHostURL());
    model.addAttribute("samsungInstanceURL", BeansUtil.getSamsungInstanceURL());
    model.addAttribute("gigyaInstanceURL", BeansUtil.getGigyaInstanceURL());

    return "_pages/login/sign-in";
  }

  /*
   * 1. 메소드명: newForgetPassword
   * 2. 클래스명: LoginController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 09. 10.
   */
  /**
   * <PRE>
   * 1. 설명
   *    비밀번호 찾기 페이지를 렌더링
   * 2. 사용법
   *    '/sign-in/forgot-password' 경로로 접근 시 호출
   * </PRE>
   * @param request 요청 객체
   * @param params 요청 파라미터
   * @param model 모델 객체
   * @return 비밀번호 찾기 페이지로 이동
   */
  @GetMapping("/sign-in/forgot-password")
  public String newForgetPassword(HttpServletRequest request,
                                  @RequestParam Map<String, String> params,
                                  Model model
  ) {
    // 요청 URL 정보를 설정
    String scheme = request.getScheme(); // http 또는 https
    String serverName = request.getServerName(); // localhost 또는 도메인명
    int serverPort = request.getServerPort(); // 8080 같은 포트 번호

    String hostURL = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);

    String samlContext = params.get("samlContext");
    String spName = params.get("spName");
    String channel = params.get("channel");

    String loginURL = hostURL + "/sign-in?channel=" + channel + "&spName=" + spName;
    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;

    model.addAttribute("channel", channel);
    model.addAttribute("loginURL", loginURL);
    model.addAttribute("logoutURL", logoutURL);
    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
    model.addAttribute("parentKeys", BeansUtil.getAllApiKeyForChannel("btp"));
    model.addAttribute("hostUrl", BeansUtil.getHostURL());
    model.addAttribute("samsungInstanceURL", BeansUtil.getSamsungInstanceURL());
    model.addAttribute("gigyaInstanceURL", BeansUtil.getGigyaInstanceURL());

    return "_pages/login/forgot-password";
  }

  /*
   * 1. 메소드명: newForgetPasswordSuccess
   * 2. 클래스명: LoginController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 09. 10.
   */
  /**
   * <PRE>
   * 1. 설명
   *    비밀번호 재설정 성공 페이지로 이동
   * 2. 사용법
   *    '/sign-in/reset-password/success' 경로로 접근 시 호출
   * </PRE>
   * @param request 요청 객체
   * @param params 요청 파라미터
   * @param model 모델 객체
   * @return 비밀번호 재설정 성공 페이지로 이동
   */
  @GetMapping("/sign-in/forgot-password/success")
  public String newForgetPasswordSuccess(HttpServletRequest request,
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

    String loginURL = hostURL + "/sign-in?channel=" + channel + "&spName=" + spName;
    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;

    model.addAttribute("channel", channel);
    model.addAttribute("loginURL", loginURL);
    model.addAttribute("logoutURL", logoutURL);
    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
    model.addAttribute("parentKeys", BeansUtil.getAllApiKeyForChannel("btp"));
    model.addAttribute("hostUrl", BeansUtil.getHostURL());
    model.addAttribute("samsungInstanceURL", BeansUtil.getSamsungInstanceURL());
    model.addAttribute("gigyaInstanceURL", BeansUtil.getGigyaInstanceURL());

    return "_pages/login/forgot-password-success";
  }

  /*
   * 1. 메소드명: newResetPassword
   * 2. 클래스명: LoginController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    비밀번호 재설정 페이지로 이동
   * 2. 사용법
   *    '/sign-in/reset-password' 경로로 접근 시 호출
   * </PRE>
   * @param request 요청 객체
   * @param params 요청 파라미터
   * @param model 모델 객체
   * @return 비밀번호 재설정 페이지로 이동
   */
  @GetMapping("/sign-in/reset-password")
  public String newResetPassword(HttpServletRequest request,
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

    String loginURL = hostURL + "/sign-in?channel=" + channel + "&spName=" + spName;
    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;

    model.addAttribute("channel", channel);
    model.addAttribute("loginURL", loginURL);
    model.addAttribute("logoutURL", logoutURL);
    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
    model.addAttribute("parentKeys", BeansUtil.getAllApiKeyForChannel("btp"));
    model.addAttribute("hostUrl", BeansUtil.getHostURL());
    model.addAttribute("samsungInstanceURL", BeansUtil.getSamsungInstanceURL());
    model.addAttribute("gigyaInstanceURL", BeansUtil.getGigyaInstanceURL());

    return "_pages/login/reset-password";
  }

  /*
   * 1. 메소드명: newResetPasswordSuccess
   * 2. 클래스명: LoginController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    비밀번호 재설정 성공 페이지로 이동
   * 2. 사용법
   *    '/sign-in/reset-password/success' 경로로 접근 시 호출
   * </PRE>
   * @param request 요청 객체
   * @param params 요청 파라미터
   * @param model 모델 객체
   * @return 비밀번호 재설정 성공 페이지로 이동
   */
  @GetMapping("/sign-in/reset-password/success")
  public String newResetPasswordSuccess(HttpServletRequest request,
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
    String channelName = BeansUtil.findParentKeyByValue(params.get("apiKey"));

    String loginURL = hostURL + "/sign-in?channel=" + channel + "&spName=" + spName;
    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;

    model.addAttribute("channel", channel);
    model.addAttribute("channelName", channelName);
    model.addAttribute("loginURL", loginURL);
    model.addAttribute("logoutURL", logoutURL);
    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
    model.addAttribute("parentKeys", BeansUtil.getAllApiKeyForChannel("btp"));
    model.addAttribute("redirectChannelURL", BeansUtil.getRedirectChannelLoginPageURL(channelName));
    model.addAttribute("hostUrl", BeansUtil.getHostURL());
    model.addAttribute("samsungInstanceURL", BeansUtil.getSamsungInstanceURL());
    model.addAttribute("gigyaInstanceURL", BeansUtil.getGigyaInstanceURL());

    return "_pages/login/reset-password-success";
  }

  /*
   * 1. 메소드명: tfaEmail
   * 2. 클래스명: LoginController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    MFA 2단계 인증 이메일 페이지로 이동
   * 2. 사용법
   *    '/sign-in/tfa/email' 경로로 접근 시 호출
   * </PRE>
   * @param request 요청 객체
   * @param params 요청 파라미터
   * @param model 모델 객체
   * @return 2단계 인증 이메일 페이지로 이동
   */
  @GetMapping("/sign-in/tfa/email")
  public String tfaEmail(HttpServletRequest request,
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

    String loginURL = hostURL + "/sign-in?channel=" + channel + "&spName=" + spName;
    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;

    model.addAttribute("channel", channel);
    model.addAttribute("loginURL", loginURL);
    model.addAttribute("logoutURL", logoutURL);
    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
    model.addAttribute("parentKeys", BeansUtil.getAllApiKeyForChannel("btp"));
    model.addAttribute("hostUrl", BeansUtil.getHostURL());
    model.addAttribute("samsungInstanceURL", BeansUtil.getSamsungInstanceURL());
    model.addAttribute("gigyaInstanceURL", BeansUtil.getGigyaInstanceURL());

    return "_pages/login/tfa-email";
  }

  /*
   * 1. 메소드명: tfaOtp
   * 2. 클래스명: LoginController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    MFA 2단계 인증 OTP 페이지로 이동
   * 2. 사용법
   *    '/sign-in/tfa/otp' 경로로 접근 시 호출
   * </PRE>
   * @param request 요청 객체
   * @param params 요청 파라미터
   * @param model 모델 객체
   * @return 2단계 인증 OTP 페이지로 이동
   */
  @GetMapping("/sign-in/tfa/otp")
  public String tfaOtp(HttpServletRequest request,
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

    String loginURL = hostURL + "/sign-in?channel=" + channel + "&spName=" + spName;
    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;

    model.addAttribute("channel", channel);
    model.addAttribute("loginURL", loginURL);
    model.addAttribute("logoutURL", logoutURL);
    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
    model.addAttribute("parentKeys", BeansUtil.getAllApiKeyForChannel("btp"));
    model.addAttribute("hostUrl", BeansUtil.getHostURL());
    model.addAttribute("samsungInstanceURL", BeansUtil.getSamsungInstanceURL());
    model.addAttribute("gigyaInstanceURL", BeansUtil.getGigyaInstanceURL());

    return "_pages/login/tfa-otp";
  }

  /*
   * 1. 메소드명: loginProxy
   * 2. 클래스명: LoginController
   * 3. 작성자명: 임준혁
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    로그인 프록시를 위한 페이지로 이동
   * 2. 사용법
   *    '/login-proxy' 경로로 접근 시 호출
   * </PRE>
   * @param request 요청 객체
   * @param params 요청 파라미터
   * @param model 모델 객체
   * @return 프록시 로그인 페이지로 이동
   */
  @GetMapping("/login-proxy")
  public String loginProxy(HttpServletRequest request,
                           @RequestParam Map<String, String> params,
                           Model model
  ) {
    String scheme = request.getScheme(); // http 또는 https
    String serverName = request.getServerName(); // localhost 또는 도메인명
    int serverPort = request.getServerPort(); // 8080 같은 포트 번호
    String hostURL = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);

    String channel = params.get("channel");
    String spName = params.get("spName");

    String loginURL = hostURL + "/sign-in?channel=" + channel + "&spName=" + spName;
    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;

    model.addAttribute("channel", channel);
    model.addAttribute("loginURL", loginURL);
    model.addAttribute("logoutURL", logoutURL);
    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
    model.addAttribute("parentKeys", BeansUtil.getAllApiKeyForChannel("btp"));
    model.addAttribute("hostUrl", BeansUtil.getHostURL());
    model.addAttribute("samsungInstanceURL", BeansUtil.getSamsungInstanceURL());
    model.addAttribute("gigyaInstanceURL", BeansUtil.getGigyaInstanceURL());

    return "_pages/login/login-proxy";
  }

  /*
   * 1. 메소드명: approvalStatusError
   * 2. 클래스명: LoginController
   * 3. 작성자명: 임준혁
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    승인 상태 오류 페이지로 이동
   * 2. 사용법
   *    '/approval-status-error' 경로로 접근 시 호출
   * </PRE>
   * @return 승인 상태 오류 페이지로 이동
   */
  @GetMapping("/approval-status-error")
  public String approvalStatusError() {
    return "_pages/login/approval-status-error";
  }
}
