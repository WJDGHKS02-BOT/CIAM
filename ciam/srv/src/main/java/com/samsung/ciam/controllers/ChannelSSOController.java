package com.samsung.ciam.controllers;

import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/signin")
public class ChannelSSOController {

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

    String loginURL = hostURL + "signin/" + channel + "/login?spName=" + spName;
    String logoutURL = hostURL + "signin/" + channel + "/logout?spName=" + spName;

    model.addAttribute("url", hostURL);
    model.addAttribute("channel", channel);
    model.addAttribute("loginURL", loginURL);
    model.addAttribute("logoutURL", logoutURL);
    model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));

    String content = "fragments/signin/proxy";
    model.addAttribute("content", content);

    return "registration";
  }

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

  @GetMapping("/{channel}/error")
  public String samlError(@PathVariable("channel") String channel, @RequestParam("error") String error, Model model) {

    model.addAttribute("channel", channel);
    model.addAttribute("error", error);
    model.addAttribute("loginPage", BeansUtil.getLoginPageForChannel(channel));
    return "channelError";
  }

  @GetMapping("/{channel}/saml-ad")
  public String channelSamlAd(Model model, @PathVariable String channel) {
    String cdcKey = BeansUtil.getApiKeyForChannel(channel);
    model.addAttribute("gigyaApiKey", cdcKey);
    return "channelSamlAd";
  }

  @GetMapping("/login-error")
  public String loginError(Model model, @PathVariable String channel) {
    String cdcKey = BeansUtil.getApiKeyForChannel(channel);
    model.addAttribute("gigyaApiKey", cdcKey);
    return "channelSamlAd";
  }

  @GetMapping("/{channel}/logout")
  public String logout(@PathVariable("channel") String channel, Model model) {
    model.addAttribute("redirectTo", BeansUtil.getLoginPageForChannel(channel));
    model.addAttribute("cdcKey", BeansUtil.getApiKeyForChannel(channel));
    return "channelLogout";
  }

  @GetMapping("/newChannelEntryPoint/{channel}")
  public String newChannelEntryPoint(@PathVariable String channel, Model model) {
    model.addAttribute("channel", channel);
    return "newChannelEntryPoint";
  }

  @PostMapping("/newChannelEntryPointSubmit/{channel}")
  public String newChannelEntryPointSubmit(@PathVariable String channel, @RequestParam String regToken, Model model) {
    // Handle form submission
    // ...
    model.addAttribute("channel", channel);
    model.addAttribute("regToken", regToken);
    return "resultPage"; // 결과를 보여줄 페이지
  }

  @GetMapping("/consentUpdate/{channel}")
  public String consentUpdate(@PathVariable String channel, Model model) {
    model.addAttribute("loginPage", BeansUtil.getLoginPageForChannel(channel));
    return "consentUpdatedThankYou";
  }
}