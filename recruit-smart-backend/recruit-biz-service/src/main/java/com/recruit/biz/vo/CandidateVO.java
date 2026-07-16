package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CandidateVO {

    private Long id;

    private String name;

    private String gender;

    private Integer age;

    private String phone;

    private String email;

    private String education;

    private String school;

    /**
     * 专业，不是主修课程
     */
    private String major;

    private Integer yearsOfExperience;


    private String currentStatus;

    private String source;

    /**
     * 是否已经绑定登录账号
     */
    private Boolean hasAccount;

    private LocalDateTime createdAt;
}
