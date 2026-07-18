package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.recruit.biz.controller.AdminUserController;
import com.recruit.biz.dto.AdminPasswordResetDTO;
import com.recruit.biz.dto.AdminUserCreateDTO;
import com.recruit.biz.dto.AdminUserRoleUpdateDTO;
import com.recruit.biz.dto.AdminUserStatusUpdateDTO;
import com.recruit.biz.dto.AdminUserUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.SysRole;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.SysRoleMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.vo.AdminUserVO;
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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private CandidateMapper candidateMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @BeforeEach
    void setUpUserContext() {
        UserContext.set(new CurrentUser(1L, "admin", "ADMIN"));
    }

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void adminControllerOnlyAllowsAdminRole() {
        RequireRoles requireRoles = AdminUserController.class
                .getAnnotation(RequireRoles.class);

        assertNotNull(requireRoles);
        assertEquals(1, requireRoles.value().length);
        assertTrue(Arrays.asList(requireRoles.value()).contains("ADMIN"));
    }

    @Test
    void createsCandidateAccountAndCandidateProfile() {
        AdminUserCreateDTO dto = createDTO(4L);
        when(sysUserMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(candidateMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(sysRoleMapper.selectById(4L)).thenReturn(role(4L, "CANDIDATE"));
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(10L);
            return 1;
        });

        Long userId = adminUserService.createUser(dto);

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        ArgumentCaptor<Candidate> candidateCaptor = ArgumentCaptor.forClass(Candidate.class);
        verify(sysUserMapper).insert(userCaptor.capture());
        verify(candidateMapper).insert(candidateCaptor.capture());
        assertEquals(10L, userId);
        assertEquals("encoded-password", userCaptor.getValue().getPassword());
        assertEquals(1, userCaptor.getValue().getStatus());
        assertEquals(10L, candidateCaptor.getValue().getUserId());
        assertEquals("测试用户", candidateCaptor.getValue().getName());
        assertEquals("ADMIN_CREATE", candidateCaptor.getValue().getSource());
        assertEquals(1L, candidateCaptor.getValue().getCreatedBy());
    }

    @Test
    void createsHrAccountWithoutCandidateProfile() {
        AdminUserCreateDTO dto = createDTO(2L);
        when(sysUserMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(sysRoleMapper.selectById(2L)).thenReturn(role(2L, "HR"));
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(11L);
            return 1;
        });

        Long userId = adminUserService.createUser(dto);

        assertEquals(11L, userId);
        verify(candidateMapper, never()).insert(any(Candidate.class));
    }

    @Test
    void rejectsDuplicateUsername() {
        AdminUserCreateDTO dto = createDTO(2L);
        when(sysUserMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminUserService.createUser(dto)
        );

        assertEquals("用户名已存在", exception.getMessage());
        verify(sysRoleMapper, never()).selectById(any());
        verify(sysUserMapper, never()).insert(any(SysUser.class));
    }

    @Test
    void removingCandidateRoleDetachesCandidateProfile() {
        SysUser user = user(20L, 4L);
        Candidate candidate = new Candidate();
        candidate.setId(30L);
        candidate.setUserId(20L);
        when(sysUserMapper.selectById(20L)).thenReturn(user);
        when(sysRoleMapper.selectById(4L)).thenReturn(role(4L, "CANDIDATE"));
        when(sysRoleMapper.selectById(2L)).thenReturn(role(2L, "HR"));
        when(candidateMapper.selectOne(any(Wrapper.class))).thenReturn(candidate);
        AdminUserRoleUpdateDTO dto = new AdminUserRoleUpdateDTO();
        dto.setRoleId(2L);

        adminUserService.updateRole(20L, dto);

        ArgumentCaptor<Candidate> candidateCaptor = ArgumentCaptor.forClass(Candidate.class);
        verify(candidateMapper).updateById(candidateCaptor.capture());
        assertNull(candidateCaptor.getValue().getUserId());
        assertEquals(2L, user.getRoleId());
        verify(sysUserMapper).updateById(user);
    }

    @Test
    void assigningCandidateRoleCreatesCandidateProfile() {
        SysUser user = user(20L, 2L);
        user.setPhone(null);
        when(sysUserMapper.selectById(20L)).thenReturn(user);
        when(sysRoleMapper.selectById(2L)).thenReturn(role(2L, "HR"));
        when(sysRoleMapper.selectById(4L)).thenReturn(role(4L, "CANDIDATE"));
        when(candidateMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        AdminUserRoleUpdateDTO dto = new AdminUserRoleUpdateDTO();
        dto.setRoleId(4L);

        adminUserService.updateRole(20L, dto);

        ArgumentCaptor<Candidate> candidateCaptor = ArgumentCaptor.forClass(Candidate.class);
        verify(candidateMapper).insert(candidateCaptor.capture());
        assertEquals(20L, candidateCaptor.getValue().getUserId());
        assertEquals(4L, user.getRoleId());
    }

    @Test
    void cannotDisableCurrentAdminAccount() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, 1L));
        AdminUserStatusUpdateDTO dto = new AdminUserStatusUpdateDTO();
        dto.setStatus(0);

        assertThrows(
                BusinessException.class,
                () -> adminUserService.updateStatus(1L, dto)
        );

        verify(sysUserMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    void cannotChangeCurrentAdminRole() {
        AdminUserRoleUpdateDTO dto = new AdminUserRoleUpdateDTO();
        dto.setRoleId(2L);

        assertThrows(
                BusinessException.class,
                () -> adminUserService.updateRole(1L, dto)
        );

        verify(sysUserMapper, never()).selectById(any());
    }

    @Test
    void resetsOtherUsersPasswordWithEncoder() {
        SysUser user = user(20L, 2L);
        when(sysUserMapper.selectById(20L)).thenReturn(user);
        when(passwordEncoder.encode("new-password"))
                .thenReturn("encoded-new-password");
        AdminPasswordResetDTO dto = new AdminPasswordResetDTO();
        dto.setNewPassword("new-password");
        dto.setConfirmPassword("new-password");

        adminUserService.resetPassword(20L, dto);

        assertEquals("encoded-new-password", user.getPassword());
        verify(passwordEncoder).encode("new-password");
        verify(sysUserMapper).updateById(user);
    }

    @Test
    void updatesLinkedCandidateIdentityFields() {
        SysUser user = user(20L, 4L);
        Candidate candidate = new Candidate();
        candidate.setId(30L);
        candidate.setUserId(20L);
        when(sysUserMapper.selectById(20L)).thenReturn(user);
        when(candidateMapper.selectOne(any(Wrapper.class))).thenReturn(candidate);
        when(sysUserMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(candidateMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        AdminUserUpdateDTO dto = new AdminUserUpdateDTO();
        dto.setRealName("新姓名");
        dto.setPhone("13900000009");
        dto.setEmail("new@example.com");

        adminUserService.updateUser(20L, dto);

        assertEquals("新姓名", user.getRealName());
        assertEquals("13900000009", candidate.getPhone());
        assertEquals("new@example.com", candidate.getEmail());
        verify(sysUserMapper).updateById(user);
        verify(candidateMapper).updateById(candidate);
    }

    @Test
    void userDetailContainsRoleAndStatusFields() {
        SysUser user = user(20L, 2L);
        user.setStatus(0);
        when(sysUserMapper.selectById(20L)).thenReturn(user);
        when(sysRoleMapper.selectById(2L)).thenReturn(role(2L, "HR"));

        AdminUserVO result = adminUserService.getUserDetail(20L);

        assertEquals("HR", result.getRoleCode());
        assertEquals("HR", result.getRoleName());
        assertEquals("禁用", result.getStatusText());
        assertFalse(result.getStatus() == 1);
    }

    private AdminUserCreateDTO createDTO(Long roleId) {
        AdminUserCreateDTO dto = new AdminUserCreateDTO();
        dto.setUsername("test_user");
        dto.setPassword("123456");
        dto.setRealName("测试用户");
        dto.setPhone("13900000008");
        dto.setEmail("test@example.com");
        dto.setRoleId(roleId);
        return dto;
    }

    private SysUser user(Long id, Long roleId) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRealName("用户" + id);
        user.setPhone("13900000008");
        user.setEmail("user@example.com");
        user.setRoleId(roleId);
        user.setStatus(1);
        return user;
    }

    private SysRole role(Long id, String code) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleCode(code);
        role.setRoleName(code);
        return role;
    }
}
