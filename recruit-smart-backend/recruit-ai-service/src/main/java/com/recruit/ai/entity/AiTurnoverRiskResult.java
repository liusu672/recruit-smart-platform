package com.recruit.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_turnover_risk_result")
public class AiTurnoverRiskResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long employeeId;

    private String riskLevel;
    private Integer riskScore;
    private String summary;

    /**
     * JSON数组字符串
     */
    private String riskReasons;

    /**
     * JSON数组字符串
     */
    private String suggestions;

    private String source;
    private String modelName;
    private String promptVersion;

    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}