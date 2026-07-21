package com.recruit.ai.controller;

import com.recruit.ai.dto.response.ResumeParseResponse;
import com.recruit.ai.service.ResumeParseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(
        name = "AI简历解析接口",
        description = "用于提取PDF、DOC、DOCX或TXT简历正文和结构化信息"
)
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ResumeParseController {

    private final ResumeParseService resumeParseService;

    @Operation(
            summary = "解析简历文件",
            description = "提取简历正文、技能、项目经历、工作经历和教育信息"
    )
    @PostMapping(
            value = "/resume-parse",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResumeParseResponse parseResume(
            @RequestPart("file") MultipartFile file
    ) {
        return resumeParseService.parseResume(file);
    }
}
