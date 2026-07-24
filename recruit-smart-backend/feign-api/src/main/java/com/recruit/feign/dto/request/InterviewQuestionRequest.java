package com.recruit.feign.dto.request;

import lombok.Data;

@Data
public class InterviewQuestionRequest {
    private Long interviewId;
    private String interviewRound;
    private Long jobId;
    private Long candidateId;
    private Long resumeId;
    private String jobTitle;
    private String requirements;
    private String responsibilities;
    private String resumeText;
    private String skills;
    private String projectExperience;
    private String workExperience;
}
