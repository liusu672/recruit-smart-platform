package com.recruit.biz.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class InterviewCandidateBriefVO {
    private String education;
    private String school;
    private Integer yearsOfExperience;
    private String resumeName;
    private List<String> skills;
    private String workExperience;
    private String projectExperience;
    private BigDecimal matchScore;
    private String matchSummary;
    private List<String> riskPoints;
}
