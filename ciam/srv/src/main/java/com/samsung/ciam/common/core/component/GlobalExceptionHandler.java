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

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private ErrorLogRepository errorLogRepository;

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