package com.samsung.ciam.common.beans;

import com.samsung.ciam.common.interceptor.SamlInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.LocaleResolver;
import java.util.Locale;

/**
 * 1. FileName	: WebConfig.java
 * 2. Package	: com.samsung.ciam.common.beans
 * 3. Comments	:
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

/*
 * 1. 클래스명	: WebConfig
 * 2. 파일명	: WebConfig.java
 * 3. 패키지명	: com.samsung.ciam.common.beans
 * 4. 작성자명	: 서정환
 * 5. 작성일자	: 2024. 11. 04.
 */

/**
 * <PRE>
 * 1. 설명
 *  WEB설정 관리 파일 -> 리소스(이미지,CSS), 로케일(다국어) 언어 설정 관리, 인터셉터 관리
 * </PRE>
 */

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {


    @Autowired
    private SamlInterceptor samlInterceptor;

    /*
     * 1. 메소드명: addResourceHandlers
     * 2. 클래스명: WebConfig
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    정적 리소스 핸들러 등록 (이미지, CSS 등) 및 캐시 기간 설정
     * 2. 사용법
     *    addResourceHandlers(ResourceHandlerRegistry registry)
     * </PRE>
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(60 * 60 * 24 * 365);  // Cache period of 1 year
    }

    /*
     * 1. 메소드명: localeResolver
     * 2. 클래스명: WebConfig
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    기본 로케일을 영어로 설정하는 로케일 리졸버 정의
     * 2. 사용법
     *    localeResolver()
     * </PRE>
     * @return LocaleResolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }

    /*
     * 1. 메소드명: localeChangeInterceptor
     * 2. 클래스명: WebConfig
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    로케일 변경 인터셉터 등록 (langLocale 파라미터를 통해 언어 변경 지원)
     * 2. 사용법
     *    localeChangeInterceptor()
     * </PRE>
     * @return LocaleChangeInterceptor
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("langLocale");
        return lci;
    }

    /*
     * 1. 메소드명: addInterceptors
     * 2. 클래스명: WebConfig
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    요청에 대한 인터셉터 설정 (SAML 및 로케일 변경 인터셉터 추가)
     * 2. 사용법
     *    addInterceptors(InterceptorRegistry registry)
     * </PRE>
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());

        //인터셉터 제외 URL 등록및 MYPAGE URL 엔드포인트 설정
        registry.addInterceptor(samlInterceptor)
                .addPathPatterns("/myPage/**")
                .excludePathPatterns("/sso/login", "/error", "/sso/metadata","/myPage/selectChannel","/myPage/selectedChannel","/api/restapi/extension/sec/retryapprovalflow","/api/restapi/extension/sec/cdcInsertEmployData", "/api/restapi/extension/sec/updateConsentContentPublished", "/consent/consentView");
    }

    /*
     * 1. 메소드명: addCorsMappings
     * 2. 클래스명: WebConfig
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CORS (Cross-Origin Resource Sharing) 설정으로 특정 도메인, 메서드 및 헤더 허용
     * 2. 사용법
     *    addCorsMappings(CorsRegistry registry)
     * </PRE>
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // "*" 패턴으로 모든 도메인 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("Authorization", "Content-Type")
                .exposedHeaders("Custom-Header")
                .allowCredentials(true)
                .maxAge(3600);
    }

}