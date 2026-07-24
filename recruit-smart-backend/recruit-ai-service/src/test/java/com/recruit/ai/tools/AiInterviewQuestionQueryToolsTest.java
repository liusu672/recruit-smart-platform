package com.recruit.ai.tools;

import com.recruit.ai.entity.AiInterviewQuestion;
import com.recruit.ai.mapper.AiInterviewQuestionMapper;
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
class AiInterviewQuestionQueryToolsTest {

    @Mock
    private AiInterviewQuestionMapper mapper;

    private AiInterviewQuestionQueryTools tools;

    @BeforeEach
    void setUp() {
        tools = new AiInterviewQuestionQueryTools(mapper);
    }

    @Test
    void queryRecent_returnsFormattedResults() {
        AiInterviewQuestion entity = createEntity(1L, 100L);
        when(mapper.selectList(any())).thenReturn(List.of(entity));

        String result = tools.queryRecentInterviewQuestionResults(5);
        assertTrue(result.contains("结果ID：1"));
        assertTrue(result.contains("岗位ID：100"));
    }

    @Test
    void queryRecent_withNoResults_returnsEmptyMessage() {
        when(mapper.selectList(any())).thenReturn(List.of());
        assertEquals("当前没有AI面试题生成结果。", tools.queryRecentInterviewQuestionResults(5));
    }

    @Test
    void queryRecent_whenMapperThrows_returnsErrorMessage() {
        when(mapper.selectList(any())).thenThrow(new RuntimeException("DB error"));
        assertTrue(tools.queryRecentInterviewQuestionResults(5).contains("失败"));
    }

    @Test
    void queryByJobId_withNullId_returnsError() {
        assertTrue(tools.queryInterviewQuestionResultsByJobId(null).contains("不能为空"));
    }

    @Test
    void queryByCandidateId_returnsFormattedResults() {
        AiInterviewQuestion entity = createEntity(1L, 100L);
        when(mapper.selectList(any())).thenReturn(List.of(entity));

        String result = tools.queryInterviewQuestionResultsByCandidateId(50L);
        assertTrue(result.contains("候选人ID：50"));
    }

    @Test
    void queryByCandidateId_withNullId_returnsError() {
        assertTrue(tools.queryInterviewQuestionResultsByCandidateId(null).contains("不能为空"));
    }

    @Test
    void queryByCandidateId_withNoResults_returnsNotFound() {
        when(mapper.selectList(any())).thenReturn(List.of());
        assertTrue(tools.queryInterviewQuestionResultsByCandidateId(999L).contains("未查询到"));
    }

    private static AiInterviewQuestion createEntity(Long id, Long jobId) {
        AiInterviewQuestion e = new AiInterviewQuestion();
        e.setId(id);
        e.setTaskId(1L);
        e.setJobId(jobId);
        e.setCandidateId(50L);
        e.setCategory("Java后端");
        e.setSummary("已生成面试题");
        e.setSource("LLM");
        e.setGeneratedAt(LocalDateTime.now());
        return e;
    }
}
