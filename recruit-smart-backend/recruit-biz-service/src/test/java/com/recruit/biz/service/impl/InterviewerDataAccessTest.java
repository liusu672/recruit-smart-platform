package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.recruit.biz.controller.CandidateController;
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
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.security.UserContext;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewerDataAccessTest {

    @Mock
    private CandidateMapper candidateMapper;
    @Mock
    private JobPositionMapper jobPositionMapper;
    @Mock
    private ResumeMapper resumeMapper;
    @Mock
    private JobApplicationMapper jobApplicationMapper;
    @Mock
    private InterviewMapper interviewMapper;

    @InjectMocks
    private JobApplicationServiceImpl jobApplicationService;
    @InjectMocks
    private ResumeServiceImpl resumeService;

    @BeforeEach
    void setUpUserContext() {
        UserContext.set(new CurrentUser(3L, "interviewer01", "INTERVIEWER"));
    }

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void candidateDetailEndpointDoesNotAllowInterviewerRole()
            throws NoSuchMethodException {
        Method method = CandidateController.class.getMethod(
                "getCandidateDetail",
                Long.class
        );
        RequireRoles requireRoles = method.getAnnotation(RequireRoles.class);

        assertNotNull(requireRoles);
        assertFalse(Arrays.asList(requireRoles.value()).contains("INTERVIEWER"));
    }

    @Test
    void interviewerCannotAccessUnassignedApplication() {
        JobApplication application = application(10L, 20L);
        when(jobApplicationMapper.selectById(10L)).thenReturn(application);
        when(interviewMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        assertThrows(
                BusinessException.class,
                () -> jobApplicationService.getDetail(10L)
        );
    }

    @Test
    void interviewerCanAccessAssignedApplication() {
        JobApplication application = application(10L, 20L);
        when(jobApplicationMapper.selectById(10L)).thenReturn(application);
        when(interviewMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertNotNull(jobApplicationService.getDetail(10L));
    }

    @Test
    void interviewerJobApplicationListIsEmptyWithoutAssignments() {
        JobPosition job = new JobPosition();
        job.setId(30L);
        when(jobPositionMapper.selectById(30L)).thenReturn(job);
        when(interviewMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        var result = jobApplicationService.listJobApplications(30L, null);

        assertTrue(result.getRecords().isEmpty());
        assertTrue(result.getTotal() == 0L);
    }

    @Test
    void interviewerCannotAccessResumeFromUnassignedApplication() {
        Resume resume = new Resume();
        resume.setId(40L);
        resume.setCandidateId(20L);
        when(resumeMapper.selectById(40L)).thenReturn(resume);
        when(jobApplicationMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(application(10L, 20L)));
        when(interviewMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        assertThrows(
                BusinessException.class,
                () -> resumeService.getDetail(40L)
        );
    }

    @Test
    void interviewerCanAccessResumeFromAssignedApplication() {
        Resume resume = new Resume();
        resume.setId(40L);
        resume.setCandidateId(20L);
        when(resumeMapper.selectById(40L)).thenReturn(resume);
        when(jobApplicationMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(application(10L, 20L)));
        when(interviewMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertNotNull(resumeService.getDetail(40L));
    }

    private JobApplication application(Long id, Long candidateId) {
        JobApplication application = new JobApplication();
        application.setId(id);
        application.setCandidateId(candidateId);
        application.setStatus("INTERVIEWING");
        return application;
    }
}
