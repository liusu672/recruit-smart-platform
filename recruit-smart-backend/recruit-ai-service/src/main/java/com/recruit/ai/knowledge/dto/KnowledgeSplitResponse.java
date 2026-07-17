package com.recruit.ai.knowledge.dto;

import com.recruit.ai.knowledge.model.DocumentChunk;
import lombok.Data;

import java.util.List;

@Data
public class KnowledgeSplitResponse {
    private boolean success;
    private Integer totalChunks;
    private List<DocumentChunk> chunks;
    private String message;
}