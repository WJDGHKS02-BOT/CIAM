package com.samsung.ciam.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorController {

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

    @RequestMapping("/error/mandatoryfields")
    public String handleMandatoryFieldsError(HttpServletRequest request, Model model) {
        // Retrieve the message dynamically from the request parameters
        String message = request.getParameter("message");

        // Set default message if none is provided
        if (message == null || message.isEmpty()) {
            message = "Some mandatory fields are missing or invalid. Please check and try again.";
        }

        // Add the message to the model
        model.addAttribute("message", message);

        // Redirect to the mandatory fields error page
        return "error/mandatoryfields";
    }
}
