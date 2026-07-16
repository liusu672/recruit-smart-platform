package com.recruit.biz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "resume.storage")
public class ResumeStorageProperties {
    private String rootPath;
    private Long maxSize;
}
