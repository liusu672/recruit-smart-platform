package com.recruit.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_feedback_summary")
public class AiFeedbackSummary {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long interviewId;
    private Long candidateId;
    private Long jobId;

    private String summary;

    /**
     * JSON数组字符串
     */
    private String advantages;

    /**
     * JSON数组字符串
     */
    private String risks;

    private String suggestion;

    private String source;
    private String modelName;
    private String promptVersion;

    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}