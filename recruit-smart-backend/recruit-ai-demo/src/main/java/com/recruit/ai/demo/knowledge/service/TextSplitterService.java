package com.recruit.ai.demo.knowledge.service;

import com.recruit.ai.demo.knowledge.model.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextSplitterService {

    private static final int DEFAULT_CHUNK_SIZE = 500;

    private static final int DEFAULT_CHUNK_OVERLAP = 50;

    public List<DocumentChunk> splitText(String text,
                                         Integer chunkSize,
                                         Integer chunkOverlap,
                                         String sourceDocument) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        int size = chunkSize == null || chunkSize <= 0 ? DEFAULT_CHUNK_SIZE : chunkSize;
        int overlap = chunkOverlap == null || chunkOverlap < 0 ? DEFAULT_CHUNK_OVERLAP : chunkOverlap;

        if (overlap >= size) {
            overlap = size / 10;
        }

        List<DocumentChunk> chunks = new ArrayList<>();
        int start = 0;
        int chunkIndex = 0;

        while (start < text.length()) {
            int end = Math.min(start + size, text.length());
            String content = text.substring(start, end).trim();

            if (!content.isEmpty()) {
                chunks.add(new DocumentChunk(content, sourceDocument, chunkIndex));
                chunkIndex++;
            }

            if (end >= text.length()) {
                break;
            }

            start = end - overlap;
        }

        return chunks;
    }
}