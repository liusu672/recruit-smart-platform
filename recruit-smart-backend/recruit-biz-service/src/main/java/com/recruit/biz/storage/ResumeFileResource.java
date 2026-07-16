package com.recruit.biz.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
public class ResumeFileResource {
    private Resource resource;
    private String fileName;
    private String contentType;
    private long contentLength;
}
