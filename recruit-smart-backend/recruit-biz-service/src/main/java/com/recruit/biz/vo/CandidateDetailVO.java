package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CandidateDetailVO {

    private Long id;
    private String name;
    private String gender;
    private Integer age;
    private String phone;
    private String email;
    private String education;
    private String school;
    private String major;
    private Integer yearsOfExperience;
    private String currentStatus;
    private String source;
    private Boolean hasAccount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ResumeSummaryVO> resumes;
}
