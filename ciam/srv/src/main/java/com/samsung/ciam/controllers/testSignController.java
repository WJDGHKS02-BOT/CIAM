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

@Controller
@RequestMapping("/test/sign-in")
public class testSignController {

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


