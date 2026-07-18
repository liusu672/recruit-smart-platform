package com.recruit.ai.service;

import com.recruit.ai.dto.request.FeedbackSummaryRequest;
import com.recruit.ai.dto.response.FeedbackSummaryResponse;

public interface AiFeedbackSummaryResultService {

    void saveResult(Long taskId,
                    FeedbackSummaryRequest request,
                    FeedbackSummaryResponse response,
                    String source);
}