package com.samsung.ciam.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/convert")
public class ConvertController {

    private final LocaleResolver localeResolver;

    // LocaleResolver를 주입받음
    public ConvertController(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @PostMapping("/{channel}")
    public RedirectView convert(
            @PathVariable String channel,
            @RequestParam Map<String, String> payload,
            @RequestParam(value = "langLocale", required = false) String langLocale, // langLocale 파라미터 추가
            HttpSession session,
            HttpServletRequest request) {

        // sba 채널일 경우에만 추가 로직 적용
        if ("sba".equalsIgnoreCase(channel)) {
            // 필수값 체크: countryCode, loginID, channelUID가 없거나 빈 값일 경우 에러 페이지로 이동
            if (isNullOrEmpty(payload.get("countryCode")) ||
                    isNullOrEmpty(payload.get("loginID")) ||
                    isNullOrEmpty(payload.get("channelUID"))) {
                return new RedirectView("/error/mandatoryfields"); // 필수 값 누락 시 에러 페이지로 리다이렉트
            }

            // 미리 tcpp_language 값을 세팅
            if (!isNullOrEmpty(payload.get("languages"))) {
                String language = extractLanguageCode(payload.get("languages"));
                language = "en_US".equals(language) ? "en" : "ko_KR".equals(language) ? "ko" : language;
                session.setAttribute("tcpp_language", language);
            }

            // 필요한 key 리스트
            List<String> keys = Arrays.asList(
                    "firstName", "lastName", "loginID", "cmdmAccountID", "department", "userPhone",
                    "languages", "countryCode", "channelType", "industryType", "gbm1", "gbm2",
                    "gbm3", "gbm4", "channelUID", "subsidiaryName", "userType"
            );

            // key가 없으면 빈 문자열로 설정
            keys.forEach(key -> payload.putIfAbsent(key, ""));

            // session에 값 설정
            session.setAttribute("convertYn", "Y");
            session.setAttribute("convertData", payload);
            session.setAttribute("channelUID", payload.get("channelUID"));
            session.setAttribute("convertAccountId", payload.get("cmdmAccountID"));


            // langLocale 파라미터가 있을 경우 로케일 설정
            if (langLocale != null && !langLocale.isEmpty()) {
                Locale locale = Locale.forLanguageTag(langLocale);
                localeResolver.setLocale(request, null, locale); // 새로운 로케일 설정
            }
        }
        // gmapvd 또는 gmapda 채널일 경우 channelUID만 설정
        else {
            List<String> keys = Arrays.asList(
                    "channelUID"
            );

            // key가 없으면 빈 문자열로 설정
            keys.forEach(key -> payload.putIfAbsent(key, ""));

            session.setAttribute("channelUID", payload.get("channelUID"));
        }

        return new RedirectView("/registration/" + channel);
    }

    // 국가 코드가 포함된 언어 코드를 처리하는 메소드
    private String extractLanguageCode(String languages) {
        if (languages.contains("_")) {
            return languages.split("_")[0]; // en_US -> en, ko_KR -> ko
        }
        return languages; // 언어 코드만 있을 경우 그대로 리턴
    }

    // 필수 값이 null이거나 빈 문자열인지 확인하는 메소드
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}