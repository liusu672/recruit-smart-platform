package com.recruit.biz.vo;

import lombok.Data;

@Data
public class OfferCandidateOptionVO {
    private Long applicationId;
    private Long candidateId;
    private String candidateName;
    private Long jobId;
    private String jobTitle;
    private String department;
    private Integer interviewScore;
    private String interviewSuggestion;
}
