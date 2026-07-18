package com.recruit.biz.controller;

import com.recruit.biz.dto.AdminPasswordResetDTO;
import com.recruit.biz.dto.AdminUserCreateDTO;
import com.recruit.biz.dto.AdminUserQueryDTO;
import com.recruit.biz.dto.AdminUserRoleUpdateDTO;
import com.recruit.biz.dto.AdminUserStatusUpdateDTO;
import com.recruit.biz.dto.AdminUserUpdateDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.AdminUserService;
import com.recruit.biz.vo.AdminUserVO;
import com.recruit.biz.vo.RoleOptionVO;
import com.recruit.common.result.PageResult;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理员用户管理接口")
@RestController
@RequestMapping("/admin")
@RequireRoles({"ADMIN"})
public class AdminUserController {

    @Resource
    private AdminUserService adminUserService;

    @Operation(summary = "分页查询系统用户")
    @GetMapping("/users")
    public Result<PageResult<AdminUserVO>> pageUsers(
            @Valid @ModelAttribute AdminUserQueryDTO dto) {
        return Result.success(adminUserService.pageUsers(dto));
    }

    @Operation(summary = "查询系统用户详情")
    @GetMapping("/users/{id}")
    public Result<AdminUserVO> getUserDetail(
            @PathVariable("id") Long id) {
        return Result.success(adminUserService.getUserDetail(id));
    }

    @Operation(summary = "创建系统用户")
    @PostMapping("/users")
    public Result<Long> createUser(
            @Valid @RequestBody AdminUserCreateDTO dto) {
        return Result.success(adminUserService.createUser(dto));
    }

    @Operation(summary = "修改系统用户资料")
    @PutMapping("/users/{id}")
    public Result<Void> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdminUserUpdateDTO dto) {
        adminUserService.updateUser(id, dto);
        return Result.success();
    }

    @Operation(summary = "启用或禁用系统用户")
    @PutMapping("/users/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdminUserStatusUpdateDTO dto) {
        adminUserService.updateStatus(id, dto);
        return Result.success();
    }

    @Operation(summary = "修改系统用户角色")
    @PutMapping("/users/{id}/role")
    public Result<Void> updateRole(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdminUserRoleUpdateDTO dto) {
        adminUserService.updateRole(id, dto);
        return Result.success();
    }

    @Operation(summary = "重置系统用户密码")
    @PutMapping("/users/{id}/reset-password")
    public Result<Void> resetPassword(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdminPasswordResetDTO dto) {
        adminUserService.resetPassword(id, dto);
        return Result.success();
    }

    @Operation(summary = "查询系统角色选项")
    @GetMapping("/roles")
    public Result<List<RoleOptionVO>> listRoles() {
        return Result.success(adminUserService.listRoles());
    }
}
