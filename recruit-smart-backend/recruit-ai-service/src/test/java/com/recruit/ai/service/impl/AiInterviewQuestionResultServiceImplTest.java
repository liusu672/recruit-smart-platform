package com.recruit.ai.service.impl;

import com.recruit.ai.entity.AiInterviewQuestion;
import com.recruit.ai.mapper.AiInterviewQuestionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiInterviewQuestionResultServiceImplTest {

    @Mock
    private AiInterviewQuestionMapper mapper;
    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<AiInterviewQuestion> captor;

    private AiInterviewQuestionResultServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AiInterviewQuestionResultServiceImpl(mapper, objectMapper);
    }

    @Test
    void saveResult_insertsEntity() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        com.recruit.ai.dto.request.InterviewQuestionRequest request =
                new com.recruit.ai.dto.request.InterviewQuestionRequest();
        request.setInterviewId(10L);
        request.setJobId(20L);
        request.setCandidateId(30L);
        request.setResumeId(40L);

        com.recruit.ai.dto.response.InterviewQuestionResponse response =
                new com.recruit.ai.dto.response.InterviewQuestionResponse();
        response.setCategory("Java后端");
        response.setSummary("已生成");

        service.saveResult(1L, request, response, "LLM");

        verify(mapper).insert(captor.capture());
        AiInterviewQuestion entity = captor.getValue();
        assertEquals(1L, entity.getTaskId());
        assertEquals(10L, entity.getInterviewId());
        assertEquals("Java后端", entity.getCategory());
        assertEquals("LLM", entity.getSource());
    }

    @Test
    void saveResult_whenMapperThrows_doesNotPropagate() {
        doThrow(new RuntimeException("DB error")).when(mapper).insert(any());

        assertDoesNotThrow(() -> service.saveResult(1L,
                new com.recruit.ai.dto.request.InterviewQuestionRequest(),
                new com.recruit.ai.dto.response.InterviewQuestionResponse(), "LLM"));
    }
}
