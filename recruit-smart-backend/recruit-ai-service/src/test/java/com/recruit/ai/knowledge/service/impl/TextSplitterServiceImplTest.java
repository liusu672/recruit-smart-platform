package com.recruit.ai.knowledge.service.impl;

import com.recruit.ai.knowledge.model.DocumentChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextSplitterServiceImplTest {

    private TextSplitterServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TextSplitterServiceImpl();
    }

    @Test
    void splitText_withShortText_returnsOneChunk() {
        String text = "这是一个简短的文档内容。";
        List<DocumentChunk> chunks = service.splitText(text, 500, 50, "test.txt");

        assertEquals(1, chunks.size());
        assertEquals(text, chunks.get(0).getContent());
        assertEquals(0, chunks.get(0).getChunkIndex());
        assertEquals(0, chunks.get(0).getStartOffset());
        assertEquals(text.length(), chunks.get(0).getEndOffset());
    }

    @Test
    void splitText_withLongText_returnsMultipleChunks() {
        String text = "A".repeat(1000);
        List<DocumentChunk> chunks = service.splitText(text, 200, 20, "doc.txt");

        assertTrue(chunks.size() > 1);
        chunks.forEach(chunk -> {
            assertNotNull(chunk.getContent());
            assertNotNull(chunk.getSourceDocument());
            assertEquals("doc.txt", chunk.getSourceDocument());
            assertTrue(chunk.getChunkIndex() >= 0);
        });
    }

    @Test
    void splitText_withChunkSizeEqualToText_returnsOneChunk() {
        String text = "恰好合适的内容";
        List<DocumentChunk> chunks = service.splitText(text, text.length(), 0, "test.txt");

        assertEquals(1, chunks.size());
        assertEquals(text, chunks.get(0).getContent());
    }

    @Test
    void splitText_withNullText_returnsEmptyList() {
        List<DocumentChunk> chunks = service.splitText(null, 500, 50, "test.txt");
        assertTrue(chunks.isEmpty());
    }

    @Test
    void splitText_withBlankText_returnsEmptyList() {
        List<DocumentChunk> chunks = service.splitText("   ", 500, 50, "test.txt");
        assertTrue(chunks.isEmpty());
    }

    @Test
    void splitText_withZeroChunkSize_usesDefault() {
        String text = "测试默认分块大小的文档内容，应该使用默认的500字符大小进行分块。";
        List<DocumentChunk> chunks = service.splitText(text, 0, 50, "test.txt");

        assertFalse(chunks.isEmpty());
        assertEquals(1, chunks.size());
    }

    @Test
    void splitText_withNegativeChunkOverlap_usesDefault() {
        String text = "A".repeat(1000);
        List<DocumentChunk> chunks = service.splitText(text, 500, -1, "test.txt");

        assertFalse(chunks.isEmpty());
        assertTrue(chunks.size() >= 2);
    }

    @Test
    void splitText_withOverlapGreaterThanChunkSize_adjustsOverlap() {
        String text = "A".repeat(1000);
        List<DocumentChunk> chunks = service.splitText(text, 200, 300, "test.txt");

        assertFalse(chunks.isEmpty());
        // overlap should be adjusted to chunkSize/10 = 20
        assertTrue(chunks.size() > 4);
    }

    @Test
    void splitText_chunksHaveSequentialIndices() {
        String text = "A".repeat(500);
        List<DocumentChunk> chunks = service.splitText(text, 100, 10, "test.txt");

        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).getChunkIndex());
        }
    }

    @Test
    void splitText_trimmedWhitespace() {
        String text = "  开头空格  中间空格   ";
        List<DocumentChunk> chunks = service.splitText(text, 500, 50, "test.txt");

        assertEquals(1, chunks.size());
        assertFalse(chunks.get(0).getContent().startsWith(" "));
    }
}
