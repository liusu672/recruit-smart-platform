package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.biz.dto.InterviewAiQuestionDTO;
import com.recruit.biz.entity.AiMatchResult;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.EmployeeProfile;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.mapper.AiMatchResultMapper;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.EmployeeProfileMapper;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.vo.AiMatchSummaryVO;
import com.recruit.common.exception.BusinessException;
import com.recruit.feign.client.AiServiceClient;
import com.recruit.feign.dto.request.InterviewQuestionRequest;
import com.recruit.feign.dto.request.ResumeMatchRequest;
import com.recruit.feign.dto.request.TurnoverRiskRequest;
import com.recruit.feign.dto.response.FeedbackSummaryResponse;
import com.recruit.feign.dto.response.InterviewQuestionResponse;
import com.recruit.feign.dto.response.InterviewQuestionItemResponse;
import com.recruit.feign.dto.response.ResumeMatchResponse;
import com.recruit.feign.dto.response.TurnoverRiskResponse;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAggregationServiceImplTest {

    @BeforeAll
    static void initializeTableInfo() {
        for (Class<?> entity : List.of(
                AiMatchResult.class,
                InterviewFeedback.class,
                EmployeeProfile.class
        )) {
            TableInfoHelper.initTableInfo(
                    new MapperBuilderAssistant(
                            new MybatisConfiguration(),
                            ""
                    ),
                    entity
            );
        }
    }

    @Mock
    private AiServiceClient aiServiceClient;
    @Mock
    private JobApplicationMapper jobApplicationMapper;
    @Mock
    private JobPositionMapper jobPositionMapper;
    @Mock
    private CandidateMapper candidateMapper;
    @Mock
    private ResumeMapper resumeMapper;
    @Mock
    private InterviewMapper interviewMapper;
    @Mock
    private InterviewFeedbackMapper interviewFeedbackMapper;
    @Mock
    private EmployeeProfileMapper employeeProfileMapper;
    @Mock
    private AiMatchResultMapper aiMatchResultMapper;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks
    private AiAggregationServiceImpl aiAggregationService;

    @BeforeEach
    void setUp() {
        UserContext.set(new CurrentUser(1L, "hr", "HR"));
    }

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void generateResumeMatchBuildsRequestAndReturnsSavedResult() {
        stubApplicationContext();
        ResumeMatchResponse response = new ResumeMatchResponse();
        response.setScore(88);
        response.setLevel("HIGH");
        response.setSummary("匹配度较高");
        when(aiServiceClient.matchResume(any())).thenReturn(response);

        AiMatchResult saved = new AiMatchResult();
        saved.setMatchScore(new BigDecimal("88.00"));
        saved.setRecommendLevel("HIGH");
        saved.setRecommendReason("匹配度较高");
        saved.setMatchedPoints("[\"Java\",\"Spring Boot\"]");
        saved.setRiskPoints("[\"大型系统经验待核实\"]");
        when(aiMatchResultMapper.selectOne(any())).thenReturn(saved);

        AiMatchSummaryVO result =
                aiAggregationService.generateResumeMatch(10L);

        assertEquals(new BigDecimal("88.00"), result.getMatchScore());
        assertEquals(2, result.getMatchedPoints().size());
        ArgumentCaptor<ResumeMatchRequest> captor =
                ArgumentCaptor.forClass(ResumeMatchRequest.class);
        verify(aiServiceClient).matchResume(captor.capture());
        assertEquals(10L, captor.getValue().getApplicationId());
        assertEquals("Java工程师", captor.getValue().getJobTitle());
    }

    @Test
    void generateQuestionsAllowsAssignedInterviewerAndAddsFocus() {
        UserContext.set(new CurrentUser(9L, "interviewer", "INTERVIEWER"));
        stubInterviewContext(9L);
        InterviewQuestionResponse response = new InterviewQuestionResponse();
        InterviewQuestionItemResponse question =
                new InterviewQuestionItemResponse();
        question.setTitle("并发控制");
        question.setContent("请介绍项目中的并发控制方案");
        question.setFocus(List.of("并发安全"));
        question.setDifficulty("MEDIUM");
        response.setQuestions(List.of(question));
        when(aiServiceClient.generateInterviewQuestions(any()))
                .thenReturn(response);

        InterviewAiQuestionDTO dto = new InterviewAiQuestionDTO();
        dto.setFocus("并发控制");
        InterviewQuestionResponse result =
                aiAggregationService.generateInterviewQuestions(20L, dto);

        assertEquals(1, result.getQuestions().size());
        ArgumentCaptor<InterviewQuestionRequest> captor =
                ArgumentCaptor.forClass(InterviewQuestionRequest.class);
        verify(aiServiceClient).generateInterviewQuestions(captor.capture());
        assertEquals(20L, captor.getValue().getInterviewId());
        assertTrue(captor.getValue().getRequirements().contains("并发控制"));
    }

    @Test
    void generateFeedbackSummaryUpdatesBusinessFeedback() {
        stubInterviewContext(9L);
        InterviewFeedback feedback = new InterviewFeedback();
        feedback.setId(30L);
        feedback.setInterviewId(20L);
        feedback.setState("SUBMITTED");
        feedback.setScore(86);
        feedback.setComment("技术基础扎实");
        feedback.setSuggestion("PASS");
        when(interviewFeedbackMapper.selectOne(any())).thenReturn(feedback);

        FeedbackSummaryResponse response = new FeedbackSummaryResponse();
        response.setSummary("候选人技术基础扎实，建议进入下一轮。");
        when(aiServiceClient.generateFeedbackSummary(any()))
                .thenReturn(response);
        when(interviewFeedbackMapper.update(
                eq(null),
                any(LambdaUpdateWrapper.class)
        )).thenReturn(1);

        FeedbackSummaryResponse result =
                aiAggregationService.generateFeedbackSummary(20L);

        assertEquals(response.getSummary(), result.getSummary());
        verify(interviewFeedbackMapper).update(
                eq(null),
                any(LambdaUpdateWrapper.class)
        );
    }

    @Test
    void assessTurnoverRiskUpdatesEmployeeProfile() {
        EmployeeProfile employee = new EmployeeProfile();
        employee.setId(40L);
        employee.setName("测试员工");
        employee.setDepartment("技术部");
        employee.setPosition("Java工程师");
        employee.setPerformanceSummary("绩效稳定");
        employee.setPerformanceScore(82);
        employee.setAttendanceScore(96);
        employee.setSatisfactionScore(78);
        when(employeeProfileMapper.selectById(40L)).thenReturn(employee);

        TurnoverRiskResponse response = new TurnoverRiskResponse();
        response.setRiskLevel("LOW");
        response.setRiskScore(20);
        when(aiServiceClient.predictTurnoverRisk(any())).thenReturn(response);
        when(employeeProfileMapper.update(
                eq(null),
                any(LambdaUpdateWrapper.class)
        )).thenReturn(1);

        TurnoverRiskResponse result =
                aiAggregationService.assessTurnoverRisk(40L);

        assertEquals("LOW", result.getRiskLevel());
        ArgumentCaptor<TurnoverRiskRequest> requestCaptor =
                ArgumentCaptor.forClass(TurnoverRiskRequest.class);
        verify(aiServiceClient).predictTurnoverRisk(
                requestCaptor.capture()
        );
        assertEquals(82, requestCaptor.getValue().getPerformanceScore());
        assertEquals(96, requestCaptor.getValue().getAttendanceScore());
        assertEquals(78, requestCaptor.getValue().getSatisfactionScore());
        verify(employeeProfileMapper).update(
                eq(null),
                any(LambdaUpdateWrapper.class)
        );
    }

    @Test
    void generateQuestionsRejectsUnassignedInterviewer() {
        UserContext.set(new CurrentUser(8L, "interviewer", "INTERVIEWER"));
        stubInterviewContext(9L);

        assertThrows(
                BusinessException.class,
                () -> aiAggregationService.generateInterviewQuestions(
                        20L,
                        null
                )
        );
        verifyNoInteractions(aiServiceClient);
    }

    @Test
    void generateResumeMatchRejectsUnparsedResume() {
        stubApplicationContext();
        Resume emptyResume = resume();
        emptyResume.setParsedContent(null);
        emptyResume.setSkills(null);
        emptyResume.setProjectExperience(null);
        emptyResume.setWorkExperience(null);
        when(resumeMapper.selectById(13L)).thenReturn(emptyResume);

        assertThrows(
                BusinessException.class,
                () -> aiAggregationService.generateResumeMatch(10L)
        );
        verifyNoInteractions(aiServiceClient);
    }

    private void stubInterviewContext(Long interviewerId) {
        Interview interview = new Interview();
        interview.setId(20L);
        interview.setApplicationId(10L);
        interview.setInterviewerId(interviewerId);
        interview.setStatus("SCHEDULED");
        when(interviewMapper.selectById(20L)).thenReturn(interview);
        stubApplicationContext();
    }

    private void stubApplicationContext() {
        JobApplication application = new JobApplication();
        application.setId(10L);
        application.setJobId(11L);
        application.setCandidateId(12L);
        application.setResumeId(13L);
        when(jobApplicationMapper.selectById(10L)).thenReturn(application);

        JobPosition job = new JobPosition();
        job.setId(11L);
        job.setTitle("Java工程师");
        job.setResponsibilities("负责微服务接口开发");
        job.setRequirements("熟悉Java和Spring Boot");
        when(jobPositionMapper.selectById(11L)).thenReturn(job);

        Candidate candidate = new Candidate();
        candidate.setId(12L);
        candidate.setName("测试候选人");
        when(candidateMapper.selectById(12L)).thenReturn(candidate);
        when(resumeMapper.selectById(13L)).thenReturn(resume());
    }

    private Resume resume() {
        Resume resume = new Resume();
        resume.setId(13L);
        resume.setCandidateId(12L);
        resume.setParsedContent("具有三年Java后端开发经验");
        resume.setSkills("Java,Spring Boot,MySQL");
        return resume;
    }
}
