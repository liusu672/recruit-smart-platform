package com.recruit.biz.service;

import com.recruit.biz.dto.InterviewAiQuestionDTO;
import com.recruit.biz.vo.AiMatchSummaryVO;
import com.recruit.feign.dto.response.FeedbackSummaryResponse;
import com.recruit.feign.dto.response.InterviewQuestionResponse;
import com.recruit.feign.dto.response.TurnoverRiskResponse;
import com.recruit.feign.dto.response.TurnoverRiskHistoryResponse;

import java.util.List;

public interface AiAggregationService {
    AiMatchSummaryVO generateResumeMatch(Long applicationId);

    InterviewQuestionResponse generateInterviewQuestions(
            Long interviewId,
            InterviewAiQuestionDTO dto
    );

    FeedbackSummaryResponse generateFeedbackSummary(Long interviewId);

    TurnoverRiskResponse assessTurnoverRisk(Long employeeId);

    List<TurnoverRiskHistoryResponse>
    listTurnoverRiskHistory(Long employeeId);
}
