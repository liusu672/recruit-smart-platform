package com.recruit.ai.service.impl;

import com.recruit.ai.entity.AiTask;
import com.recruit.ai.mapper.AiTaskMapper;
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
class AiTaskServiceImplTest {

    @Mock
    private AiTaskMapper mapper;

    @Captor
    private ArgumentCaptor<AiTask> captor;

    private AiTaskServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AiTaskServiceImpl(mapper);
    }

    @Test
    void createRunningTask_insertsAndReturnsId() {
        when(mapper.insert(any())).then(invocation -> {
            AiTask t = invocation.getArgument(0);
            t.setId(100L);
            return 1;
        });

        Long taskId = service.createRunningTask("RESUME_MATCH", 200L, "APPLICATION", "deepseek", "v1");

        assertEquals(100L, taskId);
        verify(mapper).insert(captor.capture());
        AiTask task = captor.getValue();
        assertEquals("RESUME_MATCH", task.getTaskType());
        assertEquals(200L, task.getBizId());
        assertEquals("RUNNING", task.getStatus());
        assertNotNull(task.getStartedAt());
    }

    @Test
    void createRunningTask_afterInsert_idIsSetFromEntity() {
        AiTask saved = new AiTask();
        saved.setId(99L);
        when(mapper.insert(any())).then(invocation -> {
            AiTask t = invocation.getArgument(0);
            t.setId(99L);
            return 1;
        });

        Long taskId = service.createRunningTask("T", 1L, "B", "m", "v1");
        assertEquals(99L, taskId);
    }

    @Test
    void markSuccess_updatesTask() {
        service.markSuccess(1L, "LLM");

        verify(mapper).updateById(captor.capture());
        AiTask task = captor.getValue();
        assertEquals(1L, task.getId());
        assertEquals("SUCCESS", task.getStatus());
        assertEquals("LLM", task.getSource());
        assertNotNull(task.getFinishedAt());
    }

    @Test
    void markSuccess_withNullTaskId_doesNothing() {
        service.markSuccess(null, "LLM");
        verify(mapper, never()).updateById(any());
    }

    @Test
    void markFailed_updatesTask() {
        service.markFailed(1L, "error occurred");

        verify(mapper).updateById(captor.capture());
        AiTask task = captor.getValue();
        assertEquals(1L, task.getId());
        assertEquals("FAILED", task.getStatus());
        assertEquals("error occurred", task.getErrorMessage());
        assertNotNull(task.getFinishedAt());
    }

    @Test
    void markFailed_withNullTaskId_doesNothing() {
        service.markFailed(null, "error");
        verify(mapper, never()).updateById(any());
    }
}
