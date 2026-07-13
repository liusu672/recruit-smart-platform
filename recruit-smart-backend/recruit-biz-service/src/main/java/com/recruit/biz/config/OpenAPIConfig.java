package com.recruit.biz.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI recruitOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("智能招聘与人才管理平台接口文档")
                        .description("传统业务接口")
                        .version("1.0.0")
                );
    }
}
