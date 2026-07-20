package com.recruit.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.recruit.biz.mapper")
@SpringBootApplication(scanBasePackages = "com.recruit")
public class RecruitBizApp {
    public static void main(String[] args){
        SpringApplication.run(RecruitBizApp.class,args);
    }
}

