package com.recruit.ai.controller;

import com.recruit.ai.dto.response.ResumeParseResponse;
import com.recruit.ai.service.ResumeParseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ResumeParseControllerTest {

    @Mock
    private ResumeParseService resumeParseService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ResumeParseController(resumeParseService))
                .build();
    }

    @Test
    void parseResume_withPdf_returnsOk() throws Exception {
        ResumeParseResponse response = new ResumeParseResponse();
        response.setSkills(List.of("Java", "Spring"));
        response.setParsedContent("简历文本");
        when(resumeParseService.parseResume(any())).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile(
                "file", "简历.pdf", "application/pdf", "dummy content".getBytes()
        );

        mockMvc.perform(multipart("/api/ai/resume-parse")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills[0]").value("Java"))
                .andExpect(jsonPath("$.parsedContent").value("简历文本"));
    }

    @Test
    void parseResume_returnsServiceResponse() throws Exception {
        when(resumeParseService.parseResume(any())).thenReturn(new ResumeParseResponse());

        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes()
        );

        mockMvc.perform(multipart("/api/ai/resume-parse")
                        .file(file))
                .andExpect(status().isOk());
    }
}
