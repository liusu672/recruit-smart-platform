package com.recruit.ai.controller;

import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.response.InterviewQuestionResponse;
import com.recruit.ai.service.InterviewQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI面试题生成接口", description = "根据岗位和候选人简历生成推荐面试题")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class InterviewQuestionController {

    private final InterviewQuestionService interviewQuestionService;

    @Operation(summary = "生成面试题", description = "根据岗位要求、简历内容和项目经历生成面试问题")
    @PostMapping("/interview-questions")
    public InterviewQuestionResponse generateQuestions(@RequestBody InterviewQuestionRequest request) {
        return interviewQuestionService.generateQuestions(request);
    }
}