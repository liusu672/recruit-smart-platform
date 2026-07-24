package com.recruit.ai.knowledge.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DocumentParserServiceImplTest {

    private DocumentParserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DocumentParserServiceImpl();
    }

    @Test
    void parseDocument_withTxtFile_returnsContent() throws IOException {
        String content = "这是简历文本内容\n第二行";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        String result = service.parseDocument("简历.txt", inputStream);

        assertTrue(result.contains("这是简历文本内容"));
        assertTrue(result.contains("第二行"));
    }

    @Test
    void parseDocument_withNullFileName_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.parseDocument(null, new ByteArrayInputStream("a".getBytes())));
    }

    @Test
    void parseDocument_withBlankFileName_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.parseDocument("  ", new ByteArrayInputStream("a".getBytes())));
    }

    @Test
    void parseDocument_withNullInputStream_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.parseDocument("test.txt", null));
    }

    @Test
    void parseDocument_withUnsupportedExtension_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.parseDocument("image.png", new ByteArrayInputStream("fake".getBytes())));
    }

    @Test
    void parseDocument_withTxtFileAndWhitespace_cleansText() throws IOException {
        String content = "  这是  简历文本  \n\n\n  第二行  \n";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        String result = service.parseDocument("test.txt", inputStream);

        assertFalse(result.contains("  "));
        assertFalse(result.contains("\n\n\n"));
    }

    @Test
    void parseDocument_withNonExistentFilePath_throwsIOException(@TempDir Path tempDir) {
        Path nonExistent = tempDir.resolve("missing.pdf");
        IOException ex = assertThrows(IOException.class,
                () -> service.parseDocument(nonExistent.toString()));
        assertTrue(ex.getMessage().contains("不存在"));
    }

    @Test
    void parseDocument_withTxtContentEmpty_returnsEmptyString() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("   \n  ".getBytes());
        String result = service.parseDocument("file.txt", inputStream);
        assertEquals("", result);
    }

    @Test
    void parseDocument_caseInsensitiveExtension() throws IOException {
        String content = "内容";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        String result = service.parseDocument("简历.TXT", inputStream);

        assertTrue(result.contains("内容"));
    }

    @Test
    void parseDocument_withRealFilePath_returnsContent(@TempDir Path tempDir) throws IOException {
        Path txtFile = tempDir.resolve("test.txt");
        Files.writeString(txtFile, "文件内容测试");

        String result = service.parseDocument(txtFile.toString());

        assertEquals("文件内容测试", result);
    }
}
