package com.recruit.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_task")
public class AiTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskType;
    private Long bizId;
    private String bizType;

    private String status;
    private String source;

    private String modelName;
    private String promptVersion;

    private String errorMessage;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}