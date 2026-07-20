package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.biz.assembler.InterviewWorkspaceAssembler;
import com.recruit.biz.entity.AiInterviewQuestion;
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
import com.recruit.biz.mapper.AiInterviewQuestionMapper;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.InterviewService;
import com.recruit.biz.service.InterviewWorkspaceService;
import com.recruit.biz.vo.InterviewDetailVO;
import com.recruit.biz.vo.InterviewTaskSummaryVO;
import com.recruit.biz.vo.InterviewQuestionVO;
import com.recruit.biz.vo.InterviewWorkspaceVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

@Service
public class InterviewWorkspaceServiceImpl
        implements InterviewWorkspaceService {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Resource
    private InterviewService interviewService;
    @Resource
    private InterviewMapper interviewMapper;
    @Resource
    private JobApplicationMapper jobApplicationMapper;
    @Resource
    private CandidateMapper candidateMapper;
    @Resource
    private JobPositionMapper jobPositionMapper;
    @Resource
    private ResumeMapper resumeMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private AiMatchResultMapper aiMatchResultMapper;
    @Resource
    private AiInterviewQuestionMapper aiInterviewQuestionMapper;
    @Resource
    private InterviewFeedbackMapper interviewFeedbackMapper;
    @Resource
    private InterviewWorkspaceAssembler interviewWorkspaceAssembler;

    @Override
    public PageResult<InterviewTaskSummaryVO> listTasks(
            InterviewTaskQueryDTO dto
    ) {
        InterviewTaskQueryDTO query = dto == null
                ? new InterviewTaskQueryDTO()
                : dto;
        LambdaQueryWrapper<Interview> wrapper = new LambdaQueryWrapper<>();
        applyRoleScope(wrapper);

        if (hasText(query.getStatus())) {
            wrapper.eq(Interview::getStatus, query.getStatus());
        }
        if (!applyFeedbackFilter(wrapper, query.getFeedbackState())) {
            return emptyPage();
        }
        if (hasText(query.getKeyword())
                && !applyKeywordFilter(wrapper, query.getKeyword().trim())) {
            return emptyPage();
        }

        wrapper.orderByAsc(Interview::getInterviewTime)
                .orderByAsc(Interview::getId);
        Page<Interview> result = interviewMapper.selectPage(
                new Page<>(
                        normalizePageNum(query.getPageNum()),
                        normalizePageSize(query.getPageSize())
                ),
                wrapper
        );
        return buildTaskPage(result);
    }

    @Override
    public InterviewWorkspaceVO getWorkspace(Long interviewId) {
        InterviewDetailVO detail = interviewService.getDetail(interviewId);
        if (detail.getApplicationId() == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "面试关联的投递记录不存在"
            );
        }

        Candidate candidate = detail.getCandidateId() == null
                ? null
                : candidateMapper.selectById(detail.getCandidateId());
        Resume resume = detail.getResumeId() == null
                ? null
                : resumeMapper.selectById(detail.getResumeId());
        AiMatchResult aiMatch = aiMatchResultMapper.selectOne(
                new LambdaQueryWrapper<AiMatchResult>()
                        .eq(
                                AiMatchResult::getApplicationId,
                                detail.getApplicationId()
                        )
        );
        InterviewFeedback feedback = interviewFeedbackMapper.selectOne(
                new LambdaQueryWrapper<InterviewFeedback>()
                        .eq(
                                InterviewFeedback::getInterviewId,
                                interviewId
                        )
        );
        feedback = hideDraftContentFromStaff(feedback);

        InterviewWorkspaceVO workspace = interviewWorkspaceAssembler.toWorkspace(
                detail,
                candidate,
                resume,
                aiMatch,
                feedback
        );
        workspace.setQuestions(loadAiQuestions(interviewId));
        return workspace;
    }

    private List<InterviewQuestionVO> loadAiQuestions(Long interviewId) {
        AiInterviewQuestion result = aiInterviewQuestionMapper.selectOne(
                new LambdaQueryWrapper<AiInterviewQuestion>()
                        .eq(
                                AiInterviewQuestion::getInterviewId,
                                interviewId
                        )
                        .orderByDesc(AiInterviewQuestion::getGeneratedAt)
                        .orderByDesc(AiInterviewQuestion::getId)
                        .last("LIMIT 1")
        );
        if (result == null
                || result.getQuestions() == null
                || result.getQuestions().isBlank()) {
            return List.of();
        }

        try {
            List<String> questions = JSON_MAPPER.readValue(
                    result.getQuestions(),
                    new TypeReference<List<String>>() {
                    }
            );
            return IntStream.range(0, questions.size())
                    .mapToObj(index -> toQuestionVO(
                            result,
                            questions.get(index),
                            index
                    ))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private InterviewQuestionVO toQuestionVO(
            AiInterviewQuestion result,
            String question,
            int index
    ) {
        InterviewQuestionVO vo = new InterviewQuestionVO();
        vo.setId(result.getId() + "-" + index);
        vo.setCategory(result.getCategory());
        vo.setQuestion(question);
        vo.setSource(result.getSource());
        return vo;
    }

    private InterviewFeedback hideDraftContentFromStaff(
            InterviewFeedback feedback
    ) {
        if (feedback == null
                || !"DRAFT".equals(feedback.getState())
                || "INTERVIEWER".equals(UserContext.getRoleCode())) {
            return feedback;
        }
        InterviewFeedback hidden = new InterviewFeedback();
        hidden.setInterviewId(feedback.getInterviewId());
        hidden.setInterviewerId(feedback.getInterviewerId());
        hidden.setState("DRAFT");
        return hidden;
    }

    private void applyRoleScope(LambdaQueryWrapper<Interview> wrapper) {
        String roleCode = UserContext.getRoleCode();
        if ("INTERVIEWER".equals(roleCode)) {
            wrapper.eq(
                    Interview::getInterviewerId,
                    UserContext.getUserId()
            );
            return;
        }
        if (!"HR".equals(roleCode) && !"ADMIN".equals(roleCode)) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "无权查看面试工作台任务"
            );
        }
    }

    private boolean applyFeedbackFilter(
            LambdaQueryWrapper<Interview> wrapper,
            String feedbackState
    ) {
        if (!hasText(feedbackState)) {
            return true;
        }

        if ("DRAFT".equals(feedbackState)
                || "SUBMITTED".equals(feedbackState)) {
            LambdaQueryWrapper<InterviewFeedback> feedbackWrapper =
                    new LambdaQueryWrapper<InterviewFeedback>()
                            .select(InterviewFeedback::getInterviewId);
            if ("DRAFT".equals(feedbackState)) {
                feedbackWrapper.eq(InterviewFeedback::getState, "DRAFT");
            } else {
                feedbackWrapper.and(condition -> condition
                        .eq(InterviewFeedback::getState, "SUBMITTED")
                        .or()
                        .isNull(InterviewFeedback::getState)
                );
            }
            Set<Long> matchingInterviewIds = interviewFeedbackMapper
                    .selectList(feedbackWrapper)
                    .stream()
                    .map(InterviewFeedback::getInterviewId)
                    .collect(Collectors.toSet());
            if (matchingInterviewIds.isEmpty()) {
                return false;
            }
            wrapper.in(Interview::getId, matchingInterviewIds);
            return true;
        }
        if ("EMPTY".equals(feedbackState)) {
            Set<Long> feedbackInterviewIds = interviewFeedbackMapper
                    .selectList(
                            new LambdaQueryWrapper<InterviewFeedback>()
                                    .select(
                                            InterviewFeedback::getInterviewId
                                    )
                    )
                    .stream()
                    .map(InterviewFeedback::getInterviewId)
                    .collect(Collectors.toSet());
            if (!feedbackInterviewIds.isEmpty()) {
                wrapper.notIn(Interview::getId, feedbackInterviewIds);
            }
            return true;
        }
        throw new BusinessException(
                ErrorCode.PARAM_ERROR,
                "反馈状态不正确"
        );
    }

    private boolean applyKeywordFilter(
            LambdaQueryWrapper<Interview> wrapper,
            String keyword
    ) {
        Set<Long> candidateIds = candidateMapper.selectList(
                        new LambdaQueryWrapper<Candidate>()
                                .like(Candidate::getName, keyword)
                )
                .stream()
                .map(Candidate::getId)
                .collect(Collectors.toSet());
        Set<Long> jobIds = jobPositionMapper.selectList(
                        new LambdaQueryWrapper<JobPosition>()
                                .and(condition -> condition
                                        .like(JobPosition::getTitle, keyword)
                                        .or()
                                        .like(JobPosition::getDepartment, keyword)
                                )
                )
                .stream()
                .map(JobPosition::getId)
                .collect(Collectors.toSet());
        Set<Long> applicationIds = findMatchingApplicationIds(
                candidateIds,
                jobIds
        );
        Set<Long> interviewerIds = sysUserMapper.selectList(
                        new LambdaQueryWrapper<SysUser>()
                                .and(condition -> condition
                                        .like(SysUser::getRealName, keyword)
                                        .or()
                                        .like(SysUser::getUsername, keyword)
                                )
                )
                .stream()
                .map(SysUser::getId)
                .collect(Collectors.toSet());

        if (applicationIds.isEmpty() && interviewerIds.isEmpty()) {
            return false;
        }
        wrapper.and(condition -> {
            if (!applicationIds.isEmpty() && !interviewerIds.isEmpty()) {
                condition.in(Interview::getApplicationId, applicationIds)
                        .or()
                        .in(Interview::getInterviewerId, interviewerIds);
            } else if (!applicationIds.isEmpty()) {
                condition.in(Interview::getApplicationId, applicationIds);
            } else {
                condition.in(Interview::getInterviewerId, interviewerIds);
            }
        });
        return true;
    }

    private Set<Long> findMatchingApplicationIds(
            Set<Long> candidateIds,
            Set<Long> jobIds
    ) {
        if (candidateIds.isEmpty() && jobIds.isEmpty()) {
            return Set.of();
        }
        LambdaQueryWrapper<JobApplication> wrapper =
                new LambdaQueryWrapper<>();
        wrapper.and(condition -> {
            if (!candidateIds.isEmpty() && !jobIds.isEmpty()) {
                condition.in(JobApplication::getCandidateId, candidateIds)
                        .or()
                        .in(JobApplication::getJobId, jobIds);
            } else if (!candidateIds.isEmpty()) {
                condition.in(JobApplication::getCandidateId, candidateIds);
            } else {
                condition.in(JobApplication::getJobId, jobIds);
            }
        });
        return jobApplicationMapper.selectList(wrapper)
                .stream()
                .map(JobApplication::getId)
                .collect(Collectors.toSet());
    }

    private PageResult<InterviewTaskSummaryVO> buildTaskPage(
            Page<Interview> result
    ) {
        List<Interview> interviews = result.getRecords();
        if (interviews.isEmpty()) {
            return new PageResult<>(result.getTotal(), List.of());
        }

        Set<Long> applicationIds = interviews.stream()
                .map(Interview::getApplicationId)
                .collect(Collectors.toSet());
        Set<Long> interviewerIds = interviews.stream()
                .map(Interview::getInterviewerId)
                .collect(Collectors.toSet());
        Set<Long> interviewIds = interviews.stream()
                .map(Interview::getId)
                .collect(Collectors.toSet());

        Map<Long, JobApplication> applicationMap = jobApplicationMapper
                .selectBatchIds(applicationIds)
                .stream()
                .collect(Collectors.toMap(
                        JobApplication::getId,
                        Function.identity()
                ));
        Set<Long> candidateIds = applicationMap.values().stream()
                .map(JobApplication::getCandidateId)
                .collect(Collectors.toSet());
        Set<Long> jobIds = applicationMap.values().stream()
                .map(JobApplication::getJobId)
                .collect(Collectors.toSet());

        Map<Long, Candidate> candidateMap = candidateIds.isEmpty()
                ? Map.of()
                : candidateMapper.selectBatchIds(candidateIds)
                .stream()
                .collect(Collectors.toMap(
                        Candidate::getId,
                        Function.identity()
                ));
        Map<Long, JobPosition> jobMap = jobIds.isEmpty()
                ? Map.of()
                : jobPositionMapper.selectBatchIds(jobIds)
                .stream()
                .collect(Collectors.toMap(
                        JobPosition::getId,
                        Function.identity()
                ));
        Map<Long, SysUser> interviewerMap = interviewerIds.isEmpty()
                ? Map.of()
                : sysUserMapper.selectBatchIds(interviewerIds)
                .stream()
                .collect(Collectors.toMap(
                        SysUser::getId,
                        Function.identity()
                ));
        Map<Long, InterviewFeedback> feedbackMap = interviewFeedbackMapper
                .selectList(
                        new LambdaQueryWrapper<InterviewFeedback>()
                                .in(
                                        InterviewFeedback::getInterviewId,
                                        interviewIds
                                )
                )
                .stream()
                .collect(Collectors.toMap(
                        InterviewFeedback::getInterviewId,
                        Function.identity()
                ));

        List<InterviewTaskSummaryVO> records = interviews.stream()
                .map(interview -> {
                    JobApplication application = applicationMap.get(
                            interview.getApplicationId()
                    );
                    Candidate candidate = application == null
                            ? null
                            : candidateMap.get(application.getCandidateId());
                    JobPosition job = application == null
                            ? null
                            : jobMap.get(application.getJobId());
                    return interviewWorkspaceAssembler.toTaskSummary(
                            interview,
                            application,
                            candidate,
                            job,
                            interviewerMap.get(interview.getInterviewerId()),
                            feedbackMap.get(interview.getId())
                    );
                })
                .toList();
        return new PageResult<>(result.getTotal(), records);
    }

    private PageResult<InterviewTaskSummaryVO> emptyPage() {
        return new PageResult<>(0L, List.of());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 || pageSize > 100
                ? 10
                : pageSize;
    }
}
