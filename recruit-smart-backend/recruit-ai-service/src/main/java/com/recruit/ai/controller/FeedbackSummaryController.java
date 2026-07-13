package com.recruit.ai.controller;

import com.recruit.ai.dto.request.FeedbackSummaryRequest;
import com.recruit.ai.dto.response.FeedbackSummaryResponse;
import com.recruit.ai.service.FeedbackSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI面试反馈摘要接口", description = "根据面试官反馈内容生成结构化摘要")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class FeedbackSummaryController {

    private final FeedbackSummaryService feedbackSummaryService;

    @Operation(summary = "生成面试反馈摘要", description = "根据面试反馈文本、候选人信息和岗位信息生成优势、风险点和建议")
    @PostMapping("/feedback-summary")
    public FeedbackSummaryResponse generateSummary(@RequestBody FeedbackSummaryRequest request) {
        return feedbackSummaryService.generateSummary(request);
    }
}
