package com.recruit.ai.controller;

import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import com.recruit.ai.service.ResumeMatchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI简历匹配接口")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ResumeMatchController {

    private final ResumeMatchService resumeMatchService;

    @PostMapping("/resume-match")
    public ResumeMatchResponse matchResume(@RequestBody ResumeMatchRequest request) {
        return resumeMatchService.matchResume(request);
    }
}
