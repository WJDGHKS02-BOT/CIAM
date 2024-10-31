package com.samsung.ciam.controllers;

import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.samsung.ciam.services.AuditLogService;


@RestController
@RequestMapping("/auditlog")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @RequestMapping("/create")
    public String createAuditLog(@RequestBody Map<String, String> auditLogData, HttpSession session) {
        try{
            auditLogService.addAuditLog(session, auditLogData);
            return "success";
        }catch(Exception e){
            return e.getMessage();
        }
    }

}
