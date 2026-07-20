package com.recruit.biz.service.impl;

import com.recruit.biz.dto.PasswordUpdateDTO;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.SysRoleMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPasswordTest {

    @Mock private SysUserMapper sysUserMapper;
    @Mock private SysRoleMapper sysRoleMapper;
    @Mock private CandidateMapper candidateMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserServiceImpl userService;

    @Test
    void updatePasswordWrongOldThrows() {
        UserContext.set(new CurrentUser(1L, "user", "CANDIDATE"));
        SysUser user = new SysUser(); user.setId(1L); user.setPassword("real"); user.setStatus(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(passwordEncoder.matches("wrong", "real")).thenReturn(false);

        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("wrong"); dto.setNewPassword("newPwd"); dto.setConfirmPassword("newPwd");
        assertThrows(BusinessException.class, () -> userService.updatePassword(dto));
        UserContext.clear();
    }

    @Test
    void updatePasswordSameAsOldThrows() {
        UserContext.set(new CurrentUser(1L, "user", "CANDIDATE"));
        SysUser user = new SysUser(); user.setId(1L); user.setPassword("encodedOld"); user.setStatus(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(passwordEncoder.matches("oldPwd", "encodedOld")).thenReturn(true);

        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("oldPwd"); dto.setNewPassword("oldPwd"); dto.setConfirmPassword("oldPwd");
        assertThrows(BusinessException.class, () -> userService.updatePassword(dto));
        UserContext.clear();
    }
}
