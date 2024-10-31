package com.samsung.ciam.controllers;

import com.samsung.ciam.utils.BeansUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChannelPasswordController {

  @GetMapping("resetpwd")
  public String resetpwd(Model model) {
    String cdcKey = BeansUtil.getApiKeyForChannel("default");
    // model.addAttribute("gigyaApiKey", cdcKey);
    model.addAttribute("apiKey", cdcKey);

    String content = "fragments/signin/portalResetpwd";
    String headScript = "fragments/signin/headScript";
    String loginAPISrc = "https://cdns.gigya.com/js/gigya.js?apikey=";

    Boolean isBTPLogin = true;

    model.addAttribute("content", content);
    model.addAttribute("headScript", headScript);
    model.addAttribute("loginAPISrc", loginAPISrc);
    model.addAttribute("isBTPLogin", isBTPLogin);

    return "registration";
  }
}
