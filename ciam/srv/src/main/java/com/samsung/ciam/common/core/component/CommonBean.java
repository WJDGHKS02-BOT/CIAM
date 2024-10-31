package com.samsung.ciam.common.core.component;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class CommonBean {

    @Bean(name="env")
    public Environment getEnv(Environment env) {
        return env;
    }
}
