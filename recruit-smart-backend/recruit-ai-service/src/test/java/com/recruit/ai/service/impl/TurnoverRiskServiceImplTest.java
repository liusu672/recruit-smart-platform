package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.risk.TurnoverRiskAlgorithm;
import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResult;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.service.AiTaskService;
import com.recruit.ai.service.AiTurnoverRiskResultService;
import com.recruit.ai.service.llm.LlmTurnoverRiskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnoverRiskServiceImplTest {

    @Mock
    private TurnoverRiskAlgorithm turnoverRiskAlgorithm;
    @Mock
    private KnowledgeBaseService knowledgeBaseService;
    @Mock
    private LlmTurnoverRiskService llmTurnoverRiskService;
    @Mock
    private AiTaskService aiTaskService;
    @Mock
    private AiTurnoverRiskResultService aiTurnoverRiskResultService;

    private TurnoverRiskServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TurnoverRiskServiceImpl(
                turnoverRiskAlgorithm, knowledgeBaseService,
                llmTurnoverRiskService, aiTaskService, aiTurnoverRiskResultService
        );
    }

    @Test
    void predictRisk_llmPath_success() {
        TurnoverRiskRequest request = createRequest();
        TurnoverRiskResponse llmResponse = new TurnoverRiskResponse();
        llmResponse.setRiskLevel("LOW");
        llmResponse.setRiskScore(20);

        KnowledgeSearchResponse searchResponse = new KnowledgeSearchResponse();
        KnowledgeSearchResult result = new KnowledgeSearchResult();
        result.setContent("离职风险参考数据");
        searchResponse.setResults(List.of(result));

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(1L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt())).thenReturn(searchResponse);
        when(llmTurnoverRiskService.predict(any(TurnoverRiskRequest.class), anyString())).thenReturn(llmResponse);

        TurnoverRiskResponse response = service.predictRisk(request);

        assertNotNull(response);
        assertEquals("LOW", response.getRiskLevel());
        assertEquals(20, response.getRiskScore());
        verify(aiTurnoverRiskResultService).saveResult(eq(1L), eq(request), eq(response), eq("LLM"));
        verify(aiTaskService).markSuccess(1L, "LLM");
    }

    @Test
    void predictRisk_whenLlmFails_fallsBackToAlgorithm() {
        TurnoverRiskRequest request = createRequest();
        TurnoverRiskResponse ruleResponse = new TurnoverRiskResponse();
        ruleResponse.setRiskLevel("MEDIUM");
        ruleResponse.setRiskScore(55);

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(2L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt())).thenThrow(new RuntimeException("搜索失败"));
        when(turnoverRiskAlgorithm.predict(any(TurnoverRiskRequest.class))).thenReturn(ruleResponse);

        TurnoverRiskResponse response = service.predictRisk(request);

        assertEquals("MEDIUM", response.getRiskLevel());
        assertEquals(55, response.getRiskScore());
        verify(aiTurnoverRiskResultService).saveResult(eq(2L), eq(request), eq(response), eq("RULE"));
        verify(aiTaskService).markSuccess(2L, "RULE");
    }

    @Test
    void predictRisk_whenSaveFails_marksFailedAndThrows() {
        TurnoverRiskRequest request = createRequest();
        TurnoverRiskResponse llmResponse = new TurnoverRiskResponse();

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(3L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt()))
                .thenReturn(new KnowledgeSearchResponse());
        when(llmTurnoverRiskService.predict(any(TurnoverRiskRequest.class), anyString())).thenReturn(llmResponse);
        doThrow(new RuntimeException("保存失败")).when(aiTurnoverRiskResultService)
                .saveResult(anyLong(), any(), any(), anyString());

        assertThrows(RuntimeException.class, () -> service.predictRisk(request));
        verify(aiTaskService).markFailed(3L, "保存失败");
    }

    @Test
    void predictRisk_withNullSearchResult_usesEmptyContext() {
        TurnoverRiskRequest request = createRequest();
        TurnoverRiskResponse llmResponse = new TurnoverRiskResponse();

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(4L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt())).thenReturn(null);
        when(llmTurnoverRiskService.predict(any(TurnoverRiskRequest.class), eq(""))).thenReturn(llmResponse);

        service.predictRisk(request);

        verify(llmTurnoverRiskService).predict(any(TurnoverRiskRequest.class), eq(""));
    }

    private static TurnoverRiskRequest createRequest() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setEmployeeId(1001L);
        request.setEmployeeName("张三");
        request.setDepartment("技术部");
        request.setPosition("Java开发");
        return request;
    }
}
