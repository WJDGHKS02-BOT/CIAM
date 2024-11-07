package com.samsung.ciam.controllers;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.samsung.ciam.repositories.ConsentContentRepository;
import com.samsung.ciam.services.ApprovalConfigurationService;
import com.samsung.ciam.services.SystemContentsService;
import com.samsung.ciam.services.UserProfileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 1. 파일명   : SystemConsentsController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : 약관 마스터 관리 기능을 제공하는 컨트롤러
 * 4. 작성자   : 한국민
 * 5. 작성일자 : 2024. 11. 04.
 * 6. 히스토리 :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * 날짜         | 이름         | 설명
 * <p>
 * -------------|--------------|------------------------------------
 * <p>
 * 2024. 11. 04 | 한국민       | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@RestController
@RequestMapping("/systemConsents")
public class SystemConsentsController {
    
    @Autowired
    private SystemContentsService systemContentsService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private ApprovalConfigurationService approvalConfigurationService;

    @Autowired
    private ConsentContentRepository consentContentRepository;

    /*
     * 1. 메소드명: searchConsentManagement
     * 2. 클래스명: SystemConsentsController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관 관리 목록을 검색하여 반환
     * 2. 사용법
     *    요청 바디에 검색 조건을 포함하여 POST 요청으로 호출
     * 3. 예시 데이터
     *    - Input: 약관 유형 및 검색 조건이 포함된 맵
     *    - Output: 검색된 약관 관리 리스트
     * </PRE>
     * @param payload 요청 파라미터 맵
     * @param session 사용자 세션
     * @return 약관 관리 리스트
     */
    @PostMapping("/consentManagementList")
    public List<Map<String, Object>> searchConsentManagement(@RequestBody Map<String, String> payload, HttpSession session) {
        return systemContentsService.searchConsentManagement(payload, session);
    }

    /*
     * 1. 메소드명: insertConsentManagement
     * 2. 클래스명: SystemConsentsController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    새로운 약관을 저장
     * 2. 사용법
     *    약관 데이터를 요청 파라미터로 포함하여 POST 요청으로 호출
     * 3. 예시 데이터
     *    - Input: 약관 정보가 포함된 파라미터 맵
     *    - Output: 삽입 결과에 따라 리다이렉트 URL 반환
     * </PRE>
     * @param request HTTP 요청 객체
     * @param session 사용자 세션
     * @param payload 요청 파라미터 맵
     * @param model 모델 객체
     * @param redirectAttributes 리다이렉트 속성 객체
     * @return 삽입 후 리다이렉트 뷰
     * @throws ParseException 파싱 예외 발생 시
     */
    @PostMapping("/insertConsentManagement")
    public RedirectView insertConsentManagement(HttpServletRequest request,HttpSession session,@RequestParam Map<String, String> payload, Model model, RedirectAttributes redirectAttributes) throws ParseException {
        systemContentsService.insertConsentManagement(payload, session, request);
        return userProfileService.consentManager(request, session, model, redirectAttributes);
    }

    /*
     * 1. 메소드명: updateConsentManagement
     * 2. 클래스명: SystemConsentsController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    기존 약관을 업데이트
     * 2. 사용법
     *    업데이트할 약관 데이터를 요청 파라미터로 포함하여 POST 요청으로 호출
     * 3. 예시 데이터
     *    - Input: 업데이트할 약관 정보가 포함된 파라미터 맵
     *    - Output: 업데이트 후 리다이렉트 URL 반환
     * </PRE>
     * @param request HTTP 요청 객체
     * @param session 사용자 세션
     * @param payload 요청 파라미터 맵
     * @param model 모델 객체
     * @param redirectAttributes 리다이렉트 속성 객체
     * @return 업데이트 후 리다이렉트 뷰
     * @throws ParseException 파싱 예외 발생 시
     */
    @PostMapping("/updateConsentManagement")
    public RedirectView updateConsentManagement(HttpServletRequest request,HttpSession session,@RequestParam Map<String, String> payload, Model model, RedirectAttributes redirectAttributes) throws ParseException {
        if(consentContentRepository.getStatusId(Integer.parseInt(payload.get("id"))).equals("published")) {
            systemContentsService.insertConsentManagement(payload, session, request);
        } else {
            systemContentsService.updateConsentManagement(payload, session, request);
        }
        return userProfileService.consentManager(request, session, model, redirectAttributes);
    }

    /*
     * 1. 메소드명: duplicationConsentCheck
     * 2. 클래스명: SystemConsentsController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    중복된 약관이 있는지 확인
     * 2. 사용법
     *    확인할 약관 데이터를 요청 바디로 포함하여 POST 요청으로 호출
     * 3. 예시 데이터
     *    - Input: 중복 여부 확인을 위한 약관 데이터
     *    - Output: 중복 여부 결과 맵
     * </PRE>
     * @param payload 요청 파라미터 맵
     * @param session 사용자 세션
     * @param request HTTP 요청 객체
     * @param redirectAttributes 리다이렉트 속성 객체
     * @return 중복 여부 결과 맵
     */
    @PostMapping("/duplicationConsentCheck")
    public Map<String, Object> duplicationConsentCheck(@RequestBody Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        return systemContentsService.duplicationConsentCheck(payload, session, request, redirectAttributes);
    }

    /*
     * 1. 메소드명: searchApprovalConfiguration
     * 2. 클래스명: SystemConsentsController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    승인 구성 목록을 검색하여 반환
     * 2. 사용법
     *    요청 바디에 검색 조건을 포함하여 POST 요청으로 호출
     * 3. 예시 데이터
     *    - Input: 승인 구성 검색 조건
     *    - Output: 승인 구성 리스트
     * </PRE>
     * @param payload 요청 파라미터 맵
     * @param session 사용자 세션
     * @return 승인 구성 리스트
     */
    @PostMapping("/approvalConfigurationList")
    public List<Map<String, Object>> searchApprovalConfiguration(@RequestBody Map<String, String> payload, HttpSession session) {
        return approvalConfigurationService.searchApprovalConfiguration(payload, session);
    }
}
