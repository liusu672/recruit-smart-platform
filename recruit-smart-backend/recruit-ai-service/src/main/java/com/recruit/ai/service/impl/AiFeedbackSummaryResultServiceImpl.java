package com.recruit.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.FeedbackSummaryRequest;
import com.recruit.ai.dto.response.FeedbackSummaryResponse;
import com.recruit.ai.entity.AiFeedbackSummary;
import com.recruit.ai.mapper.AiFeedbackSummaryMapper;
import com.recruit.ai.service.AiFeedbackSummaryResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiFeedbackSummaryResultServiceImpl implements AiFeedbackSummaryResultService {

    private final AiFeedbackSummaryMapper aiFeedbackSummaryMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void saveResult(Long taskId,
                           FeedbackSummaryRequest request,
                           FeedbackSummaryResponse response,
                           String source) {
        try {
            AiFeedbackSummary result = new AiFeedbackSummary();

            result.setTaskId(taskId);

            result.setInterviewId(request.getInterviewId());
            result.setCandidateId(request.getCandidateId());
            result.setJobId(request.getJobId());

            result.setSummary(response.getSummary());
            result.setAdvantages(objectMapper.writeValueAsString(response.getAdvantages()));
            result.setRisks(objectMapper.writeValueAsString(response.getRisks()));
            result.setSuggestion(response.getSuggestion());

            result.setSource(source);
            result.setModelName("deepseek-chat");
            result.setPromptVersion("feedback-summary-v1");
            result.setGeneratedAt(LocalDateTime.now());

            aiFeedbackSummaryMapper.insert(result);
        } catch (Exception e) {
            log.error("保存AI面试反馈摘要结果失败", e);
        }
    }
}