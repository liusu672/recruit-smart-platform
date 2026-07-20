package com.recruit.biz.service.impl;
import com.recruit.biz.dto.AdminUserQueryDTO;
import com.recruit.biz.entity.SysRole;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.vo.AdminUserVO;
import com.recruit.common.result.PageResult;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class T14AdminPageTest {
    @Mock SysUserMapper sum; @Mock SysRoleMapper srm; @Mock CandidateMapper cm;
    @Mock org.springframework.security.crypto.password.PasswordEncoder pe;
    @InjectMocks AdminUserServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"admin","ADMIN")); }
    @AfterEach void clear() { UserContext.clear(); }
    @Test void pageUsersWithUsernameFilter() {
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysUser>(1,10,2);
        SysUser u1 = new SysUser(); u1.setId(1L); u1.setUsername("admin"); u1.setStatus(1); u1.setRoleId(10L);
        SysUser u2 = new SysUser(); u2.setId(2L); u2.setUsername("hr"); u2.setStatus(1); u2.setRoleId(10L);
        page.setRecords(List.of(u1,u2));
        when(sum.selectPage(any(),any())).thenReturn(page);
        SysRole role = new SysRole(); role.setId(10L); role.setRoleName("管理员");
        when(srm.selectBatchIds(any())).thenReturn(List.of(role));
        var dto = new AdminUserQueryDTO(); dto.setKeyword("admin");
        PageResult<AdminUserVO> r = s.pageUsers(dto);
        assertEquals(2L, r.getTotal());
    }
    @Test void pageUsersWithStatusFilter() {
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysUser>(1,10,0);
        page.setRecords(List.of());
        when(sum.selectPage(any(),any())).thenReturn(page);
        var dto = new AdminUserQueryDTO(); dto.setStatus(1);
        assertEquals(0L, s.pageUsers(dto).getTotal());
    }
}
