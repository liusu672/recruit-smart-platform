package com.recruit.biz.controller;

import com.recruit.biz.dto.CandidateCreateDTO;
import com.recruit.biz.dto.CandidateQueryDTO;
import com.recruit.biz.dto.CandidateSelfUpdateDTO;
import com.recruit.biz.dto.CandidateUpdateDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.CandidateService;
import com.recruit.biz.vo.CandidateDetailVO;
import com.recruit.biz.vo.CandidateVO;
import com.recruit.common.result.PageResult;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name="候选人管理接口")
@RestController
@RequestMapping("/candidate")
public class CandidateController {
    @Resource
    private CandidateService candidateService;
    @RequireRoles({"ADMIN","HR"})
    @Operation(summary = "新增候选人")
    @PostMapping
    public Result<Long> createCandidate(@Valid @RequestBody CandidateCreateDTO dto){
        return Result.success(candidateService.createCandidate(dto));
    }
    @RequireRoles({"ADMIN","HR"})
    @Operation(summary = "更新候选人信息")
    @PutMapping("/{id}")
    public Result<Void> updateCandidate(@PathVariable("id") Long id, @Valid @RequestBody CandidateUpdateDTO dto){
        candidateService.updateCandidate(id,dto);
        return Result.success();
    }

    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "候选人查看本人求职资料")
    @GetMapping("/me")
    public Result<CandidateDetailVO> getCurrentCandidate(){
        return Result.success(candidateService.getCurrentCandidate());
    }

    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "候选人修改本人求职资料")
    @PutMapping("/me")
    public Result<Void> updateCurrentCandidate(
            @Valid @RequestBody CandidateSelfUpdateDTO dto){
        candidateService.updateCurrentCandidate(dto);
        return Result.success();
    }

    @RequireRoles({"ADMIN","HR"})
    @Operation(summary = "查看候选人详情")
    @GetMapping("/{id}")
    public Result<CandidateDetailVO> getCandidateDetail(
            @PathVariable("id") Long id){
        return Result.success(candidateService.getCandidateDetail(id));
    }

    @RequireRoles({"ADMIN","HR"})
    @Operation(summary = "候选人列表")
    @GetMapping
    public Result<PageResult<CandidateVO>> pageCandidates(@Valid @ModelAttribute CandidateQueryDTO dto){
        return Result.success(candidateService.pageCandidate(dto));
    }
}
