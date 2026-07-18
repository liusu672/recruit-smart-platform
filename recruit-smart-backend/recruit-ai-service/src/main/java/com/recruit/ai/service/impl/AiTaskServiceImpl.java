package com.recruit.ai.service.impl;

import com.recruit.ai.entity.AiTask;
import com.recruit.ai.mapper.AiTaskMapper;
import com.recruit.ai.service.AiTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiTaskServiceImpl implements AiTaskService {

    private final AiTaskMapper aiTaskMapper;

    @Override
    public Long createRunningTask(String taskType,
                                  Long bizId,
                                  String bizType,
                                  String modelName,
                                  String promptVersion) {
        AiTask task = new AiTask();
        task.setTaskType(taskType);
        task.setBizId(bizId);
        task.setBizType(bizType);
        task.setStatus("RUNNING");
        task.setModelName(modelName);
        task.setPromptVersion(promptVersion);
        task.setStartedAt(LocalDateTime.now());

        aiTaskMapper.insert(task);
        return task.getId();
    }

    @Override
    public void markSuccess(Long taskId, String source) {
        if (taskId == null) {
            return;
        }

        AiTask task = new AiTask();
        task.setId(taskId);
        task.setStatus("SUCCESS");
        task.setSource(source);
        task.setFinishedAt(LocalDateTime.now());

        aiTaskMapper.updateById(task);
    }

    @Override
    public void markFailed(Long taskId, String errorMessage) {
        if (taskId == null) {
            return;
        }

        AiTask task = new AiTask();
        task.setId(taskId);
        task.setStatus("FAILED");
        task.setErrorMessage(errorMessage);
        task.setFinishedAt(LocalDateTime.now());

        aiTaskMapper.updateById(task);
    }
}