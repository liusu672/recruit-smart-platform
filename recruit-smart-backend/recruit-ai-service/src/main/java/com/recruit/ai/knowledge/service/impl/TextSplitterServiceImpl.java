package com.recruit.ai.knowledge.service.impl;

import com.recruit.ai.knowledge.model.DocumentChunk;
import com.recruit.ai.knowledge.service.TextSplitterService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextSplitterServiceImpl implements TextSplitterService {

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_CHUNK_OVERLAP = 50;

    @Override
    public List<DocumentChunk> splitText(String text, int chunkSize, int chunkOverlap, String sourceDocument) {
        List<DocumentChunk> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        int finalChunkSize = normalizeChunkSize(chunkSize);
        int finalChunkOverlap = normalizeChunkOverlap(chunkOverlap, finalChunkSize);

        int textLength = text.length();
        int start = 0;
        int chunkIndex = 0;

        while (start < textLength) {
            int end = Math.min(start + finalChunkSize, textLength);
            String content = text.substring(start, end).trim();

            if (!content.isEmpty()) {
                DocumentChunk chunk = new DocumentChunk();
                chunk.setContent(content);
                chunk.setSourceDocument(sourceDocument);
                chunk.setChunkIndex(chunkIndex);
                chunk.setStartOffset(start);
                chunk.setEndOffset(end);
                chunks.add(chunk);
                chunkIndex++;
            }

            if (end >= textLength) {
                break;
            }

            start = end - finalChunkOverlap;
        }

        return chunks;
    }

    private int normalizeChunkSize(int chunkSize) {
        if (chunkSize <= 0) {
            return DEFAULT_CHUNK_SIZE;
        }
        return chunkSize;
    }

    private int normalizeChunkOverlap(int chunkOverlap, int chunkSize) {
        if (chunkOverlap < 0) {
            return DEFAULT_CHUNK_OVERLAP;
        }
        if (chunkOverlap >= chunkSize) {
            return chunkSize / 10;
        }
        return chunkOverlap;
    }
}