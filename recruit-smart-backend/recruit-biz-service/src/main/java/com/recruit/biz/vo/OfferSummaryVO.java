package com.recruit.biz.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OfferSummaryVO {
    private Long id;
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private String department;
    private BigDecimal salary;
    private LocalDate entryDate;
    private String workLocation;
    private String status;
    private String statusText;
    private LocalDateTime sentAt;
    private LocalDateTime acceptedAt;
}
