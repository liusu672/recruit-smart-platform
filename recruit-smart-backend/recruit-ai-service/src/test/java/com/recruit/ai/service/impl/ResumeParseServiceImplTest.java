package com.recruit.ai.service.impl;

import com.recruit.ai.dto.response.ResumeParseResponse;
import com.recruit.ai.knowledge.service.DocumentParserService;
import com.recruit.ai.service.llm.LlmResumeParseService;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeParseServiceImplTest {

    @Mock
    private DocumentParserService documentParserService;
    @Mock
    private LlmResumeParseService llmResumeParseService;

    private ResumeParseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ResumeParseServiceImpl(documentParserService, llmResumeParseService);
    }

    @Test
    void parseResume_withValidPdf_returnsParsedResponse() throws IOException {
        MultipartFile file = createMockFile("简历.pdf", "pdf", "张三 5年后端开发经验");
        when(documentParserService.parseDocument(anyString(), any(InputStream.class)))
                .thenReturn("张三 5年后端开发经验");

        ResumeParseResponse llmResponse = new ResumeParseResponse();
        llmResponse.setSkills(List.of("Java", "Spring"));
        llmResponse.setWarnings(List.of());
        when(llmResumeParseService.extract(anyString())).thenReturn(llmResponse);

        ResumeParseResponse response = service.parseResume(file);

        assertNotNull(response);
        assertEquals(List.of("Java", "Spring"), response.getSkills());
        assertTrue(response.getWarnings().isEmpty());
        assertNotNull(response.getParsedContent());
    }

    @Test
    void parseResume_withTxtFile_returnsParsedResponse() throws IOException {
        MultipartFile file = createMockFile("简历.txt", "txt", "前端开发经验");
        when(documentParserService.parseDocument(anyString(), any(InputStream.class)))
                .thenReturn("前端开发经验");

        ResumeParseResponse llmResponse = new ResumeParseResponse();
        llmResponse.setSkills(List.of("Vue", "JavaScript"));
        when(llmResumeParseService.extract(anyString())).thenReturn(llmResponse);

        ResumeParseResponse response = service.parseResume(file);

        assertNotNull(response);
        assertEquals(List.of("Vue", "JavaScript"), response.getSkills());
    }

    @Test
    void parseResume_whenFileIsNull_throwsException() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.parseResume(null));
        assertTrue(ex.getMessage().contains("不能为空"));
    }

    @Test
    void parseResume_whenFileIsEmpty_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.parseResume(file));
        assertTrue(ex.getMessage().contains("不能为空"));
    }

    @Test
    void parseResume_whenFileTooBig_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(11L * 1024 * 1024);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.parseResume(file));
        assertTrue(ex.getMessage().contains("10MB"));
    }

    @Test
    void parseResume_withUnsupportedExtension_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1000L);
        when(file.getOriginalFilename()).thenReturn("简历.png");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.parseResume(file));
        assertTrue(ex.getMessage().contains("PDF、DOC"));
    }

    @Test
    void parseResume_whenFileNameContainsPathTraversal_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1000L);
        when(file.getOriginalFilename()).thenReturn("../简历.pdf");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.parseResume(file));
        assertTrue(ex.getMessage().contains("不合法"));
    }

    @Test
    void parseResume_whenParsedContentEmpty_throwsException() throws IOException {
        MultipartFile file = createMockFile("简历.pdf", "pdf", "");
        when(documentParserService.parseDocument(anyString(), any(InputStream.class)))
                .thenReturn("");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.parseResume(file));
        assertTrue(ex.getMessage().contains("未能从简历中提取文本"));
    }

    @Test
    void parseResume_whenLlmFails_throwsException() throws IOException {
        MultipartFile file = createMockFile("简历.pdf", "pdf", "有内容的简历文本");
        when(documentParserService.parseDocument(anyString(), any(InputStream.class)))
                .thenReturn("有内容的简历文本");
        when(llmResumeParseService.extract(anyString())).thenThrow(new RuntimeException("LLM失败"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.parseResume(file));
        assertTrue(ex.getMessage().contains("解析失败"));
    }

    @Test
    void parseResume_whenLlmReturnsNull_throwsException() throws IOException {
        MultipartFile file = createMockFile("简历.pdf", "pdf", "有内容的简历文本");
        when(documentParserService.parseDocument(anyString(), any(InputStream.class)))
                .thenReturn("有内容的简历文本");
        when(llmResumeParseService.extract(anyString())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.parseResume(file));
        assertTrue(ex.getMessage().contains("结果为空"));
    }

    @Test
    void parseResume_whenContentTruncated_addsWarning() throws IOException {
        // 构造一个超过50000字符的文本
        String longText = "A".repeat(60000);
        MultipartFile file = createMockFile("简历.pdf", "pdf", longText);
        when(documentParserService.parseDocument(anyString(), any(InputStream.class)))
                .thenReturn(longText);

        ResumeParseResponse llmResponse = new ResumeParseResponse();
        llmResponse.setSkills(List.of("Java"));
        llmResponse.setWarnings(List.of());
        when(llmResumeParseService.extract(anyString())).thenReturn(llmResponse);

        ResumeParseResponse response = service.parseResume(file);

        assertNotNull(response);
        assertTrue(response.getWarnings().stream().anyMatch(w -> w.contains("50000")));
    }

    @Test
    void parseResume_deduplicatesWarnings() throws IOException {
        MultipartFile file = createMockFile("简历.pdf", "pdf", "A".repeat(60000));
        when(documentParserService.parseDocument(anyString(), any(InputStream.class)))
                .thenReturn("A".repeat(60000));

        ResumeParseResponse llmResponse = new ResumeParseResponse();
        llmResponse.setSkills(List.of("Java"));
        llmResponse.setWarnings(List.of("重复警告", "重复警告"));
        when(llmResumeParseService.extract(anyString())).thenReturn(llmResponse);

        ResumeParseResponse response = service.parseResume(file);

        // 原始2个重复警告去重后剩1个，加上截断警告共2个
        assertEquals(2, response.getWarnings().size());
    }

    @Test
    void parseResume_normalizesSkills() throws IOException {
        MultipartFile file = createMockFile("简历.pdf", "pdf", "Java开发");
        when(documentParserService.parseDocument(anyString(), any(InputStream.class)))
                .thenReturn("Java开发");

        ResumeParseResponse llmResponse = new ResumeParseResponse();
        llmResponse.setSkills(List.of(" Java ", "Java", " Spring "));
        when(llmResumeParseService.extract(anyString())).thenReturn(llmResponse);

        ResumeParseResponse response = service.parseResume(file);

        // 去重+trim后应为 ["Java", "Spring"]
        assertEquals(2, response.getSkills().size());
        assertTrue(response.getSkills().contains("Java"));
        assertTrue(response.getSkills().contains("Spring"));
    }

    private static MultipartFile createMockFile(String fileName, String extension, String content) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn((long) content.length());
        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes()));
        return file;
    }
}
