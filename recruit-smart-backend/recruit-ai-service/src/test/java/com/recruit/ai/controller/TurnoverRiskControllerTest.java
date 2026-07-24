package com.recruit.ai.controller;

import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import com.recruit.ai.service.AiTurnoverRiskResultService;
import com.recruit.ai.service.TurnoverRiskService;
import com.recruit.feign.dto.response.TurnoverRiskHistoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TurnoverRiskControllerTest {

    @Mock
    private TurnoverRiskService turnoverRiskService;
    @Mock
    private AiTurnoverRiskResultService aiTurnoverRiskResultService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TurnoverRiskController(turnoverRiskService, aiTurnoverRiskResultService))
                .build();
    }

    @Test
    void predictRisk_returnsOk() throws Exception {
        TurnoverRiskResponse response = new TurnoverRiskResponse();
        response.setRiskLevel("HIGH");
        response.setRiskScore(75);

        when(turnoverRiskService.predictRisk(any())).thenReturn(response);

        mockMvc.perform(post("/api/ai/turnover-risk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeName\":\"张三\",\"performanceScore\":40}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.riskScore").value(75));
    }

    @Test
    void listHistory_returnsOk() throws Exception {
        TurnoverRiskHistoryResponse history = new TurnoverRiskHistoryResponse();
        history.setId(1L);
        history.setRiskLevel("LOW");
        history.setRiskScore(20);

        when(aiTurnoverRiskResultService.listByEmployeeId(anyLong()))
                .thenReturn(List.of(history));

        mockMvc.perform(get("/api/ai/turnover-risk/employees/1001/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].riskLevel").value("LOW"))
                .andExpect(jsonPath("$[0].riskScore").value(20));
    }

    @Test
    void listHistory_withNoResults_returnsEmptyArray() throws Exception {
        when(aiTurnoverRiskResultService.listByEmployeeId(anyLong()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ai/turnover-risk/employees/999/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
