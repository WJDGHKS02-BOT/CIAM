package com.samsung.ciam.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.samsung.ciam.services.CdcTraitService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Slf4j
@RestController
public class CdcApiController {

    @Autowired
    private CdcTraitService cdcTraitService;

    @ResponseBody
    @PostMapping("/getAccountInfo")
    public JsonNode getAccountInfo(@RequestBody Map<String, String> payload, HttpSession session, RedirectAttributes redirectAttributes) {
        JsonNode jsonNode =  cdcTraitService.getCdcUser(payload.get("uid"),0);

        return jsonNode;
    }
}
