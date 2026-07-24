package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.question.InterviewQuestionAlgorithm;
import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.response.InterviewQuestionResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResult;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.prompt.InterviewQuestionPrompts;
import com.recruit.ai.service.AiInterviewQuestionResultService;
import com.recruit.ai.service.AiTaskService;
import com.recruit.ai.service.InterviewQuestionService;
import com.recruit.ai.service.llm.LlmInterviewQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewQuestionServiceImpl implements InterviewQuestionService {

    private static final String TASK_TYPE = "INTERVIEW_QUESTION";
    private static final String BIZ_TYPE = "RESUME";
    private static final String MODEL_NAME = "deepseek-chat";
    private static final String PROMPT_VERSION =
            InterviewQuestionPrompts.VERSION;

    private final InterviewQuestionAlgorithm interviewQuestionAlgorithm;
    private final KnowledgeBaseService knowledgeBaseService;
    private final LlmInterviewQuestionService llmInterviewQuestionService;
    private final AiTaskService aiTaskService;
    private final AiInterviewQuestionResultService aiInterviewQuestionResultService;

    @Override
    public InterviewQuestionResponse generateQuestions(InterviewQuestionRequest request) {
        Long taskId = aiTaskService.createRunningTask(
                TASK_TYPE,
                request.getResumeId(),
                BIZ_TYPE,
                MODEL_NAME,
                PROMPT_VERSION
        );

        try {
            InterviewQuestionResponse response;
            String source;

            try {
                String query = buildKnowledgeQuery(request);
                KnowledgeSearchResponse searchResponse = knowledgeBaseService.searchKnowledge(query, 5);
                String knowledgeContext = buildKnowledgeContext(searchResponse);

                response = llmInterviewQuestionService.generate(request, knowledgeContext);
                source = "LLM";
            } catch (Exception e) {
                log.warn("知识库检索或大模型面试题生成失败，降级使用规则算法", e);
                response = interviewQuestionAlgorithm.generate(request);
                source = "RULE";
            }

            aiInterviewQuestionResultService.saveResult(taskId, request, response, source);
            aiTaskService.markSuccess(taskId, source);

            return response;
        } catch (Exception e) {
            aiTaskService.markFailed(taskId, e.getMessage());
            throw e;
        }
    }

    private String buildKnowledgeQuery(InterviewQuestionRequest request) {
        StringBuilder builder = new StringBuilder();
        String roundLabel = InterviewQuestionPrompts.roundLabel(
                request.getInterviewRound()
        );
        builder.append("三轮面试 ")
                .append(roundLabel)
                .append(" ")
                .append(InterviewQuestionPrompts.roundSearchTerms(
                        request.getInterviewRound()))
                .append(" 面试题生成规则 ")
                .append(InterviewQuestionPrompts.roundGoal(
                        request.getInterviewRound()
                ))
                .append(" ");

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

        builder.append("面试题 评价标准 评价依据 岗位技能 当前轮次 ")
                .append(roundLabel);
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
