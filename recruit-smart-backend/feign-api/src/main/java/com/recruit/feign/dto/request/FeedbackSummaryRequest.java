package com.recruit.feign.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class FeedbackSummaryRequest {
    private Long interviewId;
    private Long candidateId;
    private Long jobId;
    private String jobTitle;
    private String candidateName;
    private String interviewRound;
    private String feedbackText;
    private Integer score;
    private String suggestion;
    private List<FeedbackScoreItemRequest> scorecard;
}
