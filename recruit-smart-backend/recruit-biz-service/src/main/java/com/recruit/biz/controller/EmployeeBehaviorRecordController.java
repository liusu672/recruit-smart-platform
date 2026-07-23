package com.recruit.biz.controller;

import com.recruit.biz.dto.EmployeeBehaviorSaveDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.EmployeeBehaviorRecordService;
import com.recruit.biz.vo.EmployeeBehaviorRecordVO;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "员工行为记录接口")
@RestController
@RequestMapping(
        "/employees/{employeeId}/behavior-records"
)
@RequiredArgsConstructor
public class EmployeeBehaviorRecordController {

    private final EmployeeBehaviorRecordService service;

    @PostMapping
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "新增员工周期行为记录")
    public Result<Long> create(
            @PathVariable("employeeId") Long employeeId,
            @Valid @RequestBody EmployeeBehaviorSaveDTO dto
    ) {
        return Result.success(
                service.create(employeeId, dto)
        );
    }

    @GetMapping
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "查询员工历史行为记录")
    public Result<List<EmployeeBehaviorRecordVO>> list(
            @PathVariable("employeeId") Long employeeId
    ) {
        return Result.success(service.list(employeeId));
    }

    @PutMapping("/{recordId}")
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "修改员工行为记录草稿")
    public Result<Void> update(
            @PathVariable("employeeId") Long employeeId,
            @PathVariable("recordId") Long recordId,
            @Valid @RequestBody EmployeeBehaviorSaveDTO dto
    ) {
        service.update(employeeId, recordId, dto);
        return Result.success();
    }

    @PostMapping("/{recordId}/confirm")
    @RequireRoles({"HR", "ADMIN"})
    @Operation(summary = "确认员工行为记录")
    public Result<Void> confirm(
            @PathVariable("employeeId") Long employeeId,
            @PathVariable("recordId") Long recordId
    ) {
        service.confirm(employeeId, recordId);
        return Result.success();
    }
}