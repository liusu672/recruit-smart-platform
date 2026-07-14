package com.recruit.biz.controller;

import com.recruit.biz.dto.CandidateCreateDTO;
import com.recruit.biz.dto.CandidateQueryDTO;
import com.recruit.biz.dto.CandidateUpdateDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.CandidateService;
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
    @Operation(description = "新增候选人")
    @PostMapping
    public Result<Long> createCandidate(@Valid @RequestBody CandidateCreateDTO dto){
        return Result.success(candidateService.createCandidate(dto));
    }
    @RequireRoles({"ADMIN","HR"})
    @Operation(description = "更新候选人信息")
    @PutMapping("/{id}")
    public Result<Void> updateCandidate(@PathVariable("id") Long id, @Valid @RequestBody CandidateUpdateDTO dto){
        candidateService.updateCandidate(id,dto);
        return Result.success();
    }
    @RequireRoles({"ADMIN","HR"})
    @Operation(description = "候选人列表")
    @GetMapping
    public Result<PageResult<CandidateVO>> pageCandidates(@Valid @ModelAttribute CandidateQueryDTO dto){
        return Result.success(candidateService.pageCandidate(dto));
    }
}
