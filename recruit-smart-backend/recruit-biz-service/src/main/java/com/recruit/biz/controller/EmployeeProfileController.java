package com.recruit.biz.controller;

import com.recruit.biz.dto.EmployeeQueryDTO;
import com.recruit.biz.dto.EmployeeStatusUpdateDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.EmployeeProfileService;
import com.recruit.biz.vo.EmployeeDetailVO;
import com.recruit.biz.vo.EmployeeSummaryVO;
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

@Tag(name = "员工档案接口")
@RestController
@RequestMapping("/employees")
public class EmployeeProfileController {

    @Resource
    private EmployeeProfileService employeeProfileService;

    @GetMapping
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "查询员工档案列表")
    public Result<PageResult<EmployeeSummaryVO>> listEmployees(
            @Valid @ModelAttribute EmployeeQueryDTO dto
    ) {
        return Result.success(employeeProfileService.listEmployees(dto));
    }

    @GetMapping("/{id}")
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "查询员工档案详情")
    public Result<EmployeeDetailVO> getDetail(@PathVariable("id") Long id) {
        return Result.success(employeeProfileService.getDetail(id));
    }

    @PutMapping("/{id}/status")
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "修改员工状态")
    public Result<Void> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody EmployeeStatusUpdateDTO dto
    ) {
        employeeProfileService.updateStatus(id, dto);
        return Result.success();
    }
}
