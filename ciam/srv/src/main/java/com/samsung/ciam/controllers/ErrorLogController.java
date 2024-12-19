package com.samsung.ciam.controllers;

import com.samsung.ciam.models.ErrorLog;
import com.samsung.ciam.repositories.CommonCodeRepository;
import com.samsung.ciam.repositories.ErrorLogRepository;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
public class ErrorLogController {

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private ErrorLogRepository errorLogRepository;

    @GetMapping("/errorLogList")
    public ModelAndView errorLogList(
            ServletRequest servletRequest,
            HttpServletRequest request,
            Model model
    ) {

        ModelAndView modelAndView = new ModelAndView("errorLogList");
        modelAndView.addObject("errorMessage", commonCodeRepository.findByHeaderOrderBySortOrder("ERROR_MESSAGE"));

        return modelAndView;
    }

    @GetMapping("/errorLogListSearch")
    @ResponseBody
    public List<ErrorLog> errorLogListSearch(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String errorMessage,
            @RequestParam(required = false, defaultValue = "ALL") String completionFlag // 기본값을 "ALL"로 설정
    ) {
        // 날짜 데이터를 "yyyy-MM-dd HH:mm:ss" 형식으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime formattedStartDate = (startDate != null && !startDate.isEmpty())
                ? LocalDateTime.parse(startDate + " 00:00:00", formatter)
                : null;

        LocalDateTime formattedEndDate = (endDate != null && !endDate.isEmpty())
                ? LocalDateTime.parse(endDate + " 23:59:59", formatter)
                : null;

        String preparedErrorMessage = (errorMessage != null && !errorMessage.trim().isEmpty())
                ? "%" + errorMessage.trim() + "%"
                : null;

        // "ALL"을 처리하여 NULL로 전달, "true"/"false"는 그대로
        String preparedCompletionFlag = (completionFlag.equals("ALL")) ? null : completionFlag;

        return errorLogRepository.searchErrorLogs(
                formattedStartDate,
                formattedEndDate,
                preparedErrorMessage,
                preparedCompletionFlag
        );
    }

    @PostMapping("/updateCompletion")
    @ResponseBody
    public ResponseEntity<String> updateCompletion(@RequestBody Map<String, List<Long>> payload) {
        List<Long> ids = payload.get("ids"); // 체크박스에서 선택된 ID 리스트를 받음
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("선택된 항목이 없습니다.");
        }

        try {
            // 현재 서울 시간 기준으로 날짜 생성
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

            // ID 리스트를 기반으로 completion_flag 및 completion_at 업데이트
            ids.forEach(id -> {
                ErrorLog errorLog = errorLogRepository.findById(id).orElse(null);
                if (errorLog != null) {
                    errorLog.setCompletionFlag(true); // completion_flag 값을 true로 설정
                    errorLog.setCompletionAt(now); // completion_at 값을 현재 시간으로 설정
                    errorLogRepository.save(errorLog); // 변경 사항 저장
                }
            });

            return ResponseEntity.ok("완료 처리가 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("완료 처리 중 오류가 발생했습니다.");
        }
    }


}
