package com.recruit.biz.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OfferDetailVO {
    private Long id;
    private Long applicationId;
    private String applicationStatus;
    private Long jobId;
    private String jobTitle;
    private String department;
    private Long candidateId;
    private String candidateName;
    private BigDecimal salary;
    private LocalDate entryDate;
    private Integer probationMonths;
    private String workLocation;
    private String status;
    private String statusText;
    private String remark;
    private LocalDateTime sentAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
