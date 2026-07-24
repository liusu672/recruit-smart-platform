package com.recruit.biz.controller;

import com.recruit.biz.dto.JobApplicationCreateDTO;
import com.recruit.biz.dto.JobApplicationQueryDTO;
import com.recruit.biz.dto.JobApplicationRejectDTO;
import com.recruit.biz.dto.JobApplicationScreeningDTO;
import com.recruit.biz.dto.JobApplicationStatusUpdateDTO;
import com.recruit.biz.dto.PipelineApplicationQueryDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.JobApplicationService;
import com.recruit.biz.service.PipelineService;
import com.recruit.biz.vo.JobApplicationSummaryVO;
import com.recruit.biz.vo.JobApplicationDetailVO;
import com.recruit.biz.vo.PipelineApplicationDetailVO;
import com.recruit.biz.vo.PipelineApplicationSummaryVO;
import com.recruit.biz.vo.PipelineStageCountVO;
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
    @Resource
    private PipelineService pipelineService;

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

    @GetMapping("/pipeline")
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "查询招聘流程看板")
    public Result<PageResult<PipelineApplicationSummaryVO>> listPipeline(
            @Valid @ModelAttribute PipelineApplicationQueryDTO dto
    ) {
        return Result.success(pipelineService.listPipeline(dto));
    }

    @GetMapping("/pipeline/stage-counts")
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "查询招聘流程阶段数量")
    public Result<java.util.List<PipelineStageCountVO>> listPipelineStageCounts(
            @Valid @ModelAttribute PipelineApplicationQueryDTO dto
    ) {
        return Result.success(pipelineService.listStageCounts(dto));
    }

    @GetMapping("/{id}/pipeline")
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "查询招聘流程详情")
    public Result<PipelineApplicationDetailVO> getPipelineDetail(
            @PathVariable("id") Long id
    ) {
        return Result.success(pipelineService.getPipelineDetail(id));
    }

    @GetMapping("/{id}")
    @RequireRoles({"CANDIDATE", "HR", "ADMIN", "INTERVIEWER"})
    @Operation(summary = "查询投递详情")
    public Result<JobApplicationDetailVO> getDetail(
            @PathVariable("id") Long id
    ) {
        return Result.success(jobApplicationService.getDetail(id));
    }

    @PutMapping("/{id}/withdraw")
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "撤回职位投递")
    public Result<Void> withdraw(@PathVariable("id") Long id) {
        jobApplicationService.withdraw(id);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "拒绝投递")
    public Result<Void> reject(
            @PathVariable("id") Long id,
            @Valid @RequestBody JobApplicationRejectDTO dto
    ) {
        jobApplicationService.reject(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/screening")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "提交简历筛选结论")
    public Result<Void> reviewScreening(
            @PathVariable("id") Long id,
            @Valid @RequestBody JobApplicationScreeningDTO dto
    ) {
        jobApplicationService.reviewScreening(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "修改投递状态")
    public Result<Void> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody JobApplicationStatusUpdateDTO dto
    ) {
        jobApplicationService.updateStatus(id, dto);
        return Result.success();
    }

}
