package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewDetailVO {
    private Long id;
    private Long applicationId;
    private String applicationStatus;
    private Long jobId;
    private String jobTitle;
    private String department;
    private Long candidateId;
    private String candidateName;
    private Long resumeId;
    private String resumeName;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
