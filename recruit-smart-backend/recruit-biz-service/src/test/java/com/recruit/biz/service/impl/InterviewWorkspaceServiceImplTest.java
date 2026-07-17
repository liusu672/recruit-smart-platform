package com.recruit.biz.service.impl;

import com.recruit.biz.assembler.InterviewWorkspaceAssembler;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import com.recruit.biz.dto.InterviewTaskQueryDTO;
import com.recruit.biz.entity.AiMatchResult;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.AiMatchResultMapper;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.InterviewService;
import com.recruit.biz.support.InterviewScorecardCodec;
import com.recruit.biz.vo.InterviewTaskSummaryVO;
import com.recruit.biz.vo.InterviewDetailVO;
import com.recruit.biz.vo.InterviewWorkspaceVO;
import com.recruit.common.result.PageResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewWorkspaceServiceImplTest {

    @BeforeAll
    static void initializeTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(
                        new MybatisConfiguration(),
                        ""
                ),
                InterviewFeedback.class
        );
    }

    @Mock
    private InterviewService interviewService;
    @Mock
    private InterviewMapper interviewMapper;
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
    private AiMatchResultMapper aiMatchResultMapper;
    @Mock
    private InterviewFeedbackMapper interviewFeedbackMapper;
    @Spy
    private InterviewWorkspaceAssembler interviewWorkspaceAssembler =
            new InterviewWorkspaceAssembler(
                    new InterviewScorecardCodec(new ObjectMapper())
            );
    @InjectMocks
    private InterviewWorkspaceServiceImpl interviewWorkspaceService;

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    @SuppressWarnings("unchecked")
    void listTasksAggregatesPagedInterviewerTasks() {
        UserContext.set(new CurrentUser(6L, "interviewer", "INTERVIEWER"));
        Interview interview = new Interview();
        interview.setId(1L);
        interview.setApplicationId(2L);
        interview.setInterviewerId(6L);
        interview.setRound("FIRST");
        interview.setInterviewTime(LocalDateTime.of(2026, 7, 16, 14, 0));
        interview.setMethod("ONLINE");
        interview.setLocation("线上会议");
        interview.setStatus("COMPLETED");
        Page<Interview> page = new Page<>(1, 10);
        page.setTotal(1L);
        page.setRecords(List.of(interview));
        when(interviewMapper.selectPage(
                any(Page.class),
                any(LambdaQueryWrapper.class)
        )).thenReturn(page);

        JobApplication application = new JobApplication();
        application.setId(2L);
        application.setCandidateId(3L);
        application.setJobId(5L);
        when(jobApplicationMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(application));
        Candidate candidate = new Candidate();
        candidate.setId(3L);
        candidate.setName("测试候选人");
        when(candidateMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(candidate));
        JobPosition job = new JobPosition();
        job.setId(5L);
        job.setTitle("Java工程师");
        job.setDepartment("技术部");
        when(jobPositionMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(job));
        SysUser interviewer = new SysUser();
        interviewer.setId(6L);
        interviewer.setRealName("测试面试官");
        when(sysUserMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(interviewer));
        InterviewFeedback feedback = new InterviewFeedback();
        feedback.setInterviewId(1L);
        when(interviewFeedbackMapper.selectList(any()))
                .thenReturn(List.of(feedback));

        PageResult<InterviewTaskSummaryVO> result =
                interviewWorkspaceService.listTasks(
                        new InterviewTaskQueryDTO()
                );

        assertEquals(1L, result.getTotal());
        assertEquals("测试候选人", result.getRecords().get(0).getCandidateName());
        assertEquals("技术部", result.getRecords().get(0).getDepartment());
        assertEquals("SUBMITTED", result.getRecords().get(0).getFeedbackState());
    }

    @Test
    void listTasksReturnsEmptyPageWhenNoDraftFeedbackExists() {
        UserContext.set(new CurrentUser(2L, "hr", "HR"));
        InterviewTaskQueryDTO query = new InterviewTaskQueryDTO();
        query.setFeedbackState("DRAFT");

        PageResult<InterviewTaskSummaryVO> result =
                interviewWorkspaceService.listTasks(query);

        assertEquals(0L, result.getTotal());
        assertEquals(0, result.getRecords().size());
    }

    @Test
    void getWorkspaceAggregatesCandidateResumeAiAndFeedback() {
        InterviewDetailVO detail = detail();
        when(interviewService.getDetail(1L)).thenReturn(detail);

        Candidate candidate = new Candidate();
        candidate.setId(3L);
        candidate.setEducation("本科");
        candidate.setSchool("武汉理工大学");
        candidate.setYearsOfExperience(3);
        when(candidateMapper.selectById(3L)).thenReturn(candidate);

        Resume resume = new Resume();
        resume.setId(4L);
        resume.setResumeName("Java后端简历");
        resume.setSkills("Java, Spring Boot，MySQL");
        resume.setWorkExperience("3年后端开发经验");
        resume.setProjectExperience("参与招聘管理平台开发");
        when(resumeMapper.selectById(4L)).thenReturn(resume);

        AiMatchResult aiMatch = new AiMatchResult();
        aiMatch.setMatchScore(new BigDecimal("88.50"));
        aiMatch.setRecommendReason("岗位匹配度较高");
        aiMatch.setRiskSummary("复杂业务经验需要核实；稳定性需要确认");
        when(aiMatchResultMapper.selectOne(any())).thenReturn(aiMatch);

        InterviewFeedback feedback = new InterviewFeedback();
        feedback.setId(5L);
        feedback.setInterviewId(1L);
        feedback.setInterviewerId(6L);
        feedback.setScore(86);
        feedback.setState("SUBMITTED");
        feedback.setScorecardJson("""
                [{"key":"professional","label":"专业能力",\
                "description":"核心能力","score":4,\
                "evidence":"Java基础扎实"}]
                """);
        feedback.setComment("基础扎实");
        feedback.setSuggestion("PASS");
        feedback.setCreatedAt(LocalDateTime.of(2026, 7, 16, 15, 0));
        feedback.setSubmittedAt(LocalDateTime.of(2026, 7, 16, 15, 0));
        when(interviewFeedbackMapper.selectOne(any())).thenReturn(feedback);

        InterviewWorkspaceVO result =
                interviewWorkspaceService.getWorkspace(1L);

        assertEquals("SUBMITTED", result.getFeedbackState());
        assertEquals("Java工程师", result.getJobTitle());
        assertEquals(3, result.getCandidateBrief().getSkills().size());
        assertEquals(2, result.getCandidateBrief().getRiskPoints().size());
        assertEquals(new BigDecimal("88.50"),
                result.getCandidateBrief().getMatchScore());
        assertEquals(1, result.getScorecard().size());
        assertEquals(4, result.getScorecard().get(0).getScore());
        assertEquals(86, result.getFeedback().getScore());
        assertNotNull(result.getFeedback().getSubmittedAt());
    }

    @Test
    void getWorkspaceReturnsEmptyFeedbackAndQuestionsWhenNotGenerated() {
        when(interviewService.getDetail(1L)).thenReturn(detail());
        when(candidateMapper.selectById(3L)).thenReturn(new Candidate());
        when(resumeMapper.selectById(4L)).thenReturn(new Resume());
        when(aiMatchResultMapper.selectOne(any())).thenReturn(null);
        when(interviewFeedbackMapper.selectOne(any())).thenReturn(null);

        InterviewWorkspaceVO result =
                interviewWorkspaceService.getWorkspace(1L);

        assertEquals("EMPTY", result.getFeedbackState());
        assertEquals("", result.getFeedback().getComment());
        assertNull(result.getFeedback().getId());
        assertEquals(0, result.getQuestions().size());
        assertEquals(0, result.getCandidateBrief().getRiskPoints().size());
    }

    private InterviewDetailVO detail() {
        InterviewDetailVO detail = new InterviewDetailVO();
        detail.setId(1L);
        detail.setApplicationId(2L);
        detail.setCandidateId(3L);
        detail.setCandidateName("测试候选人");
        detail.setResumeId(4L);
        detail.setJobId(5L);
        detail.setJobTitle("Java工程师");
        detail.setDepartment("技术部");
        detail.setInterviewerId(6L);
        detail.setInterviewerName("测试面试官");
        detail.setRound("FIRST");
        detail.setRoundText("一面");
        detail.setMethod("ONLINE");
        detail.setMethodText("线上面试");
        detail.setStatus("COMPLETED");
        detail.setStatusText("已完成");
        detail.setInterviewTime(LocalDateTime.of(2026, 7, 16, 14, 0));
        return detail;
    }
}
