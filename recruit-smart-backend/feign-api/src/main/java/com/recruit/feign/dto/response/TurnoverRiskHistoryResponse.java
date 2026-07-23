package com.recruit.feign.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TurnoverRiskHistoryResponse {

    private Long id;

    private Long taskId;

    private Long employeeId;

    private String sentimentLabel;

    private Integer sentimentRiskScore;

    private String sentimentSummary;

    private String riskLevel;

    private Integer riskScore;

    private String summary;

    private List<String> riskReasons;

    private List<String> suggestions;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private List<Long> behaviorRecordIds;

    private String source;

    private String modelName;

    private String promptVersion;

    private LocalDateTime generatedAt;
}