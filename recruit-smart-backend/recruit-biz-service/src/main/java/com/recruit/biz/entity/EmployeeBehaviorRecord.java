package com.recruit.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("employee_behavior_record")
public class EmployeeBehaviorRecord {

    @TableId(type = IdType.AUTO)
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