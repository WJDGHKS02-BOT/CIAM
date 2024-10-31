package com.samsung.ciam.controllers;
import com.samsung.ciam.repositories.ChannelRepository;
import com.samsung.ciam.services.CdcTraitService;
import jakarta.persistence.Access;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Controller
@RequestMapping("/channel-profile")
public class ChannelProfileController {

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private ChannelRepository channelRepository;

    @GetMapping("/{channel}")
    public RedirectView convert(
            @PathVariable String channel,
            ServletRequest servletRequest,
            HttpServletRequest request,
            Model model,
            HttpSession session
    ) {

        String channelDisplayName = channelRepository.selectChannelDisplayName(channel);

        session.setAttribute("session_channel",channel);
        session.setAttribute("session_display_channel",channelDisplayName);

        cdcTraitService.setAdminSession(session);

        return new RedirectView("/myPage/personalInformation");
    }
}
