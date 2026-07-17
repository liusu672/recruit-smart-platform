package com.recruit.ai.knowledge.dto;

import lombok.Data;

import java.util.Map;

@Data
public class KnowledgeSearchResult {
    private String content;
    private String sourceDocument;
    private Integer chunkIndex;
    private Integer startOffset;
    private Integer endOffset;
    private Map<String, Object> metadata;
}