package com.recruit.biz.service.impl;

import com.recruit.biz.dto.AdminPasswordResetDTO;
import com.recruit.biz.dto.AdminUserRoleUpdateDTO;
import com.recruit.biz.dto.AdminUserStatusUpdateDTO;
import com.recruit.biz.entity.SysRole;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.SysRoleMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.vo.AdminUserVO;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserCoverageTest {

    @Mock private SysUserMapper sysUserMapper;
    @Mock private SysRoleMapper sysRoleMapper;
    @Mock private CandidateMapper candidateMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AdminUserServiceImpl adminUserService;

    @BeforeEach void setUp() { UserContext.set(new CurrentUser(1L, "admin", "ADMIN")); }
    @AfterEach void clear() { UserContext.clear(); }

    private SysUser user() {
        SysUser u = new SysUser(); u.setId(5L); u.setUsername("otherUser");
        u.setRealName("其他用户"); u.setStatus(1); u.setRoleId(10L);
        return u;
    }
    private SysRole role(Long id) { SysRole r = new SysRole(); r.setId(id); return r; }

    @Test
    void getUserDetailReturnsVO() {
        when(sysUserMapper.selectById(1L)).thenReturn(user());
        when(sysRoleMapper.selectById(10L)).thenReturn(role(10L));
        AdminUserVO vo = adminUserService.getUserDetail(1L);
        assertNotNull(vo);
        assertEquals("otherUser", vo.getUsername());
    }

    @Test
    void getUserDetailNotFoundThrows() {
        when(sysUserMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> adminUserService.getUserDetail(99L));
    }

    @Test
    void updateStatusSuccess() {
        when(sysUserMapper.selectById(5L)).thenReturn(user());
        when(sysUserMapper.updateById(any())).thenReturn(1);
        AdminUserStatusUpdateDTO dto = new AdminUserStatusUpdateDTO();
        dto.setStatus(0);
        adminUserService.updateStatus(5L, dto);
        verify(sysUserMapper).updateById(any());
    }

    @Test
    void resetPasswordSuccess() {
        when(sysUserMapper.selectById(5L)).thenReturn(user());
        when(passwordEncoder.encode("newPass")).thenReturn("encoded");
        when(sysUserMapper.updateById(any())).thenReturn(1);
        AdminPasswordResetDTO dto = new AdminPasswordResetDTO();
        dto.setNewPassword("newPass"); dto.setConfirmPassword("newPass");
        adminUserService.resetPassword(5L, dto);
        verify(sysUserMapper).updateById(any());
    }

    @Test
    void resetPasswordMismatchThrows() {
        AdminPasswordResetDTO dto = new AdminPasswordResetDTO();
        dto.setNewPassword("new1"); dto.setConfirmPassword("new2");
        assertThrows(BusinessException.class, () -> adminUserService.resetPassword(1L, dto));
    }

    @Test
    void listRolesReturnsRoles() {
        SysRole role = new SysRole(); role.setId(1L); role.setRoleCode("HR");
        role.setRoleName("招聘专员");
        when(sysRoleMapper.selectList(any())).thenReturn(List.of(role));
        var roles = adminUserService.listRoles();
        assertEquals(1, roles.size());
        assertEquals("HR", roles.get(0).getRoleCode());
    }
}
