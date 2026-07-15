package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobApplicationDetailVO {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String department;
    private Long candidateId;
    private String candidateName;
    private Long resumeId;
    private String resumeName;
    private String resumeFileType;
    private String status;
    private String statusText;
    private Integer allowAdjustment;
    private Long adjustedJobId;
    private String source;
    private String rejectReasonCode;
    private String rejectReason;
    private String hrNote;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
