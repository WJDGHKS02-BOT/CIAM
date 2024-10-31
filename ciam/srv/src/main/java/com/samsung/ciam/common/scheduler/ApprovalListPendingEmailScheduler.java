package com.samsung.ciam.common.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.gigya.service.GigyaService;
import com.samsung.ciam.services.MailService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ApprovalListPendingEmailScheduler {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MailService mailService;

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private ObjectMapper objectMapper;

    //@Scheduled(cron = "0 0 18 * * ?", zone = "Asia/Seoul") // 매일 18:00 한국 시간에 실행
    public void executePendingEmailNotification() {
        // 쿼리 작성
        String query = "SELECT wm.requestor_email, " +
                "wm.requested_date, " +
                "wm.wf_id, " +
                "wm.channel, " +
                "wl.approver_id " +
                "FROM wf_list wl " +
                "JOIN wf_master wm ON wl.wf_id = wm.wf_id " +
                "WHERE wm.status = 'pending' " +
                "AND (DATE(wm.requested_date) = CURRENT_DATE - INTERVAL '7 days' " +
                "     OR DATE(wm.requested_date) = CURRENT_DATE - INTERVAL '3 days')";

        // 쿼리 실행
        List<Object[]> results = entityManager.createNativeQuery(query)
                .getResultList();

        List<Map<String, Object>> mappedResults = new ArrayList<>();

        for (Object[] result : results) {
            // Gigya Service로 사용자 정보 조회 (approver_id 사용)
            String approverId = (String) result[4];
            GSResponse accUserResponse = gigyaService.executeRequest("default", "accounts.getAccountInfo", Map.of("UID", approverId));

            if (accUserResponse.getErrorCode() == 0) {
                try {
                    Map<String, Object> accUser = objectMapper.readValue(accUserResponse.getResponseText(), Map.class);

                    // accUser가 유효한 경우만 처리
                    if (accUser != null && accUser.get("profile") != null) {
                        // 사용자 정보에서 firstName과 lastName 가져오기
                        String firstName = (String) ((Map<String, Object>) accUser.get("profile")).getOrDefault("firstName", "");
                        String lastName = (String) ((Map<String, Object>) accUser.get("profile")).getOrDefault("lastName", "");

                        // firstName과 lastName이 유효한 경우에만 mappedResults에 추가
                        if (!firstName.isEmpty() || !lastName.isEmpty()) {
                            Map<String, Object> termsMap = new HashMap<>();
                            termsMap.put("firstName", firstName);  // 사용자 First Name
                            termsMap.put("lastName", lastName);    // 사용자 Last Name
                            termsMap.put("ChannelName", result[3]);  // Channel (wm.channel)
                            termsMap.put("ApprovalRequestEmail", result[0]);  // Email (wm.requestor_email)
                            termsMap.put("ApprovalRequestDate", result[1] != null ? result[1].toString() : "");  // 요청일자 (wm.requested_date)
                            termsMap.put("cdc_uid", approverId);  // 요청일자 (wm.requested_date)

                            mappedResults.add(termsMap);
                        }
                    }

                } catch (JsonProcessingException e) {
                    log.error("Error processing user information for UID: {}", approverId, e);
                }
            } else {
                log.error("Error retrieving user information for approver: {}", approverId);
            }
        }

        // 메일 발송
        for (Map<String, Object> pendingItem : mappedResults) {
            try {
                Map<String, Object> emailParams = new HashMap<>();
                emailParams.put("template", "TEMPLET-NEW-004");
                emailParams.put("firstName", pendingItem.get("firstName"));       // 사용자 First Name
                emailParams.put("lastName", pendingItem.get("lastName"));         // 사용자 Last Name
                emailParams.put("ChannelName", pendingItem.get("ChannelName"));   // Channel Name
                emailParams.put("channel", pendingItem.get("ChannelName"));   // Channel Name
                emailParams.put("approvalRequestEmail", pendingItem.get("ApprovalRequestEmail")); // Email
                emailParams.put("approvalRequestDate", pendingItem.get("ApprovalRequestDate"));   // 요청일
                emailParams.put("cdc_uid", pendingItem.get("cdc_uid"));   // 요청일

                // 메일 발송
                mailService.sendMail(emailParams);

                log.info("Email sent to user: {}", pendingItem.get("ApprovalRequestEmail"));
            } catch (Exception e) {
                log.error("Error sending email for: {}", pendingItem.get("ApprovalRequestEmail"), e);
            }
        }
    }
}