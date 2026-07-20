package com.recruit.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_match_result")
public class AiMatchResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private Long applicationId;
    private Long jobId;
    private Long candidateId;
    private Long resumeId;

    private BigDecimal matchScore;
    private String recommendLevel;
    private String recommendReason;
    private String highlightSummary;
    private String matchedPoints;
    private String riskSummary;
    private String riskPoints;
    private String suggestion;
    private String source;
    private String modelName;
    private String promptVersion;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}