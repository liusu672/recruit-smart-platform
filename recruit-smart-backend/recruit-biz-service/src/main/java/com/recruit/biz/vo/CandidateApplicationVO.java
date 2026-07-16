package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CandidateApplicationVO {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String department;
    private Long resumeId;
    private String status;
    private String statusText;
    private Boolean allowAdjustment;
    private String source;
    private String hrNote;
    private LocalDateTime appliedAt;
    private AiMatchSummaryVO aiMatch;
}
