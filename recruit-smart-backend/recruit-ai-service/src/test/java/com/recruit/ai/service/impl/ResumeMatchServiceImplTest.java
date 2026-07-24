package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.match.ResumeMatchAlgorithm;
import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResult;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.service.AiMatchResultService;
import com.recruit.ai.service.AiTaskService;
import com.recruit.ai.service.llm.LlmResumeMatchService;
import com.recruit.common.exception.BusinessException;
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
class ResumeMatchServiceImplTest {

    @Mock
    private ResumeMatchAlgorithm resumeMatchAlgorithm;
    @Mock
    private KnowledgeBaseService knowledgeBaseService;
    @Mock
    private LlmResumeMatchService llmResumeMatchService;
    @Mock
    private AiTaskService aiTaskService;
    @Mock
    private AiMatchResultService aiMatchResultService;

    private ResumeMatchServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ResumeMatchServiceImpl(
                resumeMatchAlgorithm, knowledgeBaseService,
                llmResumeMatchService, aiTaskService, aiMatchResultService
        );
    }

    @Test
    void matchResume_llmPath_success() {
        ResumeMatchRequest request = createRequest();
        ResumeMatchResponse llmResponse = new ResumeMatchResponse();
        llmResponse.setScore(85);
        llmResponse.setLevel("HIGH");

        KnowledgeSearchResponse searchResponse = new KnowledgeSearchResponse();
        KnowledgeSearchResult result = new KnowledgeSearchResult();
        result.setContent("知识库参考内容");
        searchResponse.setResults(List.of(result));

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(1L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt())).thenReturn(searchResponse);
        when(llmResumeMatchService.match(any(ResumeMatchRequest.class), anyString())).thenReturn(llmResponse);

        ResumeMatchResponse response = service.matchResume(request);

        assertNotNull(response);
        assertEquals(85, response.getScore());
        verify(aiMatchResultService).saveResult(eq(1L), eq(request), eq(response), eq("LLM"));
        verify(aiTaskService).markSuccess(1L, "LLM");
    }

    @Test
    void matchResume_whenLlmFails_fallsBackToAlgorithm() {
        ResumeMatchRequest request = createRequest();
        ResumeMatchResponse ruleResponse = new ResumeMatchResponse();
        ruleResponse.setScore(60);
        ruleResponse.setLevel("MEDIUM");

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(2L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt())).thenThrow(new RuntimeException("知识库异常"));
        when(resumeMatchAlgorithm.match(any(ResumeMatchRequest.class), anyString())).thenReturn(ruleResponse);

        ResumeMatchResponse response = service.matchResume(request);

        assertNotNull(response);
        assertEquals(60, response.getScore());
        verify(aiMatchResultService).saveResult(eq(2L), eq(request), eq(response), eq("RULE"));
        verify(aiTaskService).markSuccess(2L, "RULE");
    }

    @Test
    void matchResume_whenSaveFails_marksTaskFailedAndThrows() {
        ResumeMatchRequest request = createRequest();
        ResumeMatchResponse llmResponse = new ResumeMatchResponse();
        llmResponse.setScore(85);

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(3L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt()))
                .thenReturn(new KnowledgeSearchResponse());
        when(llmResumeMatchService.match(any(ResumeMatchRequest.class), anyString())).thenReturn(llmResponse);
        doThrow(new RuntimeException("保存失败")).when(aiMatchResultService)
                .saveResult(anyLong(), any(), any(), anyString());

        assertThrows(RuntimeException.class, () -> service.matchResume(request));
        verify(aiTaskService).markFailed(3L, "保存失败");
    }

    @Test
    void matchResume_withEmptySearchResults_usesEmptyKnowledgeContext() {
        ResumeMatchRequest request = createRequest();
        ResumeMatchResponse llmResponse = new ResumeMatchResponse();

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(4L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt()))
                .thenReturn(new KnowledgeSearchResponse());
        when(llmResumeMatchService.match(any(ResumeMatchRequest.class), eq(""))).thenReturn(llmResponse);

        service.matchResume(request);

        verify(llmResumeMatchService).match(any(ResumeMatchRequest.class), eq(""));
    }

    private static ResumeMatchRequest createRequest() {
        ResumeMatchRequest request = new ResumeMatchRequest();
        request.setApplicationId(100L);
        request.setJobId(200L);
        request.setCandidateId(300L);
        request.setResumeId(400L);
        request.setJobTitle("Java开发");
        request.setRequirements("Spring Boot, MySQL");
        return request;
    }
}
