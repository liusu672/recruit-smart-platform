package com.recruit.ai.service;

import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.response.InterviewQuestionResponse;

public interface InterviewQuestionService {
    InterviewQuestionResponse generateQuestions(InterviewQuestionRequest interviewQuestionRequest);
}
