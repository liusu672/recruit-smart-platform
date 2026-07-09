package com.recruit.biz.controller;

import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.common.result.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Resource
    SysUserMapper sysUserMapper;
    @GetMapping("/test/user/{id}")
    public Result<SysUser> getUser(@PathVariable("id") Long id){
        SysUser user=sysUserMapper.selectById(id);
        return Result.success(user);
    }
}
