package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.question.InterviewQuestionAlgorithm;
import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.response.InterviewQuestionResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResult;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.service.InterviewQuestionService;
import com.recruit.ai.service.llm.LlmInterviewQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewQuestionServiceImpl implements InterviewQuestionService {

    private final InterviewQuestionAlgorithm interviewQuestionAlgorithm;
    private final KnowledgeBaseService knowledgeBaseService;
    private final LlmInterviewQuestionService llmInterviewQuestionService;

    @Override
    public InterviewQuestionResponse generateQuestions(InterviewQuestionRequest request) {
        String query = buildKnowledgeQuery(request);
        KnowledgeSearchResponse searchResponse = knowledgeBaseService.searchKnowledge(query, 3);
        String knowledgeContext = buildKnowledgeContext(searchResponse);

        try {
            return llmInterviewQuestionService.generate(request, knowledgeContext);
        } catch (Exception e) {
            log.warn("大模型面试题生成失败，降级使用规则算法", e);
            return interviewQuestionAlgorithm.generate(request);
        }
    }

    private String buildKnowledgeQuery(InterviewQuestionRequest request) {
        StringBuilder builder = new StringBuilder();

        if (request.getJobTitle() != null) {
            builder.append(request.getJobTitle()).append(" ");
        }
        if (request.getRequirements() != null) {
            builder.append(request.getRequirements()).append(" ");
        }
        if (request.getResponsibilities() != null) {
            builder.append(request.getResponsibilities()).append(" ");
        }
        if (request.getSkills() != null) {
            builder.append(request.getSkills()).append(" ");
        }

        builder.append("面试题 评价标准 技术能力 项目经验 岗位技能");
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