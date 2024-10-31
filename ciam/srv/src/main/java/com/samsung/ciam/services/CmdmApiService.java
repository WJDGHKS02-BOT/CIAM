package com.samsung.ciam.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CmdmApiService {

    private String authToken;
    private JdbcTemplate jdbcTemplate; // Assume this is initialized properly
    private HttpClient httpClient;

    public CmdmApiService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.httpClient = HttpClient.newHttpClient();
    }
    
    public void withdrawAccount(HttpServletRequest request, String context, String contactID, String accountDataJson) {
        authenticate();
        
        if (authToken == null || authToken.isEmpty()) {
            log.info("No auth token found on update account endpoint.");
            return;
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode accountData = objectMapper.readTree(accountDataJson);

            String usedChannelName = accountData.has("regSource") ? getChannelExternalId(accountData.get("regSource").asText()) : "";

            // String fullURL = request.getRequestURL().toString();
            // String hostURL = fullURL.split("signin")[0];
            String apiUrl = "https://LAY026713/IF_CIAM_CMDM/restapi/extension/sec/contact/update";

            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("context", context);

            Map<String, Object> bodyParams = new HashMap<>();
            bodyParams.put("contactid", contactID);
            bodyParams.put("acctid", accountData.path("data").path("accountID").asText());
            bodyParams.put("email", accountData.path("profile").path("email").asText(""));
            bodyParams.put("countrycode", accountData.path("profile").path("country").asText(""));
            bodyParams.put("lastname_lo", accountData.path("profile").path("lastName").asText(""));
            bodyParams.put("firstname_lo", accountData.path("profile").path("firstName").asText(""));
            bodyParams.put("regch", usedChannelName);
            bodyParams.put("opt_in", accountData.path("preferences").path("b2b").path("marketing").path("lastConsentModified").asBoolean(false) ? "Y" : "N");
            bodyParams.put("opt_indate", accountData.path("preferences").path("b2b").path("marketing").path("lastConsentModified").asText(getCurrentDate()));
            bodyParams.put("opt_out", accountData.path("preferences").path("b2b").path("marketing").path("lastConsentModified").asBoolean(false) ? "N" : "Y");
            bodyParams.put("opt_outdate", accountData.path("preferences").path("b2b").path("marketing").path("lastConsentModified").asText(getCurrentDate()));
            bodyParams.put("changedate", getCurrentDateTime());
            bodyParams.put("changerid", accountData.path("data").path("changerID").asText(""));
            bodyParams.put("contactstatus", "Expired");

            log.info("Withdraw CMDM account body params: {}", objectMapper.writeValueAsString(bodyParams));

            String responseData = sendPostRequest(apiUrl, bodyParams, queryParams);

            log.info("existing CMDM account withdrawal: {}", responseData);

        } catch (IOException e) {
            log.error("Error processing account data: {}", e.getMessage());
        }
    }

    private void authenticate() {
        // 인증 로직 구현
        this.authToken = "dummy-auth-token"; // 실제 인증 토큰 로직 필요
    }

    private String getChannelExternalId(String regSource) {
        // 채널 외부 ID 조회
        String sql = "SELECT external_id FROM Channels WHERE channel_name LIKE ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{regSource}, String.class);
    }

    private String sendPostRequest(String apiUrl, Map<String, Object> bodyParams, Map<String, String> queryParams) throws IOException {
        // Query parameters 처리
        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        if (!queryParams.isEmpty()) {
            urlBuilder.append("?");
            queryParams.forEach((key, value) -> urlBuilder.append(key).append("=").append(value).append("&"));
            urlBuilder.setLength(urlBuilder.length() - 1); // 마지막 '&' 제거
        }

        // HTTP POST 요청
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(bodyParams);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("HTTP request interrupted", e);
        }
    }

    private String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}