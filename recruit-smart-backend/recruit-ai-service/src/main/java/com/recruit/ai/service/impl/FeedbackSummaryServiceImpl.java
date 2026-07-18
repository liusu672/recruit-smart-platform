package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.summary.FeedbackSummaryAlgorithm;
import com.recruit.ai.dto.request.FeedbackSummaryRequest;
import com.recruit.ai.dto.response.FeedbackSummaryResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResult;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.service.AiFeedbackSummaryResultService;
import com.recruit.ai.service.AiTaskService;
import com.recruit.ai.service.FeedbackSummaryService;
import com.recruit.ai.service.llm.LlmFeedbackSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackSummaryServiceImpl implements FeedbackSummaryService {

    private static final String TASK_TYPE = "FEEDBACK_SUMMARY";
    private static final String BIZ_TYPE = "INTERVIEW";
    private static final String MODEL_NAME = "deepseek-chat";
    private static final String PROMPT_VERSION = "feedback-summary-v1";

    private final FeedbackSummaryAlgorithm feedbackSummaryAlgorithm;
    private final KnowledgeBaseService knowledgeBaseService;
    private final LlmFeedbackSummaryService llmFeedbackSummaryService;
    private final AiTaskService aiTaskService;
    private final AiFeedbackSummaryResultService aiFeedbackSummaryResultService;

    @Override
    public FeedbackSummaryResponse generateSummary(FeedbackSummaryRequest request) {
        Long taskId = aiTaskService.createRunningTask(
                TASK_TYPE,
                request.getInterviewId(),
                BIZ_TYPE,
                MODEL_NAME,
                PROMPT_VERSION
        );

        try {
            FeedbackSummaryResponse response;
            String source;

            try {
                String query = buildKnowledgeQuery(request);
                KnowledgeSearchResponse searchResponse = knowledgeBaseService.searchKnowledge(query, 3);
                String knowledgeContext = buildKnowledgeContext(searchResponse);

                response = llmFeedbackSummaryService.generate(request, knowledgeContext);
                source = "LLM";
            } catch (Exception e) {
                log.warn("知识库检索或大模型面试反馈摘要失败，降级使用规则算法", e);
                response = feedbackSummaryAlgorithm.generate(request);
                source = "RULE";
            }

            aiFeedbackSummaryResultService.saveResult(taskId, request, response, source);
            aiTaskService.markSuccess(taskId, source);

            return response;
        } catch (Exception e) {
            aiTaskService.markFailed(taskId, e.getMessage());
            throw e;
        }
    }

    private String buildKnowledgeQuery(FeedbackSummaryRequest request) {
        StringBuilder builder = new StringBuilder();

        if (request.getJobTitle() != null) {
            builder.append(request.getJobTitle()).append(" ");
        }
        if (request.getFeedbackText() != null) {
            builder.append(request.getFeedbackText()).append(" ");
        }

        builder.append("面试评价标准 候选人优势 不足 风险点 录用建议");
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