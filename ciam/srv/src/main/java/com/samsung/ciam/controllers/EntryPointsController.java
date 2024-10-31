package com.samsung.ciam.controllers;

import com.samsung.ciam.services.EntryPointsService;
import com.samsung.ciam.utils.BeansUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Controller
@RequestMapping("/new-channel")
public class EntryPointsController {

    @Autowired
    private EntryPointsService entryPointsService;

    @GetMapping("/register/{key}")
    public String newChannelEntryPoint(@PathVariable("key") String param, Model model) {
        model.addAttribute("channel", param);
        return "samsungAdRegistration/newChannelEntryPoint";
    }

    @PostMapping("/register/{key}")
    public String newChannelEntryPointSubmit(@PathVariable("key") String param,
                                             @RequestParam("regToken") String regToken,
                                             @RequestParam(value = "convertLoginId", required = false) String convertLoginId,
                                             @RequestParam(value = "adCdcUid", required = false) String adCdcUid,
                                             @RequestParam(value = "convertUid", required = false) String convertUid,
                                             HttpSession session,
                                             RedirectAttributes redirectAttributes) {
        // 세션에 값 저장
        session.setAttribute("newChannel", param);
        session.setAttribute("regToken", regToken);

        if (convertLoginId != null) {
            session.setAttribute("convertLoginId", convertLoginId);
        }

        if (adCdcUid != null) {
            session.setAttribute("adCdcUid", adCdcUid);
        }

        if (convertUid != null && !convertUid.isEmpty()) {
            session.setAttribute("convertUid", convertUid);
        }

        // 다음 경로로 리다이렉트
        redirectAttributes.addFlashAttribute("channel", param);
        return "redirect:/new-channel/account/" + param;
    }

    @GetMapping("/account/{key}")
    public ModelAndView newChannelAccount(@PathVariable String key, HttpSession session, Model model) {
        return entryPointsService.newChannelAccount(key, session);
    }

    @PostMapping("/account/{param}")
    public RedirectView newChannelAccountSubmit(@PathVariable String param,
                                                @RequestParam Map<String, String> requestParams,
                                                HttpSession session,
                                                HttpServletRequest request,
                                                RedirectAttributes redirectAttributes) {
        return entryPointsService.newChannelAccountSubmit(requestParams, param, session, request, redirectAttributes);
    }
    @GetMapping("/consent")
    public ModelAndView consent(@RequestParam("param") String param, HttpSession session) {
        return entryPointsService.consent(param, session);
    }

    @PostMapping("/consentSubmit")
    public RedirectView consentSubmit(@RequestParam(value = "channel") String channel,
                                      @RequestParam Map<String, String> payload,
                                      HttpSession session,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {
        return entryPointsService.consentSubmit(payload, channel, session,redirectAttributes,request);
    }

    @GetMapping("/mfa")
    public ModelAndView mfa(@RequestParam("param") String param, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("registration");
        String content = "fragments/samsungAdRegistration/multiFactorAuthentication";
        modelAndView.addObject("content", content);
        modelAndView.addObject("channel", param);

        return modelAndView;
    }

    @PostMapping("/mfaSubmit")
    public RedirectView mfaSubmit(@RequestParam(value = "channel") String param,
                                  @RequestParam Map<String, String> payload,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        return entryPointsService.mfaSubmit(param, request, payload,redirectAttributes);
    }

    @PostMapping("/ssoAccess")
    public RedirectView ssoAccess(@RequestParam("targetChannel") String targetChannel,
                                        @RequestParam("cdc_uid") String cdcUid, // Receive `cdc_uid` as a parameter
                                        HttpSession session) {
        return entryPointsService.ssoAccess(targetChannel, cdcUid, session);
    }


//    @GetMapping("/registerComplete")
//    public ModelAndView registerComplete(@RequestParam("param") String param, @RequestParam Map<String, Object> allParams, HttpServletRequest request,HttpSession session,Model model) {
//        model.addAttribute("loginPage", BeansUtil.getLoginPageForChannel(param));
//        return entryPointsService.signupComplete(param, allParams, request,session);
//    }


}