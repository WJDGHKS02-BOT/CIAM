package com.samsung.ciam.common.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.gigya.service.GigyaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;


// 365일 동안 로그인하지 않은 사용자의 상태를 disabled로 변경
@Slf4j
@Component
public class DisableInactiveUsersScheduler {

    @Autowired
    private GigyaService gigyaService;

    //@Scheduled(cron = "0 0 19 * * ?") // 매일 오후 7시에 실행
    public void executeDisableInactiveUsers() {
        String query = "SELECT UID, data.channels "
                + "FROM accounts "
                + "LIMIT 50000";

        try {
            GSResponse response = gigyaService.executeRequest("default", "accounts.search", query);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseData = objectMapper.readTree(response.getResponseText());

            if (responseData.has("results")) {
                LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

                for (JsonNode user : responseData.get("results")) {
                    String uid = user.get("UID").asText();
                    JsonNode channels = user.path("data").path("channels");

                    boolean allChannelsInactive = true;

                    Iterator<Map.Entry<String, JsonNode>> fields = channels.fields();

                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fields.next();
                        JsonNode channelData = entry.getValue();

                        if (channelData.has("lastLogin")) {
                            LocalDate lastLogin = LocalDate.parse(channelData.get("lastLogin").asText().substring(0, 10));
                            long daysSinceLastLogin = ChronoUnit.DAYS.between(lastLogin, today);

                            if (daysSinceLastLogin < 365) {
                                allChannelsInactive = false;
                                break;
                            }
                        }
                    }

                    if (allChannelsInactive) {
                        updateAccountStatus(uid, "disabled");
                        log.info("User account status updated to disabled for UID: {}", uid);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error executing disable inactive users", e);
        }
    }

    private void updateAccountStatus(String uid, String status) {
        try {
            // CDC 파라미터 설정
            Map<String, Object> cdcParams = Map.of(
                    "UID", uid
            );

            // 데이터 필드 설정
            Map<String, Object> dataFields = Map.of(
                    "userStatus", status
            );

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