package com.recruit.ai.knowledge.service;

import com.recruit.ai.knowledge.model.DocumentChunk;

import java.util.List;

public interface TextSplitterService {
    List<DocumentChunk> splitText(String text, int chunkSize, int chunkOverlap, String sourceDocument);
}