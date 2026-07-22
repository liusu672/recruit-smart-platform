package com.recruit.ai.service;

import com.recruit.ai.dto.response.ResumeParseResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeParseService {
    ResumeParseResponse parseResume(MultipartFile file);
}
