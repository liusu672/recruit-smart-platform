package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewSummaryVO {
    private Long id;
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateName;
    private Long interviewerId;
    private String interviewerName;
    private String round;
    private String roundText;
    private LocalDateTime interviewTime;
    private String method;
    private String methodText;
    private String location;
    private String status;
    private String statusText;
    private LocalDateTime assignedAt;
    private LocalDateTime scheduledAt;
    private Integer feedbackScore;
    private String feedbackSuggestion;
}
