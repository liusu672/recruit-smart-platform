package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DashboardTaskVO {
    private String type;
    private Long relatedId;
    private Long applicationId;
    private Long candidateId;
    private String candidateName;
    private Long jobId;
    private String jobTitle;
    private String title;
    private String status;
    private String statusText;
    private LocalDateTime occurredAt;
}
