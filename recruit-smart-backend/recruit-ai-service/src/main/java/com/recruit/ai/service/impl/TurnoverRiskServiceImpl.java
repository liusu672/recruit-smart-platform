package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.risk.TurnoverRiskAlgorithm;
import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResult;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
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
    private static final String PROMPT_VERSION = "turnover-risk-v1";

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

    private String buildKnowledgeQuery(TurnoverRiskRequest request) {
        StringBuilder builder = new StringBuilder();

        if (request.getDepartment() != null) {
            builder.append(request.getDepartment()).append(" ");
        }
        if (request.getPosition() != null) {
            builder.append(request.getPosition()).append(" ");
        }
        if (request.getPerformanceSummary() != null) {
            builder.append(request.getPerformanceSummary()).append(" ");
        }
        if (request.getAttendanceSummary() != null) {
            builder.append(request.getAttendanceSummary()).append(" ");
        }
        if (request.getSatisfactionFeedback() != null) {
            builder.append(request.getSatisfactionFeedback()).append(" ");
        }

        builder.append("离职风险 员工满意度 绩效下降 考勤异常 干预建议");
        return builder.toString().trim();
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