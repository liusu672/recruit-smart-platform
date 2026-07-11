package com.recruit.biz.controller;

import com.recruit.biz.dto.LoginDTO;
import com.recruit.biz.service.AuthService;
import com.recruit.biz.vo.LoginVO;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
