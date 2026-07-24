package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.biz.dto.CandidateRegisterDTO;
import com.recruit.biz.dto.LoginDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.SysRole;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.SysRoleMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.AuthService;
import com.recruit.biz.vo.LoginVO;
import com.recruit.biz.vo.UserInfoVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.util.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private CandidateMapper candidateMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Override
    public LoginVO login(LoginDTO dto){
        SysUser user= sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername,dto.getUsername())
        );
        if(user==null){
            throw new BusinessException(ErrorCode.NOT_FOUND,"用户名或密码错误");
        }
        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"用户名或密码错误");
        }
        if(!Integer.valueOf(1).equals(user.getStatus())){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"该账号已被禁用");
        }
        SysRole sysRole=sysRoleMapper.selectById(user.getRoleId());
        if (sysRole == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN,"用户角色不存在");
        }
        return completeLogin(user, sysRole);
    }
    @Override
    public UserInfoVO getCurrentUser(){
        Long userId=UserContext.getUserId();
        SysUser user= sysUserMapper.selectById(userId);
        if(user==null){
            throw new BusinessException(ErrorCode.UNAUTHORIZED,"用户不存在");
        }
        if(!Integer.valueOf(1).equals(user.getStatus())){
            throw new BusinessException(ErrorCode.UNAUTHORIZED,"该用户已被禁用");
        }
        SysRole role =sysRoleMapper.selectById(user.getRoleId());
        UserInfoVO userInfo=new UserInfoVO();
        userInfo.setUserId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setStatus(user.getStatus());
        userInfo.setRoleId(user.getRoleId());

        if (role != null) {
            userInfo.setRoleCode(role.getRoleCode());
            userInfo.setRoleName(role.getRoleName());
        }
        return userInfo;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public LoginVO register(CandidateRegisterDTO dto){
        if(!dto.getPassword().equals(dto.getConfirmPassword())){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"两次输入的密码不一致");
        }
        Long usernameCount=sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername,dto.getUsername())
        );
        if(usernameCount>0){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"该用户名已存在");
        }
        Long userPhoneCount=sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone,dto.getPhone())
        );
        if(userPhoneCount>0){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"手机号已被注册");
        }
        Long candidatePhoneCount=candidateMapper.selectCount(
                new LambdaQueryWrapper<Candidate>()
                        .eq(Candidate::getPhone,dto.getPhone())
        );
        if(candidatePhoneCount>0){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"手机号已被绑定，请联系HR");
        }
        SysRole role = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, "CANDIDATE")
        );
        if (role == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "候选人角色未配置"
            );
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getName().trim());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRoleId(role.getId());
        user.setStatus(1);
        sysUserMapper.insert(user);
        Candidate candidate = new Candidate();
        candidate.setUserId(user.getId());
        candidate.setName(dto.getName().trim());
        candidate.setGender(dto.getGender());
        candidate.setAge(dto.getAge());
        candidate.setPhone(dto.getPhone());
        candidate.setEmail(dto.getEmail());
        candidate.setEducation(dto.getEducation());
        candidate.setSchool(dto.getSchool());
        candidate.setMajor(dto.getMajor());
        candidate.setYearsOfExperience(
                dto.getYearsOfExperience() == null
                        ? 0 : dto.getYearsOfExperience()
        );
        candidate.setCurrentStatus("AVAILABLE");
        candidate.setSource("SELF_REGISTER");
        candidate.setCreatedBy(user.getId());

        candidateMapper.insert(candidate);

        return completeLogin(user, role);
    }

    private LoginVO completeLogin(SysUser user, SysRole role) {
        LocalDateTime loginTime = LocalDateTime.now();
        SysUser loginUpdate = new SysUser();
        loginUpdate.setId(user.getId());
        loginUpdate.setLastLoginAt(loginTime);
        sysUserMapper.updateById(loginUpdate);
        user.setLastLoginAt(loginTime);
        return buildLoginVO(user, role);
    }

    private LoginVO buildLoginVO(SysUser user,SysRole role){
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                role.getRoleCode()
        );

        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setUserId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setRoleId(role.getId());
        userInfo.setRoleCode(role.getRoleCode());
        userInfo.setRoleName(role.getRoleName());
        userInfo.setStatus(user.getStatus());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setTokenType("Bearer");
        loginVO.setUserInfo(userInfo);

        return loginVO;
    }
}
