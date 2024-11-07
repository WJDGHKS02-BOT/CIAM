package com.samsung.ciam.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samsung.ciam.services.ApprovalAdminService;
import com.samsung.ciam.services.ApprovalConfigurationService;
import com.samsung.ciam.services.AuditLogService;
import com.samsung.ciam.services.SystemService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 1. 파일명   : ApprovalConfigurationController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : 승인 룰 마스터와 관련된 관리 작업을 수행하는 컨트롤러로, 승인 룰 검색, 추가, 수정, 삭제 등의 기능을 제공
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
@RequestMapping("/approvalConfiguration")
public class ApprovalConfigurationController {

    @Autowired
    private ApprovalConfigurationService approvalConfigurationService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private ApprovalAdminService approvalAdminService;

    @Autowired
    private AuditLogService auditLogService;

    /*
     * 1. 메소드명: searchApprovalConfiguration
     * 2. 클래스명: approvalConfiguration
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    승인 룰 설정 목록을 검색하고, 검색 조건과 결과에 따라 Audit 로그를 기록
     * 2. 사용법
     *    POST 요청으로 승인 설정 목록을 조회
     * 3. 예시 데이터
     *    - Input: 검색 조건을 포함한 JSON 형태의 요청 데이터
     *    - Output: 검색된 승인 룰 설정 리스트
     * </PRE>
     * @param payload 검색 조건
     * @param session 사용자 세션
     * @return 승인 룰 설정 목록
     */
    @PostMapping("/approvalConfigurationList")
    public List<Map<String, Object>> searchApprovalConfiguration(@RequestBody Map<String, String> payload, HttpSession session) {
        List<Map<String, Object>> resultList = approvalConfigurationService.searchApprovalConfiguration(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "ListView"); // 6가지 : ListView, DetailedView, View, Search, Creation, Modification
        param.put("condition", String.valueOf(payload));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", String.valueOf(resultList.size()));
        auditLogService.addAuditLog(session, param);
        return resultList;
    }

