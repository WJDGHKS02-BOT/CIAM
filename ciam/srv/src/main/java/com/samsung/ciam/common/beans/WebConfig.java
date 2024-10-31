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

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {


    @Autowired
    private SamlInterceptor samlInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(60 * 60 * 24 * 365);  // Cache period of 1 year
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("langLocale");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());

        registry.addInterceptor(samlInterceptor)
                .addPathPatterns("/myPage/**")
                .excludePathPatterns("/sso/login", "/error", "/sso/metadata","/myPage/selectChannel","/myPage/selectedChannel","/api/restapi/extension/sec/retryapprovalflow","/api/restapi/extension/sec/cdcInsertEmployData", "/api/restapi/extension/sec/updateConsentContentPublished", "/consent/consentView");
                ///myPage/cdcInsertEmployData 2024.08.21 kimjy 재직인증 cdc i/f test용 임시 url
    }

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

    /*@Bean
    public FilterRegistrationBean<CharacterEncodingFilter> characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        FilterRegistrationBean<CharacterEncodingFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }*/

}