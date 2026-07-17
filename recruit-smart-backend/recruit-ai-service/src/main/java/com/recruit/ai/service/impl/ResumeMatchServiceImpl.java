package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.match.ResumeMatchAlgorithm;
import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResult;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.service.ResumeMatchService;
import com.recruit.ai.service.llm.LlmResumeMatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeMatchServiceImpl implements ResumeMatchService {

    private final ResumeMatchAlgorithm resumeMatchAlgorithm;
    private final KnowledgeBaseService knowledgeBaseService;
    private final LlmResumeMatchService llmResumeMatchService;

    @Override
    public ResumeMatchResponse matchResume(ResumeMatchRequest request) {
        String query = buildKnowledgeQuery(request);
        KnowledgeSearchResponse searchResponse = knowledgeBaseService.searchKnowledge(query, 3);
        String knowledgeContext = buildKnowledgeContext(searchResponse);

        try {
            return llmResumeMatchService.match(request, knowledgeContext);
        } catch (Exception e) {
            log.warn("大模型匹配失败，降级使用规则算法", e);
            return resumeMatchAlgorithm.match(request, knowledgeContext);
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
        if (searchResponse == null || searchResponse.getResults() == null) {
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