package com.samsung.ciam.services;

import com.samsung.ciam.repositories.TempWfMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 1. FileName   : TempWfIdGeneratorService.java
 * 2. Package    : com.samsung.ciam.services
 * 3. Comments   : 임시(TEMP) 워크플로우 ID(wf_id)를 생성하기 위한 서비스 클래스
 * 4. Author     : 서정환
 * 5. DateTime   : 2024. 11. 04.
 * 6. History    :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * Date        | Name          | Comment
 * <p>
 * -------------  -----------------   ------------------------------
 * <p>
 * 2024. 11. 04.       | 서정환           | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class TempWfIdGeneratorService {

    // wf_id 생성 시 날짜 형식을 지정하는 포맷터
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private TempWfMasterRepository wfMasterRepository;

    /*
     * 1. 메소드명: generateNewWfId
     * 2. 클래스명: TempWfIdGeneratorService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    새로운 임시 wf_id를 생성하는 메소드로, 현재 날짜와 연속 번호를 결합하여 생성(테스트 승인 페이지용)
     * 2. 사용법
     *    현재 날짜와 연결된 wf_id의 최대 값을 조회하고 새로운 ID 생성
     * 3. 예시 데이터
     *    - Input: 없음
     *    - Output: "20241104_0001" (오늘 날짜가 2024년 11월 4일인 경우)
     * </PRE>
     * @return 생성된 임시 wf_id 문자열
     */
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
