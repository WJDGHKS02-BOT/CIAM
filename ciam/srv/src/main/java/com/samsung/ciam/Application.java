package com.samsung.ciam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
//@EnableScheduling  // 스케줄러 활성화
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
