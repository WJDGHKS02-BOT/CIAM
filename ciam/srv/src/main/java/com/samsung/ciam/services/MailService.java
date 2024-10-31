package com.samsung.ciam.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsung.ciam.models.ChannelInvitation;
import com.samsung.ciam.models.Channels;
import com.samsung.ciam.repositories.ChannelInvitationRepository;
import com.samsung.ciam.repositories.ChannelRepository;
import com.samsung.ciam.utils.BeansUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MailService {

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ChannelInvitationRepository channelInvitationRepository;

    public Map<String, Object> sendMail(Map<String, Object> paramArray) {
        Map<String, Object> emailDataArray = populateMailContent(paramArray);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = new HashMap<>();  // To store the result or error
        try {
            String emailDataJson = objectMapper.writeValueAsString(emailDataArray);

            HttpHeaders headers = createHeaders(
                    BeansUtil.getApplicationProperty("email.service.login"),
                    BeansUtil.getApplicationProperty("email.service.password")
            );
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(emailDataJson, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    BeansUtil.getApplicationProperty("email.service.url") + "/mail",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            log.info("Email response: {}", responseEntity.getBody());
            return objectMapper.readValue(responseEntity.getBody(), Map.class);
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("HTTP Request failed with status {} and response: {}", e.getStatusCode(), responseBody);

            responseMap.put("error", "HTTP error occurred");
            responseMap.put("statusCode", e.getStatusCode());
            responseMap.put("response", responseBody);
        } catch (Exception e) {
            // Log any other exceptions without interrupting the flow
            log.error("Error sending email", e);

            responseMap.put("error", "Unexpected error occurred");
            responseMap.put("exception", e.getMessage());
            return responseMap; // Return error information without throwing
        }
        return responseMap;
    }

    private HttpHeaders createHeaders(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private Map<String, Object> populateMailContent(Map<String, Object> paramArray) {
        Map<String, Object> emailDataArray = new HashMap<>();
        Map<String, Object> fields = new HashMap<>();

        try {
            // Validate
            if (!paramArray.containsKey("channel")) {
                throw new IllegalArgumentException("channel is required");
            }

            JsonNode CDCUser = cdcTraitService.getCdcUser(paramArray.get("cdc_uid").toString(), 0);

            // Common structure
            String firstName = CDCUser.path("profile").path("firstName").asText("");
            String lastName = CDCUser.path("profile").path("lastName").asText("");
            String email = CDCUser.path("profile").path("email").asText("");
            String language = cdcTraitService.determineLanguage(CDCUser.path("profile").path("locale").asText("en"));

            emailDataArray.put("template", paramArray.get("template"));
            emailDataArray.put("language", language);
            emailDataArray.put("to", List.of(Map.of(
                    "name", firstName + " " + lastName,
                    "mail", email
            )));

            Channels channel = channelRepository.findByChannelName(paramArray.get("channel").toString())
                    .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

            // Template specific structure
            switch (paramArray.get("template").toString()) {
                case "TEMPLET-002":
                    emailDataArray.put("channel", "SFDC");
                    fields.put("$Channel Admin", paramArray.get("approverName"));
                    fields.put("$Channel", paramArray.get("channel"));
                    fields.put("$CIAM Admin", "CIAM ADMIN");
                    break;
                case "TEMPLET-031":
                    emailDataArray.put("channel", "SFDC");
                    fields.put("$Channel.user", paramArray.get("approverName"));
                    fields.put("$Channel", paramArray.get("channel"));
                    break;
                case "TEMPLET-030":
                    emailDataArray.put("channel", "SFDC");
                    fields.put("$Channel.user", paramArray.get("approverName"));
                    fields.put("$Channel", paramArray.get("channel"));
                    fields.put("$Reject.Reason", paramArray.get("reason"));
                    break;
                case "TEMPLET-032":
                    try {
                        ChannelInvitation channelInvitation = channelInvitationRepository.findById(Long.parseLong(paramArray.get("ch_inv_id").toString()))
                                .orElseThrow(() -> new IllegalArgumentException("Channel invitation not found"));

                        fields.put("$Channel", channel.getChannelDisplayName());
                        fields.put("$Channel.TEMP.User", firstName + " " + lastName);
                        fields.put("$Channel.User", createRegistrationLink(paramArray, channelInvitation.getToken()));
                        fields.put("$CIAM Admin", "CIAM Admin");
                        fields.put("$docURL", createDocURL(paramArray, channelInvitation.getToken(), CDCUser.path("profile").path("languages").asText("")));
                    } catch (Exception e) {
                        log.error("Error processing template TEMPLET-032: {}", e.getMessage());
                    }
                    break;
                case "TEMPLET-NEW-005":
                    try {
                        ChannelInvitation channelNewInvitation = channelInvitationRepository.findById(Long.parseLong(paramArray.get("ch_inv_id").toString()))
                                .orElseThrow(() -> new IllegalArgumentException("Channel invitation not found"));

                        fields.put("$Channel", channel.getChannelDisplayName());
                        fields.put("$Channel.TEMP.User", firstName + " " + lastName);
                        fields.put("$Channel.User", createRegistrationLink(paramArray, channelNewInvitation.getToken()));
                        fields.put("$CIAM Admin", "CIAM Admin");
                        fields.put("$docURL", createDocURL(paramArray, channelNewInvitation.getToken(), CDCUser.path("profile").path("languages").asText("")));
                        fields.put("$firstName", paramArray.get("firstName"));
                        fields.put("$lastName", paramArray.get("lastName"));
                    } catch (Exception e) {
                        log.error("Error processing template TEMPLET-NEW-005: {}", e.getMessage());
                    }
                    break;
                case "TEMPLET-NEW-002":
                    fields.put("$ChannelName", channel.getChannelDisplayName());
                    fields.put("$firstName", paramArray.get("firstName"));
                    fields.put("$lastName", paramArray.get("lastName"));
                    fields.put("$ApprovalRequestEmail", paramArray.get("approvalRequestEmail"));
                    break;
                case "TEMPLET-NEW-004":
                    fields.put("$ChannelName", channel.getChannelDisplayName());
                    fields.put("$firstName", paramArray.get("firstName"));
                    fields.put("$lastName", paramArray.get("lastName"));
                    fields.put("$ApprovalRequestEmail", paramArray.get("approvalRequestEmail"));
                    fields.put("$ApprovalRequestDate", paramArray.get("approvalRequestDate"));
                    break;
                case "TEMPLET-NEW-006": // 재직인증 승인요청 알람
                    fields.put("$ChannelName", channel.getChannelDisplayName());
                    fields.put("$firstName", paramArray.get("firstName"));
                    fields.put("$lastName", paramArray.get("lastName"));
                    //fields.put("$docURL", createDocURL(paramArray, channelNewInvitation.getToken(), CDCUser.path("profile").path("languages").asText("")));
                break;
                case "TEMPLET-NEW-008": // 재직 인증 반려 
                    fields.put("$firstName", paramArray.get("firstName"));
                    fields.put("$lastName", paramArray.get("lastName"));
                    fields.put("$EmployeeVerificationRejector", paramArray.get("EmployeeVerificationRejector"));
                    fields.put("$EmployeeVerificationRejectReason", paramArray.get("EmployeeVerificationRejectReason"));
                break;
                case "TEMPLET-NEW-009": // 재직 인증 기한 마감
                    fields.put("$firstName", paramArray.get("firstName"));
                    fields.put("$lastName", paramArray.get("lastName"));
                    fields.put("$EmployeeVerificationRejector", paramArray.get("EmployeeVerificationRejector"));
                    fields.put("$EmployeeVerificationRejectReason", paramArray.get("EmployeeVerificationRejectReason"));
                break;
                case "TEMPLET-NEW-003": // Admin이 가입 승인 반려 시점
                    fields.put("$ChannelName", channel.getChannelDisplayName());
                    fields.put("$firstName", paramArray.get("firstName"));
                    fields.put("$lastName", paramArray.get("lastName"));
                    fields.put("$ApprovalRequestRejector", paramArray.get("ApprovalRequestRejector"));
                    fields.put("$ApprovalRequestRejectReason", paramArray.get("ApprovalRequestRejectReason"));
                break;
                case "TEMPLET-NEW-012": // 권한 변경 반려 안내, Admin이 Role Manager에서 권한 변경을 거절한 시점
                    fields.put("$firstName", paramArray.get("firstName"));
                    fields.put("$lastName", paramArray.get("lastName"));
                    fields.put("$RoleManagerRejector", paramArray.get("RoleManagerRejector"));
                    fields.put("$RoleManagerRejectReason", paramArray.get("RoleManagerRejectReason"));
                break;
                default:
                    log.error("Invalid template type: {}", paramArray.get("template").toString());
            }

            emailDataArray.put("fields", List.of(fields));
        } catch (Exception e) {
            log.error("Error in populateMailContent method: {}", e.getMessage());
            // 예외를 다시 던지지 않고 메서드가 종료되도록 함
        }

        return emailDataArray;
    }

    public String createRegistrationLink(Map<String, Object> paramArray, String token) {
        return String.format("%s/registration/company?param=%s&t=%s&channelType=%s",
                BeansUtil.getApplicationProperty("app.base.url"), paramArray.get("channel"), token,paramArray.get("channelType"));
    }

    public String createDocURL(Map<String, Object> paramArray, String token, String language) {
        return String.format("%s/registration/company?param=%s&t=%s&lang=%s&channelType=%s",
                BeansUtil.getApplicationProperty("app.base.url"), paramArray.get("channel"), token, language,paramArray.get("channelType"));
    }
}