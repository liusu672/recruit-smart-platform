package com.recruit.biz.controller;

import com.recruit.biz.dto.PasswordUpdateDTO;
import com.recruit.biz.dto.UserProfileUpdateDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.UserService;
import com.recruit.biz.vo.InterviewerOptionVO;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "个人中心接口")
@RestController
@RequestMapping("/users")
public class UserController {

    @Resource
    private UserService userService;

    @Operation(summary = "查询可指派的面试官")
    @GetMapping("/interviewers")
    @RequireRoles({"ADMIN", "HR"})
    public Result<List<InterviewerOptionVO>> listInterviewers() {
        return Result.success(userService.listInterviewers());
    }

    @Operation(summary = "修改当前用户个人信息")
    @PutMapping("/me")
    public Result<Void> updateCurrentUser(
            @Valid @RequestBody UserProfileUpdateDTO dto) {
        userService.updateCurrentUser(dto);
        return Result.success();
    }

    @Operation(summary = "修改当前用户密码")
    @PutMapping("/me/password")
    public Result<Void> updatePassword(
            @Valid @RequestBody PasswordUpdateDTO dto) {
        userService.updatePassword(dto);
        return Result.success();
    }
}
