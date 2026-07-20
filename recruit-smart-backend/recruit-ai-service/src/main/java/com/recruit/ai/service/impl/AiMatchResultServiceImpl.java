package com.recruit.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import com.recruit.ai.entity.AiMatchResult;
import com.recruit.ai.mapper.AiMatchResultMapper;
import com.recruit.ai.prompt.ResumeMatchPrompts;
import com.recruit.ai.service.AiMatchResultService;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiMatchResultServiceImpl implements AiMatchResultService {

    private final AiMatchResultMapper aiMatchResultMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void saveResult(Long taskId,
                           ResumeMatchRequest request,
                           ResumeMatchResponse response,
                           String source) {
        validateRequest(request, response);

        AiMatchResult result = new AiMatchResult();
        result.setTaskId(taskId);
        result.setApplicationId(request.getApplicationId());
        result.setJobId(request.getJobId());
        result.setCandidateId(request.getCandidateId());
        result.setResumeId(request.getResumeId());

        result.setMatchScore(response.getScore() == null
                ? null
                : BigDecimal.valueOf(response.getScore()));
        result.setRecommendLevel(response.getLevel());
        result.setRecommendReason(response.getSummary());
        result.setHighlightSummary(summarize(response.getMatchedPoints()));
        result.setMatchedPoints(toJson(response.getMatchedPoints()));
        result.setRiskSummary(summarize(response.getRiskPoints()));
        result.setRiskPoints(toJson(response.getRiskPoints()));
        result.setSuggestion(response.getSuggestion());

        result.setSource(source);
        result.setModelName("deepseek-chat");
        result.setPromptVersion(ResumeMatchPrompts.VERSION);
        result.setGeneratedAt(LocalDateTime.now());

        int affectedRows = aiMatchResultMapper.upsert(result);
        if (affectedRows < 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "保存AI简历匹配结果失败"
            );
        }
    }

    private void validateRequest(
            ResumeMatchRequest request,
            ResumeMatchResponse response
    ) {
        if (request == null || response == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "AI简历匹配请求或结果不能为空"
            );
        }
        if (request.getApplicationId() == null
                || request.getJobId() == null
                || request.getCandidateId() == null
                || request.getResumeId() == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "AI简历匹配关联业务数据不完整"
            );
        }
    }

    private String toJson(List<String> points) {
        if (points == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(points);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI简历匹配明细序列化失败"
            );
        }
    }

    private String summarize(List<String> points) {
        if (points == null || points.isEmpty()) {
            return null;
        }
        return String.join("；", points);
    }
}
