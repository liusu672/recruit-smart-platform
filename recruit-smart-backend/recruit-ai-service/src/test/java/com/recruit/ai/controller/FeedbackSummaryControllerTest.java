package com.recruit.ai.controller;

import com.recruit.ai.dto.request.FeedbackSummaryRequest;
import com.recruit.ai.dto.response.FeedbackSummaryResponse;
import com.recruit.ai.service.FeedbackSummaryService;
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
class FeedbackSummaryControllerTest {

    @Mock
    private FeedbackSummaryService feedbackSummaryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FeedbackSummaryController(feedbackSummaryService))
                .build();
    }

    @Test
    void generateSummary_returnsOk() throws Exception {
        FeedbackSummaryResponse response = new FeedbackSummaryResponse();
        response.setAdvantages(List.of("基础扎实", "表达清晰"));
        response.setSuggestion("建议录用");

        when(feedbackSummaryService.generateSummary(any())).thenReturn(response);

        mockMvc.perform(post("/api/ai/feedback-summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"feedbackText\":\"候选人的基础扎实\",\"score\":85}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.advantages[0]").value("基础扎实"))
                .andExpect(jsonPath("$.suggestion").value("建议录用"));
    }

    @Test
    void generateSummary_delegatesToService() throws Exception {
        when(feedbackSummaryService.generateSummary(any())).thenReturn(new FeedbackSummaryResponse());

        mockMvc.perform(post("/api/ai/feedback-summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"feedbackText\":\"表现良好\"}"))
                .andExpect(status().isOk());
    }
}
