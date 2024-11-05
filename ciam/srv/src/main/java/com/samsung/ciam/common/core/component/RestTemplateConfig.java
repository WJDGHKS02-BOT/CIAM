package com.samsung.ciam.common.core.component;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 1. FileName	: RestTemplateConfig.java
 * 2. Package	: com.samsung.ciam.common.core.component
 * 3. Comments	: RestTemplate 빈을 생성하여 외부 API 통신에 사용할 수 있도록 설정
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
public class RestTemplateConfig {

    /*
     * 1. 메소드명: restTemplate
     * 2. 클래스명: RestTemplateConfig
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    외부 API와 통신하기 위한 RestTemplate 빈 생성
     * 2. 사용법
     *    RestTemplate을 주입받아 외부 HTTP 요청에 사용
     * </PRE>
     * @return RestTemplate 외부 API 통신을 위한 RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}