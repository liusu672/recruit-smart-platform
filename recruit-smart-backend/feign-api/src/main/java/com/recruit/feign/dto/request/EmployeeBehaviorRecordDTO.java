package com.recruit.feign.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeBehaviorRecordDTO {

    private Long recordId;
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
}