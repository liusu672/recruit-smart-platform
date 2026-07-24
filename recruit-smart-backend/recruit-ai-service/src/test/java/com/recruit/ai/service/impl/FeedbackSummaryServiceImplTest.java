package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.summary.FeedbackSummaryAlgorithm;
import com.recruit.ai.dto.request.FeedbackSummaryRequest;
import com.recruit.ai.dto.response.FeedbackSummaryResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResult;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.service.AiFeedbackSummaryResultService;
import com.recruit.ai.service.AiTaskService;
import com.recruit.ai.service.llm.LlmFeedbackSummaryService;
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
class FeedbackSummaryServiceImplTest {

    @Mock
    private FeedbackSummaryAlgorithm feedbackSummaryAlgorithm;
    @Mock
    private KnowledgeBaseService knowledgeBaseService;
    @Mock
    private LlmFeedbackSummaryService llmFeedbackSummaryService;
    @Mock
    private AiTaskService aiTaskService;
    @Mock
    private AiFeedbackSummaryResultService aiFeedbackSummaryResultService;

    private FeedbackSummaryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FeedbackSummaryServiceImpl(
                feedbackSummaryAlgorithm, knowledgeBaseService,
                llmFeedbackSummaryService, aiTaskService, aiFeedbackSummaryResultService
        );
    }

    @Test
    void generateSummary_llmPath_success() {
        FeedbackSummaryRequest request = createRequest();
        FeedbackSummaryResponse llmResponse = new FeedbackSummaryResponse();
        llmResponse.setSummary("优秀候选人");

        KnowledgeSearchResponse searchResponse = new KnowledgeSearchResponse();
        KnowledgeSearchResult result = new KnowledgeSearchResult();
        result.setContent("面试评价标准参考");
        searchResponse.setResults(List.of(result));

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(1L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt())).thenReturn(searchResponse);
        when(llmFeedbackSummaryService.generate(any(FeedbackSummaryRequest.class), anyString()))
                .thenReturn(llmResponse);

        FeedbackSummaryResponse response = service.generateSummary(request);

        assertNotNull(response);
        assertEquals("优秀候选人", response.getSummary());
        verify(aiFeedbackSummaryResultService).saveResult(eq(1L), eq(request), eq(response), eq("LLM"));
        verify(aiTaskService).markSuccess(1L, "LLM");
    }

    @Test
    void generateSummary_whenLlmFails_fallsBackToAlgorithm() {
        FeedbackSummaryRequest request = createRequest();
        FeedbackSummaryResponse ruleResponse = new FeedbackSummaryResponse();
        ruleResponse.setSuggestion("建议录用");

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(2L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt())).thenThrow(new RuntimeException("搜索失败"));
        when(feedbackSummaryAlgorithm.generate(any(FeedbackSummaryRequest.class))).thenReturn(ruleResponse);

        FeedbackSummaryResponse response = service.generateSummary(request);

        assertEquals("建议录用", response.getSuggestion());
        verify(aiFeedbackSummaryResultService).saveResult(eq(2L), eq(request), eq(response), eq("RULE"));
        verify(aiTaskService).markSuccess(2L, "RULE");
    }

    @Test
    void generateSummary_whenSaveFails_marksFailedAndThrows() {
        FeedbackSummaryRequest request = createRequest();
        FeedbackSummaryResponse llmResponse = new FeedbackSummaryResponse();

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(3L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt()))
                .thenReturn(new KnowledgeSearchResponse());
        when(llmFeedbackSummaryService.generate(any(FeedbackSummaryRequest.class), anyString()))
                .thenReturn(llmResponse);
        doThrow(new RuntimeException("保存失败")).when(aiFeedbackSummaryResultService)
                .saveResult(anyLong(), any(), any(), anyString());

        assertThrows(RuntimeException.class, () -> service.generateSummary(request));
        verify(aiTaskService).markFailed(3L, "保存失败");
    }

    @Test
    void generateSummary_withNullSearchResult_usesEmptyContext() {
        FeedbackSummaryRequest request = createRequest();
        FeedbackSummaryResponse llmResponse = new FeedbackSummaryResponse();

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(4L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt())).thenReturn(null);
        when(llmFeedbackSummaryService.generate(any(FeedbackSummaryRequest.class), eq("")))
                .thenReturn(llmResponse);

        service.generateSummary(request);

        verify(llmFeedbackSummaryService).generate(any(FeedbackSummaryRequest.class), eq(""));
    }

    private static FeedbackSummaryRequest createRequest() {
        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setInterviewId(50L);
        request.setCandidateId(60L);
        request.setJobTitle("Java开发");
        request.setFeedbackText("候选人表现良好");
        return request;
    }
}
