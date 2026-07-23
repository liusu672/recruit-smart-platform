package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.risk.TurnoverRiskAlgorithm;
import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResult;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.prompt.TurnoverRiskPrompts;
import com.recruit.ai.service.AiTaskService;
import com.recruit.ai.service.AiTurnoverRiskResultService;
import com.recruit.ai.service.TurnoverRiskService;
import com.recruit.ai.service.llm.LlmTurnoverRiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TurnoverRiskServiceImpl implements TurnoverRiskService {

    private static final String TASK_TYPE = "TURNOVER_RISK";
    private static final String BIZ_TYPE = "EMPLOYEE";
    private static final String MODEL_NAME = "deepseek-chat";
    private static final String PROMPT_VERSION =
            TurnoverRiskPrompts.VERSION;

    private final TurnoverRiskAlgorithm turnoverRiskAlgorithm;
    private final KnowledgeBaseService knowledgeBaseService;
    private final LlmTurnoverRiskService llmTurnoverRiskService;
    private final AiTaskService aiTaskService;
    private final AiTurnoverRiskResultService aiTurnoverRiskResultService;

    @Override
    public TurnoverRiskResponse predictRisk(TurnoverRiskRequest request) {
        Long taskId = aiTaskService.createRunningTask(
                TASK_TYPE,
                request.getEmployeeId(),
                BIZ_TYPE,
                MODEL_NAME,
                PROMPT_VERSION
        );

        try {
            TurnoverRiskResponse response;
            String source;

            try {
                String query = buildKnowledgeQuery(request);
                KnowledgeSearchResponse searchResponse = knowledgeBaseService.searchKnowledge(query, 3);
                String knowledgeContext = buildKnowledgeContext(searchResponse);

                response = llmTurnoverRiskService.predict(request, knowledgeContext);
                source = "LLM";
            } catch (Exception e) {
                log.warn("知识库检索或大模型离职风险预测失败，降级使用规则算法", e);
                response = turnoverRiskAlgorithm.predict(request);
                source = "RULE";
            }

            aiTurnoverRiskResultService.saveResult(taskId, request, response, source);
            aiTaskService.markSuccess(taskId, source);

            return response;
        } catch (Exception e) {
            aiTaskService.markFailed(taskId, e.getMessage());
            throw e;
        }
    }

    private String buildKnowledgeQuery(
            TurnoverRiskRequest request
    ) {
        StringBuilder builder = new StringBuilder();

        append(builder, request.getDepartment());
        append(builder, request.getPosition());
        append(builder, request.getPerformanceTrend());
        append(builder, request.getAttendanceTrend());
        append(builder, request.getSatisfactionTrend());
        append(builder, request.getLatestFeedback());

        builder.append("员工离职风险 情绪分析 ")
                .append("绩效下降 考勤异常 满意度下降 ")
                .append("HR干预建议");

        return builder.toString().trim();
    }

    private void append(
            StringBuilder builder,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            builder.append(value).append(" ");
        }
    }

    private String buildKnowledgeContext(KnowledgeSearchResponse searchResponse) {
        if (searchResponse == null || searchResponse.getResults() == null || searchResponse.getResults().isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (KnowledgeSearchResult result : searchResponse.getResults()) {
            if (result.getContent() != null && !result.getContent().isBlank()) {
                builder.append(result.getContent()).append("\n");
            }
        }
        return builder.toString().trim();
    }
}