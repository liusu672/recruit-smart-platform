package com.recruit.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class RecruitAiApp {
    public static void main(String[] args) {
        SpringApplication.run(RecruitAiApp.class, args);
    }
}
