package com.recruit.biz.controller;

import com.recruit.biz.dto.JobPositionCreateDTO;
import com.recruit.biz.dto.JobPositionQueryDTO;
import com.recruit.biz.dto.JobPositionUpdateDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.JobPositionService;
import com.recruit.biz.vo.JobPositionVO;
import com.recruit.common.result.PageResult;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "职位管理接口")
@RestController
@RequestMapping("/jobs")
public class JobPositionController {

    @Resource
    private JobPositionService jobPositionService;

    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "新增职位")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody JobPositionCreateDTO dto){
        return Result.success(jobPositionService.createJob(dto));
    }

    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "更新职位")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @Valid @RequestBody JobPositionUpdateDTO dto) {
        jobPositionService.updateJob(id, dto);
        return Result.success();
    }

    @RequireRoles({"ADMIN", "HR", "INTERVIEWER"})
    @Operation(summary = "查找职位")
    @GetMapping("/{id}")
    public Result<JobPositionVO> getById(@PathVariable("id") Long id){
        JobPositionVO job=jobPositionService.getById(id);
        return Result.success(job);
    }

    @RequireRoles({"ADMIN", "HR", "INTERVIEWER"})
    @Operation(summary = "职位列表")
    @GetMapping
    public Result<PageResult<JobPositionVO>> pageJobs(@Valid @ModelAttribute JobPositionQueryDTO dto){
        return Result.success(jobPositionService.jobPages(dto));
    }

    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "候选人查看招聘中职位详情")
    @GetMapping("/open/{id}")
    public Result<JobPositionVO> getOpenById(@PathVariable("id") Long id){
        return Result.success(jobPositionService.getOpenById(id));
    }

    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "候选人查看招聘中职位列表")
    @GetMapping("/open")
    public Result<PageResult<JobPositionVO>> openJobPages(
            @Valid @ModelAttribute JobPositionQueryDTO dto){
        return Result.success(jobPositionService.openJobPages(dto));
    }

    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "发布职位")
    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable("id") Long id){
        jobPositionService.publishJob(id);
        return Result.success();
    }

    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "暂停职位")
    @PutMapping("/{id}/pause")
    public Result<Void> pause(@PathVariable("id") Long id){
        jobPositionService.pauseJob(id);
        return Result.success();
    }

    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "恢复职位")
    @PutMapping("/{id}/resume")
    public Result<Void> resume(@PathVariable("id") Long id){
        jobPositionService.resumeJob(id);
        return Result.success();
    }

    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "关闭职位")
    @PutMapping("/{id}/close")
    public Result<Void> close(@PathVariable("id") Long id){
        jobPositionService.closeJob(id);
        return Result.success();
    }
}
