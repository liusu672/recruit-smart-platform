package com.recruit.biz.controller;

import com.recruit.biz.dto.CandidateRegisterDTO;
import com.recruit.biz.dto.LoginDTO;
import com.recruit.biz.service.AuthService;
import com.recruit.biz.vo.LoginVO;
import com.recruit.biz.vo.UserInfoVO;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name="登录认证接口")
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Resource
    private AuthService authService;
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto){
        LoginVO loginVO=authService.login(dto);
        return Result.success(loginVO);
    }
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<UserInfoVO> me(){
        return Result.success(authService.getCurrentUser());
    }
    @Operation(summary = "候选人注册")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody CandidateRegisterDTO dto){
        return Result.success(authService.register(dto));
    }
}
