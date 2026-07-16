package com.recruit.biz.controller;

import com.recruit.biz.dto.JobApplicationHRQueryDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.JobApplicationService;
import com.recruit.biz.vo.JobApplicationHRSummaryVO;
import com.recruit.common.result.PageResult;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "HR投递管理接口")
@RestController
@RequestMapping("/jobs")
public class JobApplicationHRController {

    @Resource
    private JobApplicationService jobApplicationService;

    @GetMapping("/{jobId}/applications")
    @RequireRoles({"ADMIN", "HR", "INTERVIEWER"})
    @Operation(summary = "HR按职位查询投递列表")
    public Result<PageResult<JobApplicationHRSummaryVO>> listJobApplications(
            @PathVariable Long jobId,
            @Valid @ModelAttribute JobApplicationHRQueryDTO dto
    ) {
        return Result.success(
                jobApplicationService.listJobApplications(jobId, dto)
        );
    }
}
