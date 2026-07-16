package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.question.InterviewQuestionAlgorithm;
import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.response.InterviewQuestionResponse;
import com.recruit.ai.service.InterviewQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterviewQuestionServiceImpl implements InterviewQuestionService {
    private final InterviewQuestionAlgorithm interviewQuestionAlgorithm;

    @Override
    public InterviewQuestionResponse generateQuestions(InterviewQuestionRequest request) {
        return interviewQuestionAlgorithm.generate(request);
    }
}
