package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.summary.FeedbackSummaryAlgorithm;
import com.recruit.ai.dto.request.FeedbackSummaryRequest;
import com.recruit.ai.dto.response.FeedbackSummaryResponse;
import com.recruit.ai.service.FeedbackSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackSummaryServiceImpl implements FeedbackSummaryService {
    private final FeedbackSummaryAlgorithm feedbackSummaryAlgorithm;

    @Override
    public FeedbackSummaryResponse generateSummary(FeedbackSummaryRequest request) {
        return feedbackSummaryAlgorithm.generate(request);
    }
}
