package com.recruit.ai.service;

import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.response.InterviewQuestionResponse;

public interface AiInterviewQuestionResultService {

    void saveResult(Long taskId,
                    InterviewQuestionRequest request,
                    InterviewQuestionResponse response,
                    String source);
}