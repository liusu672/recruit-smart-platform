package com.recruit.ai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI recruitAiOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Recruit Smart AI Service API")
                        .description("智能招聘平台 AI 模块接口文档")
                        .version("1.0"));
    }

    @Bean
    public GroupedOpenApi aiApi() {
        return GroupedOpenApi.builder()
                .group("ai-service")
                .packagesToScan("com.recruit.ai.controller")
                .pathsToMatch("/**")
                .build();
    }
}
