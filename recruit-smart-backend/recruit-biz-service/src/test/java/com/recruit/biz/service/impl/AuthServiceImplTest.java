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
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.vo.LoginVO;
import com.recruit.biz.vo.UserInfoVO;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CandidateMapper candidateMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    // ==================== login() ====================

    @Test
    void loginSuccessReturnsLoginVOWithToken() {
        LoginDTO dto = loginDTO("admin", "123456");
        SysUser user = sysUser(1L, "admin", "encodedPwd", 1, 10L);
        SysRole role = sysRole(10L, "ADMIN", "系统管理员");
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("123456", "encodedPwd")).thenReturn(true);
        when(sysRoleMapper.selectById(10L)).thenReturn(role);
        when(jwtUtil.generateToken(1L, "admin", "ADMIN")).thenReturn("mock-jwt-token");

        LoginVO result = authService.login(dto);

        assertNotNull(result);
        assertEquals("mock-jwt-token", result.getToken());
        assertEquals("Bearer", result.getTokenType());
        assertNotNull(result.getUserInfo());
        assertEquals(1L, result.getUserInfo().getUserId());
        assertEquals("admin", result.getUserInfo().getUsername());
        assertEquals("ADMIN", result.getUserInfo().getRoleCode());
    }

    @Test
    void loginUserNotFoundThrowsException() {
        LoginDTO dto = loginDTO("unknown", "123456");
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(dto));
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    @Test
    void loginWrongPasswordThrowsException() {
        LoginDTO dto = loginDTO("admin", "wrong");
        SysUser user = sysUser(1L, "admin", "encodedPwd", 1, 10L);
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encodedPwd")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(dto));
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    @Test
    void loginDisabledUserThrowsException() {
        LoginDTO dto = loginDTO("admin", "123456");
        SysUser user = sysUser(1L, "admin", "encodedPwd", 0, 10L);
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("123456", "encodedPwd")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(dto));
        assertEquals("该账号已被禁用", ex.getMessage());
    }

    @Test
    void loginRoleNotFoundThrowsException() {
        LoginDTO dto = loginDTO("admin", "123456");
        SysUser user = sysUser(1L, "admin", "encodedPwd", 1, 10L);
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("123456", "encodedPwd")).thenReturn(true);
        when(sysRoleMapper.selectById(10L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(dto));
        assertEquals("用户角色不存在", ex.getMessage());
    }

    // ==================== getCurrentUser() ====================

    @Test
    void getCurrentUserSuccessReturnsUserInfo() {
        UserContext.set(new CurrentUser(1L, "admin", "ADMIN"));
        SysUser user = sysUser(1L, "admin", "pwd", 1, 10L);
        SysRole role = sysRole(10L, "ADMIN", "系统管理员");
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(sysRoleMapper.selectById(10L)).thenReturn(role);

        UserInfoVO result = authService.getCurrentUser();

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("admin", result.getUsername());
        assertEquals("系统管理员", result.getRealName());
        assertEquals("ADMIN", result.getRoleCode());
        assertEquals("系统管理员", result.getRoleName());
    }

    @Test
    void getCurrentUserNotFoundThrowsException() {
        UserContext.set(new CurrentUser(99L, "ghost", "ADMIN"));
        when(sysUserMapper.selectById(99L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> authService.getCurrentUser());
    }

    @Test
    void getCurrentUserDisabledThrowsException() {
        UserContext.set(new CurrentUser(1L, "admin", "ADMIN"));
        SysUser user = sysUser(1L, "admin", "pwd", 0, 10L);
        when(sysUserMapper.selectById(1L)).thenReturn(user);

        assertThrows(BusinessException.class,
                () -> authService.getCurrentUser());
    }

    // ==================== register() ====================

    @Test
    void registerSuccessCreatesUserAndCandidate() {
        CandidateRegisterDTO dto = registerDTO("newuser", "pass123", "pass123", "张三",
                "13800138000", "zhangsan@test.com", "男", 25);
        SysRole candidateRole = sysRole(20L, "CANDIDATE", "候选人");

        when(sysUserMapper.selectCount(argThat(wrapper -> {
            CandidateRegisterDTO queryDto = dto;
            return true; // wrapper matching is complex; we just verify counts
        }))).thenReturn(0L, 0L);
        when(candidateMapper.selectCount(any())).thenReturn(0L);
        when(sysRoleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(candidateRole);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded-pass123");
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser u = invocation.getArgument(0);
            u.setId(100L);
            return 1;
        });
        when(candidateMapper.insert(any(Candidate.class))).thenReturn(1);
        when(jwtUtil.generateToken(100L, "newuser", "CANDIDATE")).thenReturn("mock-jwt");

        LoginVO result = authService.register(dto);

        assertNotNull(result);
        assertEquals("mock-jwt", result.getToken());

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).insert(userCaptor.capture());
        assertEquals("newuser", userCaptor.getValue().getUsername());
        assertEquals("encoded-pass123", userCaptor.getValue().getPassword());
        assertEquals(20L, userCaptor.getValue().getRoleId());
        assertEquals(Integer.valueOf(1), userCaptor.getValue().getStatus());

        ArgumentCaptor<Candidate> candidateCaptor = ArgumentCaptor.forClass(Candidate.class);
        verify(candidateMapper).insert(candidateCaptor.capture());
        assertEquals(100L, candidateCaptor.getValue().getUserId());
        assertEquals("张三", candidateCaptor.getValue().getName());
        assertEquals("AVAILABLE", candidateCaptor.getValue().getCurrentStatus());
        assertEquals("SELF_REGISTER", candidateCaptor.getValue().getSource());
    }

    @Test
    void registerPasswordMismatchThrowsException() {
        CandidateRegisterDTO dto = registerDTO("newuser", "pass123", "pass456", "张三",
                "13800138000", null, null, null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(dto));
        assertEquals("两次输入的密码不一致", ex.getMessage());
    }

    @Test
    void registerDuplicateUsernameThrowsException() {
        CandidateRegisterDTO dto = registerDTO("existing", "pass123", "pass123", "张三",
                "13800138000", null, null, null);
        when(sysUserMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(dto));
        assertEquals("该用户名已存在", ex.getMessage());
    }

    @Test
    void registerDuplicateUserPhoneThrowsException() {
        CandidateRegisterDTO dto = registerDTO("newuser", "pass123", "pass123", "张三",
                "13800138000", null, null, null);
        when(sysUserMapper.selectCount(any())).thenReturn(0L, 1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(dto));
        assertEquals("手机号已被注册", ex.getMessage());
    }

    @Test
    void registerDuplicateCandidatePhoneThrowsException() {
        CandidateRegisterDTO dto = registerDTO("newuser", "pass123", "pass123", "张三",
                "13800138000", null, null, null);
        when(sysUserMapper.selectCount(any())).thenReturn(0L, 0L);
        when(candidateMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(dto));
        assertEquals("手机号已被绑定，请联系HR", ex.getMessage());
    }

    @Test
    void registerRoleNotConfiguredThrowsException() {
        CandidateRegisterDTO dto = registerDTO("newuser", "pass123", "pass123", "张三",
                "13800138000", null, null, null);
        when(sysUserMapper.selectCount(any())).thenReturn(0L, 0L);
        when(candidateMapper.selectCount(any())).thenReturn(0L);
        when(sysRoleMapper.selectOne(any())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(dto));
        assertEquals("候选人角色未配置", ex.getMessage());
    }

    // ==================== Factory Methods ====================

    private LoginDTO loginDTO(String username, String password) {
        LoginDTO dto = new LoginDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }

    private CandidateRegisterDTO registerDTO(String username, String password,
                                              String confirmPassword, String name,
                                              String phone, String email,
                                              String gender, Integer age) {
        CandidateRegisterDTO dto = new CandidateRegisterDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setConfirmPassword(confirmPassword);
        dto.setName(name);
        dto.setPhone(phone);
        dto.setEmail(email);
        dto.setGender(gender);
        dto.setAge(age);
        dto.setEducation("本科");
        dto.setSchool("测试大学");
        dto.setMajor("计算机科学");
        dto.setYearsOfExperience(3);
        return dto;
    }

    private SysUser sysUser(Long id, String username, String password,
                            Integer status, Long roleId) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName("系统管理员");
        user.setPhone("13800138000");
        user.setEmail("admin@test.com");
        user.setStatus(status);
        user.setRoleId(roleId);
        return user;
    }

    private SysRole sysRole(Long id, String roleCode, String roleName) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        return role;
    }
}
