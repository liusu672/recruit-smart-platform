package com.recruit.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_interview_question")
public class AiInterviewQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long interviewId;
    private Long jobId;
    private Long candidateId;
    private Long resumeId;
    private String category;
    private String summary;
    private String questions;
    private String source;
    private String modelName;
    private String promptVersion;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
