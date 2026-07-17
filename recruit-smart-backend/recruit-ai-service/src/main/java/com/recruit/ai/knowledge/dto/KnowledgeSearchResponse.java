package com.recruit.ai.knowledge.dto;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeSearchResponse {
    private boolean success;
    private String query;
    private Integer total;
    private List<KnowledgeSearchResult> results;
    private String message;
}