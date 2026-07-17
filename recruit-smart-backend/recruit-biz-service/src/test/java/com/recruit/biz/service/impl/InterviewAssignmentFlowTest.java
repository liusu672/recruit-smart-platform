package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.dto.InterviewCreateDTO;
import com.recruit.biz.dto.InterviewScheduleDTO;
import com.recruit.biz.dto.InterviewUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.SysRole;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.enums.InterviewStatus;
import com.recruit.biz.enums.JobApplicationStatus;
import com.recruit.biz.enums.ProcessEventType;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.mapper.SysRoleMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.common.exception.BusinessException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewAssignmentFlowTest {

    @BeforeAll
    static void initializeTableInfo() {
        initializeTableInfo(Interview.class);
        initializeTableInfo(InterviewFeedback.class);
        initializeTableInfo(JobApplication.class);
        initializeTableInfo(Candidate.class);
    }

    private static void initializeTableInfo(Class<?> entityType) {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                entityType
        );
    }

    @Mock
    private InterviewMapper interviewMapper;
    @Mock
    private InterviewFeedbackMapper interviewFeedbackMapper;
    @Mock
    private JobApplicationMapper jobApplicationMapper;
    @Mock
    private CandidateMapper candidateMapper;
    @Mock
    private JobPositionMapper jobPositionMapper;
    @Mock
    private ResumeMapper resumeMapper;
    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private SysRoleMapper sysRoleMapper;
    @Mock
    private ApplicationProcessEventService processEventService;
    @InjectMocks
    private InterviewServiceImpl interviewService;

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void hrAssignmentCreatesUnscheduledInterview() {
        UserContext.set(new CurrentUser(2L, "hr", "HR"));
        JobApplication application = application(
                JobApplicationStatus.SCREEN_PASSED.name()
        );
        when(jobApplicationMapper.selectById(10L)).thenReturn(application);
        when(jobPositionMapper.selectById(40L)).thenReturn(job(1));
        when(sysUserMapper.selectById(6L)).thenReturn(interviewer(6L));
        when(sysRoleMapper.selectById(3L)).thenReturn(interviewerRole());
        when(interviewMapper.selectCount(any())).thenReturn(0L);
        when(jobApplicationMapper.update(isNull(), any())).thenReturn(1);
        doAnswer(invocation -> {
            Interview interview = invocation.getArgument(0);
            interview.setId(30L);
            return 1;
        }).when(interviewMapper).insert(any(Interview.class));

        Long id = interviewService.createInterview(createDTO());

        ArgumentCaptor<Interview> captor =
                ArgumentCaptor.forClass(Interview.class);
        verify(interviewMapper).insert(captor.capture());
        Interview created = captor.getValue();
        assertEquals(30L, id);
        assertEquals(InterviewStatus.ASSIGNED.name(), created.getStatus());
        assertEquals(6L, created.getInterviewerId());
        assertNotNull(created.getAssignedAt());
        assertNull(created.getInterviewTime());
        assertNull(created.getMethod());
        assertNull(created.getScheduledAt());
        verify(processEventService).record(
                10L,
                ProcessEventType.INTERVIEW_ASSIGNED,
                null,
                InterviewStatus.ASSIGNED.name(),
                "指派测试面试官负责FIRST轮面试",
                com.recruit.biz.enums.ProcessRelatedType.INTERVIEW,
                30L
        );
    }

    @Test
    void cannotCreateSecondRoundWhenJobRequiresOneRound() {
        UserContext.set(new CurrentUser(2L, "hr", "HR"));
        JobApplication application = application(
                JobApplicationStatus.INTERVIEWING.name()
        );
        when(jobApplicationMapper.selectById(10L)).thenReturn(application);
        when(jobPositionMapper.selectById(40L)).thenReturn(job(1));

        assertThrows(
                BusinessException.class,
                () -> interviewService.createInterview(createSecondRoundDTO())
        );
        verify(interviewMapper, never()).insert(any(Interview.class));
    }

    @Test
    void cannotCreateSecondRoundBeforeFirstRoundIsCompleted() {
        UserContext.set(new CurrentUser(2L, "hr", "HR"));
        JobApplication application = application(
                JobApplicationStatus.INTERVIEWING.name()
        );
        when(jobApplicationMapper.selectById(10L)).thenReturn(application);
        when(jobPositionMapper.selectById(40L)).thenReturn(job(2));
        when(interviewMapper.selectCount(any())).thenReturn(0L);
        when(interviewMapper.selectList(any())).thenReturn(java.util.List.of());

        assertThrows(
                BusinessException.class,
                () -> interviewService.createInterview(createSecondRoundDTO())
        );
        verify(interviewMapper, never()).insert(any(Interview.class));
    }

    @Test
    void canCreateSecondRoundAfterFirstRoundAndFeedbackAreCompleted() {
        UserContext.set(new CurrentUser(2L, "hr", "HR"));
        JobApplication application = application(
                JobApplicationStatus.INTERVIEWING.name()
        );
        when(jobApplicationMapper.selectById(10L)).thenReturn(application);
        when(jobPositionMapper.selectById(40L)).thenReturn(job(2));
        when(interviewMapper.selectCount(any())).thenReturn(0L);
        when(interviewMapper.selectList(any()))
                .thenReturn(java.util.List.of(completedFirstInterview()));
        when(interviewFeedbackMapper.selectCount(any())).thenReturn(1L);
        when(sysUserMapper.selectById(6L)).thenReturn(interviewer(6L));
        when(sysRoleMapper.selectById(3L)).thenReturn(interviewerRole());
        doAnswer(invocation -> {
            Interview interview = invocation.getArgument(0);
            interview.setId(31L);
            return 1;
        }).when(interviewMapper).insert(any(Interview.class));

        Long id = interviewService.createInterview(createSecondRoundDTO());

        assertEquals(31L, id);
        ArgumentCaptor<Interview> captor =
                ArgumentCaptor.forClass(Interview.class);
        verify(interviewMapper).insert(captor.capture());
        assertEquals("SECOND", captor.getValue().getRound());
        assertEquals(
                InterviewStatus.ASSIGNED.name(),
                captor.getValue().getStatus()
        );
    }

    @Test
    void onlyAssignedInterviewerCanSchedule() {
        UserContext.set(new CurrentUser(7L, "other", "INTERVIEWER"));
        when(interviewMapper.selectById(30L))
                .thenReturn(assignedInterview());

        assertThrows(
                BusinessException.class,
                () -> interviewService.scheduleInterview(30L, scheduleDTO())
        );
        verify(interviewMapper, never()).update(
                isNull(),
                any(LambdaUpdateWrapper.class)
        );
    }

    @Test
    void assignedInterviewerCanConfirmSchedule() {
        UserContext.set(new CurrentUser(6L, "interviewer", "INTERVIEWER"));
        when(interviewMapper.selectById(30L))
                .thenReturn(assignedInterview());
        when(interviewMapper.selectCount(any())).thenReturn(0L);
        when(interviewMapper.update(isNull(), any())).thenReturn(1);

        interviewService.scheduleInterview(30L, scheduleDTO());

        verify(interviewMapper).update(
                isNull(),
                any(LambdaUpdateWrapper.class)
        );
        verify(processEventService).record(
                10L,
                ProcessEventType.INTERVIEW_SCHEDULED,
                InterviewStatus.ASSIGNED.name(),
                InterviewStatus.SCHEDULED.name(),
                "确认面试预约：2026-07-20T14:00，方式：ONLINE",
                com.recruit.biz.enums.ProcessRelatedType.INTERVIEW,
                30L
        );
    }

    @Test
    void candidateCannotViewAssignmentBeforeScheduling() {
        UserContext.set(new CurrentUser(4L, "candidate", "CANDIDATE"));
        when(interviewMapper.selectById(30L))
                .thenReturn(assignedInterview());

        assertThrows(
                BusinessException.class,
                () -> interviewService.getDetail(30L)
        );
        verify(jobApplicationMapper, never()).selectById(any());
    }

    @Test
    void candidateCanViewInterviewAfterScheduling() {
        UserContext.set(new CurrentUser(4L, "candidate", "CANDIDATE"));
        Interview interview = assignedInterview();
        interview.setStatus(InterviewStatus.SCHEDULED.name());
        interview.setInterviewTime(LocalDateTime.of(2026, 7, 20, 14, 0));
        interview.setScheduledAt(LocalDateTime.of(2026, 7, 18, 9, 0));
        when(interviewMapper.selectById(30L)).thenReturn(interview);
        when(jobApplicationMapper.selectById(10L))
                .thenReturn(application(JobApplicationStatus.INTERVIEWING.name()));
        Candidate candidate = new Candidate();
        candidate.setId(20L);
        candidate.setUserId(4L);
        candidate.setName("候选人");
        when(candidateMapper.selectOne(any())).thenReturn(candidate);
        when(candidateMapper.selectById(20L)).thenReturn(candidate);
        when(sysUserMapper.selectById(6L)).thenReturn(interviewer(6L));

        var detail = interviewService.getDetail(30L);

        assertEquals(InterviewStatus.SCHEDULED.name(), detail.getStatus());
        assertEquals("候选人", detail.getCandidateName());
        assertNotNull(detail.getScheduledAt());
    }

    @Test
    void hrCannotReassignScheduledInterview() {
        UserContext.set(new CurrentUser(2L, "hr", "HR"));
        Interview interview = assignedInterview();
        interview.setStatus(InterviewStatus.SCHEDULED.name());
        when(interviewMapper.selectById(30L)).thenReturn(interview);
        InterviewUpdateDTO dto = new InterviewUpdateDTO();
        dto.setInterviewerId(7L);

        assertThrows(
                BusinessException.class,
                () -> interviewService.updateInterview(30L, dto)
        );
        verify(sysUserMapper, never()).selectById(7L);
    }

    private InterviewCreateDTO createDTO() {
        InterviewCreateDTO dto = new InterviewCreateDTO();
        dto.setApplicationId(10L);
        dto.setInterviewerId(6L);
        dto.setRound("FIRST");
        return dto;
    }

    private InterviewScheduleDTO scheduleDTO() {
        InterviewScheduleDTO dto = new InterviewScheduleDTO();
        dto.setInterviewTime(LocalDateTime.of(2026, 7, 20, 14, 0));
        dto.setMethod("ONLINE");
        dto.setLocation("腾讯会议 123456");
        return dto;
    }

    private InterviewCreateDTO createSecondRoundDTO() {
        InterviewCreateDTO dto = createDTO();
        dto.setRound("SECOND");
        return dto;
    }

    private JobApplication application(String status) {
        JobApplication application = new JobApplication();
        application.setId(10L);
        application.setCandidateId(20L);
        application.setJobId(40L);
        application.setResumeId(50L);
        application.setStatus(status);
        return application;
    }

    private Interview assignedInterview() {
        Interview interview = new Interview();
        interview.setId(30L);
        interview.setApplicationId(10L);
        interview.setInterviewerId(6L);
        interview.setRound("FIRST");
        interview.setStatus(InterviewStatus.ASSIGNED.name());
        interview.setAssignedAt(LocalDateTime.of(2026, 7, 17, 9, 0));
        return interview;
    }

    private Interview completedFirstInterview() {
        Interview interview = assignedInterview();
        interview.setStatus(InterviewStatus.COMPLETED.name());
        return interview;
    }

    private JobPosition job(int requiredRounds) {
        JobPosition job = new JobPosition();
        job.setId(40L);
        job.setRequiredInterviewRounds(requiredRounds);
        return job;
    }

    private SysUser interviewer(Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setRoleId(3L);
        user.setStatus(1);
        user.setRealName("测试面试官");
        return user;
    }

    private SysRole interviewerRole() {
        SysRole role = new SysRole();
        role.setId(3L);
        role.setRoleCode("INTERVIEWER");
        return role;
    }
}
