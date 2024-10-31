package com.samsung.ciam.common.scheduler;

import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.gigya.service.GigyaService;
import com.samsung.ciam.models.ChannelInvitation;
import com.samsung.ciam.repositories.ChannelInvitationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Slf4j
@Component
public class invitationRejectScheduler {

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private ChannelInvitationRepository channelInvitationRepository;

    //@Scheduled(cron = "0 46 14 * * ?", zone = "Asia/Seoul") // 매일 14:00 한국 시간에 실행
    public void executeInvitationReject() {
        List<ChannelInvitation> expireInvitationList = channelInvitationRepository.selectInvitationExpiryList();

        for (ChannelInvitation expireInvitation : expireInvitationList) {
            try {
                // LoginUid가 null이 아니고 공백 문자열이 아닌 경우만 처리
                if (expireInvitation.getLoginUid() != null && !expireInvitation.getLoginUid().isBlank()) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("UID", expireInvitation.getLoginUid());

                    GSResponse response = gigyaService.executeRequest(expireInvitation.getChannel(), "accounts.deleteAccount", params);

                    // Gigya API 호출 성공 여부 확인
                    if (response.getErrorCode() == 0) {
                        expireInvitation.setStatus("rejected");

                        // 서울 시간으로 현재 시간 설정 (Timestamp로 변환)
                        LocalDateTime nowSeoul = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
                        expireInvitation.setRejectedDate(Timestamp.valueOf(nowSeoul));

                        expireInvitation.setRejectedReason("기한 만료(Deadline expires)");
                        expireInvitation.setRejectedId("BATCH");
                        channelInvitationRepository.save(expireInvitation);
                    } else {
                        log.error("Failed to delete account: {}, errorCode: {}", expireInvitation.getLoginUid(), response.getErrorCode());
                    }
                }
            } catch (Exception e) {
                log.error("Error processing invitation rejection for UID: {}", expireInvitation.getLoginUid(), e);
            }
        }
    }
}
