package com.recruit.ai.service;

import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;

public interface AiMatchResultService {

    void saveResult(Long taskId,
                    ResumeMatchRequest request,
                    ResumeMatchResponse response,
                    String source);
}