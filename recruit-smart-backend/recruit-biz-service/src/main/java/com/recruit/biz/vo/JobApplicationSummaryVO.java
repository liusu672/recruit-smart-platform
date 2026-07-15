package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobApplicationSummaryVO {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String department;
    private Long resumeId;
    private String resumeName;
    private String status;
    private String statusText;
    private Integer allowAdjustment;
    private Long adjustedJobId;
    private LocalDateTime appliedAt;
}
