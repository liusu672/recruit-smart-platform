package com.recruit.ai.service.impl;

import com.recruit.ai.entity.AiFeedbackSummary;
import com.recruit.ai.mapper.AiFeedbackSummaryMapper;
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
class AiFeedbackSummaryResultServiceImplTest {

    @Mock
    private AiFeedbackSummaryMapper mapper;
    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<AiFeedbackSummary> captor;

    private AiFeedbackSummaryResultServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AiFeedbackSummaryResultServiceImpl(mapper, objectMapper);
    }

    @Test
    void saveResult_insertsEntity() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("[\"data\"]");

        com.recruit.ai.dto.request.FeedbackSummaryRequest request =
                new com.recruit.ai.dto.request.FeedbackSummaryRequest();
        request.setInterviewId(10L);
        request.setCandidateId(20L);
        request.setJobId(30L);

        com.recruit.ai.dto.response.FeedbackSummaryResponse response =
                new com.recruit.ai.dto.response.FeedbackSummaryResponse();
        response.setSummary("优秀");
        response.setAdvantages(List.of("基础扎实"));
        response.setRisks(List.of());
        response.setSuggestion("建议录用");

        service.saveResult(1L, request, response, "LLM");

        verify(mapper).insert(captor.capture());
        AiFeedbackSummary entity = captor.getValue();
        assertEquals(1L, entity.getTaskId());
        assertEquals(10L, entity.getInterviewId());
        assertEquals("优秀", entity.getSummary());
        assertEquals("[\"data\"]", entity.getAdvantages());
        assertEquals("建议录用", entity.getSuggestion());
    }

    @Test
    void saveResult_whenMapperThrows_doesNotPropagate() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        doThrow(new RuntimeException("DB error")).when(mapper).insert(any());

        assertDoesNotThrow(() -> service.saveResult(1L,
                new com.recruit.ai.dto.request.FeedbackSummaryRequest(),
                new com.recruit.ai.dto.response.FeedbackSummaryResponse(), "LLM"));
    }
}
