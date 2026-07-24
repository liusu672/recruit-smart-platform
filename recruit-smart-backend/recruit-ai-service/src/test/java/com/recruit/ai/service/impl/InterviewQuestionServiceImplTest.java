package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.question.InterviewQuestionAlgorithm;
import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.response.InterviewQuestionResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResult;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.service.AiInterviewQuestionResultService;
import com.recruit.ai.service.AiTaskService;
import com.recruit.ai.service.llm.LlmInterviewQuestionService;
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
class InterviewQuestionServiceImplTest {

    @Mock
    private InterviewQuestionAlgorithm interviewQuestionAlgorithm;
    @Mock
    private KnowledgeBaseService knowledgeBaseService;
    @Mock
    private LlmInterviewQuestionService llmInterviewQuestionService;
    @Mock
    private AiTaskService aiTaskService;
    @Mock
    private AiInterviewQuestionResultService aiInterviewQuestionResultService;

    private InterviewQuestionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InterviewQuestionServiceImpl(
                interviewQuestionAlgorithm, knowledgeBaseService,
                llmInterviewQuestionService, aiTaskService, aiInterviewQuestionResultService
        );
    }

    @Test
    void generateQuestions_llmPath_success() {
        InterviewQuestionRequest request = createRequest();
        InterviewQuestionResponse llmResponse = new InterviewQuestionResponse();
        llmResponse.setCategory("Java后端");

        KnowledgeSearchResponse searchResponse = new KnowledgeSearchResponse();
        KnowledgeSearchResult result = new KnowledgeSearchResult();
        result.setContent("面试题参考");
        searchResponse.setResults(List.of(result));

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(1L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt())).thenReturn(searchResponse);
        when(llmInterviewQuestionService.generate(any(InterviewQuestionRequest.class), anyString()))
                .thenReturn(llmResponse);

        InterviewQuestionResponse response = service.generateQuestions(request);

        assertNotNull(response);
        assertEquals("Java后端", response.getCategory());
        verify(aiInterviewQuestionResultService).saveResult(eq(1L), eq(request), eq(response), eq("LLM"));
        verify(aiTaskService).markSuccess(1L, "LLM");
    }

    @Test
    void generateQuestions_whenLlmFails_fallsBackToAlgorithm() {
        InterviewQuestionRequest request = createRequest();
        InterviewQuestionResponse ruleResponse = new InterviewQuestionResponse();
        ruleResponse.setCategory("通用岗位");

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(2L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt())).thenThrow(new RuntimeException("搜索失败"));
        when(interviewQuestionAlgorithm.generate(any(InterviewQuestionRequest.class))).thenReturn(ruleResponse);

        InterviewQuestionResponse response = service.generateQuestions(request);

        assertEquals("通用岗位", response.getCategory());
        verify(aiInterviewQuestionResultService).saveResult(eq(2L), eq(request), eq(response), eq("RULE"));
        verify(aiTaskService).markSuccess(2L, "RULE");
    }

    @Test
    void generateQuestions_whenSaveFails_marksFailedAndThrows() {
        InterviewQuestionRequest request = createRequest();
        InterviewQuestionResponse llmResponse = new InterviewQuestionResponse();

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(3L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt()))
                .thenReturn(new KnowledgeSearchResponse());
        when(llmInterviewQuestionService.generate(any(InterviewQuestionRequest.class), anyString()))
                .thenReturn(llmResponse);
        doThrow(new RuntimeException("保存失败")).when(aiInterviewQuestionResultService)
                .saveResult(anyLong(), any(), any(), anyString());

        assertThrows(RuntimeException.class, () -> service.generateQuestions(request));
        verify(aiTaskService).markFailed(3L, "保存失败");
    }

    @Test
    void generateQuestions_withEmptyKnowledge_usesEmptyContext() {
        InterviewQuestionRequest request = createRequest();
        InterviewQuestionResponse llmResponse = new InterviewQuestionResponse();

        when(aiTaskService.createRunningTask(anyString(), anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(4L);
        when(knowledgeBaseService.searchKnowledge(anyString(), anyInt()))
                .thenReturn(new KnowledgeSearchResponse());
        when(llmInterviewQuestionService.generate(any(InterviewQuestionRequest.class), eq("")))
                .thenReturn(llmResponse);

        service.generateQuestions(request);

        verify(llmInterviewQuestionService).generate(any(InterviewQuestionRequest.class), eq(""));
    }

    private static InterviewQuestionRequest createRequest() {
        InterviewQuestionRequest request = new InterviewQuestionRequest();
        request.setInterviewId(10L);
        request.setResumeId(20L);
        request.setJobTitle("Java开发");
        request.setRequirements("Spring Boot");
        return request;
    }
}
