package com.recruit.ai.service;

public interface AiTaskService {

    Long createRunningTask(String taskType,
                           Long bizId,
                           String bizType,
                           String modelName,
                           String promptVersion);

    void markSuccess(Long taskId, String source);

    void markFailed(Long taskId, String errorMessage);
}