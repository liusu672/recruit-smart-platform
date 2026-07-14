package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.biz.dto.PasswordUpdateDTO;
import com.recruit.biz.dto.UserProfileUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.UserService;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private CandidateMapper candidateMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCurrentUser(UserProfileUpdateDTO dto) {
        if (dto.getRealName() == null
                && dto.getPhone() == null
                && dto.getEmail() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "没有需要修改的信息");
        }

        Long userId = UserContext.getUserId();
        SysUser user = getUser(userId);
        Candidate candidate = candidateMapper.selectOne(
                new LambdaQueryWrapper<Candidate>()
                        .eq(Candidate::getUserId, userId)
        );

        if (dto.getRealName() != null) {
            if (!StringUtils.hasText(dto.getRealName())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "真实姓名不能为空");
            }
            String realName = dto.getRealName().trim();
            user.setRealName(realName);
            if (candidate != null) {
                candidate.setName(realName);
            }
        }

        if (dto.getPhone() != null) {
            Long userPhoneCount = sysUserMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getPhone, dto.getPhone())
                            .ne(SysUser::getId, userId)
            );
            if (userPhoneCount > 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "手机号已被其他用户使用");
            }

            if (candidate != null) {
                Long candidatePhoneCount = candidateMapper.selectCount(
                        new LambdaQueryWrapper<Candidate>()
                                .eq(Candidate::getPhone, dto.getPhone())
                                .ne(Candidate::getId, candidate.getId())
                );
                if (candidatePhoneCount > 0) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "手机号已被其他候选人使用");
                }
                candidate.setPhone(dto.getPhone());
            }
            user.setPhone(dto.getPhone());
        }

        if (dto.getEmail() != null) {
            if (!StringUtils.hasText(dto.getEmail())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "邮箱不能为空");
            }
            String email = dto.getEmail().trim();
            user.setEmail(email);
            if (candidate != null) {
                candidate.setEmail(email);
            }
        }

        sysUserMapper.updateById(user);
        if (candidate != null) {
            candidateMapper.updateById(candidate);
        }
    }

    @Override
    public void updatePassword(PasswordUpdateDTO dto) {
        SysUser user = getUser(UserContext.getUserId());

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "原密码不正确");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次输入的新密码不一致");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "新密码不能与原密码相同");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    private SysUser getUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在或已被禁用");
        }
        return user;
    }
}
