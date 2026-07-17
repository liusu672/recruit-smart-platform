package com.recruit.biz.service.impl;

import com.recruit.biz.entity.SysRole;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.SysRoleMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.vo.InterviewerOptionVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private CandidateMapper candidateMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void listsEnabledInterviewersAsOptions() {
        SysRole interviewerRole = new SysRole();
        interviewerRole.setId(3L);
        interviewerRole.setRoleCode("INTERVIEWER");

        SysUser first = user(3L, "interviewer01", "王面试官");
        SysUser second = user(4L, "interviewer02", "刘晓");
        when(sysRoleMapper.selectOne(any())).thenReturn(interviewerRole);
        when(sysUserMapper.selectList(any())).thenReturn(List.of(first, second));

        List<InterviewerOptionVO> result = userService.listInterviewers();

        assertEquals(2, result.size());
        assertEquals(3L, result.get(0).getId());
        assertEquals("interviewer01", result.get(0).getUsername());
        assertEquals("王面试官", result.get(0).getRealName());
        assertEquals(4L, result.get(1).getId());
        verify(sysRoleMapper).selectOne(any());
        verify(sysUserMapper).selectList(any());
    }

    @Test
    void returnsEmptyWhenInterviewerRoleIsNotConfigured() {
        when(sysRoleMapper.selectOne(any())).thenReturn(null);

        List<InterviewerOptionVO> result = userService.listInterviewers();

        assertTrue(result.isEmpty());
    }

    private SysUser user(Long id, String username, String realName) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setRealName(realName);
        user.setRoleId(3L);
        user.setStatus(1);
        return user;
    }
}
