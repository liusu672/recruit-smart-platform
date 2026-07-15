package com.recruit.biz.service;

import com.recruit.biz.dto.InterviewFeedbackCreateDTO;
import com.recruit.biz.vo.InterviewFeedbackVO;

public interface InterviewFeedbackService {
    Long submitFeedback(
            Long interviewId,
            InterviewFeedbackCreateDTO dto
    );
    InterviewFeedbackVO getFeedback(Long interviewId);
}
