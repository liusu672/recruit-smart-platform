package com.recruit.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import com.recruit.ai.mapper.AiMatchResultMapper;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiMatchResultServiceImplTest {

    @Mock
    private AiMatchResultMapper aiMatchResultMapper;
    @Mock
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<com.recruit.ai.entity.AiMatchResult> resultCaptor;

    private AiMatchResultServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AiMatchResultServiceImpl(aiMatchResultMapper, objectMapper);
    }

    @Test
    void saveResult_withValidData_insertsSuccessfully() throws Exception {
        ResumeMatchRequest request = createRequest();
        ResumeMatchResponse response = createResponse();
        List<String> points = List.of("Java", "Spring");

        when(objectMapper.writeValueAsString(points)).thenReturn("[\"Java\",\"Spring\"]");
        when(aiMatchResultMapper.upsert(any())).thenReturn(1);

        service.saveResult(1L, request, response, "LLM");

        verify(aiMatchResultMapper).upsert(resultCaptor.capture());
        com.recruit.ai.entity.AiMatchResult result = resultCaptor.getValue();

        assertEquals(1L, result.getTaskId());
        assertEquals(100L, result.getApplicationId());
        assertEquals(200L, result.getJobId());
        assertEquals(300L, result.getCandidateId());
        assertEquals(85, result.getMatchScore().intValue());
        assertEquals("HIGH", result.getRecommendLevel());
        assertEquals("Java；Spring", result.getHighlightSummary());
        assertEquals("[\"Java\",\"Spring\"]", result.getMatchedPoints());
        assertEquals("LLM", result.getSource());
    }

    @Test
    void saveResult_withNullResponse_throwsException() {
        ResumeMatchRequest request = createRequest();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.saveResult(1L, request, null, "LLM"));
        assertTrue(ex.getMessage().contains("不能为空"));
    }

    @Test
    void saveResult_withMissingBizIds_throwsException() {
        ResumeMatchRequest request = new ResumeMatchRequest();
        ResumeMatchResponse response = createResponse();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.saveResult(1L, request, response, "LLM"));
        assertTrue(ex.getMessage().contains("业务数据不完整"));
    }

    @Test
    void saveResult_whenUpsertReturnsZero_throwsException() throws Exception {
        ResumeMatchRequest request = createRequest();
        ResumeMatchResponse response = createResponse();

        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(aiMatchResultMapper.upsert(any())).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.saveResult(1L, request, response, "LLM"));
        assertTrue(ex.getMessage().contains("失败"));
    }

    @Test
    void saveResult_withNullScore_setsNullMatchScore() throws Exception {
        ResumeMatchRequest request = createRequest();
        ResumeMatchResponse response = createResponse();
        response.setScore(null);

        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(aiMatchResultMapper.upsert(any())).thenReturn(1);

        service.saveResult(1L, request, response, "RULE");

        verify(aiMatchResultMapper).upsert(resultCaptor.capture());
        assertNull(resultCaptor.getValue().getMatchScore());
    }

    @Test
    void saveResult_withNullPoints_returnsNullForJson() throws Exception {
        ResumeMatchRequest request = createRequest();
        ResumeMatchResponse response = createResponse();
        response.setMatchedPoints(null);
        response.setRiskPoints(null);

        when(aiMatchResultMapper.upsert(any())).thenReturn(1);

        service.saveResult(1L, request, response, "LLM");

        verify(aiMatchResultMapper).upsert(resultCaptor.capture());
        assertNull(resultCaptor.getValue().getMatchedPoints());
        assertNull(resultCaptor.getValue().getHighlightSummary());
    }

    private static ResumeMatchRequest createRequest() {
        ResumeMatchRequest request = new ResumeMatchRequest();
        request.setApplicationId(100L);
        request.setJobId(200L);
        request.setCandidateId(300L);
        request.setResumeId(400L);
        return request;
    }

    private static ResumeMatchResponse createResponse() {
        ResumeMatchResponse response = new ResumeMatchResponse();
        response.setScore(85);
        response.setLevel("HIGH");
        response.setMatchedPoints(List.of("Java", "Spring"));
        response.setRiskPoints(List.of("缺少Redis经验"));
        response.setSummary("匹配度85分");
        response.setSuggestion("建议面试");
        return response;
    }
}
