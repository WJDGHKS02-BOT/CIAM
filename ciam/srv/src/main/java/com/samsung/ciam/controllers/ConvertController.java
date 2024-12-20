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

/**
 * 1. 파일명   : ConvertController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : 채널 전환 가입 시 필요한 정보 설정 및 언어 로케일을 설정하는 컨트롤러
 * 4. 작성자   : 서정환
 * 5. 작성일자 : 2024. 11. 04.
 * 6. 히스토리 :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * 날짜         | 이름         | 설명
 * <p>
 * -------------|--------------|------------------------------------
 * <p>
 * 2024. 11. 04 | 서정환       | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@Controller
@RequestMapping("/convert")
public class ConvertController {

    private final LocaleResolver localeResolver;

    // LocaleResolver를 주입받음
    public ConvertController(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    /*
     * 1. 메소드명: convert
     * 2. 클래스명: ConvertController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    채널 전환 시 필요 정보를 세션에 설정하고 필요한 경우 언어 로케일을 설정
     * 2. 사용법
     *    POST 요청으로 채널 정보를 전달하면 세션에 저장하고, 로케일 설정 시 langLocale 파라미터를 사용
     * 3. 예시 데이터
     *    - Input: 채널 이름, 사용자 정보 및 로케일
     *    - Output: 등록 페이지로 리디렉션
     * </PRE>
     * @param channel 채널 이름
     * @param payload 사용자 정보와 필수값을 포함한 파라미터 맵
     * @param langLocale 로케일 코드
     * @param session 세션 객체
     * @param request HTTP 요청 객체
     * @return RedirectView 등록 페이지로 리다이렉트
     */
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

            // 미리 tcpp_language(약관 언어) 값을 세팅
            if (!isNullOrEmpty(payload.get("languages"))) {
                String language = extractLanguageCode(payload.get("languages"));
                language = "en_US".equals(language) ? "en" : "ko_KR".equals(language) ? "ko" : language;
                session.setAttribute("tcpp_language", language);
            }

            // 필요한 채널전환 필드 key 리스트
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
        else if ("jcext".equalsIgnoreCase(channel)) {
            // 필수값 체크: countryCode, loginID, channelUID가 없거나 빈 값일 경우 에러 페이지로 이동
            if (    isNullOrEmpty(payload.get("loginID")) ||
                    isNullOrEmpty(payload.get("channelUID"))) {
                return new RedirectView("/error/mandatoryfields"); // 필수 값 누락 시 에러 페이지로 리다이렉트
            }

            // 미리 tcpp_language(약관 언어) 값을 세팅
            if (!isNullOrEmpty(payload.get("languages"))) {
                String language = extractLanguageCode(payload.get("languages"));
                language = "en_US".equals(language) ? "en" : "ko_KR".equals(language) ? "ko" : language;
                session.setAttribute("tcpp_language", language);
            }

            // 필요한 채널전환 필드 key 리스트
            List<String> keys = Arrays.asList(
                    "loginID", "channelUID"
            );

            // key가 없으면 빈 문자열로 설정
            keys.forEach(key -> payload.putIfAbsent(key, ""));

            // session에 값 설정
            session.setAttribute("convertYn", "Y");
            session.setAttribute("convertData", payload);
            session.setAttribute("channelUID", payload.get("channelUID"));

            // langLocale 파라미터가 있을 경우 로케일 설정
            if (langLocale != null && !langLocale.isEmpty()) {
                Locale locale = Locale.forLanguageTag(langLocale);
                localeResolver.setLocale(request, null, locale); // 새로운 로케일 설정
            }
        }
        // sba가 아닌 다른 채널일 경우 channelUID만 설정
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

    /*
     * 1. 메소드명: extractLanguageCode
     * 2. 클래스명: ConvertController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    국가 코드가 포함된 언어 코드에서 언어 부분만 추출
     * 2. 사용법
     *    국가 코드가 포함된 언어 코드를 전달하면 언어 코드만 반환
     * 3. 예시 데이터
     *    - Input: "en_US"
     *    - Output: "en"
     * </PRE>
     * @param languages 국가 코드가 포함된 언어 코드
     * @return 언어 코드
     */
    private String extractLanguageCode(String languages) {
        if (languages.contains("_")) {
            return languages.split("_")[0]; // en_US -> en, ko_KR -> ko
        }
        return languages; // 언어 코드만 있을 경우 그대로 리턴
    }

    /*
     * 1. 메소드명: isNullOrEmpty
     * 2. 클래스명: ConvertController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    값이 null이거나 빈 문자열인지 확인
     * 2. 사용법
     *    값을 전달하면 null이거나 빈 문자열일 경우 true 반환
     * 3. 예시 데이터
     *    - Input: null
     *    - Output: true
     * </PRE>
     * @param value 검증할 문자열
     * @return boolean 값이 null이거나 빈 문자열이면 true, 아니면 false
     */
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}