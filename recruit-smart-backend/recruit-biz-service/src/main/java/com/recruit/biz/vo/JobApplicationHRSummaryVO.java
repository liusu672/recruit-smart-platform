package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobApplicationHRSummaryVO {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateName;
    private String gender;
    private Integer age;
    private String education;
    private String school;
    private String major;
    private Integer yearsOfExperience;
    private Long resumeId;
    private String resumeName;
    private String resumeFileType;
    private String status;
    private String statusText;
    private Integer allowAdjustment;
    private Long adjustedJobId;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
}
