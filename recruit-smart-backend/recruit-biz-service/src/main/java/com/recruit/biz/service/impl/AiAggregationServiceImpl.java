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
import com.recruit.biz.enums.InterviewRound;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.support.InterviewScorecardCodec;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.AiAggregationService;
import com.recruit.biz.vo.AiMatchSummaryVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.feign.dto.request.FeedbackScoreItemRequest;
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
import com.recruit.biz.entity.EmployeeBehaviorRecord;
import com.recruit.biz.mapper.EmployeeBehaviorRecordMapper;
import com.recruit.feign.dto.request.EmployeeBehaviorRecordDTO;
import com.recruit.feign.dto.response.TurnoverRiskHistoryResponse;

import java.util.Collections;
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
    private final InterviewScorecardCodec scorecardCodec;
    private final ResumeMapper resumeMapper;
    private final InterviewMapper interviewMapper;
    private final InterviewFeedbackMapper interviewFeedbackMapper;
    private final EmployeeProfileMapper employeeProfileMapper;
    private final AiMatchResultMapper aiMatchResultMapper;
    private final ObjectMapper objectMapper;
    private final EmployeeBehaviorRecordMapper
            employeeBehaviorRecordMapper;

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
        request.setInterviewRound(context.interview().getRound());
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
        InterviewRound round = InterviewRound.fromCode(
                context.interview().getRound()
        );
        request.setInterviewRound(round == null
                ? context.interview().getRound()
                : round.getDescription());
        request.setFeedbackText(buildFeedbackText(feedback));
        request.setScore(feedback.getScore());

        request.setSuggestion(feedback.getSuggestion());
        request.setScorecard(toFeedbackScorecard(feedback));
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
    public TurnoverRiskResponse assessTurnoverRisk(
            Long employeeId
    ) {
        requireStaffRole();

        EmployeeProfile employee =
                employeeProfileMapper.selectById(employeeId);

        if (employee == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "员工档案不存在"
            );
        }

        List<EmployeeBehaviorRecord> records =
                employeeBehaviorRecordMapper.selectList(
                        new LambdaQueryWrapper
                                <EmployeeBehaviorRecord>()
                                .eq(
                                        EmployeeBehaviorRecord
                                                ::getEmployeeId,
                                        employeeId
                                )
                                .eq(
                                        EmployeeBehaviorRecord
                                                ::getRecordStatus,
                                        "CONFIRMED"
                                )
                                .orderByDesc(
                                        EmployeeBehaviorRecord
                                                ::getPeriodStart
                                )
                                .last("limit 6")
                );

        if (records.size() < 3) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "至少需要3期已确认的行为数据"
            );
        }

        /*
         * 查询结果是倒序，计算趋势前改成正序。
         */
        Collections.reverse(records);

        EmployeeBehaviorRecord first =
                records.getFirst();

        EmployeeBehaviorRecord latest =
                records.getLast();

        TurnoverRiskRequest request =
                new TurnoverRiskRequest();

        request.setEmployeeId(employee.getId());
        request.setEmployeeName(employee.getName());
        request.setDepartment(employee.getDepartment());
        request.setPosition(employee.getPosition());

        request.setPeriodStart(first.getPeriodStart());
        request.setPeriodEnd(latest.getPeriodEnd());

        request.setPerformanceTrend(
                buildTrend(
                        "绩效",
                        first.getPerformanceScore(),
                        latest.getPerformanceScore(),
                        records.size()
                )
        );

        request.setAttendanceTrend(
                buildTrend(
                        "考勤",
                        first.getAttendanceScore(),
                        latest.getAttendanceScore(),
                        records.size()
                )
        );

        request.setSatisfactionTrend(
                buildTrend(
                        "满意度",
                        first.getSatisfactionScore(),
                        latest.getSatisfactionScore(),
                        records.size()
                )
        );

        request.setLatestFeedback(
                latest.getFeedbackText()
        );

        request.setBehaviorRecords(
                records.stream()
                        .map(this::toBehaviorDTO)
                        .toList()
        );

        /*
         * 旧字段暂时继续赋值，
         * 防止当前旧Prompt或规则算法仍然读取这些字段。
         */
        request.setPerformanceScore(
                latest.getPerformanceScore()
        );
        request.setPerformanceSummary(
                latest.getPerformanceSummary()
        );
        request.setAttendanceScore(
                latest.getAttendanceScore()
        );
        request.setAttendanceSummary(
                latest.getAttendanceSummary()
        );
        request.setSatisfactionScore(
                latest.getSatisfactionScore()
        );
        request.setSatisfactionFeedback(
                latest.getFeedbackText()
        );

        TurnoverRiskResponse response =
                invokeAi(
                        "离职风险评估",
                        () -> aiServiceClient
                                .predictTurnoverRisk(request)
                );

        validateRiskResponse(response);

        LocalDateTime assessedAt =
                LocalDateTime.now();

        int updated = employeeProfileMapper.update(
                null,
                new LambdaUpdateWrapper<EmployeeProfile>()
                        .eq(
                                EmployeeProfile::getId,
                                employeeId
                        )
                        .set(
                                EmployeeProfile
                                        ::getTurnoverRiskLevel,
                                response.getRiskLevel()
                        )
                        .set(
                                EmployeeProfile
                                        ::getRiskAssessedAt,
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

    @Override
    public List<TurnoverRiskHistoryResponse>
    listTurnoverRiskHistory(Long employeeId) {
        requireStaffRole();

        EmployeeProfile employee =
                employeeProfileMapper.selectById(employeeId);

        if (employee == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "员工档案不存在"
            );
        }

        return invokeAi(
                "离职风险历史查询",
                () -> aiServiceClient
                        .listTurnoverRiskHistory(employeeId)
        );
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
        return hasText(feedback.getComment())
                ? feedback.getComment().trim()
                : "";
    }

    private List<FeedbackScoreItemRequest> toFeedbackScorecard(
            InterviewFeedback feedback
    ) {
        return scorecardCodec.read(feedback.getScorecardJson()).stream()
                .map(item -> {
                    FeedbackScoreItemRequest request =
                            new FeedbackScoreItemRequest();
                    request.setLabel(item.getLabel());
                    request.setDescription(item.getDescription());
                    request.setScore(item.getScore());
                    request.setEvidence(item.getEvidence());
                    return request;
                })
                .toList();
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

    private String buildTrend(
            String name,
            Integer first,
            Integer latest,
            int periods
    ) {
        if (first == null || latest == null) {
            return "近" + periods + "期"
                    + name + "数据不完整";
        }

        int change = latest - first;

        String direction;

        if (change > 0) {
            direction = "上升";
        } else if (change < 0) {
            direction = "下降";
        } else {
            direction = "持平";
        }

        return "近" + periods + "期"
                + name
                + "由"
                + first
                + "变为"
                + latest
                + "，"
                + direction
                + Math.abs(change)
                + "分";
    }

    private EmployeeBehaviorRecordDTO toBehaviorDTO(
            EmployeeBehaviorRecord record
    ) {
        EmployeeBehaviorRecordDTO dto =
                new EmployeeBehaviorRecordDTO();

        dto.setRecordId(record.getId());
        dto.setPeriodStart(record.getPeriodStart());
        dto.setPeriodEnd(record.getPeriodEnd());
        dto.setPerformanceScore(
                record.getPerformanceScore()
        );
        dto.setPerformanceSummary(
                record.getPerformanceSummary()
        );
        dto.setTaskCompletionRate(
                record.getTaskCompletionRate()
        );
        dto.setLateCount(record.getLateCount());
        dto.setAbsenceDays(record.getAbsenceDays());
        dto.setLeaveDays(record.getLeaveDays());
        dto.setOvertimeHours(record.getOvertimeHours());
        dto.setAttendanceScore(
                record.getAttendanceScore()
        );
        dto.setAttendanceSummary(
                record.getAttendanceSummary()
        );
        dto.setSatisfactionScore(
                record.getSatisfactionScore()
        );
        dto.setFeedbackText(
                record.getFeedbackText()
        );

        return dto;
    }

    private void validateRiskResponse(
            TurnoverRiskResponse response
    ) {
        if (response == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI未返回离职风险结果"
            );
        }

        if (!RISK_LEVELS.contains(
                response.getRiskLevel()
        )) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI返回的离职风险等级不正确"
            );
        }

        if (response.getRiskScore() == null
                || response.getRiskScore() < 0
                || response.getRiskScore() > 100) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI返回的离职风险分数不正确"
            );
        }

        if (response.getSentimentRiskScore() != null
                && (response.getSentimentRiskScore() < 0
                || response.getSentimentRiskScore() > 100)) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI返回的情感风险分数不正确"
            );
        }
    }
}
