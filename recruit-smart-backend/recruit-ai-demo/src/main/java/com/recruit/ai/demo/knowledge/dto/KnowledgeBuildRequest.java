package com.recruit.ai.demo.knowledge.dto;

import lombok.Data;

@Data
public class KnowledgeBuildRequest {

    private String documentPath;

    private Integer chunkSize = 500;

    private Integer chunkOverlap = 50;
}