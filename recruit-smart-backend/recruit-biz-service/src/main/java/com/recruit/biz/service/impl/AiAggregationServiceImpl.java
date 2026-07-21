package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
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
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.AiAggregationService;
import com.recruit.biz.vo.AiMatchSummaryVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.feign.client.AiServiceClient;
import com.recruit.feign.dto.request.FeedbackSummaryRequest;
import com.recruit.feign.dto.request.InterviewQuestionRequest;
import com.recruit.feign.dto.request.ResumeMatchRequest;
import com.recruit.feign.dto.request.TurnoverRiskRequest;
import com.recruit.feign.dto.response.FeedbackSummaryResponse;
import com.recruit.feign.dto.response.InterviewQuestionResponse;
import com.recruit.feign.dto.response.ResumeMatchResponse;
import com.recruit.feign.dto.response.TurnoverRiskResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAggregationServiceImpl implements AiAggregationService {

    private static final Set<String> RISK_LEVELS = Set.of(
            "LOW",
            "MEDIUM",
            "HIGH"
    );

    private final AiServiceClient aiServiceClient;
    private final JobApplicationMapper jobApplicationMapper;
    private final JobPositionMapper jobPositionMapper;
    private final CandidateMapper candidateMapper;
    private final ResumeMapper resumeMapper;
    private final InterviewMapper interviewMapper;
    private final InterviewFeedbackMapper interviewFeedbackMapper;
    private final EmployeeProfileMapper employeeProfileMapper;
    private final AiMatchResultMapper aiMatchResultMapper;
    private final ObjectMapper objectMapper;

    @Override
    public AiMatchSummaryVO generateResumeMatch(Long applicationId) {
        requireStaffRole();
        ApplicationContext context = loadApplicationContext(applicationId);
        requireResumeContent(context.resume());

        ResumeMatchRequest request = new ResumeMatchRequest();
        request.setApplicationId(context.application().getId());
        request.setJobId(context.job().getId());
        request.setCandidateId(context.candidate().getId());
        request.setResumeId(context.resume().getId());
        request.setJobTitle(context.job().getTitle());
        request.setResponsibilities(context.job().getResponsibilities());
        request.setRequirements(context.job().getRequirements());
        request.setResumeText(context.resume().getParsedContent());
        request.setSkills(context.resume().getSkills());
        request.setProjectExperience(
                context.resume().getProjectExperience()
        );
        request.setWorkExperience(context.resume().getWorkExperience());

        ResumeMatchResponse response = invokeAi(
                "简历匹配",
                () -> aiServiceClient.matchResume(request)
        );
        if (response.getScore() == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI简历匹配结果缺少匹配分数"
            );
        }

        AiMatchResult saved = aiMatchResultMapper.selectOne(
                new LambdaQueryWrapper<AiMatchResult>()
                        .eq(
                                AiMatchResult::getApplicationId,
                                applicationId
                        )
        );
        return saved == null
                ? toMatchVO(response)
                : toMatchVO(saved);
    }

    @Override
    public InterviewQuestionResponse generateInterviewQuestions(
            Long interviewId,
            InterviewAiQuestionDTO dto
    ) {
        InterviewContext context = loadInterviewContext(interviewId);
        requireInterviewAccess(context.interview());
        if ("CANCELED".equals(context.interview().getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "已取消的面试不能生成面试题"
            );
        }
        requireResumeContent(context.applicationContext().resume());

        InterviewQuestionRequest request = new InterviewQuestionRequest();
        request.setInterviewId(interviewId);
        request.setJobId(context.applicationContext().job().getId());
        request.setCandidateId(
                context.applicationContext().candidate().getId()
        );
        request.setResumeId(context.applicationContext().resume().getId());
        request.setJobTitle(context.applicationContext().job().getTitle());
        request.setResponsibilities(
                context.applicationContext().job().getResponsibilities()
        );
        request.setRequirements(appendFocus(
                context.applicationContext().job().getRequirements(),
                dto == null ? null : dto.getFocus()
        ));
        request.setResumeText(
                context.applicationContext().resume().getParsedContent()
        );
        request.setSkills(context.applicationContext().resume().getSkills());
        request.setProjectExperience(
                context.applicationContext().resume().getProjectExperience()
        );
        request.setWorkExperience(
                context.applicationContext().resume().getWorkExperience()
        );

        InterviewQuestionResponse response = invokeAi(
                "面试题生成",
                () -> aiServiceClient.generateInterviewQuestions(request)
        );
        if (response.getQuestions() == null
                || response.getQuestions().isEmpty()) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI未生成有效的面试题"
            );
        }
        return response;
    }

    @Override
    public FeedbackSummaryResponse generateFeedbackSummary(Long interviewId) {
        InterviewContext context = loadInterviewContext(interviewId);
        requireInterviewAccess(context.interview());
        InterviewFeedback feedback = interviewFeedbackMapper.selectOne(
                new LambdaQueryWrapper<InterviewFeedback>()
                        .eq(
                                InterviewFeedback::getInterviewId,
                                interviewId
                        )
        );
        if (feedback == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "请先填写面试反馈"
            );
        }
        if (!isSubmitted(feedback)
                && !"INTERVIEWER".equals(UserContext.getRoleCode())) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "反馈草稿仅本场面试官可以生成摘要"
            );
        }
        if (!hasText(feedback.getComment())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "请先填写面试评价"
            );
        }

        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setInterviewId(interviewId);
        request.setCandidateId(
                context.applicationContext().candidate().getId()
        );
        request.setJobId(context.applicationContext().job().getId());
        request.setJobTitle(context.applicationContext().job().getTitle());
        request.setCandidateName(
                context.applicationContext().candidate().getName()
        );
        request.setFeedbackText(buildFeedbackText(feedback));
        request.setScore(feedback.getScore());

        FeedbackSummaryResponse response = invokeAi(
                "面试反馈摘要",
                () -> aiServiceClient.generateFeedbackSummary(request)
        );
        if (!hasText(response.getSummary())) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI未生成有效的面试反馈摘要"
            );
        }

        int updated = interviewFeedbackMapper.update(
                null,
                new LambdaUpdateWrapper<InterviewFeedback>()
                        .eq(InterviewFeedback::getId, feedback.getId())
                        .set(
                                InterviewFeedback::getAiSummary,
                                response.getSummary()
                        )
        );
        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "保存AI面试反馈摘要失败"
            );
        }
        return response;
    }

    @Override
    public TurnoverRiskResponse assessTurnoverRisk(Long employeeId) {
        requireStaffRole();
        EmployeeProfile employee = employeeProfileMapper.selectById(
                employeeId
        );
        if (employee == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "员工档案不存在"
            );
        }

        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setEmployeeId(employee.getId());
        request.setEmployeeName(employee.getName());
        request.setDepartment(employee.getDepartment());
        request.setPosition(employee.getPosition());
        request.setPerformanceSummary(employee.getPerformanceSummary());
        request.setAttendanceSummary(employee.getAttendanceSummary());
        request.setSatisfactionFeedback(
                employee.getSatisfactionFeedback()
        );

        TurnoverRiskResponse response = invokeAi(
                "离职风险评估",
                () -> aiServiceClient.predictTurnoverRisk(request)
        );
        if (!RISK_LEVELS.contains(response.getRiskLevel())) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI返回的离职风险等级不正确"
            );
        }

        LocalDateTime assessedAt = LocalDateTime.now();
        int updated = employeeProfileMapper.update(
                null,
                new LambdaUpdateWrapper<EmployeeProfile>()
                        .eq(EmployeeProfile::getId, employeeId)
                        .set(
                                EmployeeProfile::getTurnoverRiskLevel,
                                response.getRiskLevel()
                        )
                        .set(
                                EmployeeProfile::getRiskAssessedAt,
                                assessedAt
                        )
        );
        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "保存员工离职风险评估结果失败"
            );
        }
        return response;
    }

    private ApplicationContext loadApplicationContext(Long applicationId) {
        JobApplication application = jobApplicationMapper.selectById(
                applicationId
        );
        if (application == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "投递记录不存在"
            );
        }
        JobPosition job = jobPositionMapper.selectById(application.getJobId());
        if (job == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "投递记录关联的职位不存在"
            );
        }
        Candidate candidate = candidateMapper.selectById(
                application.getCandidateId()
        );
        if (candidate == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "投递记录关联的候选人不存在"
            );
        }
        Resume resume = resumeMapper.selectById(application.getResumeId());
        if (resume == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "投递记录关联的简历不存在"
            );
        }
        if (!candidate.getId().equals(resume.getCandidateId())) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "投递记录关联的简历不属于该候选人"
            );
        }
        return new ApplicationContext(application, job, candidate, resume);
    }

    private InterviewContext loadInterviewContext(Long interviewId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "面试记录不存在"
            );
        }
        return new InterviewContext(
                interview,
                loadApplicationContext(interview.getApplicationId())
        );
    }

    private void requireResumeContent(Resume resume) {
        boolean empty = !hasText(resume.getParsedContent())
                && !hasText(resume.getSkills())
                && !hasText(resume.getProjectExperience())
                && !hasText(resume.getWorkExperience());
        if (empty) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "简历尚未解析，不能调用AI能力"
            );
        }
    }

    private void requireStaffRole() {
        String roleCode = UserContext.getRoleCode();
        if (!"ADMIN".equals(roleCode) && !"HR".equals(roleCode)) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "没有调用该AI能力的权限"
            );
        }
    }

    private void requireInterviewAccess(Interview interview) {
        String roleCode = UserContext.getRoleCode();
        if ("ADMIN".equals(roleCode) || "HR".equals(roleCode)) {
            return;
        }
        if ("INTERVIEWER".equals(roleCode)
                && UserContext.getUserId().equals(
                interview.getInterviewerId()
        )) {
            return;
        }
        throw new BusinessException(
                ErrorCode.FORBIDDEN,
                "无权调用该面试的AI能力"
        );
    }

    private String appendFocus(String requirements, String focus) {
        if (!hasText(focus)) {
            return requirements;
        }
        if (!hasText(requirements)) {
            return "补充追问主题：" + focus.trim();
        }
        return requirements.trim() + "\n补充追问主题：" + focus.trim();
    }

    private String buildFeedbackText(InterviewFeedback feedback) {
        StringBuilder builder = new StringBuilder();
        if (hasText(feedback.getComment())) {
            builder.append("面试评价：")
                    .append(feedback.getComment().trim());
        }
        if (hasText(feedback.getSuggestion())) {
            if (!builder.isEmpty()) {
                builder.append("\n");
            }
            builder.append("录用建议：")
                    .append(feedback.getSuggestion());
        }
        return builder.toString();
    }

    private boolean isSubmitted(InterviewFeedback feedback) {
        return feedback.getState() == null
                || "SUBMITTED".equals(feedback.getState());
    }

    private AiMatchSummaryVO toMatchVO(AiMatchResult result) {
        AiMatchSummaryVO vo = new AiMatchSummaryVO();
        vo.setMatchScore(result.getMatchScore());
        vo.setRecommendLevel(result.getRecommendLevel());
        vo.setRecommendReason(result.getRecommendReason());
        vo.setHighlightSummary(result.getHighlightSummary());
        vo.setRiskSummary(result.getRiskSummary());
        vo.setMatchedPoints(readList(result.getMatchedPoints()));
        vo.setRiskPoints(readList(result.getRiskPoints()));
        vo.setSuggestion(result.getSuggestion());
        vo.setSource(result.getSource());
        vo.setModelName(result.getModelName());
        vo.setGeneratedAt(result.getGeneratedAt());
        return vo;
    }

    private AiMatchSummaryVO toMatchVO(ResumeMatchResponse response) {
        AiMatchSummaryVO vo = new AiMatchSummaryVO();
        vo.setMatchScore(BigDecimal.valueOf(response.getScore()));
        vo.setRecommendLevel(response.getLevel());
        vo.setRecommendReason(response.getSummary());
        vo.setHighlightSummary(join(response.getMatchedPoints()));
        vo.setRiskSummary(join(response.getRiskPoints()));
        vo.setMatchedPoints(emptyIfNull(response.getMatchedPoints()));
        vo.setRiskPoints(emptyIfNull(response.getRiskPoints()));
        vo.setSuggestion(response.getSuggestion());
        vo.setModelName("recruit-ai-service");
        vo.setGeneratedAt(LocalDateTime.now());
        return vo;
    }

    private List<String> readList(String value) {
        if (!hasText(value)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    value,
                    new TypeReference<List<String>>() {
                    }
            );
        } catch (Exception e) {
            log.warn("解析AI结果列表失败", e);
            return List.of();
        }
    }

    private List<String> emptyIfNull(List<String> values) {
        return values == null ? List.of() : values;
    }

    private String join(List<String> values) {
        return values == null || values.isEmpty()
                ? null
                : String.join("；", values);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private <T> T invokeAi(String capability, Supplier<T> invocation) {
        try {
            T response = invocation.get();
            if (response == null) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_ERROR,
                        "AI" + capability + "返回空结果"
                );
            }
            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (FeignException e) {
            log.error("调用AI{}接口失败", capability, e);
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI" + capability + "服务暂时不可用"
            );
        } catch (RuntimeException e) {
            log.error("处理AI{}结果失败", capability, e);
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI" + capability + "处理失败"
            );
        }
    }

    private record ApplicationContext(
            JobApplication application,
            JobPosition job,
            Candidate candidate,
            Resume resume
    ) {
    }

    private record InterviewContext(
            Interview interview,
            ApplicationContext applicationContext
    ) {
    }
}
