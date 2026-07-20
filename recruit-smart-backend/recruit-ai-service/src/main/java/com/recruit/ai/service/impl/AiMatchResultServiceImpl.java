package com.recruit.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import com.recruit.ai.entity.AiMatchResult;
import com.recruit.ai.mapper.AiMatchResultMapper;
import com.recruit.ai.prompt.ResumeMatchPrompts;
import com.recruit.ai.service.AiMatchResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiMatchResultServiceImpl implements AiMatchResultService {

    private final AiMatchResultMapper aiMatchResultMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void saveResult(Long taskId,
                           ResumeMatchRequest request,
                           ResumeMatchResponse response,
                           String source) {
        try {
            AiMatchResult result = new AiMatchResult();

            result.setTaskId(taskId);

            result.setApplicationId(request.getApplicationId());
            result.setJobId(request.getJobId());
            result.setCandidateId(request.getCandidateId());
            result.setResumeId(request.getResumeId());

            result.setScore(response.getScore());
            result.setLevel(response.getLevel());
            result.setSummary(response.getSummary());
            result.setMatchedPoints(objectMapper.writeValueAsString(response.getMatchedPoints()));
            result.setRiskPoints(objectMapper.writeValueAsString(response.getRiskPoints()));
            result.setSuggestion(response.getSuggestion());

            result.setSource(source);
            result.setModelName("deepseek-chat");
            result.setPromptVersion(ResumeMatchPrompts.VERSION);
            result.setGeneratedAt(LocalDateTime.now());

            aiMatchResultMapper.insert(result);
        } catch (Exception e) {
            log.error("保存AI简历匹配结果失败", e);
        }
    }
}