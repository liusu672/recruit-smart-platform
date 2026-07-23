package com.recruit.biz.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeBehaviorRecordVO {

    private Long id;
    private Long employeeId;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    private Integer performanceScore;
    private String performanceSummary;
    private BigDecimal taskCompletionRate;

    private Integer lateCount;
    private BigDecimal absenceDays;
    private BigDecimal leaveDays;
    private BigDecimal overtimeHours;
    private Integer attendanceScore;
    private String attendanceSummary;

    private Integer satisfactionScore;
    private String feedbackText;

    private String sourceType;
    private String recordStatus;
    private Long createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}