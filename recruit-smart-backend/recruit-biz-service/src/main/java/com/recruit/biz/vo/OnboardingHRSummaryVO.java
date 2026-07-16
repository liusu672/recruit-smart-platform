package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OnboardingHRSummaryVO {
    private Long id;
    private Long offerId;
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private String department;
    private Long candidateId;
    private String candidateName;
    private String phone;
    private String email;
    private LocalDate entryDate;
    private String status;
    private String statusText;
    private String currentStep;
    private String materialStatus;
    private String materialStatusText;
    private String remark;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
