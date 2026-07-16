package com.recruit.ai.demo.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentChunk {

    private String content;

    private String sourceDocument;

    private Integer chunkIndex;
}