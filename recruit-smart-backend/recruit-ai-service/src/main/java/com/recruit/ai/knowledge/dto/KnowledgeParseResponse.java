package com.recruit.ai.knowledge.dto;

import lombok.Data;

@Data
public class KnowledgeParseResponse {
    private boolean success;
    private String content;
    private Integer length;
    private String message;
}