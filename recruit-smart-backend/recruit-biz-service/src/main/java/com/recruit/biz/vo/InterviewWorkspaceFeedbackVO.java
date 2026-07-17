package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewWorkspaceFeedbackVO {
    private Long id;
    private Long interviewId;
    private Long interviewerId;
    private Integer score;
    private String comment;
    private String suggestion;
    private String aiSummary;
    private String state;
    private LocalDateTime submittedAt;
}
