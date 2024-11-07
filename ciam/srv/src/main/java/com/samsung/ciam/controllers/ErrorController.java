package com.samsung.ciam.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 1. 파일명   : ErrorController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : 오류 페이지 처리를 위한 컨트롤러, 권한 오류 및 필수 필드 누락 오류 등을 처리
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
@Controller
public class ErrorController {

    /*
     * 1. 메소드명: handle403
     * 2. 클래스명: ErrorController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    403 접근 권한 에러 페이지를 처리하는 메소드.
     * 2. 사용법
     *    사용자가 권한이 없는 페이지에 접근할 경우, 해당 페이지로 리다이렉트되어 오류 메시지를 표시.
     * 3. 예시 데이터
     *    - Input (HTTP 요청):
     *      request URI: /error/errorPage
     *      요청 파라미터: message (선택적으로 오류 메시지 설정 가능)
     *    - Output:
     *      403 에러 페이지로 이동하여 오류 메시지 표시.
     * </PRE>
     * @param request HttpServletRequest 객체
     * @param model 모델 객체
     * @return String - "error/403" 뷰 페이지 경로
     */
    @RequestMapping("/error/errorPage")
    public String handle403(HttpServletRequest request, Model model) {
        // 메시지를 요청 파라미터에서 동적으로 가져옵니다.
        String message = request.getParameter("message");

        // 기본 메시지 설정
        if (message == null || message.isEmpty()) {
            message = "You do not have permission to access this page.";
        }

        // 모델에 메시지를 추가
        model.addAttribute("message", message);

        // 403 에러 페이지로 이동
        return "error/403";
    }

    /*
     * 1. 메소드명: handleMandatoryFieldsError
     * 2. 클래스명: ErrorController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    필수 필드 누락 오류 페이지를 처리하는 메소드.
     * 2. 사용법
     *    필수 필드가 누락되었거나 잘못된 입력이 있을 경우 오류 메시지를 표시하는 페이지로 리다이렉트.
     * 3. 예시 데이터
     *    - Input (HTTP 요청):
     *      request URI: /error/mandatoryfields
     *      요청 파라미터: message (선택적으로 오류 메시지 설정 가능)
     *    - Output:
     *      필수 필드 누락 오류 페이지로 이동하여 오류 메시지 표시.
     * </PRE>
     * @param request HttpServletRequest 객체
     * @param model 모델 객체
     * @return String - "error/mandatoryfields" 뷰 페이지 경로
     */
    @RequestMapping("/error/mandatoryfields")
    public String handleMandatoryFieldsError(HttpServletRequest request, Model model) {
        // 요청 파라미터에서 메시지를 동적으로 가져옴
        String message = request.getParameter("message");

        // 메시지가 없을 경우 기본 메시지 설정
        if (message == null || message.isEmpty()) {
            message = "Some mandatory fields are missing or invalid. Please check and try again.";
        }

        // 모델에 메시지 추가
        model.addAttribute("message", message);

        // 필수 필드 누락 오류 페이지로 이동
        return "error/mandatoryfields";
    }
}
