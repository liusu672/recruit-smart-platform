package com.recruit.ai.knowledge.dto;

import lombok.Data;

@Data
public class KnowledgeBuildResponse {

    private boolean success;
    private boolean skipped;
    private String documentKey;
    private String contentHash;
    private Integer totalChunks;
    private Integer totalDocuments;
    private String sourcePath;
    private String message;
}