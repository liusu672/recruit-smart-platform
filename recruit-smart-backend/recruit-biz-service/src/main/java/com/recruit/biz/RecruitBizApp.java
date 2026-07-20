package com.recruit.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.recruit.biz.mapper")
@EnableFeignClients(basePackages = "com.recruit.feign.client")
@SpringBootApplication(scanBasePackages = "com.recruit")
public class RecruitBizApp {
    public static void main(String[] args){
        SpringApplication.run(RecruitBizApp.class,args);
    }
}

