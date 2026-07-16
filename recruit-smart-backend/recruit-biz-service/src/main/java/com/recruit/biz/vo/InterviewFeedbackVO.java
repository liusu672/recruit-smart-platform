package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewFeedbackVO {
    private Long id;
    private Long interviewId;
    private Long interviewerId;
    private String interviewerName;
    private Integer score;
    private String comment;
    private String suggestion;
    private String suggestionText;
    private String aiSummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
