package com.recruit.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_knowledge_document")
public class AiKnowledgeDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String documentKey;
    private String sourcePath;
    private String contentHash;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private Integer chunkCount;
    private String status;
    private String lastError;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}