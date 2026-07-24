package com.recruit.ai.tools;

import com.recruit.ai.entity.AiFeedbackSummary;
import com.recruit.ai.mapper.AiFeedbackSummaryMapper;
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
class AiFeedbackSummaryQueryToolsTest {

    @Mock
    private AiFeedbackSummaryMapper mapper;

    private AiFeedbackSummaryQueryTools tools;

    @BeforeEach
    void setUp() {
        tools = new AiFeedbackSummaryQueryTools(mapper);
    }

    @Test
    void queryRecent_returnsFormattedResults() {
        AiFeedbackSummary entity = createEntity(1L, 10L, "优秀", "录用");
        when(mapper.selectList(any())).thenReturn(List.of(entity));

        String result = tools.queryRecentFeedbackSummaryResults(5);

        assertTrue(result.contains("结果ID：1"));
        assertTrue(result.contains("摘要：优秀"));
        assertTrue(result.contains("建议：录用"));
    }

    @Test
    void queryRecent_withNullLimit_defaultsTo5() {
        when(mapper.selectList(any())).thenReturn(List.of(createEntity(1L, 10L, "a", "b")));

        String result = tools.queryRecentFeedbackSummaryResults(null);
        assertTrue(result.contains("结果ID：1"));
    }

    @Test
    void queryRecent_withNoResults_returnsEmptyMessage() {
        when(mapper.selectList(any())).thenReturn(List.of());

        String result = tools.queryRecentFeedbackSummaryResults(5);
        assertEquals("当前没有AI面试反馈摘要结果。", result);
    }

    @Test
    void queryRecent_whenMapperThrows_returnsErrorMessage() {
        when(mapper.selectList(any())).thenThrow(new RuntimeException("DB error"));

        String result = tools.queryRecentFeedbackSummaryResults(5);
        assertTrue(result.contains("查询AI面试反馈摘要失败"));
    }

    @Test
    void queryByInterviewId_returnsFormattedResult() {
        AiFeedbackSummary entity = createEntity(1L, 100L, "良好", "面");
        when(mapper.selectOne(any())).thenReturn(entity);

        String result = tools.queryFeedbackSummaryByInterviewId(100L);
        assertTrue(result.contains("面试ID：100"));
    }

    @Test
    void queryByInterviewId_withNullId_returnsError() {
        String result = tools.queryFeedbackSummaryByInterviewId(null);
        assertTrue(result.contains("不能为空"));
    }

    @Test
    void queryByInterviewId_withNoResult_returnsNotFound() {
        when(mapper.selectOne(any())).thenReturn(null);

        String result = tools.queryFeedbackSummaryByInterviewId(999L);
        assertTrue(result.contains("未查询到"));
    }

    @Test
    void queryByCandidateId_returnsFormattedResults() {
        AiFeedbackSummary entity = createEntity(1L, 10L, "优", "建议");
        when(mapper.selectList(any())).thenReturn(List.of(entity));

        String result = tools.queryFeedbackSummaryByCandidateId(50L);
        assertTrue(result.contains("候选人ID：50"));
    }

    @Test
    void queryByCandidateId_withNullId_returnsError() {
        String result = tools.queryFeedbackSummaryByCandidateId(null);
        assertTrue(result.contains("不能为空"));
    }

    @Test
    void queryByCandidateId_withNoResults_returnsNotFound() {
        when(mapper.selectList(any())).thenReturn(List.of());

        String result = tools.queryFeedbackSummaryByCandidateId(999L);
        assertTrue(result.contains("未查询到"));
    }

    private static AiFeedbackSummary createEntity(Long id, Long interviewId, String summary, String suggestion) {
        AiFeedbackSummary e = new AiFeedbackSummary();
        e.setId(id);
        e.setTaskId(1L);
        e.setInterviewId(interviewId);
        e.setCandidateId(50L);
        e.setJobId(30L);
        e.setSummary(summary);
        e.setAdvantages("沟通清晰");
        e.setRisks("项目经验不足");
        e.setSuggestion(suggestion);
        e.setSource("LLM");
        e.setGeneratedAt(LocalDateTime.now());
        return e;
    }
}
