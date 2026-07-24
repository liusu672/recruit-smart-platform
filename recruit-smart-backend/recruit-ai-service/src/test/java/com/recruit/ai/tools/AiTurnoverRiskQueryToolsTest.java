package com.recruit.ai.tools;

import com.recruit.ai.entity.AiTurnoverRiskResult;
import com.recruit.ai.mapper.AiTurnoverRiskResultMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiTurnoverRiskQueryToolsTest {

    @Mock
    private AiTurnoverRiskResultMapper mapper;

    private AiTurnoverRiskQueryTools tools;

    @BeforeEach
    void setUp() {
        tools = new AiTurnoverRiskQueryTools(mapper);
    }

    @Test
    void queryRecent_returnsFormattedResults() {
        AiTurnoverRiskResult entity = createEntity(1L, 1001L, "HIGH");
        when(mapper.selectList(any())).thenReturn(List.of(entity));

        String result = tools.queryRecentTurnoverRiskResults(5);
        assertTrue(result.contains("结果ID：1"));
        assertTrue(result.contains("员工ID：1001"));
        assertTrue(result.contains("风险等级：HIGH"));
    }

    @Test
    void queryRecent_withNoResults_returnsEmptyMessage() {
        when(mapper.selectList(any())).thenReturn(List.of());
        assertEquals("当前没有AI离职风险预测结果。", tools.queryRecentTurnoverRiskResults(5));
    }

    @Test
    void queryRecent_whenMapperThrows_returnsErrorMessage() {
        when(mapper.selectList(any())).thenThrow(new RuntimeException("DB error"));
        assertTrue(tools.queryRecentTurnoverRiskResults(5).contains("查询AI离职风险预测结果失败"));
    }

    @Test
    void queryByEmployeeId_returnsFormattedResult() {
        AiTurnoverRiskResult entity = createEntity(1L, 1001L, "MEDIUM");
        when(mapper.selectOne(any())).thenReturn(entity);

        String result = tools.queryTurnoverRiskByEmployeeId(1001L);
        assertTrue(result.contains("员工ID：1001"));
        assertTrue(result.contains("MEDIUM"));
    }

    @Test
    void queryByEmployeeId_withNullId_returnsError() {
        assertTrue(tools.queryTurnoverRiskByEmployeeId(null).contains("不能为空"));
    }

    @Test
    void queryByEmployeeId_withNoResult_returnsNotFound() {
        when(mapper.selectOne(any())).thenReturn(null);
        assertTrue(tools.queryTurnoverRiskByEmployeeId(999L).contains("未查询到"));
    }

    @Test
    void queryHighRisk_returnsFormattedResults() {
        AiTurnoverRiskResult entity = createEntity(1L, 1001L, "HIGH");
        when(mapper.selectList(any())).thenReturn(List.of(entity));

        String result = tools.queryHighTurnoverRiskResults(5);
        assertTrue(result.contains("结果ID：1"));
        assertTrue(result.contains("HIGH"));
    }

    @Test
    void queryHighRisk_withNoResults_returnsEmptyMessage() {
        when(mapper.selectList(any())).thenReturn(List.of());
        assertEquals("当前没有高离职风险预测结果。", tools.queryHighTurnoverRiskResults(5));
    }

    private static AiTurnoverRiskResult createEntity(Long id, Long employeeId, String riskLevel) {
        AiTurnoverRiskResult e = new AiTurnoverRiskResult();
        e.setId(id);
        e.setTaskId(10L);
        e.setEmployeeId(employeeId);
        e.setRiskLevel(riskLevel);
        e.setRiskScore(75);
        e.setSummary("高风险");
        e.setRiskReasons("[\"绩效偏低\"]");
        e.setSuggestions("[\"建议沟通\"]");
        e.setSource("LLM");
        e.setGeneratedAt(LocalDateTime.now());
        return e;
    }
}
