package com.recruit.ai.tools;

import com.recruit.ai.entity.AiTask;
import com.recruit.ai.mapper.AiTaskMapper;
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
class AiTaskQueryToolsTest {

    @Mock
    private AiTaskMapper mapper;

    private AiTaskQueryTools tools;

    @BeforeEach
    void setUp() {
        tools = new AiTaskQueryTools(mapper);
    }

    @Test
    void queryRecent_returnsFormattedResults() {
        AiTask entity = createEntity(1L, "RESUME_MATCH", "SUCCESS");
        when(mapper.selectList(any())).thenReturn(List.of(entity));

        String result = tools.queryRecentAiTasks(5);
        assertTrue(result.contains("任务ID：1"));
        assertTrue(result.contains("RESUME_MATCH"));
        assertTrue(result.contains("SUCCESS"));
    }

    @Test
    void queryRecent_withNoResults_returnsEmptyMessage() {
        when(mapper.selectList(any())).thenReturn(List.of());
        assertEquals("当前没有AI任务记录。", tools.queryRecentAiTasks(5));
    }

    @Test
    void queryRecent_withNullLimit_defaultsTo5() {
        when(mapper.selectList(any())).thenReturn(List.of(createEntity(1L, "T", "S")));
        assertTrue(tools.queryRecentAiTasks(null).contains("任务ID：1"));
    }

    @Test
    void queryById_returnsFormattedResult() {
        AiTask entity = createEntity(1L, "INTERVIEW", "RUNNING");
        when(mapper.selectById(1L)).thenReturn(entity);

        String result = tools.queryAiTaskById(1L);
        assertTrue(result.contains("任务ID：1"));
        assertTrue(result.contains("INTERVIEW"));
    }

    @Test
    void queryById_withNullId_returnsError() {
        assertTrue(tools.queryAiTaskById(null).contains("不能为空"));
    }

    @Test
    void queryById_withNoResult_returnsNotFound() {
        when(mapper.selectById(999L)).thenReturn(null);
        assertTrue(tools.queryAiTaskById(999L).contains("未查询到"));
    }

    private static AiTask createEntity(Long id, String taskType, String status) {
        AiTask e = new AiTask();
        e.setId(id);
        e.setTaskType(taskType);
        e.setBizType("APPLICATION");
        e.setBizId(100L);
        e.setStatus(status);
        e.setSource("LLM");
        e.setModelName("deepseek-chat");
        e.setStartedAt(LocalDateTime.now());
        return e;
    }
}
