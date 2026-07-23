package com.recruit.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import com.recruit.ai.entity.AiTurnoverRiskResult;
import com.recruit.ai.mapper.AiTurnoverRiskResultMapper;
import com.recruit.ai.prompt.TurnoverRiskPrompts;
import com.recruit.ai.service.AiTurnoverRiskResultService;
import com.recruit.feign.dto.request.EmployeeBehaviorRecordDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.recruit.feign.dto.response.TurnoverRiskHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiTurnoverRiskResultServiceImpl implements AiTurnoverRiskResultService {

    private final AiTurnoverRiskResultMapper aiTurnoverRiskResultMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void saveResult(
            Long taskId,
            TurnoverRiskRequest request,
            TurnoverRiskResponse response,
            String source
    ) {
        try {
            AiTurnoverRiskResult result =
                    new AiTurnoverRiskResult();

            result.setTaskId(taskId);
            result.setEmployeeId(request.getEmployeeId());

            result.setSentimentLabel(
                    response.getSentimentLabel()
            );
            result.setSentimentRiskScore(
                    response.getSentimentRiskScore()
            );
            result.setSentimentSummary(
                    response.getSentimentSummary()
            );

            result.setRiskLevel(response.getRiskLevel());
            result.setRiskScore(response.getRiskScore());
            result.setSummary(response.getSummary());

            result.setRiskReasons(
                    objectMapper.writeValueAsString(
                            emptyIfNull(response.getRiskReasons())
                    )
            );

            result.setSuggestions(
                    objectMapper.writeValueAsString(
                            emptyIfNull(response.getSuggestions())
                    )
            );

            result.setPeriodStart(request.getPeriodStart());
            result.setPeriodEnd(request.getPeriodEnd());

            result.setBehaviorRecordIds(
                    objectMapper.writeValueAsString(
                            extractBehaviorRecordIds(request)
                    )
            );

            result.setSource(source);
            result.setModelName("deepseek-chat");
            result.setPromptVersion(
                    TurnoverRiskPrompts.VERSION
            );
            result.setGeneratedAt(LocalDateTime.now());

            int inserted =
                    aiTurnoverRiskResultMapper.insert(result);

            if (inserted != 1) {
                throw new IllegalStateException(
                        "保存AI离职风险结果失败"
                );
            }
        } catch (Exception e) {
            log.error("保存AI离职风险预测结果失败", e);

            throw new IllegalStateException(
                    "保存AI离职风险预测结果失败",
                    e
            );
        }
    }

    @Override
    public List<TurnoverRiskHistoryResponse> listByEmployeeId(
            Long employeeId
    ) {
        if (employeeId == null) {
            throw new IllegalArgumentException(
                    "员工ID不能为空"
            );
        }

        List<AiTurnoverRiskResult> results =
                aiTurnoverRiskResultMapper.selectList(
                        new LambdaQueryWrapper
                                <AiTurnoverRiskResult>()
                                .eq(
                                        AiTurnoverRiskResult
                                                ::getEmployeeId,
                                        employeeId
                                )
                                .orderByDesc(
                                        AiTurnoverRiskResult
                                                ::getGeneratedAt
                                )
                                .orderByDesc(
                                        AiTurnoverRiskResult::getId
                                )
                );

        return results.stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private List<Long> extractBehaviorRecordIds(
            TurnoverRiskRequest request
    ) {
        if (request.getBehaviorRecords() == null) {
            return List.of();
        }

        return request.getBehaviorRecords()
                .stream()
                .map(EmployeeBehaviorRecordDTO::getRecordId)
                .filter(id -> id != null)
                .toList();
    }

    private <T> List<T> emptyIfNull(List<T> values) {
        return values == null ? List.of() : values;
    }

    private TurnoverRiskHistoryResponse toHistoryResponse(
            AiTurnoverRiskResult result
    ) {
        TurnoverRiskHistoryResponse response =
                new TurnoverRiskHistoryResponse();

        response.setId(result.getId());
        response.setTaskId(result.getTaskId());
        response.setEmployeeId(result.getEmployeeId());

        response.setSentimentLabel(
                result.getSentimentLabel()
        );
        response.setSentimentRiskScore(
                result.getSentimentRiskScore()
        );
        response.setSentimentSummary(
                result.getSentimentSummary()
        );

        response.setRiskLevel(result.getRiskLevel());
        response.setRiskScore(result.getRiskScore());
        response.setSummary(result.getSummary());

        response.setRiskReasons(
                readStringList(result.getRiskReasons())
        );
        response.setSuggestions(
                readStringList(result.getSuggestions())
        );

        response.setPeriodStart(result.getPeriodStart());
        response.setPeriodEnd(result.getPeriodEnd());

        response.setBehaviorRecordIds(
                readLongList(result.getBehaviorRecordIds())
        );

        response.setSource(result.getSource());
        response.setModelName(result.getModelName());
        response.setPromptVersion(
                result.getPromptVersion()
        );
        response.setGeneratedAt(result.getGeneratedAt());

        return response;
    }

    private List<String> readStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<String>>() {
                    }
            );
        } catch (Exception e) {
            log.warn("解析风险结果字符串数组失败", e);
            return List.of();
        }
    }

    private List<Long> readLongList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<Long>>() {
                    }
            );
        } catch (Exception e) {
            log.warn("解析行为记录ID数组失败", e);
            return List.of();
        }
    }
}