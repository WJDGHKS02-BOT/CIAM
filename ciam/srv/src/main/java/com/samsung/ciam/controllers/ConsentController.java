package com.samsung.ciam.controllers;

import com.samsung.ciam.models.Consent;
import com.samsung.ciam.models.ConsentContent;
import com.samsung.ciam.repositories.ChannelRepository;
import com.samsung.ciam.repositories.ConsentRepository;
import com.samsung.ciam.services.*;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;


@RestController
@RequestMapping("/consent")
public class ConsentController {

    @Autowired
    private ConsentService consentService;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private ConsentRepository consentRepository;
    
    @GetMapping("/{channel}")
    public ModelAndView consentsView(
        @PathVariable String channel,
        ServletRequest servletRequest,
        HttpServletRequest request,
        Model model,
        HttpSession session
    ) {

        ModelAndView modelAndView = new ModelAndView("fragments/consentView");

        // common code
        // channel (channels 테이블 > value : channel_name / name : channel_display_name)
        // modelAndView.addObject("channel", channelRepository.selectChannelTypeList(""));
        String channelDisplayName = channelRepository.selectChannelDisplayName(channel);
        modelAndView.addObject("session_channel",channel);
        modelAndView.addObject("session_display_channel",channelDisplayName);
        modelAndView.addObject("contentCnt", consentRepository.getContentCntByChannel(channel));
        return modelAndView;
    }
        // return consentService.consentsView(session);
    
    @PostMapping("/getConsentTypeList")
    public List<Map<String, Object>> getConsentTypeList(@RequestBody Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        return consentService.getConsentTypeList(payload);
    }
    
    @PostMapping("/getCountryList")
    public List<Map<String, Object>> getCountryList(@RequestBody Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        return consentService.getCountryList(payload);
    }
    
    @PostMapping("/getLanguageList")
    public List<String> getLanguageList(@RequestBody Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        return consentService.getLanguageList(payload);
    }
    
    @PostMapping("/getVersionList")
    public List<Map<String, Object>> getVersionList(@RequestBody Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        return consentService.getVersionList(payload);
    }
    
    @PostMapping("/getConsentContent")
    public Map<String,String> getConsentContent(@RequestBody Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        Map<String,String> param = new HashMap<String,String>();
        param.put("content", consentService.getConsentContent(payload));
        return param;
    }
}