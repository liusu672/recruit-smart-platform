package com.recruit.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.response.InterviewQuestionResponse;
import com.recruit.ai.entity.AiInterviewQuestion;
import com.recruit.ai.mapper.AiInterviewQuestionMapper;
import com.recruit.ai.prompt.InterviewQuestionPrompts;
import com.recruit.ai.service.AiInterviewQuestionResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiInterviewQuestionResultServiceImpl implements AiInterviewQuestionResultService {

    private final AiInterviewQuestionMapper aiInterviewQuestionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void saveResult(Long taskId,
                           InterviewQuestionRequest request,
                           InterviewQuestionResponse response,
                           String source) {
        try {
            AiInterviewQuestion result = new AiInterviewQuestion();

            result.setTaskId(taskId);

            result.setJobId(request.getJobId());
            result.setCandidateId(request.getCandidateId());
            result.setResumeId(request.getResumeId());

            result.setCategory(response.getCategory());
            result.setSummary(response.getSummary());
            result.setQuestions(objectMapper.writeValueAsString(response.getQuestions()));

            result.setSource(source);
            result.setModelName("deepseek-chat");
            result.setPromptVersion(InterviewQuestionPrompts.VERSION);
            result.setGeneratedAt(LocalDateTime.now());

            aiInterviewQuestionMapper.insert(result);
        } catch (Exception e) {
            log.error("保存AI面试题生成结果失败", e);
        }
    }
}