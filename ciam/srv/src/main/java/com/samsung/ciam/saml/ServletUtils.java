package com.samsung.ciam.saml;

import com.onelogin.saml2.http.HttpRequest;
import com.onelogin.saml2.util.Util;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 1. FileName   : SamlInterceptor.java
 * 2. Package    : com.samsung.ciam.common.interceptor
 * 3. Comments   : SAML 인증 요청에 대한 인터셉터로, 사용자 인증 및 채널 선택을 수행
 * 4. Author     : 서정환
 * 5. DateTime   : 2024. 11. 04.
 * 6. History    :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * Date        | Name           | Comment
 * <p>
 * -------------  -----------------   ------------------------------
 * <p>
 * 2024. 11. 04.        | 서정환            | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
public class ServletUtils {
    private ServletUtils() {
    }

    /*
     * 1. 메소드명: makeHttpRequest
     * 2. 클래스명: ServletUtils
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    HttpServletRequest 객체로부터 SAML HttpRequest 객체를 생성.
     * 2. 사용법
     *    HttpServletRequest 객체를 HttpRequest로 변환하여 SAML 요청을 처리할 때 사용.
     * 3. 예시 데이터
     *    - Input: HttpServletRequest req
     *    - Output: 변환된 HttpRequest 객체
     * </PRE>
     * @param req HttpServletRequest 객체
     * @return 변환된 HttpRequest 객체
     */
    public static HttpRequest makeHttpRequest(HttpServletRequest req) {
        Map<String, String[]> paramsAsArray = req.getParameterMap();
        Map<String, List<String>> paramsAsList = new HashMap<>();

        for (Map.Entry<String, String[]> param : paramsAsArray.entrySet()) {
            List<String> valuesList = Arrays.asList(param.getValue());
            paramsAsList.put(param.getKey(), valuesList);
        }

        return new HttpRequest(req.getRequestURL().toString(), paramsAsList, req.getQueryString());
    }

    /*
     * 1. 메소드명: getSelfURLhost
     * 2. 클래스명: ServletUtils
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    요청의 호스트 URL을 반환하며, 포트 번호가 80, 443이 아닌 경우 포트도 포함.
     * 2. 사용법
     *    현재 요청의 호스트 URL을 얻기 위해 사용.
     * 3. 예시 데이터
     *    - Input: HttpServletRequest request
     *    - Output: 호스트 URL 문자열
     * </PRE>
     * @param request HttpServletRequest 객체
     * @return 호스트 URL 문자열
     */
    public static String getSelfURLhost(HttpServletRequest request) {
        String hostUrl = "";
        int serverPort = request.getServerPort();
        if (serverPort != 80 && serverPort != 443 && serverPort != 0) {
            hostUrl = String.format("%s://%s:%s", request.getScheme(), request.getServerName(), serverPort);
        } else {
            hostUrl = String.format("%s://%s", request.getScheme(), request.getServerName());
        }

        return hostUrl;
    }

    /*
     * 1. 메소드명: getSelfHost
     * 2. 클래스명: ServletUtils
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    서버 이름을 반환하여, 요청의 호스트명을 얻음.
     * 2. 사용법
     *    현재 요청의 호스트명만을 얻기 위해 사용.
     * 3. 예시 데이터
     *    - Input: HttpServletRequest request
     *    - Output: 호스트 이름 문자열
     * </PRE>
     * @param request HttpServletRequest 객체
     * @return 호스트 이름 문자열
     */
    public static String getSelfHost(HttpServletRequest request) {
        return request.getServerName();
    }

    /*
     * 1. 메소드명: isHTTPS
     * 2. 클래스명: ServletUtils
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    현재 요청이 HTTPS를 통해 이루어졌는지 여부를 반환.
     * 2. 사용법
     *    요청이 HTTPS인지 확인할 때 사용.
     * 3. 예시 데이터
     *    - Input: HttpServletRequest request
     *    - Output: HTTPS 여부 (boolean)
     * </PRE>
     * @param request HttpServletRequest 객체
     * @return HTTPS 여부 (true/false)
     */
    public static boolean isHTTPS(HttpServletRequest request) {
        return request.isSecure();
    }

    /*
     * 1. 메소드명: getSelfURL
     * 2. 클래스명: ServletUtils
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    현재 요청의 전체 URL을 반환함 (쿼리 문자열 포함).
     * 2. 사용법
     *    요청의 전체 URL을 얻을 때 사용.
     * 3. 예시 데이터
     *    - Input: HttpServletRequest request
     *    - Output: 전체 URL 문자열
     * </PRE>
     * @param request HttpServletRequest 객체
     * @return 전체 URL 문자열
     */
    public static String getSelfURL(HttpServletRequest request) {
        String url = getSelfURLhost(request);
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        if (null != requestUri && !requestUri.isEmpty()) {
            url = url + requestUri;
        }

        if (null != queryString && !queryString.isEmpty()) {
            url = url + '?' + queryString;
        }

        return url;
    }

