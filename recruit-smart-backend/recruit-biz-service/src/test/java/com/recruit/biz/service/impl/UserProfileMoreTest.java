package com.recruit.biz.service.impl;

import com.recruit.biz.dto.UserProfileUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.SysRoleMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class UserProfileMoreTest {

    @Mock private SysUserMapper sysUserMapper;
    @Mock private SysRoleMapper sysRoleMapper;
    @Mock private CandidateMapper candidateMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserServiceImpl userService;

    @BeforeEach void setUp() { UserContext.set(new CurrentUser(1L, "admin", "ADMIN")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test
    void updateCurrentUserPhoneAlreadyUsedThrows() {
        SysUser user = new SysUser(); user.setId(1L); user.setPassword("pwd"); user.setStatus(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(candidateMapper.selectOne(any())).thenReturn(null);
        when(sysUserMapper.selectCount(any())).thenReturn(1L);

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setPhone("13900139000");
        assertThrows(BusinessException.class, () -> userService.updateCurrentUser(dto));
    }

    @Test
    void updateCurrentUserWithCandidateSyncsName() {
        SysUser user = new SysUser(); user.setId(1L); user.setPassword("pwd"); user.setStatus(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        Candidate candidate = new Candidate(); candidate.setId(10L);
        when(candidateMapper.selectOne(any())).thenReturn(candidate);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(candidateMapper.updateById(any())).thenReturn(1);

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setRealName("新名字");
        userService.updateCurrentUser(dto);
        verify(sysUserMapper).updateById(any());
        verify(candidateMapper).updateById(any());
    }

    @Test
    void updateCurrentUserEmptyRealNameThrows() {
        SysUser user = new SysUser(); user.setId(1L); user.setPassword("pwd"); user.setStatus(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setRealName("");
        assertThrows(BusinessException.class, () -> userService.updateCurrentUser(dto));
    }
}
