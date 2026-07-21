package com.recruit.feign.client;

import com.recruit.feign.dto.request.FeedbackSummaryRequest;
import com.recruit.feign.dto.request.InterviewQuestionRequest;
import com.recruit.feign.dto.request.ResumeMatchRequest;
import com.recruit.feign.dto.request.TurnoverRiskRequest;
import com.recruit.feign.dto.response.FeedbackSummaryResponse;
import com.recruit.feign.dto.response.InterviewQuestionResponse;
import com.recruit.feign.dto.response.ResumeMatchResponse;
import com.recruit.feign.dto.response.ResumeParseResponse;
import com.recruit.feign.dto.response.TurnoverRiskResponse;
import org.springframework.http.MediaType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "recruit-ai-service", path = "/api/ai")
public interface AiServiceClient {

    @PostMapping("/resume-match")
    ResumeMatchResponse matchResume(@RequestBody ResumeMatchRequest request);

    @PostMapping(
            value = "/resume-parse",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResumeParseResponse parseResume(
            @RequestPart("file") MultipartFile file
    );

    @PostMapping("/interview-questions")
    InterviewQuestionResponse generateInterviewQuestions(
            @RequestBody InterviewQuestionRequest request
    );

    @PostMapping("/feedback-summary")
    FeedbackSummaryResponse generateFeedbackSummary(
            @RequestBody FeedbackSummaryRequest request
    );

    @PostMapping("/turnover-risk")
    TurnoverRiskResponse predictTurnoverRisk(
            @RequestBody TurnoverRiskRequest request
    );
}
