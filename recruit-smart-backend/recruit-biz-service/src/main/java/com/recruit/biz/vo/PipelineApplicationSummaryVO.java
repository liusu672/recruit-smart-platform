package com.recruit.biz.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PipelineApplicationSummaryVO {
    private Long id;
    private Long candidateId;
    private String candidateName;
    private String education;
    private Integer yearsOfExperience;
    private Long jobId;
    private String jobTitle;
    private String department;
    private String status;
    private String statusText;
    private BigDecimal matchScore;
    private String recommendLevel;
    private Long ownerId;
    private String ownerName;
    private String source;
    private String sourceText;
    private String reviewDecision;
    private LocalDateTime appliedAt;
    private LocalDateTime lastActivityAt;
}
