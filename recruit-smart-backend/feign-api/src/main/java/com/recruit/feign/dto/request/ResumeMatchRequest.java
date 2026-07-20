package com.recruit.feign.dto.request;

import lombok.Data;

@Data
public class ResumeMatchRequest {
    private Long applicationId;
    private Long jobId;
    private Long candidateId;
    private Long resumeId;
    private String jobTitle;
    private String responsibilities;
    private String requirements;
    private String resumeText;
    private String skills;
    private String projectExperience;
    private String workExperience;
}
