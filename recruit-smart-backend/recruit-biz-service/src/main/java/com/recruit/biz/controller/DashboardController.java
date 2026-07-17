package com.recruit.biz.controller;

import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.DashboardService;
import com.recruit.biz.vo.DashboardOverviewVO;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "仪表盘接口")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/overview")
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "查询招聘仪表盘聚合数据")
    public Result<DashboardOverviewVO> getOverview() {
        return Result.success(dashboardService.getOverview());
    }
}