    /*
     * 1. 메소드명: newSearchApprovalConfiguration
     * 2. 클래스명: approvalConfiguration
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    새로운 승인 설정 목록을 검색하고 추가 정보를 반환하며 Audit 로그를 기록
     * 2. 사용법
     *    POST 요청으로 새로운 승인 설정 목록을 조회
     * 3. 예시 데이터
     *    - Input: 검색 조건을 포함한 JSON 형태의 요청 데이터
     *    - Output: 승인 설정 리스트, 단계, 승인 포맷, 규칙 레벨
     * </PRE>
     * @param payload 검색 조건
     * @param session 사용자 세션
     * @return 승인 설정 목록과 관련 데이터
     */
    @PostMapping("/newApprovalConfigurationList")
    public Map<String, Object> newSearchApprovalConfiguration(@RequestBody Map<String, String> payload, HttpSession session) {
        List<Map<String, Object>> resultList = approvalConfigurationService.searchApprovalConfiguration(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Search");
        param.put("condition", String.valueOf(payload));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", String.valueOf(resultList.size()));
        auditLogService.addAuditLog(session, param);

        Map<String, Object> results = new HashMap<>();
        results.put("result", resultList);
        results.put("stage", approvalConfigurationService.getApprovalRuleMasterStage(payload, session));
        results.put("approveFormat", approvalConfigurationService.getApprovalRuleMasterApproveFormat(payload, session));
        results.put("ruleLevel", approvalConfigurationService.getPossibleApprovalRuleList(payload, session));
        return results;
    }

    /*
     * 1. 메소드명: insertApprovalRule
     * 2. 클래스명: approvalConfiguration
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    새로운 승인 룰을 삽입하고 Audit 로그를 기록
     * 2. 사용법
     *    POST 요청으로 승인 룰 삽입
     * 3. 예시 데이터
     *    - Input: 승인 룰 데이터
     *    - Output: 삽입된 승인 룰 개수
     * </PRE>
     * @param payload 삽입할 승인 룰 데이터
     * @param session 사용자 세션
     * @return 삽입된 승인 룰 개수
     */
    @PostMapping("/insertApprovalRule")
    public Integer insertApprovalRule(@RequestBody Map<String, String> payload, HttpSession session) {
        Integer resultCount = approvalConfigurationService.insertApprovalRule(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Creation");
        param.put("condition", String.valueOf(payload));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", resultCount.toString());
        auditLogService.addAuditLog(session, param);

        return resultCount;
    }

    /*
     * 1. 메소드명: updateApprovalRule
     * 2. 클래스명: approvalConfiguration
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    승인 룰을 업데이트하고 Audit 로그를 기록
     * 2. 사용법
     *    POST 요청으로 승인 룰 수정
     * 3. 예시 데이터
     *    - Input: 수정할 승인 룰 데이터
     *    - Output: 수정된 승인 룰 개수
     * </PRE>
     * @param payload 수정할 승인 룰 데이터
     * @param session 사용자 세션
     * @return 수정된 승인 룰 개수
     */
    @PostMapping("/updateApprovalRule")
    public Integer updateApprovalRule(@RequestBody Map<String, String> payload, HttpSession session) {
        Integer resultCount = approvalConfigurationService.updateApprovalRule(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Modification");
        param.put("condition", String.valueOf(payload));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", resultCount.toString());
        auditLogService.addAuditLog(session, param);

        return resultCount;
    }

    /*
     * 1. 메소드명: deleteApprovalRule
     * 2. 클래스명: approvalConfiguration
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    승인 룰을 삭제하고 Audit 로그를 기록
     * 2. 사용법
     *    POST 요청으로 승인 룰 삭제
     * 3. 예시 데이터
     *    - Input: 삭제할 승인 룰 ID
     *    - Output: 삭제된 승인 룰 개수
     * </PRE>
     * @param payload 삭제할 승인 룰 데이터
     * @param session 사용자 세션
     * @return 삭제된 승인 룰 개수
     */
    @PostMapping("/deleteApprovalRule")
    public Integer deleteApprovalRule(@RequestBody Map<String, String> payload, HttpSession session) {
        Integer resultCount = approvalConfigurationService.deleteApprovalRule(Integer.parseInt(payload.get("id")));
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Deletion");
        param.put("condition", payload.get("id"));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", resultCount.toString());
        auditLogService.addAuditLog(session, param);

        return resultCount;
    }

    /*
     * 1. 메소드명: searchUserManagment
     * 2. 클래스명: approvalConfiguration
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    승인룰의 유저를 검색하고 Audit 로그를 기록
     * 2. 사용법
     *    GET 요청으로 사용자 관리 정보 조회
     * 3. 예시 데이터
     *    - Input: 사용자 ID
     *    - Output: 사용자 관리 정보 및 메시지
     * </PRE>
     * @param userId 사용자 ID
     * @param session 사용자 세션
     * @param model 모델 객체
     * @return 사용자 관리 정보
     */
    @GetMapping("/searchUserManagement")
    public Map<String, Object> searchUserManagment(@RequestParam("userId") String userId, HttpSession session, Model model) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        Map<String, Object> userManagement = approvalConfigurationService.searchUserManagement(params,session);
        List<Object> resultList = (List<Object>) userManagement.get("result");

         // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Search");
        param.put("condition", params.get("userId").toString());
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", String.valueOf(resultList.size()));
        auditLogService.addAuditLog(session, param);

        Map<String, Object> results = new HashMap<>();
        int count = resultList.size();
        results.put("count", count);
        if(count > 0){
            results.put("result", resultList.get(0));
            results.put("message", "데이터 가져오기 성공");
        } else {
            results.put("message", "데이터가 없습니다.");
        }
        log.warn("results : {}", results);
        return results;
    }

    /*
     * 1. 메소드명: insertApprovalAdmin
     * 2. 클래스명: ApprovalConfigurationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    승인 관리자 정보를 삽입하고 Audit 로그를 기록
     * 2. 사용법
     *    POST 요청으로 승인 관리자 정보를 삽입
     * 3. 예시 데이터
     *    - Input: 승인 관리자 정보 (JSON 형식)
     *    - Output: 삽입된 관리자 수
     * </PRE>
     * @param payload 삽입할 승인 관리자 데이터
     * @param session 사용자 세션
     * @return 삽입된 승인 관리자 수
     */
    @PostMapping("/insertApprovalAdmin")
    public Integer insertApprovalAdmin(@RequestBody Map<String, String> payload, HttpSession session) {
        Integer resultCount = approvalConfigurationService.insertApprovalAdmin(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Creation");
        param.put("condition", payload.get("id"));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", resultCount.toString());
        auditLogService.addAuditLog(session, param);

        return resultCount;
    }

    /*
     * 1. 메소드명: deleteApprovalAdmin
     * 2. 클래스명: ApprovalConfigurationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    승인 관리자 정보를 삭제하고 Audit 로그를 기록
     * 2. 사용법
     *    POST 요청으로 승인 관리자 정보 삭제
     * 3. 예시 데이터
     *    - Input: 삭제할 관리자 정보 (JSON 형식)
     *    - Output: 삭제된 관리자 수
     * </PRE>
     * @param payload 삭제할 승인 관리자 데이터
     * @param session 사용자 세션
     * @return 삭제된 승인 관리자 수
     */
    @PostMapping("/deleteApprovalAdmin")
    public Integer deleteApprovalAdmin(@RequestBody Map<String, String> payload, HttpSession session) {
        Integer resultCount = approvalAdminService.deleteApprovalAdmin(payload);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Modification");
        param.put("condition", payload.get("id"));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", resultCount.toString());
        auditLogService.addAuditLog(session, param);

        return resultCount;
    }

    /*
     * 1. 메소드명: saveApprovalRuleMasterStage
     * 2. 클래스명: ApprovalConfigurationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    승인 룰 마스터 단계를 저장하고 Audit 로그를 기록
     * 2. 사용법
     *    POST 요청으로 승인 룰 마스터 단계 저장
     * 3. 예시 데이터
     *    - Input: 승인 룰 마스터 단계 정보 (JSON 형식)
     *    - Output: 저장된 승인 룰 개수
     * </PRE>
     * @param payload 승인 룰 마스터 단계 데이터
     * @param session 사용자 세션
     * @return 저장된 승인 룰 개수
     */
    @PostMapping("/saveApprovalRuleMasterStage")
    public Integer saveApprovalRuleMasterStage(@RequestBody Map<String, String> payload, HttpSession session) {
        Integer resultCount = approvalConfigurationService.saveApprovalRuleMasterStage(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "Deletion");
        param.put("condition", payload.get("id"));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", resultCount.toString());
        auditLogService.addAuditLog(session, param);

        return resultCount;
    }

    /*
     * 1. 메소드명: selectApprovalAdminList
     * 2. 클래스명: ApprovalConfigurationController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    승인 관리자 목록을 조회하고 Audit 로그를 기록
     * 2. 사용법
     *    POST 요청으로 승인 관리자 목록 조회
     * 3. 예시 데이터
     *    - Input: 승인 관리자 검색 조건 (JSON 형식)
     *    - Output: 승인 관리자 목록
     * </PRE>
     * @param payload 승인 관리자 검색 조건
     * @param session 사용자 세션
     * @return 승인 관리자 목록
     */
    @PostMapping("/selectApprovalAdminList")
    public List<Map<String, Object>> selectApprovalAdminList(@RequestBody Map<String, String> payload, HttpSession session) {
        List<Map<String, Object>> resultList = approvalAdminService.selectApprovalAdminList(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Approval_Configuration");
        param.put("action", "ListView"); // 6가지 : ListView, DetailedView, View, Search, Creation, Modification
        param.put("condition", String.valueOf(payload));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", String.valueOf(resultList.size()));
        auditLogService.addAuditLog(session, param);
        return resultList;
    }
}
