package com.samsung.ciam.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsung.ciam.common.cpi.service.CpiApiService;
import com.samsung.ciam.services.ApprovalService;
import com.samsung.ciam.services.CdcTraitService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * 1. 파일명   : CdcApiController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : CDC API를 호출하여 사용자 계정 정보와 상태를 관리하는 컨트롤러
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
public class CdcApiController {

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private CpiApiService cpiApiService;

    /*
     * 1. 메소드명: getAccountInfo
     * 2. 클래스명: CdcApiController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    UID를 사용하여 CDC API에서 사용자 계정 정보를 조회
     * 2. 사용법
     *    POST 요청으로 UID를 포함한 페이로드 전달 시 계정 정보 반환
     * 3. 예시 데이터
     *    - Input: UID를 포함한 요청 페이로드
     *    - Output: CDC API로부터 조회된 사용자 계정 정보 (JsonNode 형식)
     * </PRE>
     * @param payload UID를 포함한 요청 데이터
     * @param session 사용자 세션 객체
     * @param redirectAttributes 리다이렉트 시 사용할 속성 객체
     * @return CDC로부터 반환된 사용자 계정 정보 (JsonNode 형식)
     */
    @ResponseBody
    @PostMapping("/getAccountInfo")
    public JsonNode getAccountInfo(@RequestBody Map<String, String> payload, HttpSession session, RedirectAttributes redirectAttributes) {
        JsonNode jsonNode =  cdcTraitService.getCdcUser(payload.get("uid"),0);

        return jsonNode;
    }

    /*
     * 1. 메소드명: setAccountStatus
     * 2. 클래스명: CdcApiController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    사용자 계정의 정보를 CDC API를 통해 설정
     * 2. 사용법
     *    POST 요청으로 상태 값을 포함한 페이로드 전달 시 계정 상태 설정
     * 3. 예시 데이터
     *    - Input: 계정 상태와 관련된 페이로드
     *    - Output: 설정 결과 메시지
     * </PRE>
     * @param payload 계정 상태와 관련된 요청 데이터
     * @param session 사용자 세션 객체
     * @param redirectAttributes 리다이렉트 시 사용할 속성 객체
     * @return 설정 결과 메시지 (String)
     */
    @ResponseBody
    @PostMapping("/setAccountStatus")
    public String setAccountStatus(@RequestBody Map<String, String> payload, HttpSession session, RedirectAttributes redirectAttributes) {
       return cdcTraitService.setAccountStatus(payload ,session,redirectAttributes);
    }

    /*
     * 1. 메소드명: resetPassword
     * 2. 클래스명: CdcApiController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    패스워드 초기화 -> 로그인페이지에서 사용
     * 2. 사용법
     *    POST 요청으로 패스워드 바꿀 이메일 계정
     * 3. 예시 데이터
     *    - Input: 이메일
     *    - Output: 성공 결과 값
     * </PRE>
     * @return 설정 결과 메시지 (String)
     */
    @ResponseBody
    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody Map<String, String> payload) {
        return cdcTraitService.resetPassword(payload.get("email"));
    }

    @PostMapping("/updateConsents")
    @ResponseBody
    public String updateConsents(
            @RequestBody Map<String, Object> requestParams,
            HttpSession session) {
        return cdcTraitService.updateConsents(requestParams,session);
    }

    @PostMapping("/processWorkflow")
    @ResponseBody
    public String processWorkflow(@RequestBody Map<String, String> wfParams, HttpSession session) {
        return approvalService.processWorkflow(wfParams,session);
    }

    @PostMapping("/createContact")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createContact(@RequestBody Map<String, String> payload, HttpSession session) {
        String contactId = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode accountNode = cdcTraitService.getCdcUser(payload.get("uid"), 0);

            Map<String, Object> createdContact = cpiApiService.createContact("Context1", objectMapper.convertValue(accountNode, Map.class), session, "");

            if (createdContact != null && createdContact.get("contact") instanceof List) {
                // 응답값 타입에 따른 체크 -> LIST OR MAP
                List<Map<String, Object>> contacts = (List<Map<String, Object>>) createdContact.get("contact");
                if (!contacts.isEmpty()) {
                    // 만들어진 contactID값 조회
                    contactId = contacts.get(contacts.size() - 1).get("contactid").toString();
                }
            } else if (createdContact != null && createdContact.get("contact") instanceof Map) {
                Map<String, Object> cmdmContact = (Map<String, Object>) createdContact.get("contact");
                contactId = cmdmContact.getOrDefault("contactid", "").toString();
            }

            log.info("Creating new contact in CMDM done, new contactID: {}", contactId);

            // 성공: contactId를 반환
            return ResponseEntity.ok(Map.of(
                    "result", "success",
                    "contactId", contactId,
                    "errorCode", 200
            ));
        } catch (Exception e) {
            log.error("Error while creating contact: {}", e.getMessage(), e);

            // 에러: 에러 메시지를 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "result", "fail",
                            "msg", e.getCause() != null ? e.getCause().getMessage() : e.getMessage(),
                            "errorCode", 500
                    ));
        }
    }

}
