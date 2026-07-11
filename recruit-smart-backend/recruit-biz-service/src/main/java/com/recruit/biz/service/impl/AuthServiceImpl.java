package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.biz.dto.LoginDTO;
import com.recruit.biz.entity.SysRole;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.SysRoleMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.service.AuthService;
import com.recruit.biz.vo.LoginVO;
import com.recruit.biz.vo.UserInfoVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.util.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private JwtUtil jwtUtil;
    @Override
    public LoginVO login(LoginDTO dto){
        SysUser user= sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername,dto.getUsername())
        );
        if(user==null){
            throw new BusinessException(ErrorCode.NOT_FOUND,"用户名或密码错误");
        }
        if(!dto.getPassword().equals(user.getPassword())){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"用户名或密码错误");
        }
        if(!Integer.valueOf(1).equals(user.getStatus())){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"该账号已被禁用");
        }
        SysRole sysRole=sysRoleMapper.selectById(user.getRoleId());
        String roleCode=sysRole==null?null: sysRole.getRoleCode();
        String roleName=sysRole==null?null: sysRole.getRoleName();
        String token= jwtUtil.generateToken(user.getId(), user.getUsername(),roleCode);
        UserInfoVO userInfo=new UserInfoVO();
        userInfo.setUsername(user.getUsername());
        userInfo.setUserId(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setStatus(user.getStatus());
        userInfo.setRoleId(user.getRoleId());
        userInfo.setRoleCode(roleCode);
        userInfo.setRoleName(roleName);
        userInfo.setRealName(user.getRealName());
        LoginVO loginVO=new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(userInfo);
        loginVO.setTokenType("Bearer");
        return loginVO;
    }

}
