package com.samsung.ciam.controllers;

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.samsung.ciam.common.cpi.enums.CpiRequestFieldMapping;
import com.samsung.ciam.common.cpi.enums.CpiResponseFieldMapping;
import com.samsung.ciam.common.cpi.service.CpiApiService;
import com.samsung.ciam.services.CdcTraitService;
import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.samsung.ciam.utils.StringUtil;
import com.samsung.ciam.services.CdcCallService;
import com.samsung.ciam.services.EmpVerificationService;
import com.samsung.ciam.services.SearchService;
import com.samsung.ciam.models.BtpAccounts;
import com.samsung.ciam.models.ConsentContent;
import com.samsung.ciam.repositories.ConsentContentRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@RestController
@RequestMapping("/api/restapi/extension/sec")
public class CdcCallController {

    @Autowired
    private EmpVerificationService empVerificationService;

    @Autowired
    private CdcCallService cdcCallService;

    @Autowired
    private CpiApiService cpiApiService;

    @ResponseBody
    @GetMapping("/cdcInsertEmployData")
    public void employVerificationTableInsert(@RequestParam("param") String param) {
        //CDC에서 Data를 가져와서 DB에 넣는 API
        log.info("Received param: {}", param);
        if(param.equals("cdc_insert")) {
            log.info("action  employVerificationTableInsert");
            empVerificationService.employVerificationTableInsert();
        }
    }

    @ResponseBody
    @GetMapping("/expireEmployData")
    public void employVerificationExpire(@RequestParam("param") String param) {
        //재직인증 Expired date 가 되면 pending -> reject로 변경한다. 
        log.info("Received param: {}", param);
        if(param.equals("expire")) {
            log.info("action  employVerificationExpire");
            empVerificationService.employVerificationExpire();
        }
    }
    
    @ResponseBody
    @GetMapping("/updateConsentContentPublished")
    public String updateConsentContentPublished(@RequestParam(name="testDate", required=false) String testDate) {
        return cdcCallService.updateConsentContentPublished(testDate);
    }

    @ResponseBody
    @PostMapping("/saveSubsidiaries")
    public String saveSubsidiaries(@RequestBody Map<String, Object> requestData) {
        cpiApiService.saveSubsidiaries(requestData);
        return "Y";
    }

    @ResponseBody
    @PostMapping("/invitationCompanyMerge")
    public String invitationCompanyMerge(@RequestBody Map<String, Object> requestData) {
        cpiApiService.invitationCompanyMerge(requestData);
        return "Y";
    }

}
