package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeDetailVO {
    private Long id;
    private Long userId;
    private Long candidateId;
    private Long onboardingId;
    private String employeeNo;
    private String name;
    private String phone;
    private String email;
    private String department;
    private String position;
    private LocalDate entryDate;
    private String status;
    private String statusText;
    private String performanceSummary;
    private String attendanceSummary;
    private String satisfactionFeedback;
    private String turnoverRiskLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
