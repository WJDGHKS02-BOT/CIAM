package com.samsung.ciam.common.core.component;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 1. FileName	: CommonBean.java
 * 2. Package	: com.samsung.ciam.common.core.component
 * 3. Comments	: Spring Environment 객체를 Bean으로 등록하여 전체 애플리케이션에서 환경 설정을 쉽게 접근할 수 있도록 함
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
@Configuration
public class CommonBean {

    /*
     * 1. 메소드명: getEnv
     * 2. 클래스명: CommonBean
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    Spring의 Environment 객체를 Bean으로 등록하여, 애플리케이션 전체에서 환경 설정에 접근할 수 있도록 함
     * 2. 사용법
     *    getEnv(Environment env)
     * </PRE>
     * @param env Spring의 Environment 객체
     * @return Environment 설정된 환경 객체
     */
    @Bean(name="env")
    public Environment getEnv(Environment env) {
        return env;
    }
}
