package com.samsung.ciam.controllers;

import com.samsung.ciam.services.EntryPointsService;
import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Controller
public class EntryConsentController {

    @Autowired
    private EntryPointsService entryPointsService;

    @PostMapping("/consent-update/{key}")
    public ModelAndView newConsent(@PathVariable("key") String key,
                                   @RequestParam("uid") String uid,
                                   HttpSession session) {
        // 파라미터와 세션을 사용하여 처리 로직 수행
        return entryPointsService.newConsent(uid, key, session);
    }

    @ResponseBody
    @PostMapping("/new-consent/consentVersionCheck")
    public String consentVersionCheck(@RequestBody Map<String, String> payload, HttpSession session, RedirectAttributes redirectAttributes) {
        return entryPointsService.consentVersionCheck(payload,session,redirectAttributes);
    }

    @PostMapping("/new-consent/consentSubmit")
    public RedirectView consentSubmit(@RequestParam(value = "channel") String channel,
                                      @RequestParam Map<String, String> payload,
                                      HttpSession session,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {
        return entryPointsService.newConsentSubmit(payload, channel, session,redirectAttributes,request);
    }

    @GetMapping("/new-consent/newConsentComplete")
    public ModelAndView consentUpdatedThankYou(@RequestParam("param") String param) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/newconsent/consentUpdatedThankYou";

        modelAndView.addObject("content", content);

        return modelAndView;
    }

    @GetMapping("/consent-view")
    public String consentView(HttpServletRequest request,
                              @RequestParam Map<String, String> params,
                              Model model
    ) {
        // http://localhost:8080 부분만 가져오는 방법
        String scheme = request.getScheme(); // http 또는 https
        String serverName = request.getServerName(); // localhost 또는 도메인명
        int serverPort = request.getServerPort(); // 8080 같은 포트 번호

        String hostURL = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);

        String samlContext = params.get("samlContext");
        String spName = params.get("spName");
        String channel = params.get("channel");

        String loginURL = hostURL + "/sign-in?channel=" + channel;
        String logoutURL = hostURL + "/signin/" + channel + "/logout?spName=" + spName;

        model.addAttribute("channel", channel);
        model.addAttribute("loginURL", loginURL);
        model.addAttribute("logoutURL", logoutURL);
        model.addAttribute("apiKey", BeansUtil.getApiKeyForChannel(channel));

        return "_pages/consent-view/consent-view";
    }

}
