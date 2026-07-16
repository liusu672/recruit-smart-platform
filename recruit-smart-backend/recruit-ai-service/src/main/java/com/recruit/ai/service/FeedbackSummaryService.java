package com.recruit.ai.service;

import com.recruit.ai.dto.request.FeedbackSummaryRequest;
import com.recruit.ai.dto.response.FeedbackSummaryResponse;

public interface FeedbackSummaryService {
    FeedbackSummaryResponse generateSummary(FeedbackSummaryRequest request);
}
