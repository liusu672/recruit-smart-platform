package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.AiMatchResultMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.vo.CandidateDetailVO;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateDetailTest {

    @Mock private CandidateMapper candidateMapper;
    @Mock private ResumeMapper resumeMapper;
    @Mock private JobApplicationMapper jobApplicationMapper;
    @Mock private JobPositionMapper jobPositionMapper;
    @Mock private AiMatchResultMapper aiMatchResultMapper;
    @InjectMocks private CandidateServiceImpl candidateService;

    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L, "hr", "HR")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test
    void getDetailWithApplications() {
        Candidate candidate = new Candidate(); candidate.setId(10L);
        candidate.setName("张三"); candidate.setPhone("13800138000");
        when(candidateMapper.selectById(10L)).thenReturn(candidate);

        Resume resume = new Resume(); resume.setId(1L); resume.setCandidateId(10L);
        when(resumeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(resume));

        when(jobApplicationMapper.selectList(any())).thenReturn(List.of());

        CandidateDetailVO vo = candidateService.getCandidateDetail(10L);
        assertNotNull(vo);
        assertEquals("张三", vo.getName());
        assertNotNull(vo.getResumes());
        assertEquals(1, vo.getResumes().size());
    }

    @Test
    void getDetailNotFound() {
        when(candidateMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> candidateService.getCandidateDetail(99L));
    }
}
