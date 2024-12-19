package com.samsung.ciam.controllers;

import com.samsung.ciam.services.SamsungMfaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mfa")
public class MfaController {
  private final SamsungMfaService samsungMfaService;

  public MfaController(SamsungMfaService samsungMfaService) {
    this.samsungMfaService = samsungMfaService;
  }

  @PostMapping("/request")
  public String requestMfa(@RequestBody Map<String, String> request) throws Exception {
    return samsungMfaService.getLoginMFA(
        request.get("uid"),
        request.get("returnUrl"),
        request.get("email"),
        request.get("displayUid"),
        Boolean.parseBoolean(request.get("isBioOnly"))
    );
  }

  @PostMapping("/authenticate")
  public Map<String, Object> authenticate(@RequestBody Map<String, String> request) {
    Map<String, Object> response = new HashMap<>();
    try {
      samsungMfaService.authenticate(request.get("jwtTokenResponse"), Boolean.parseBoolean(request.get("isBioOnly")));
      response.put("success", true);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", e.getMessage());
    }
    return response;
  }
}

