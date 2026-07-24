package com.recruit.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import com.recruit.ai.entity.AiMatchResult;
import com.recruit.ai.mapper.AiMatchResultMapper;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiMatchResultServiceImplTest {

    @Mock
    private AiMatchResultMapper aiMatchResultMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AiMatchResultServiceImpl aiMatchResultService;

    @Test
    void mapsResponseToDatabaseFieldsAndUpserts() {
        when(aiMatchResultMapper.upsert(any())).thenReturn(1);

        aiMatchResultService.saveResult(
                9L,
                request(),
                response(),
                "LLM"
        );

        ArgumentCaptor<AiMatchResult> captor =
                ArgumentCaptor.forClass(AiMatchResult.class);
        verify(aiMatchResultMapper).upsert(captor.capture());
        AiMatchResult result = captor.getValue();
        assertEquals(new BigDecimal("88"), result.getMatchScore());
        assertEquals("HIGH", result.getRecommendLevel());
        assertEquals("匹配度较高", result.getRecommendReason());
        assertEquals("Java能力；项目经验", result.getHighlightSummary());
        assertEquals("[\"Java能力\",\"项目经验\"]", result.getMatchedPoints());
        assertEquals("稳定性待确认", result.getRiskSummary());
        assertEquals("LLM", result.getSource());
    }

    @Test
    void propagatesDatabaseFailureSoTaskCanBeMarkedFailed() {
        when(aiMatchResultMapper.upsert(any()))
                .thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(
                IllegalStateException.class,
                () -> aiMatchResultService.saveResult(
                        9L,
                        request(),
                        response(),
                        "LLM"
                )
        );
    }

    @Test
    void rejectsIncompleteBusinessIdentifiers() {
        ResumeMatchRequest request = request();
        request.setApplicationId(null);

        assertThrows(
                BusinessException.class,
                () -> aiMatchResultService.saveResult(
                        9L,
                        request,
                        response(),
                        "LLM"
                )
        );
    }

    private ResumeMatchRequest request() {
        ResumeMatchRequest request = new ResumeMatchRequest();
        request.setApplicationId(1L);
        request.setJobId(2L);
        request.setCandidateId(3L);
        request.setResumeId(4L);
        return request;
    }

    private ResumeMatchResponse response() {
        ResumeMatchResponse response = new ResumeMatchResponse();
        response.setScore(88);
        response.setLevel("HIGH");
        response.setSummary("匹配度较高");
        response.setMatchedPoints(List.of("Java能力", "项目经验"));
        response.setRiskPoints(List.of("稳定性待确认"));
        response.setSuggestion("建议进入面试");
        return response;
    }
}
