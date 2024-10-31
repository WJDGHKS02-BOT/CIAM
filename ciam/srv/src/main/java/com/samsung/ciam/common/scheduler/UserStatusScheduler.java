package com.samsung.ciam.common.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.gigya.service.GigyaService;
import com.samsung.ciam.services.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Component
public class UserStatusScheduler {

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private MailService mailService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //@Scheduled(cron = "0 0 18 * * ?") // 매일 오후 6시에 실행
    public void executeUserStatusCheck() {
        String query = "SELECT UID, data.channels "
                + "FROM accounts "
                + "WHERE (data.userStatus = 'active' OR data.userStatus = 'inactive') "
                + "AND (data.channels IS NOT NULL) "
                + "LIMIT 3";

        try {
            GSResponse response = gigyaService.executeRequest("default", "accounts.search", query);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseData = objectMapper.readTree(response.getResponseText());

            if (responseData.has("results")) {
                LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul")).truncatedTo(ChronoUnit.SECONDS);

                for (JsonNode user : responseData.get("results")) {
                    String uid = user.get("UID").asText();
                    JsonNode channels = user.path("data").path("channels");

                    boolean allChannelsInactive = true;
                    boolean mmpRequiresAdditionalCheck = false;
                    int inactiveChannelsCount = 0;
                    int totalChannelsCount = 0;
                    int channelsWith365Days = 0;

                    Map<String, Long> channelLoginDaysMap = new HashMap<>();

                    for (Iterator<Map.Entry<String, JsonNode>> it = channels.fields(); it.hasNext(); ) {
                        Map.Entry<String, JsonNode> entry = it.next();
                        String channelName = entry.getKey();
                        JsonNode channelData = entry.getValue();
                        totalChannelsCount++;

                        if (!channelData.has("lastLogin")) {
                            continue;
                        }

                        LocalDateTime lastLogin;
                        try {
                            lastLogin = LocalDateTime.parse(channelData.get("lastLogin").asText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                    .truncatedTo(ChronoUnit.SECONDS);
                        } catch (Exception e) {
                            lastLogin = LocalDateTime.parse(channelData.get("lastLogin").asText().substring(0, 19), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }

                        long daysSinceLastLogin = ChronoUnit.DAYS.between(lastLogin, now);
                        channelLoginDaysMap.put(channelName, daysSinceLastLogin);

                        if (daysSinceLastLogin >= 90) {
                            inactiveChannelsCount++;
                        }

                        if (channelName.equals("MMP") && daysSinceLastLogin >= 90) {
                            mmpRequiresAdditionalCheck = checkMmpLicenseCondition(channelData);
                        }

                        if (daysSinceLastLogin < 90) {
                            allChannelsInactive = false;
                        }

                        // 60일 휴면 전환 예정 메일
                        if (daysSinceLastLogin == 60 && !hasEmailBeenSent(uid, channelName, "TEMPLATE_60_DAYS", lastLogin)) {
                            sendDormantNotice(uid, channelName);
                        }

                        // 335일 자동 탈퇴 예정 메일
                        if (daysSinceLastLogin == 335 && !hasEmailBeenSent(uid, channelName, "TEMPLATE_335_DAYS", lastLogin)) {
                            sendDeactivationWarning(uid, channelName);
                        }

                        // 365일 자동 탈퇴 완료 메일
                        if (daysSinceLastLogin >= 365 && !hasEmailBeenSent(uid, channelName, "TEMPLATE_365_DAYS", lastLogin)) {
                            channelsWith365Days++;
                            sendAutoDeactivationNotice(uid, channelName); // 각 채널에 대한 자동 탈퇴 완료 메일 전송
                        }
                    }

                    if (inactiveChannelsCount == totalChannelsCount) {
                        sendATemplateMail(uid); // 모든 채널이 비활성화된 경우 A 템플릿 메일 전송
                    } else {
                        for (Map.Entry<String, Long> entry : channelLoginDaysMap.entrySet()) {
                            String channelName = entry.getKey();
                            long daysSinceLastLogin = entry.getValue();

                            if (daysSinceLastLogin >= 90 && !hasEmailBeenSent(uid, channelName, "TEMPLATE_B", now)) {
                                if (channelName.equals("MMP") && mmpRequiresAdditionalCheck) {
                                    sendBTemplateMail(uid, channelName); // MMP 채널의 추가 조건 충족 시 B 템플릿 메일 전송
                                } else if (!channelName.equals("MMP")) {
                                    sendBTemplateMail(uid, channelName); // 일반 채널 B 템플릿 메일 전송
                                }
                            }
                        }
                    }

                    if (channelsWith365Days == totalChannelsCount) {
                        sendUserAutoDeactivationMail(uid);
                    }

                    if (allChannelsInactive) {
                        deactivateUserAccount(uid);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error executing user status check", e);
        }
    }

    private boolean hasEmailBeenSent(String uid, String channelName, String emailType, LocalDateTime lastLogin) {
        String query = "SELECT COUNT(*) FROM MailSendHistory WHERE uid = ? AND channelName = ? AND emailType = ? AND sendDate >= ?";
        LocalDateTime cutoffDate = lastLogin.plusDays(1); // lastLogin 이후에 보낸 이메일만 체크
        int count = jdbcTemplate.queryForObject(query, new Object[]{uid, channelName, emailType, cutoffDate}, Integer.class);
        return count > 0;
    }

    private void recordEmailSent(String uid, String channelName, String emailType) {
        String query = "INSERT INTO MailSendHistory (uid, channelName, emailType, sendDate) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(query, uid, channelName, emailType, LocalDateTime.now());
    }

    private boolean checkMmpLicenseCondition(JsonNode channelData) {
        // MMP 채널의 라이센스 종료일 이후 1년이 지났는지 확인하는 로직을 구현
        return true; // 예시로 true 반환, 실제 구현 필요
    }

    private void sendDormantNotice(String uid, String channelName) {
        Map<String, Object> paramArr = Map.of(
                "template", "TEMPLATE_60_DAYS",
                "cdc_uid", uid,
                "channel", channelName
        );
        mailService.sendMail(paramArr);
        log.info("60일 휴면 전환 예정 메일 전송: UID = {}, 채널 = {}", uid, channelName);
        recordEmailSent(uid, channelName, "TEMPLATE_60_DAYS");
    }

    private void sendDeactivationWarning(String uid, String channelName) {
        Map<String, Object> paramArr = Map.of(
                "template", "TEMPLATE_335_DAYS",
                "cdc_uid", uid,
                "channel", channelName
        );
        mailService.sendMail(paramArr);
        log.info("335일 자동 탈퇴 예정 메일 전송: UID = {}, 채널 = {}", uid, channelName);
        recordEmailSent(uid, channelName, "TEMPLATE_335_DAYS");
    }

    private void sendAutoDeactivationNotice(String uid, String channelName) {
        Map<String, Object> paramArr = Map.of(
                "template", "TEMPLATE_365_DAYS",
                "cdc_uid", uid,
                "channel", channelName
        );
        mailService.sendMail(paramArr);
        log.info("365일 자동 탈퇴 완료 메일 전송: UID = {}, 채널 = {}", uid, channelName);
        recordEmailSent(uid, channelName, "TEMPLATE_365_DAYS");
    }

    private void sendUserAutoDeactivationMail(String uid) {
        Map<String, Object> paramArr = Map.of(
                "template", "TEMPLATE_USER_365_DAYS",
                "cdc_uid", uid
        );
        mailService.sendMail(paramArr);
        log.info("사용자에게 365일 자동 탈퇴 완료 메일 전송: UID = {}", uid);
        recordEmailSent(uid, null, "TEMPLATE_USER_365_DAYS");
    }

    private void sendATemplateMail(String uid) {
        Map<String, Object> paramArr = Map.of(
                "template", "TEMPLATE_A",
                "cdc_uid", uid
        );
        mailService.sendMail(paramArr);
        log.info("A 템플릿 메일 전송: UID = {}", uid);
        recordEmailSent(uid, null, "TEMPLATE_A");
    }

    private void sendBTemplateMail(String uid, String channelName) {
        Map<String, Object> paramArr = Map.of(
                "template", "TEMPLATE_B",
                "cdc_uid", uid,
                "channel", channelName
        );
        mailService.sendMail(paramArr);
        log.info("B 템플릿 메일 전송: UID = {}, 채널 = {}", uid, channelName);
        recordEmailSent(uid, channelName, "TEMPLATE_B");
    }

    private void deactivateUserAccount(String uid) {
        updateAccountStatus(uid, "inactive");
        log.info("User account deactivated: {}", uid);
    }

    private void updateAccountStatus(String uid, String status) {
        try {
            Map<String, Object> dataFields = Map.of(
                    "userStatus", status
            );

            Map<String, Object> cdcParams = Map.of(
                    "UID", uid,
                    "data", dataFields
            );

            GSResponse response = gigyaService.executeRequest("default", "accounts.setAccountInfo", cdcParams);
            log.info("Account status updated to {} for UID: {}. Response: {}", status, uid, response.getResponseText());
        } catch (Exception e) {
            log.error("Error updating account status for UID: {}", uid, e);
        }
    }
}