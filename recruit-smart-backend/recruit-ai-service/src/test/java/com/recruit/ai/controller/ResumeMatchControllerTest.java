package com.recruit.ai.controller;

import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import com.recruit.ai.service.ResumeMatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ResumeMatchControllerTest {

    @Mock
    private ResumeMatchService resumeMatchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ResumeMatchController(resumeMatchService))
                .build();
    }

    @Test
    void matchResume_returnsOk() throws Exception {
        ResumeMatchResponse response = new ResumeMatchResponse();
        response.setScore(85);
        response.setLevel("HIGH");
        when(resumeMatchService.matchResume(any())).thenReturn(response);

        mockMvc.perform(post("/api/ai/resume-match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\":\"Java开发\",\"requirements\":\"Spring Boot\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(85))
                .andExpect(jsonPath("$.level").value("HIGH"));
    }

    @Test
    void matchResume_delegatesToService() throws Exception {
        when(resumeMatchService.matchResume(any())).thenReturn(new ResumeMatchResponse());

        mockMvc.perform(post("/api/ai/resume-match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\":\"Java开发\"}"))
                .andExpect(status().isOk());
    }
}
