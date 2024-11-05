package com.samsung.ciam.common.core.component;

import com.samsung.ciam.models.ErrorLog;
import com.samsung.ciam.repositories.ErrorLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * 1. FileName	: GlobalExceptionHandler.java
 * 2. Package	: com.samsung.ciam.common.core.component
 * 3. Comments	: 전역 예외 처리를 담당하여 발생한 예외를 처리하고, 오류를 로그 및 데이터베이스에 저장
 * 4. Author	: 서정환
 * 5. DateTime	: 2024. 11. 04.
 * 6. History	:
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * Date		 |	Name			|	Comment
 * <p>
 * -------------  -----------------   ------------------------------
 * <p>
 * 2024. 11. 04.		 | 서정환			|	최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private ErrorLogRepository errorLogRepository;

    /*
     * 1. 메소드명: handleException
     * 2. 클래스명: GlobalExceptionHandler
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    애플리케이션 전역에서 발생하는 예외를 처리하고, 필요한 경우 예외 정보를 데이터베이스에 저장 후 에러 페이지로 이동
     * 2. 사용법
     *    handleException(Exception ex, HttpServletRequest request, HttpSession session)
     * </PRE>
     * @param ex 발생한 예외 객체
     * @param request 예외가 발생한 HTTP 요청 정보
     * @param session 현재 사용자 세션 정보
     * @return ModelAndView 에러 페이지로 리다이렉트하기 위한 ModelAndView 객체
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex, HttpServletRequest request, HttpSession session) {
        String errorMessage = ex.getMessage();
        String id ="";

        // "No static resource" 문자열이 포함된 경우 DB에 기록하지 않음
        if (errorMessage != null && !errorMessage.contains("No static resource")) {
            // 스택 트레이스를 문자열로 변환
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String stackTrace = sw.toString(); // 전체 스택 트레이스

            // 스택 트레이스에서 첫 번째 요소(가장 최근 발생한 에러) 가져오기
            StackTraceElement element = ex.getStackTrace()[0];
            String methodName = element.getMethodName();
            String className = element.getClassName();
            int lineNumber = element.getLineNumber();

            String email="";
            String uid = "";

            if(session.getAttribute("loginUserId")!=null) {
                email = (String) session.getAttribute("loginUserId");
            }
            if(session.getAttribute("uid")!=null) {
                uid = (String) session.getAttribute("uid");
            }

            if("".equals(email) && session.getAttribute("cdc_email")!=null) {
                email = (String) session.getAttribute("cdc_email");
            }
            if("".equals(uid) && session.getAttribute("cdc_uid")!=null) {
                uid = (String) session.getAttribute("cdc_uid");
            }

            // 에러 로그 객체 생성
            ErrorLog errorLog = new ErrorLog(
                    errorMessage,
                    stackTrace,
                    uid,  // 사용자 ID (로그인된 사용자 ID)
                    request.getRequestURI(),  // 에러가 발생한 URL
                    methodName,               // 에러가 발생한 메소드 이름
                    lineNumber,               // 에러가 발생한 라인 번호
                    className,                // 에러가 발생한 클래스 이름
                    "system",
                    email
            );

            // 에러 로그를 데이터베이스에 저장
            errorLogRepository.save(errorLog);

            // log.error()로 에러 기록 (Slack으로 전송됨)
            log.error("Exception occurred - ID: {}, URL: {}, Class: {}, Method: {}, Line: {}, Message: {}",
                    id, request.getRequestURI(), className, methodName, lineNumber, errorMessage, ex);

            id = errorLog.getId().toString();
        }

        // 에러 페이지로 이동
        ModelAndView modelAndView = new ModelAndView("error/commonError"); // 에러 페이지로 리다이렉트
        modelAndView.addObject("message", errorMessage); // 메시지 전달
        modelAndView.addObject("errorNo", id); // 메시지 전달
        return modelAndView;
    }
}