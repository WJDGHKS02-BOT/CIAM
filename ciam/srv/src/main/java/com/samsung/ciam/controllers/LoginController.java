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

@Controller
public class LoginController {

  @ModelAttribute
  public void addLocaleToModel(HttpServletRequest request, Model model) {
    Locale locale = request.getLocale(); // 브라우저의 locale 설정
    String language = locale.toString();  // 기본적으로 en_US 또는 ko_KR 등의 형식일 수 있음

    if (language.contains("_")) {
      language = language.split("_")[0];  // 언더스코어(_)로 분리하고 첫 번째 값만 사용
    }
    model.addAttribute("langLocale", language); // model에 locale 정보를 추가
  }

  @GetMapping("/")
  public RedirectView rootUrl() {
    return new RedirectView("/myPage/personalInformation");
  }

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

  @GetMapping("/sign-in/forgot-password")
  public String newForgetPassword(HttpServletRequest request,
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

    return "_pages/login/forgot-password";
  }

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

  @GetMapping("/approval-status-error")
  public String approvalStatusError() {
    return "_pages/login/approval-status-error";
  }
}
