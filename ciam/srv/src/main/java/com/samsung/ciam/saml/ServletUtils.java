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

public class ServletUtils {
    private ServletUtils() {
    }

    public static HttpRequest makeHttpRequest(HttpServletRequest req) {
        Map<String, String[]> paramsAsArray = req.getParameterMap();
        Map<String, List<String>> paramsAsList = new HashMap<>();

        for (Map.Entry<String, String[]> param : paramsAsArray.entrySet()) {
            List<String> valuesList = Arrays.asList(param.getValue());
            paramsAsList.put(param.getKey(), valuesList);
        }

        return new HttpRequest(req.getRequestURL().toString(), paramsAsList, req.getQueryString());
    }

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

    public static String getSelfHost(HttpServletRequest request) {
        return request.getServerName();
    }

    public static boolean isHTTPS(HttpServletRequest request) {
        return request.isSecure();
    }

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

    public static String getSelfURLNoQuery(HttpServletRequest request) {
        return request.getRequestURL().toString();
    }

    public static String getSelfRoutedURLNoQuery(HttpServletRequest request) {
        String url = getSelfURLhost(request);
        String requestUri = request.getRequestURI();
        if (null != requestUri && !requestUri.isEmpty()) {
            url = url + requestUri;
        }

        return url;
    }

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

    public static void sendRedirect(HttpServletResponse response, String location, Map<String, String> parameters) throws IOException {
        sendRedirect(response, location, parameters, false);
    }

    public static void sendRedirect(HttpServletResponse response, String location) throws IOException {
        Map<String, String> parameters = new HashMap();
        sendRedirect(response, location, parameters);
    }
}

