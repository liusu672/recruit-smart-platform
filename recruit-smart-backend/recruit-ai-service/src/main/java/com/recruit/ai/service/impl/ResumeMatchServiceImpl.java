package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.match.ResumeMatchAlgorithm;
import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResult;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.service.AiMatchResultService;
import com.recruit.ai.service.AiTaskService;
import com.recruit.ai.service.ResumeMatchService;
import com.recruit.ai.service.llm.LlmResumeMatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeMatchServiceImpl implements ResumeMatchService {

    private static final String TASK_TYPE = "RESUME_MATCH";
    private static final String BIZ_TYPE = "APPLICATION";
    private static final String MODEL_NAME = "deepseek-chat";
    private static final String PROMPT_VERSION = "resume-match-v1";

    private final ResumeMatchAlgorithm resumeMatchAlgorithm;
    private final KnowledgeBaseService knowledgeBaseService;
    private final LlmResumeMatchService llmResumeMatchService;
    private final AiTaskService aiTaskService;
    private final AiMatchResultService aiMatchResultService;

    @Override
    public ResumeMatchResponse matchResume(ResumeMatchRequest request) {
        Long taskId = aiTaskService.createRunningTask(
                TASK_TYPE,
                request.getApplicationId(),
                BIZ_TYPE,
                MODEL_NAME,
                PROMPT_VERSION
        );

        try {
            ResumeMatchResponse response;
            String source;

            try {
                String query = buildKnowledgeQuery(request);
                KnowledgeSearchResponse searchResponse = knowledgeBaseService.searchKnowledge(query, 3);
                String knowledgeContext = buildKnowledgeContext(searchResponse);

                response = llmResumeMatchService.match(request, knowledgeContext);
                source = "LLM";
            } catch (Exception e) {
                log.warn("知识库检索或大模型匹配失败，降级使用规则算法", e);
                response = resumeMatchAlgorithm.match(request, "");
                source = "RULE";
            }

            aiMatchResultService.saveResult(taskId, request, response, source);
            aiTaskService.markSuccess(taskId, source);

            return response;
        } catch (Exception e) {
            aiTaskService.markFailed(taskId, e.getMessage());
            throw e;
        }
    }

    private String buildKnowledgeQuery(ResumeMatchRequest request) {
        StringBuilder builder = new StringBuilder();

        if (request.getJobTitle() != null) {
            builder.append(request.getJobTitle()).append(" ");
        }
        if (request.getResponsibilities() != null) {
            builder.append(request.getResponsibilities()).append(" ");
        }
        if (request.getRequirements() != null) {
            builder.append(request.getRequirements()).append(" ");
        }

        builder.append("简历匹配标准 岗位技能 胜任力要求");
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