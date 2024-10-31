package com.samsung.ciam.controllers;

import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@Profile({"local", "dev"})
public class DemoController {
  @GetMapping("/demo")
  public ModelAndView demo() {
    return new ModelAndView("welcome");
  }

  @GetMapping("/testconsent")
  public ModelAndView testconsent() {
    return new ModelAndView("testconsent");
  }

  @GetMapping("/testgetAccountInfo")
  public ModelAndView testgetAccountInfo() {
    return new ModelAndView("testgetAccountInfo");
  }

//  @GetMapping("/sign-in")
//  public String newSignIn(HttpServletRequest request,
//                          @RequestParam Map<String, String> params,
//                          Model model
//  ) {
//    String scheme = request.getScheme(); // http 또는 https
//    String serverName = request.getServerName(); // localhost 또는 도메인명
//    int serverPort = request.getServerPort(); // 8080 같은 포트 번호
//
//    String hostURL = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);
//
//    String samlContext = params.get("samlContext");
//    String spName = params.get("spName");
//    String channel = params.get("channel");
//
//    String loginURL = hostURL + "/sign-in?channel=" + channel;
//    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;
//
//    model.addAttribute("channel", channel);
//    model.addAttribute("loginURL", loginURL);
//    model.addAttribute("logoutURL", logoutURL);
//    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
//
//    return "_pages/login/sign-in";
//  }
//
//  @GetMapping("/sign-in/forgot-password")
//  public String newForgetPassword(HttpServletRequest request,
//                                  @RequestParam Map<String, String> params,
//                                  Model model
//  ) {
//    String scheme = request.getScheme(); // http 또는 https
//    String serverName = request.getServerName(); // localhost 또는 도메인명
//    int serverPort = request.getServerPort(); // 8080 같은 포트 번호
//
//    String hostURL = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);
//
//    String samlContext = params.get("samlContext");
//    String spName = params.get("spName");
//    String channel = params.get("channel");
//
//    String loginURL = hostURL + "/sign-in?channel=" + channel;
//    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;
//
//    model.addAttribute("channel", channel);
//    model.addAttribute("loginURL", loginURL);
//    model.addAttribute("logoutURL", logoutURL);
//    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
//
//    return "_pages/login/forgot-password";
//  }
//
//  @GetMapping("/sign-in/forgot-password/success")
//  public String newForgetPasswordSuccess(HttpServletRequest request,
//                                         @RequestParam Map<String, String> params,
//                                         Model model
//  ) {
//    String scheme = request.getScheme(); // http 또는 https
//    String serverName = request.getServerName(); // localhost 또는 도메인명
//    int serverPort = request.getServerPort(); // 8080 같은 포트 번호
//
//    String hostURL = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);
//
//    String samlContext = params.get("samlContext");
//    String spName = params.get("spName");
//    String channel = params.get("channel");
//
//    String loginURL = hostURL + "/sign-in?channel=" + channel;
//    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;
//
//    model.addAttribute("channel", channel);
//    model.addAttribute("loginURL", loginURL);
//    model.addAttribute("logoutURL", logoutURL);
//    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
//
//    return "_pages/login/forgot-password-success";
//  }
//
//  @GetMapping("/sign-in/tfa/email")
//  public String tfaEmail(HttpServletRequest request,
//                         @RequestParam Map<String, String> params,
//                         Model model
//  ) {
//    String scheme = request.getScheme(); // http 또는 https
//    String serverName = request.getServerName(); // localhost 또는 도메인명
//    int serverPort = request.getServerPort(); // 8080 같은 포트 번호
//
//    String hostURL = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);
//
//    String samlContext = params.get("samlContext");
//    String spName = params.get("spName");
//    String channel = params.get("channel");
//
//    String loginURL = hostURL + "/sign-in?channel=" + channel;
//    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;
//
//    model.addAttribute("channel", channel);
//    model.addAttribute("loginURL", loginURL);
//    model.addAttribute("logoutURL", logoutURL);
//    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
//
//    return "_pages/login/tfa-email";
//  }
//
//  @GetMapping("/sign-in/tfa/otp")
//  public String tfaOtp(HttpServletRequest request,
//                       @RequestParam Map<String, String> params,
//                       Model model
//  ) {
//    String scheme = request.getScheme(); // http 또는 https
//    String serverName = request.getServerName(); // localhost 또는 도메인명
//    int serverPort = request.getServerPort(); // 8080 같은 포트 번호
//
//    String hostURL = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);
//
//    String samlContext = params.get("samlContext");
//    String spName = params.get("spName");
//    String channel = params.get("channel");
//
//    String loginURL = hostURL + "/sign-in?channel=" + channel;
//    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;
//
//    model.addAttribute("channel", channel);
//    model.addAttribute("loginURL", loginURL);
//    model.addAttribute("logoutURL", logoutURL);
//    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
//
//    return "_pages/login/tfa-otp";
//  }
//
//  @GetMapping("/consent-view")
//  public String consentView(HttpServletRequest request,
//                            @RequestParam Map<String, String> params,
//                            Model model
//  ) {
//    // http://localhost:8080 부분만 가져오는 방법
//    String scheme = request.getScheme(); // http 또는 https
//    String serverName = request.getServerName(); // localhost 또는 도메인명
//    int serverPort = request.getServerPort(); // 8080 같은 포트 번호
//
//    String hostURL = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);
//
//    String samlContext = params.get("samlContext");
//    String spName = params.get("spName");
//    String channel = params.get("channel");
//
//    String loginURL = hostURL + "/sign-in?channel=" + channel;
//    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;
//
//    model.addAttribute("channel", channel);
//    model.addAttribute("loginURL", loginURL);
//    model.addAttribute("logoutURL", logoutURL);
//    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
//
//    return "_pages/consent-view/consent-view";
//  }
//
//  @GetMapping("/login-proxy")
//  public String loginProxy(HttpServletRequest request,
//                           @RequestParam Map<String, String> params,
//                           Model model
//  ) {
//    String scheme = request.getScheme(); // http 또는 https
//    String serverName = request.getServerName(); // localhost 또는 도메인명
//    int serverPort = request.getServerPort(); // 8080 같은 포트 번호
//    String hostURL = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);
//
//    String channel = params.get("channel");
//    String spName = params.get("spName");
//
//    String loginURL = hostURL + "/sign-in?channel=" + channel;
//    String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;
//
//    model.addAttribute("loginURL", loginURL);
//    model.addAttribute("logoutURL", logoutURL);
//    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));
//
//    return "_pages/login/login-proxy";
//  }
//
//  @GetMapping("/approval-status-error")
//  public String approvalStatusError() {
//    return "_pages/login/approval-status-error";
//  }
}