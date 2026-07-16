package com.recruit.ai.demo.knowledge.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class KnowledgeSearchResultVO {

    private String content;

    private String sourceDocument;

    private Integer chunkIndex;

    private Map<String, Object> metadata;
}