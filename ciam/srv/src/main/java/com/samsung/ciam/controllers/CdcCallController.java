package com.samsung.ciam.controllers;

import java.util.*;

import com.samsung.ciam.common.cpi.service.CpiApiService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.samsung.ciam.services.CdcCallService;
import com.samsung.ciam.services.EmpVerificationService;

/**
 * 1. 파일명   : CdcCallController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : CDC와 CPI 관련 데이터를 관리하는 API 컨트롤러로 재직 인증, 약관 게시, 회사 정보 저장 등의 작업을 처리
 * 4. 작성자   : 서정환
 * 5. 작성일자 : 2024. 11. 04.
 * 6. 히스토리 :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * 날짜        | 이름          | 설명
 * <p>
 * -------------|--------------|------------------------------------
 * <p>
 * 2024. 11. 04 | 서정환       | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@Slf4j
@RestController
@RequestMapping("/api/restapi/extension/sec")
public class CdcCallController {

    @Autowired
    private EmpVerificationService empVerificationService;

    @Autowired
    private CdcCallService cdcCallService;

    @Autowired
    private CpiApiService cpiApiService;

    /*
     * 1. 메소드명: employVerificationTableInsert
     * 2. 클래스명: CdcCallController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CDC로부터 사용자 정보를 가져와 재직 인증 사용자 테이블에 저장
     * 2. 사용법
     *    GET 요청으로 param 값이 "cdc_insert"일 경우 데이터 삽입 작업 실행
     * 3. 예시 데이터
     *    - Input: param="cdc_insert"
     *    - Output: 재직 인증 데이터가 사용자 테이블에 삽입됨
     * </PRE>
     * @param param 요청 파라미터
     */
    @ResponseBody
    @GetMapping("/cdcInsertEmployData")
    public void employVerificationTableInsert(@RequestParam("param") String param) {
        //CDC에서 Data를 가져와서 DB에 넣는 API
        log.info("Received param: {}", param);
        if(param.equals("cdc_insert")) {
            log.info("action  employVerificationTableInsert");
            empVerificationService.employVerificationTableInsert();
        }
    }

    /*
     * 1. 메소드명: employVerificationExpire
     * 2. 클래스명: CdcCallController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    재직 인증 만료일이 지난 사용자들의 상태를 'pending'에서 'rejected'로 변경
     * 2. 사용법
     *    GET 요청으로 param 값이 "expire"일 경우 만료 처리 작업 실행
     * 3. 예시 데이터
     *    - Input: param="expire"
     *    - Output: 만료된 재직 인증 사용자 상태가 'rejected'로 변경됨
     * </PRE>
     * @param param 요청 파라미터
     */
    @ResponseBody
    @GetMapping("/expireEmployData")
    public void employVerificationExpire(@RequestParam("param") String param) {
        //재직인증 Expired date 가 되면 pending -> reject로 변경한다. 
        log.info("Received param: {}", param);
        if(param.equals("expire")) {
            log.info("action  employVerificationExpire");
            empVerificationService.employVerificationExpire();
        }
    }

    /*
     * 1. 메소드명: updateConsentContentPublished
     * 2. 클래스명: CdcCallController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관 마스터를 게시 상태(Published)로 업데이트
     * 2. 사용법
     *    GET 요청으로 Date를 전달하여 약관 게시 처리 실행
     * 3. 예시 데이터
     *    - Input: testDate="2024-11-04"
     *    - Output: 지정된 날짜에 해당하는 약관이 게시됨
     * </PRE>
     * @param testDate 테스트용 게시 날짜
     * @return 처리 결과 메시지
     */
    @ResponseBody
    @GetMapping("/updateConsentContentPublished")
    public String updateConsentContentPublished(@RequestParam(name="testDate", required=false) String testDate) {
        return cdcCallService.updateConsentContentPublished(testDate);
    }

    /*
     * 1. 메소드명: saveSubsidiaries
     * 2. 클래스명: CdcCallController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CPI로부터 전달된 회사 정보 및 법인 정보를 데이터베이스에 저장
     * 2. 사용법
     *    POST 요청으로 회사 및 법인 정보가 담긴 데이터 전달 시 데이터 저장
     * 3. 예시 데이터
     *    - Input: 회사 및 법인 정보가 포함된 JSON 데이터
     *    - Output: 회사 및 법인 정보가 데이터베이스에 저장됨
     * </PRE>
     * @param requestData 회사 및 법인 정보 데이터
     * @return 저장 성공 메시지 "Y"
     */
    @ResponseBody
    @PostMapping("/saveSubsidiaries")
    public String saveSubsidiaries(@RequestBody Map<String, Object> requestData) {
        cpiApiService.saveSubsidiaries(requestData);
        return "Y";
    }

    /*
     * 1. 메소드명: invitationCompanyMerge
     * 2. 클래스명: CdcCallController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    MERGE된 회사 정보를 기반으로 초대 테이블의 회사 코드 갱신
     * 2. 사용법
     *    POST 요청으로 병합된 회사 정보가 담긴 데이터 전달 시 회사 코드 업데이트
     * 3. 예시 데이터
     *    - Input: MERGE된 회사 정보가 포함된 JSON 데이터
     *    - Output: 초대 테이블의 회사 코드가 갱신됨
     * </PRE>
     * @param requestData MERGE된 회사 정보 데이터
     * @return 업데이트 성공 메시지 "Y"
     */
    @ResponseBody
    @PostMapping("/invitationCompanyMerge")
    public String invitationCompanyMerge(@RequestBody Map<String, Object> requestData) {
        cpiApiService.invitationCompanyMerge(requestData);
        return "Y";
    }

}
