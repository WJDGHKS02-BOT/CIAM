package com.samsung.ciam.common.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.gigya.service.GigyaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


//비활성화된 유저의 상태를 masked로 변경
@Slf4j
@Component
public class MaskInactiveUsersScheduler {

    @Autowired
    private GigyaService gigyaService;

    //@Scheduled(cron = "0 0 20 * * ?") // 매일 오후 8시에 실행
    public void executeMaskInactiveUsers() {
        String query = "SELECT UID FROM accounts WHERE data.userStatus = 'disable' LIMIT 50000";

        try {
            GSResponse response = gigyaService.executeRequest("default", "accounts.search", query);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseData = objectMapper.readTree(response.getResponseText());

            if (responseData.has("results")) {
                for (JsonNode user : responseData.get("results")) {
                    String uid = user.get("UID").asText();
                    updateAccountStatus(uid, "masked");
                    log.info("User account status updated to masked for UID: {}", uid);
                }
            }
        } catch (Exception e) {
            log.error("Error executing mask inactive users", e);
        }
    }

    private void updateAccountStatus(String uid, String status) {
        try {
            // CDC 파라미터 설정
            Map<String, Object> cdcParams = new HashMap<>();
            cdcParams.put("UID", uid);

            // 데이터 필드 설정
            Map<String, Object> dataFields = new HashMap<>();
            dataFields.put("userStatus", status);  // userStatus만 업데이트

            // 데이터 필드를 CDC 파라미터에 추가
            ObjectMapper objectMapper = new ObjectMapper();
            cdcParams.put("data", objectMapper.writeValueAsString(dataFields));

            // CDC 요청 실행
            GSResponse response = gigyaService.executeRequest("default", "accounts.setAccountInfo", cdcParams);
            log.info("Account status updated to {} for UID: {}. Response: {}", status, uid, response.getResponseText());
        } catch (Exception e) {
            log.error("Error updating account status for UID: {}", uid, e);
        }
    }
}