package com.samsung.ciam.controllers;

import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@Profile({"local", "dev"})
@RequestMapping("/converttest")
public class ConvertTestController {

    @GetMapping("/{channel}")
    public ModelAndView convert(
            @PathVariable String channel,
            ServletRequest servletRequest,
            HttpServletRequest request,
            @RequestParam Map<String, String> params,
            Model model
    ) {

        ModelAndView modelAndView = new ModelAndView("converttest");
        modelAndView.addObject("channel", channel);

        return modelAndView;
    }

    @PostMapping("/{channel}")
    public ModelAndView convert(@PathVariable String channel, @RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        ModelAndView modelAndView = new ModelAndView("convert");
        modelAndView.addObject("firstName",payload.get("firstName"));
        modelAndView.addObject("lastName",payload.get("lastName"));
        modelAndView.addObject("loginID",payload.get("loginID"));
        modelAndView.addObject("cmdmAccountID",payload.get("cmdmAccountID"));
        modelAndView.addObject("department",payload.get("department"));
        modelAndView.addObject("userPhone",payload.get("userPhone"));
        modelAndView.addObject("languages",payload.get("languages"));
        modelAndView.addObject("countryCode",payload.get("countryCode"));
        modelAndView.addObject("channelType",payload.get("channelType"));
        modelAndView.addObject("industryType",payload.get("industryType"));
        modelAndView.addObject("gbm1",payload.get("gbm1"));
        modelAndView.addObject("gbm2",payload.get("gbm2"));
        modelAndView.addObject("gbm3",payload.get("gbm3"));
        modelAndView.addObject("gbm4",payload.get("gbm4"));
        modelAndView.addObject("channelUID",payload.get("channelUID"));
        modelAndView.addObject("subsidiaryName",payload.get("subsidiaryName"));
        modelAndView.addObject("userType",payload.get("userType"));
        return modelAndView;

    }

}
