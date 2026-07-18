package com.recruit.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import com.recruit.ai.entity.AiTurnoverRiskResult;
import com.recruit.ai.mapper.AiTurnoverRiskResultMapper;
import com.recruit.ai.service.AiTurnoverRiskResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiTurnoverRiskResultServiceImpl implements AiTurnoverRiskResultService {

    private final AiTurnoverRiskResultMapper aiTurnoverRiskResultMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void saveResult(Long taskId,
                           TurnoverRiskRequest request,
                           TurnoverRiskResponse response,
                           String source) {
        try {
            AiTurnoverRiskResult result = new AiTurnoverRiskResult();

            result.setTaskId(taskId);
            result.setEmployeeId(request.getEmployeeId());

            result.setRiskLevel(response.getRiskLevel());
            result.setRiskScore(response.getRiskScore());
            result.setSummary(response.getSummary());
            result.setRiskReasons(objectMapper.writeValueAsString(response.getRiskReasons()));
            result.setSuggestions(objectMapper.writeValueAsString(response.getSuggestions()));

            result.setSource(source);
            result.setModelName("deepseek-chat");
            result.setPromptVersion("turnover-risk-v1");
            result.setGeneratedAt(LocalDateTime.now());

            aiTurnoverRiskResultMapper.insert(result);
        } catch (Exception e) {
            log.error("保存AI离职风险预测结果失败", e);
        }
    }
}