package com.recruit.ai.dto.request;

import lombok.Data;

@Data
public class TurnoverRiskRequest {
    private Long employeeId;
    private String employeeName;
    private String department;
    private String position;

    private String performanceSummary;
    private String attendanceSummary;
    private String satisfactionFeedback;
    private String interviewFeedback;
    private Integer performanceScore;
    private Integer attendanceScore;
    private Integer satisfactionScore;
}
