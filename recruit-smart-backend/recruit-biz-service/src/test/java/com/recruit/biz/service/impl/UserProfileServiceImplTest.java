package com.recruit.biz.service.impl;

import com.recruit.biz.dto.PasswordUpdateDTO;
import com.recruit.biz.dto.UserProfileUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock private SysUserMapper sysUserMapper;
    @Mock private CandidateMapper candidateMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() { UserContext.set(new CurrentUser(1L, "admin", "ADMIN")); }

    @AfterEach
    void clear() { UserContext.clear(); }

    @Test
    void updateCurrentUserSuccess() {
        SysUser user = new SysUser();
        user.setId(1L); user.setUsername("admin"); user.setRealName("管理员");
        user.setPhone("13800138000"); user.setStatus(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setRealName("新名字");
        dto.setEmail("new@test.com");

        userService.updateCurrentUser(dto);

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).updateById(captor.capture());
        assertEquals("新名字", captor.getValue().getRealName());
    }

    @Test
    void updateCurrentUserNoFieldsThrows() {
        assertThrows(BusinessException.class,
                () -> userService.updateCurrentUser(new UserProfileUpdateDTO()));
    }

    @Test
    void updatePasswordSuccess() {
        SysUser user = new SysUser();
        user.setId(1L); user.setPassword("encodedOld"); user.setStatus(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(passwordEncoder.matches("oldPass", "encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("oldPass");
        dto.setNewPassword("newPass");
        dto.setConfirmPassword("newPass");

        userService.updatePassword(dto);
        verify(sysUserMapper).updateById(any(SysUser.class));
    }

    @Test
    void updatePasswordMismatchThrows() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("old");
        dto.setNewPassword("new1");
        dto.setConfirmPassword("new2");
        assertThrows(BusinessException.class,
                () -> userService.updatePassword(dto));
    }

    @Test
    void updatePasswordWrongOldThrows() {
        SysUser user = new SysUser(); user.setId(1L); user.setPassword("real"); user.setStatus(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(passwordEncoder.matches("wrong", "real")).thenReturn(false);

        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("wrong"); dto.setNewPassword("new"); dto.setConfirmPassword("new");
        assertThrows(BusinessException.class,
                () -> userService.updatePassword(dto));
    }
}
