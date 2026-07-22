package com.recruit.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("employee_profile")
public class EmployeeProfile {
    @TableId(type = IdType.AUTO)
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
    private String performanceSummary;
    private Integer performanceScore;
    private String attendanceSummary;
    private Integer attendanceScore;
    private String satisfactionFeedback;
    private Integer satisfactionScore;
    private String turnoverRiskLevel;
    private LocalDateTime riskAssessedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
