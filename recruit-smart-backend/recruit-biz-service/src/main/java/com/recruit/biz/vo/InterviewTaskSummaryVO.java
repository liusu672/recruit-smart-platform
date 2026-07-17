package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewTaskSummaryVO {
    private Long id;
    private Long applicationId;
    private Long candidateId;
    private String candidateName;
    private String jobTitle;
    private String department;
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
    private Long interviewerId;
    private String interviewerName;
    private String feedbackState;
    private String feedbackStateText;
}
