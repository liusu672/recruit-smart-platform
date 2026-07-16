package com.recruit.ai.demo.knowledge.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KnowledgeBuildVO {

    private Boolean success;

    private Integer totalChunks;

    private Integer totalDocuments;

    private String sourcePath;

    private String message;
}