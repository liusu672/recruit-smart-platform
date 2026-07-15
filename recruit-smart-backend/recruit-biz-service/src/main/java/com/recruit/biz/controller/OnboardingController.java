package com.recruit.biz.controller;

import com.recruit.biz.dto.OnboardingMaterialRejectDTO;
import com.recruit.biz.dto.OnboardingCancelDTO;
import com.recruit.biz.dto.OnboardingQueryDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.OnboardingService;
import com.recruit.biz.vo.OnboardingDetailVO;
import com.recruit.biz.vo.OnboardingHRSummaryVO;
import com.recruit.biz.vo.OnboardingSummaryVO;
import com.recruit.common.result.PageResult;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "入职流程接口")
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {

    @Resource
    private OnboardingService onboardingService;

    @GetMapping("/me")
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "查询本人入职流程")
    public Result<List<OnboardingSummaryVO>> listMyOnboarding() {
        return Result.success(onboardingService.listMyOnboarding());
    }

    @GetMapping("/{id}")
    @RequireRoles({"CANDIDATE", "HR", "ADMIN"})
    @Operation(summary = "查询入职流程详情")
    public Result<OnboardingDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(onboardingService.getDetail(id));
    }

    @PutMapping("/{id}/submit-materials")
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "候选人提交入职材料")
    public Result<Void> submitMaterials(@PathVariable Long id) {
        onboardingService.submitMaterials(id);
        return Result.success();
    }

    @GetMapping
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "HR查询入职流程列表")
    public Result<PageResult<OnboardingHRSummaryVO>> listOnboarding(
            @Valid @ModelAttribute OnboardingQueryDTO dto
    ) {
        return Result.success(onboardingService.listOnboarding(dto));
    }

    @PutMapping("/{id}/approve-materials")
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "HR审核通过入职材料")
    public Result<Void> approveMaterials(@PathVariable Long id) {
        onboardingService.approveMaterials(id);
        return Result.success();
    }

    @PutMapping("/{id}/reject-materials")
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "HR驳回入职材料")
    public Result<Void> rejectMaterials(
            @PathVariable Long id,
            @Valid @RequestBody OnboardingMaterialRejectDTO dto
    ) {
        onboardingService.rejectMaterials(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/complete")
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "完成入职并创建员工档案")
    public Result<Void> complete(@PathVariable Long id) {
        onboardingService.completeOnboarding(id);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "取消入职流程")
    public Result<Void> cancel(
            @PathVariable Long id,
            @Valid @RequestBody OnboardingCancelDTO dto
    ) {
        onboardingService.cancelOnboarding(id, dto);
        return Result.success();
    }
}
