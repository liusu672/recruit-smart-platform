package com.recruit.biz.controller;

import com.recruit.biz.dto.JobApplicationCreateDTO;
import com.recruit.biz.dto.JobApplicationQueryDTO;
import com.recruit.biz.dto.JobApplicationRejectDTO;
import com.recruit.biz.dto.JobApplicationStatusUpdateDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.JobApplicationService;
import com.recruit.biz.vo.JobApplicationSummaryVO;
import com.recruit.biz.vo.JobApplicationDetailVO;
import com.recruit.common.result.PageResult;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "职位投递接口")
@RestController
@RequestMapping("/applications")
public class JobApplicationController {

    @Resource
    private JobApplicationService jobApplicationService;

    @PostMapping
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "投递职位")
    public Result<Long> create(
            @Valid @RequestBody JobApplicationCreateDTO dto
    ) {
        return Result.success(
                jobApplicationService.createApplication(dto)
        );
    }

    @GetMapping("/me")
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "查询本人投递列表")
    public Result<PageResult<JobApplicationSummaryVO>> listMyApplications(
            @Valid @ModelAttribute JobApplicationQueryDTO dto
    ) {
        return Result.success(
                jobApplicationService.listMyApplications(dto)
        );
    }

    @GetMapping("/{id}")
    @RequireRoles({"CANDIDATE", "HR", "ADMIN", "INTERVIEWER"})
    @Operation(summary = "查询投递详情")
    public Result<JobApplicationDetailVO> getDetail(
            @PathVariable Long id
    ) {
        return Result.success(jobApplicationService.getDetail(id));
    }

    @PutMapping("/{id}/withdraw")
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "撤回职位投递")
    public Result<Void> withdraw(@PathVariable Long id) {
        jobApplicationService.withdraw(id);
        return Result.success();
    }

    @PutMapping("/{id}/screen-pass")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "筛选通过投递")
    public Result<Void> passScreen(@PathVariable Long id) {
        jobApplicationService.passScreen(id);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "拒绝投递")
    public Result<Void> reject(
            @PathVariable Long id,
            @Valid @RequestBody JobApplicationRejectDTO dto
    ) {
        jobApplicationService.reject(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "修改投递状态")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody JobApplicationStatusUpdateDTO dto
    ) {
        jobApplicationService.updateStatus(id, dto);
        return Result.success();
    }

}
