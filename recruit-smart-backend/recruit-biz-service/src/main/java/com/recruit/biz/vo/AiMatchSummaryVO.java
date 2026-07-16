package com.recruit.biz.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AiMatchSummaryVO {
    private BigDecimal matchScore;
    private String recommendLevel;
    private String recommendReason;
    private String highlightSummary;
    private String riskSummary;
    private String modelName;
    private LocalDateTime generatedAt;
}
