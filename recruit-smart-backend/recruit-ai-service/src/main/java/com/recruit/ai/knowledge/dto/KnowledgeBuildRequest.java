package com.recruit.ai.knowledge.dto;

import lombok.Data;

@Data
public class KnowledgeBuildRequest {
    private String documentPath;
    private Integer chunkSize;
    private Integer chunkOverlap;
}