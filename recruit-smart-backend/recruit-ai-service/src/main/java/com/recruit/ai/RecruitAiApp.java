package com.recruit.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@MapperScan("com.recruit.ai.mapper")
@SpringBootApplication
public class RecruitAiApp {
    public static void main(String[] args) {
        SpringApplication.run(RecruitAiApp.class, args);
    }
}
