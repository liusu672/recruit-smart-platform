package com.recruit.biz.controller;

import com.recruit.biz.dto.OfferCreateDTO;
import com.recruit.biz.dto.OfferUpdateDTO;
import com.recruit.biz.dto.OfferQueryDTO;
import com.recruit.biz.dto.OfferHRQueryDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.OfferService;
import com.recruit.biz.vo.OfferDetailVO;
import com.recruit.biz.vo.OfferSummaryVO;
import com.recruit.biz.vo.OfferHRSummaryVO;
import com.recruit.common.result.PageResult;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Offer管理接口")
@RestController
@RequestMapping("/offers")
public class OfferController {

    @Resource
    private OfferService offerService;

    @PostMapping
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "创建Offer草稿")
    public Result<Long> create(@Valid @RequestBody OfferCreateDTO dto) {
        return Result.success(offerService.createOffer(dto));
    }

    @PutMapping("/{id}")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "修改Offer草稿")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody OfferUpdateDTO dto
    ) {
        offerService.updateOffer(id, dto);
        return Result.success();
    }

    @GetMapping("/{id}")
    @RequireRoles({"ADMIN", "HR", "CANDIDATE"})
    @Operation(summary = "查询Offer详情")
    public Result<OfferDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(offerService.getDetail(id));
    }

    @PutMapping("/{id}/send")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "发送Offer")
    public Result<Void> send(@PathVariable Long id) {
        offerService.sendOffer(id);
        return Result.success();
    }

    @GetMapping("/me")
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "候选人查询本人Offer列表")
    public Result<PageResult<OfferSummaryVO>> listMyOffers(
            @Valid @ModelAttribute OfferQueryDTO dto
    ) {
        return Result.success(offerService.listMyOffers(dto));
    }

    @PutMapping("/{id}/accept")
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "候选人接受Offer")
    public Result<Void> accept(@PathVariable Long id) {
        offerService.acceptOffer(id);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "候选人拒绝Offer")
    public Result<Void> reject(@PathVariable Long id) {
        offerService.rejectOffer(id);
        return Result.success();
    }

    @GetMapping
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "HR查询Offer列表")
    public Result<PageResult<OfferHRSummaryVO>> listOffers(
            @Valid @ModelAttribute OfferHRQueryDTO dto
    ) {
        return Result.success(offerService.listOffers(dto));
    }

    @PutMapping("/{id}/revoke")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "撤回Offer")
    public Result<Void> revoke(@PathVariable Long id) {
        offerService.revokeOffer(id);
        return Result.success();
    }
}
