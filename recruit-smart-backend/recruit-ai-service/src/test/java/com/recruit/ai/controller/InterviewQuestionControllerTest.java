package com.recruit.ai.controller;

import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.feign.dto.response.InterviewQuestionItemResponse;
import com.recruit.ai.dto.response.InterviewQuestionResponse;
import com.recruit.ai.service.InterviewQuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InterviewQuestionControllerTest {

    @Mock
    private InterviewQuestionService interviewQuestionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new InterviewQuestionController(interviewQuestionService))
                .build();
    }

    @Test
    void generateQuestions_returnsOk() throws Exception {
        InterviewQuestionResponse response = new InterviewQuestionResponse();
        response.setCategory("Java后端");
        InterviewQuestionItemResponse item = new InterviewQuestionItemResponse();
        item.setTitle("Java基础");
        item.setContent("请介绍Java基础知识");
        response.setQuestions(List.of(item));

        when(interviewQuestionService.generateQuestions(any())).thenReturn(response);

        mockMvc.perform(post("/api/ai/interview-questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\":\"Java开发\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Java后端"))
                .andExpect(jsonPath("$.questions[0].title").value("Java基础"));
    }

    @Test
    void generateQuestions_delegatesToService() throws Exception {
        when(interviewQuestionService.generateQuestions(any())).thenReturn(new InterviewQuestionResponse());

        mockMvc.perform(post("/api/ai/interview-questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\":\"Java\"}"))
                .andExpect(status().isOk());
    }
}
