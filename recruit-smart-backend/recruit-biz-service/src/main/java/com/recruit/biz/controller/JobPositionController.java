package com.recruit.biz.controller;

import com.recruit.biz.dto.JobPositionCreateDTO;
import com.recruit.biz.dto.JobPositionQueryDTO;
import com.recruit.biz.dto.JobPositionUpdateDTO;
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

    @Operation(summary = "新增职位")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody JobPositionCreateDTO dto){
        return Result.success(jobPositionService.createJob(dto));
    }

    @Operation(summary = "更新职位")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @Valid @RequestBody JobPositionUpdateDTO dto) {
        jobPositionService.updateJob(id, dto);
        return Result.success();
    }

    @Operation(summary = "查找职位")
    @GetMapping("/{id}")
    public Result<JobPositionVO> getById(@PathVariable("id") Long id){
        JobPositionVO job=jobPositionService.getById(id);
        return Result.success(job);
    }

    @Operation(summary = "职位列表")
    @GetMapping
    public Result<PageResult<JobPositionVO>> pageJobs(JobPositionQueryDTO dto){
        return Result.success(jobPositionService.jobPages(dto));
    }
    @Operation(summary = "发布职位")
    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable("id") Long id){
        jobPositionService.publishJob(id);
        return Result.success();
    }
    @Operation(summary = "关闭职位")
    @PutMapping("/{id}/close")
    public Result<Void> close(@PathVariable("id") Long id){
        jobPositionService.closeJob(id);
        return Result.success();
    }
}