package com.samsung.ciam.controllers;

import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.samsung.ciam.services.AuditLogService;

/**
 * 1. 파일명   : AuditLogController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : Audit 로그를 생성하고 저장하는 기능을 제공하는 컨트롤러
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
@RestController
@RequestMapping("/auditlog")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    /*
     * 1. 메소드명: createAuditLog
     * 2. 클래스명: AuditLogController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    Audit 로그 데이터를 받아 로그를 생성하고 저장
     * 2. 사용법
     *    POST 요청으로 Audit 로그 데이터를 전달하여 로그 생성
     * 3. 예시 데이터
     *    - Input: 로그 데이터 (Map 형식)
     *    - Output: 성공 시 "success", 실패 시 에러 메시지 반환
     * </PRE>
     * @param auditLogData 생성할 Audit 로그 데이터
     * @param session 사용자 세션 객체
     * @return 생성 결과 메시지 ("success" 또는 에러 메시지)
     */
    @RequestMapping("/create")
    public String createAuditLog(@RequestBody Map<String, String> auditLogData, HttpSession session) {
        try{
            auditLogService.addAuditLog(session, auditLogData);
            return "success";
        }catch(Exception e){
            return e.getMessage();
        }
    }

}
