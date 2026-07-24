package com.recruit.ai.tools;

import com.recruit.ai.entity.AiMatchResult;
import com.recruit.ai.mapper.AiMatchResultMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiMatchResultQueryToolsTest {

    @Mock
    private AiMatchResultMapper mapper;

    private AiMatchResultQueryTools tools;

    @BeforeEach
    void setUp() {
        tools = new AiMatchResultQueryTools(mapper);
    }

    @Test
    void queryRecent_returnsFormattedResults() {
        AiMatchResult entity = createEntity(1L, 100L);
        when(mapper.selectList(any())).thenReturn(List.of(entity));

        String result = tools.queryRecentResumeMatchResults(5);
        assertTrue(result.contains("结果ID：1"));
        assertTrue(result.contains("投递ID：100"));
        assertTrue(result.contains("匹配分数：85"));
    }

    @Test
    void queryRecent_withNoResults_returnsEmptyMessage() {
        when(mapper.selectList(any())).thenReturn(List.of());
        assertEquals("当前没有AI简历匹配结果。", tools.queryRecentResumeMatchResults(5));
    }

    @Test
    void queryRecent_whenMapperThrows_returnsErrorMessage() {
        when(mapper.selectList(any())).thenThrow(new RuntimeException("DB error"));
        assertTrue(tools.queryRecentResumeMatchResults(5).contains("查询AI简历匹配结果失败"));
    }

    @Test
    void queryByApplicationId_returnsFormattedResult() {
        AiMatchResult entity = createEntity(1L, 100L);
        when(mapper.selectOne(any())).thenReturn(entity);

        String result = tools.queryResumeMatchResultByApplicationId(100L);
        assertTrue(result.contains("投递ID：100"));
    }

    @Test
    void queryByApplicationId_withNullId_returnsError() {
        assertTrue(tools.queryResumeMatchResultByApplicationId(null).contains("不能为空"));
    }

    @Test
    void queryByApplicationId_withNoResult_returnsNotFound() {
        when(mapper.selectOne(any())).thenReturn(null);
        assertTrue(tools.queryResumeMatchResultByApplicationId(999L).contains("未查询到"));
    }

    @Test
    void queryLowResults_returnsFormattedResults() {
        AiMatchResult entity = createEntity(1L, 100L);
        when(mapper.selectList(any())).thenReturn(List.of(entity));

        String result = tools.queryLowResumeMatchResults(5);
        assertTrue(result.contains("结果ID：1"));
    }

    @Test
    void queryLowResults_withNoResults_returnsEmptyMessage() {
        when(mapper.selectList(any())).thenReturn(List.of());
        assertEquals("当前没有低匹配的AI简历匹配结果。", tools.queryLowResumeMatchResults(5));
    }

    private static AiMatchResult createEntity(Long id, Long applicationId) {
        AiMatchResult e = new AiMatchResult();
        e.setId(id);
        e.setTaskId(1L);
        e.setApplicationId(applicationId);
        e.setJobId(200L);
        e.setCandidateId(300L);
        e.setResumeId(400L);
        e.setMatchScore(BigDecimal.valueOf(85));
        e.setRecommendLevel("HIGH");
        e.setRecommendReason("匹配度85分");
        e.setSuggestion("建议面试");
        e.setSource("LLM");
        e.setGeneratedAt(LocalDateTime.now());
        return e;
    }
}
