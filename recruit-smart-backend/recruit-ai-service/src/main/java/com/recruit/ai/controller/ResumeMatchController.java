package com.recruit.ai.controller;

import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import com.recruit.ai.service.ResumeMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI简历匹配接口", description = "用于计算候选人简历与岗位要求的匹配度")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ResumeMatchController {

    private final ResumeMatchService resumeMatchService;

    @Operation(summary = "简历岗位匹配", description = "根据岗位信息和简历信息返回匹配分数、推荐等级、风险点和建议")
    @PostMapping("/resume-match")
    public ResumeMatchResponse matchResume(@RequestBody ResumeMatchRequest request) {
        return resumeMatchService.matchResume(request);
    }
}
