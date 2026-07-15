package com.recruit.biz.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OnboardingDetailVO {
    private Long id;
    private Long offerId;
    private String offerStatus;
    private Long applicationId;
    private String applicationStatus;
    private Long jobId;
    private String jobTitle;
    private String department;
    private Long candidateId;
    private String candidateName;
    private String phone;
    private String email;
    private BigDecimal salary;
    private LocalDate entryDate;
    private Integer probationMonths;
    private String workLocation;
    private String status;
    private String statusText;
    private String currentStep;
    private String materialStatus;
    private String materialStatusText;
    private String remark;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
