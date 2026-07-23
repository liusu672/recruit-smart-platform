package com.recruit.feign.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TurnoverRiskRequest {

    private Long employeeId;
    private String employeeName;
    private String department;
    private String position;

    /*
     * 旧版单期字段暂时保留，
     * 避免现有代码在改造完成前无法编译。
     */
    private String performanceSummary;
    private String attendanceSummary;
    private String satisfactionFeedback;
    private String interviewFeedback;
    private Integer performanceScore;
    private Integer attendanceScore;
    private Integer satisfactionScore;

    /*
     * 新版多周期分析字段。
     */
    private LocalDate periodStart;
    private LocalDate periodEnd;

    private List<EmployeeBehaviorRecordDTO> behaviorRecords;

    private String performanceTrend;
    private String attendanceTrend;
    private String satisfactionTrend;

    private String latestFeedback;
}