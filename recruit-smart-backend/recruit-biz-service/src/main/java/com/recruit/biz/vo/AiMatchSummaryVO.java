package com.recruit.biz.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AiMatchSummaryVO {
    private BigDecimal matchScore;
    private String recommendLevel;
    private String recommendReason;
    private String highlightSummary;
    private List<String> matchedPoints;
    private String riskSummary;
    private List<String> riskPoints;
    private String suggestion;
    private String source;
    private String modelName;
    private LocalDateTime generatedAt;
}
