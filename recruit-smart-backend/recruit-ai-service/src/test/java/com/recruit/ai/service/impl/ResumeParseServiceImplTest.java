package com.recruit.ai.service.impl;

import com.recruit.ai.dto.response.ResumeParseResponse;
import com.recruit.ai.knowledge.service.DocumentParserService;
import com.recruit.ai.service.llm.LlmResumeParseService;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeParseServiceImplTest {

    @Mock
    private DocumentParserService documentParserService;

    @Mock
    private LlmResumeParseService llmResumeParseService;

    @InjectMocks
    private ResumeParseServiceImpl resumeParseService;

    @Test
    void extractsTextAndReturnsNormalizedStructuredResult()
            throws Exception {
        MockMultipartFile file = file("resume.pdf", "pdf-content");
        when(documentParserService.parseDocument(
                eq("resume.pdf"),
                any(InputStream.class)
        )).thenReturn("两年Java开发经验");

        ResumeParseResponse llmResponse = new ResumeParseResponse();
        llmResponse.setSkills(List.of(" Java ", "Java", "Spring Boot"));
        llmResponse.setWarnings(List.of(" 信息不完整 ", "信息不完整"));
        when(llmResumeParseService.extract("两年Java开发经验"))
                .thenReturn(llmResponse);

        ResumeParseResponse response = resumeParseService.parseResume(file);

        assertEquals("两年Java开发经验", response.getParsedContent());
        assertEquals(List.of("Java", "Spring Boot"), response.getSkills());
        assertEquals(List.of("信息不完整"), response.getWarnings());
        verify(llmResumeParseService).extract("两年Java开发经验");
    }

    @Test
    void rejectsUnsupportedFileTypeBeforeParsing() {
        MockMultipartFile file = file("resume.rtf", "rtf-content");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resumeParseService.parseResume(file)
        );

        assertEquals(400, exception.getCode());
    }

    @Test
    void rejectsDocumentWithoutExtractableText() throws Exception {
        MockMultipartFile file = file("resume.pdf", "pdf-content");
        when(documentParserService.parseDocument(
                eq("resume.pdf"),
                any(InputStream.class)
        )).thenReturn(" ");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resumeParseService.parseResume(file)
        );

        assertEquals(400, exception.getCode());
    }

    private MockMultipartFile file(String fileName, String content) {
        return new MockMultipartFile(
                "file",
                fileName,
                "application/octet-stream",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
