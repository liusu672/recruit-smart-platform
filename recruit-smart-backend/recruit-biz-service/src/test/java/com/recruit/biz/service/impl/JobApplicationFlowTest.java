package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.dto.JobApplicationCreateDTO;
import com.recruit.biz.dto.JobApplicationRejectDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.common.exception.BusinessException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationFlowTest {

    @BeforeAll
    static void init() {
        for (var c : new Class<?>[]{JobApplication.class, JobPosition.class, Candidate.class, Resume.class, Interview.class})
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), c);
    }

    @Mock private JobApplicationMapper jobApplicationMapper;
    @Mock private JobPositionMapper jobPositionMapper;
    @Mock private CandidateMapper candidateMapper;
    @Mock private ResumeMapper resumeMapper;
    @Mock private InterviewMapper interviewMapper;
    @Mock private ApplicationProcessEventService processEventService;

    @InjectMocks
    private JobApplicationServiceImpl jobApplicationService;

    @BeforeEach void setUp() { UserContext.set(new CurrentUser(1L, "candidate", "CANDIDATE")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test
    void createApplicationSuccess() {
        Candidate candidate = new Candidate(); candidate.setId(10L); candidate.setUserId(1L);
        when(candidateMapper.selectOne(any())).thenReturn(candidate);
        JobPosition job = new JobPosition(); job.setId(20L); job.setStatus("OPEN");
        when(jobPositionMapper.selectById(20L)).thenReturn(job);
        Resume resume = new Resume(); resume.setId(30L); resume.setCandidateId(10L);
        when(resumeMapper.selectById(30L)).thenReturn(resume);
        when(jobApplicationMapper.selectCount(any())).thenReturn(0L);
        doAnswer(inv -> { ((JobApplication)inv.getArgument(0)).setId(99L); return 1; })
                .when(jobApplicationMapper).insert(any(JobApplication.class));

        JobApplicationCreateDTO dto = new JobApplicationCreateDTO();
        dto.setJobId(20L); dto.setResumeId(30L);

        assertEquals(99L, jobApplicationService.createApplication(dto));
        verify(processEventService).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void createApplicationCandidateNotBoundThrows() {
        when(candidateMapper.selectOne(any())).thenReturn(null);
        assertThrows(BusinessException.class,
                () -> jobApplicationService.createApplication(new JobApplicationCreateDTO()));
    }

    @Test
    void createApplicationDuplicateThrows() {
        Candidate candidate = new Candidate(); candidate.setId(10L); candidate.setUserId(1L);
        when(candidateMapper.selectOne(any())).thenReturn(candidate);
        JobPosition job = new JobPosition(); job.setId(20L); job.setStatus("OPEN");
        when(jobPositionMapper.selectById(20L)).thenReturn(job);
        Resume resume = new Resume(); resume.setId(30L); resume.setCandidateId(10L);
        when(resumeMapper.selectById(30L)).thenReturn(resume);
        when(jobApplicationMapper.selectCount(any())).thenReturn(1L);

        var dto = new JobApplicationCreateDTO(); dto.setJobId(20L); dto.setResumeId(30L);
        assertThrows(BusinessException.class, () -> jobApplicationService.createApplication(dto));
    }

    @Test
    void getDetailAsAdminSuccess() {
        UserContext.set(new CurrentUser(2L, "admin", "ADMIN"));
        JobApplication app = new JobApplication(); app.setId(1L); app.setCandidateId(10L);
        app.setJobId(20L); app.setStatus("SUBMITTED");
        when(jobApplicationMapper.selectById(1L)).thenReturn(app);
        when(jobPositionMapper.selectById(20L)).thenReturn(new JobPosition());
        when(candidateMapper.selectById(10L)).thenReturn(new Candidate());

        var detail = jobApplicationService.getDetail(1L);
        assertNotNull(detail);
    }

    @Test
    void withdrawSuccess() {
        Candidate candidate = new Candidate(); candidate.setId(10L); candidate.setUserId(1L);
        when(candidateMapper.selectOne(any())).thenReturn(candidate);
        JobApplication app = new JobApplication(); app.setId(1L); app.setCandidateId(10L);
        app.setStatus("SUBMITTED");
        when(jobApplicationMapper.selectById(1L)).thenReturn(app);
        when(jobApplicationMapper.update(eq(null), any())).thenReturn(1);

        jobApplicationService.withdraw(1L);
        verify(jobApplicationMapper).update(eq(null), any());
    }

    @Test
    void withdrawInvalidStatusThrows() {
        Candidate candidate = new Candidate(); candidate.setId(10L); candidate.setUserId(1L);
        when(candidateMapper.selectOne(any())).thenReturn(candidate);
        JobApplication app = new JobApplication(); app.setId(1L); app.setCandidateId(10L);
        app.setStatus("REJECTED");
        when(jobApplicationMapper.selectById(1L)).thenReturn(app);
        assertThrows(BusinessException.class, () -> jobApplicationService.withdraw(1L));
    }

    @Test
    void rejectSubmittedToScreenRejectSuccess() {
        UserContext.set(new CurrentUser(2L, "hr", "HR"));
        JobApplication app = new JobApplication(); app.setId(1L); app.setCandidateId(10L);
        app.setStatus("SUBMITTED");
        when(jobApplicationMapper.selectById(1L)).thenReturn(app);
        when(jobApplicationMapper.update(eq(null), any())).thenReturn(1);

        JobApplicationRejectDTO dto = new JobApplicationRejectDTO();
        dto.setReasonCode("SKILL_MISMATCH"); dto.setReason("技能不匹配");
        jobApplicationService.reject(1L, dto);
        verify(jobApplicationMapper).update(eq(null), any());
    }
}
