package com.recruit.ai.dto.request;

import lombok.Data;

@Data
public class FeedbackSummaryRequest {
    private Long interviewId;
    private Long candidateId;
    private Long jobId;

    private String jobTitle;
    private String candidateName;
    private String feedbackText;
    private Integer score;
}
