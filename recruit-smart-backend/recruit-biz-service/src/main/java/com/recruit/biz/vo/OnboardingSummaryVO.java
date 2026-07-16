package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OnboardingSummaryVO {
    private Long id;
    private Long offerId;
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private String department;
    private LocalDate entryDate;
    private String status;
    private String statusText;
    private String currentStep;
    private String materialStatus;
    private String materialStatusText;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
