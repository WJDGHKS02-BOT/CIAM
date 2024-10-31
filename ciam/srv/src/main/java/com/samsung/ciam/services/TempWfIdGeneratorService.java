package com.samsung.ciam.services;

import com.samsung.ciam.repositories.TempWfMasterRepository;
import com.samsung.ciam.repositories.WfMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class TempWfIdGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private TempWfMasterRepository wfMasterRepository;

    // wf_id 생성 메소드
    public String generateNewWfId() {
        // 오늘 날짜를 yyyyMMdd 형식으로 얻기
        String todayDate = LocalDate.now().format(DATE_FORMATTER);
        String prefix = todayDate + "_";

        // 현재 날짜에 해당하는 wf_id의 최대값을 조회
        String maxWfId = wfMasterRepository.findMaxWfIdWithPrefix(prefix);

        int nextNumber = 1; // 기본값은 1

        if (maxWfId != null) {
            // maxWfId가 존재하면 마지막 4자리 숫자를 추출하고 +1
            String[] parts = maxWfId.split("_");
            if (parts.length == 2) {
                try {
                    nextNumber = Integer.parseInt(parts[1]) + 1;
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid wf_id format: " + maxWfId, e);
                }
            }
        }

        // 새로운 wf_id 생성
        return String.format("%s%04d", prefix, nextNumber);
    }
}
