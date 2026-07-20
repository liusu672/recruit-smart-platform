package com.recruit.biz.service.impl;

import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.SysRoleMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminFinTest {
    @Mock SysUserMapper sum; @Mock SysRoleMapper srm; @Mock CandidateMapper cm; @Mock PasswordEncoder pe;
    @InjectMocks AdminUserServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"admin","ADMIN")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test void getDetailUserNotFound() {
        when(sum.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> s.getUserDetail(99L));
    }
}