    /*
     * 1. 메소드명: getSelfURLNoQuery
     * 2. 클래스명: ServletUtils
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    현재 요청의 전체 URL에서 쿼리 문자열 없이 반환.
     * 2. 사용법
     *    쿼리 문자열을 제외한 URL을 얻을 때 사용.
     * 3. 예시 데이터
     *    - Input: HttpServletRequest request
     *    - Output: 쿼리 없는 URL 문자열
     * </PRE>
     * @param request HttpServletRequest 객체
     * @return 쿼리 문자열이 제외된 URL 문자열
     */
    public static String getSelfURLNoQuery(HttpServletRequest request) {
        return request.getRequestURL().toString();
    }

    /*
     * 1. 메소드명: getSelfRoutedURLNoQuery
     * 2. 클래스명: ServletUtils
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    현재 요청의 쿼리 문자열 없이 라우팅된 URL을 반환.
     * 2. 사용법
     *    쿼리 문자열 없이 라우팅된 URL을 얻을 때 사용.
     * 3. 예시 데이터
     *    - Input: HttpServletRequest request
     *    - Output: 라우팅된 URL 문자열
     * </PRE>
     * @param request HttpServletRequest 객체
     * @return 쿼리 없는 라우팅된 URL 문자열
     */
    public static String getSelfRoutedURLNoQuery(HttpServletRequest request) {
        String url = getSelfURLhost(request);
        String requestUri = request.getRequestURI();
        if (null != requestUri && !requestUri.isEmpty()) {
            url = url + requestUri;
        }

        return url;
    }

    /*
     * 1. 메소드명: sendRedirect
     * 2. 클래스명: ServletUtils
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    리디렉션 위치와 매개변수를 설정하여 리디렉션을 수행.
     *    stay 플래그가 true일 경우 리디렉션하지 않고 URL만 반환.
     * 2. 사용법
     *    특정 URL로 리디렉션을 수행할 때 사용.
     * 3. 예시 데이터
     *    - Input: HttpServletResponse response, location, parameters, stay
     *    - Output: 리디렉션된 URL 문자열
     * </PRE>
     * @param response HttpServletResponse 객체
     * @param location 리디렉션할 위치 URL
     * @param parameters URL에 추가할 매개변수
     * @param stay 리디렉션 여부 (true일 경우 리디렉션하지 않음)
     * @return 리디렉션된 URL 문자열
     * @throws IOException 입력/출력 오류 발생 시
     */
    public static String sendRedirect(HttpServletResponse response, String location, Map<String, String> parameters, Boolean stay) throws IOException {
        String target = location;
        if (!parameters.isEmpty()) {
            boolean first = !location.contains("?");
            Iterator var6 = parameters.entrySet().iterator();

            while(var6.hasNext()) {
                Map.Entry<String, String> parameter = (Map.Entry)var6.next();
                if (first) {
                    target = target + "?";
                    first = false;
                } else {
                    target = target + "&";
                }

                target = target + (String)parameter.getKey();
                if (!((String)parameter.getValue()).isEmpty()) {
                    target = target + "=" + Util.urlEncoder((String)parameter.getValue());
                }
            }
        }

        if (!stay) {
            response.sendRedirect(target);
        }

        return target;
    }

    /*
     * 1. 메소드명: sendRedirect (오버로드)
     * 2. 클래스명: ServletUtils
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    리디렉션 위치와 매개변수를 설정하여 리디렉션을 수행. (stay는 기본 false로 설정)
     * 2. 사용법
     *    기본적으로 리디렉션을 수행할 때 사용.
     * 3. 예시 데이터
     *    - Input: response, location, parameters
     *    - Output: 리디렉션된 URL
     * </PRE>
     * @param response HttpServletResponse 객체
     * @param location 리디렉션할 위치 URL
     * @param parameters URL에 추가할 매개변수
     * @throws IOException 입력/출력 오류 발생 시
     */
    public static void sendRedirect(HttpServletResponse response, String location, Map<String, String> parameters) throws IOException {
        sendRedirect(response, location, parameters, false);
    }

    /*
     * 1. 메소드명: sendRedirect (오버로드)
     * 2. 클래스명: ServletUtils
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    리디렉션 위치를 설정하여 리디렉션을 수행 (매개변수 없이).
     * 2. 사용법
     *    단순 리디렉션을 수행할 때 사용.
     * 3. 예시 데이터
     *    - Input: response, location
     *    - Output: 리디렉션된 URL
     * </PRE>
     * @param response HttpServletResponse 객체
     * @param location 리디렉션할 위치 URL
     * @throws IOException 입력/출력 오류 발생 시
     */
    public static void sendRedirect(HttpServletResponse response, String location) throws IOException {
        Map<String, String> parameters = new HashMap();
        sendRedirect(response, location, parameters);
    }
}

