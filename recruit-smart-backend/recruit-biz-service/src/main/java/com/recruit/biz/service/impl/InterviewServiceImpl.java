package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.InterviewCreateDTO;
import com.recruit.biz.dto.InterviewQueryDTO;
import com.recruit.biz.dto.InterviewScheduleDTO;
import com.recruit.biz.dto.InterviewUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.entity.SysRole;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.enums.InterviewMethod;
import com.recruit.biz.enums.InterviewRound;
import com.recruit.biz.enums.InterviewStatus;
import com.recruit.biz.enums.JobApplicationStatus;
import com.recruit.biz.enums.ProcessEventType;
import com.recruit.biz.enums.ProcessRelatedType;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.mapper.SysRoleMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.InterviewService;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.biz.vo.InterviewSummaryVO;
import com.recruit.biz.vo.InterviewDetailVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InterviewServiceImpl implements InterviewService {

    @Resource
    private InterviewMapper interviewMapper;

    @Resource
    private InterviewFeedbackMapper interviewFeedbackMapper;

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
    private SysRoleMapper sysRoleMapper;

    @Resource
    private ApplicationProcessEventService processEventService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInterview(InterviewCreateDTO dto) {
        JobApplication application = jobApplicationMapper.selectById(
                dto.getApplicationId()
        );
        if (application == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "投递记录不存在"
            );
        }

        boolean canSchedule =
                JobApplicationStatus.SCREEN_PASSED.name()
                        .equals(application.getStatus())
                        || JobApplicationStatus.INTERVIEWING.name()
                        .equals(application.getStatus());
        if (!canSchedule) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "当前投递状态不能安排面试"
            );
        }

        validateInterviewRound(application, dto.getRound());

        SysUser interviewer = sysUserMapper.selectById(
                dto.getInterviewerId()
        );
        if (interviewer == null
                || !Integer.valueOf(1).equals(interviewer.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "面试官不存在或已被禁用"
            );
        }

        SysRole role = sysRoleMapper.selectById(interviewer.getRoleId());
        if (role == null
                || !"INTERVIEWER".equals(role.getRoleCode())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "指定用户不是面试官角色"
            );
        }

        Interview interview = new Interview();
        interview.setApplicationId(application.getId());
        interview.setInterviewerId(dto.getInterviewerId());
        interview.setRound(dto.getRound());
        interview.setStatus(InterviewStatus.ASSIGNED.name());
        interview.setCreatedBy(UserContext.getUserId());
        interview.setAssignedAt(LocalDateTime.now());
        interviewMapper.insert(interview);

        if (JobApplicationStatus.SCREEN_PASSED.name()
                .equals(application.getStatus())) {
            int updated = jobApplicationMapper.update(
                    null,
                    new LambdaUpdateWrapper<JobApplication>()
                            .eq(JobApplication::getId, application.getId())
                            .eq(
                                    JobApplication::getStatus,
                                    JobApplicationStatus.SCREEN_PASSED.name()
                            )
                            .set(
                                    JobApplication::getStatus,
                                    JobApplicationStatus.INTERVIEWING.name()
                            )
            );
            if (updated != 1) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_ERROR,
                        "更新投递状态失败"
                );
            }
        }

        processEventService.record(
                application.getId(),
                ProcessEventType.INTERVIEW_ASSIGNED,
                null,
                InterviewStatus.ASSIGNED.name(),
                "指派" + interviewer.getRealName()
                        + "负责" + dto.getRound() + "轮面试",
                ProcessRelatedType.INTERVIEW,
                interview.getId()
        );

        return interview.getId();
    }

    private void validateInterviewRound(
            JobApplication application,
            String roundCode
    ) {
        InterviewRound round = InterviewRound.fromCode(roundCode);
        if (round == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "面试轮次不正确"
            );
        }

        JobPosition job = jobPositionMapper.selectById(application.getJobId());
        if (job == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "投递记录关联的职位不存在"
            );
        }

        int requiredRounds = job.getRequiredInterviewRounds() == null
                ? 1
                : job.getRequiredInterviewRounds();
        if (round.getOrder() > requiredRounds) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "该职位只要求完成" + requiredRounds + "轮面试"
            );
        }

        Long existingRoundCount = interviewMapper.selectCount(
                new LambdaQueryWrapper<Interview>()
                        .eq(Interview::getApplicationId, application.getId())
                        .eq(Interview::getRound, round.name())
                        .ne(Interview::getStatus, InterviewStatus.CANCELED.name())
        );
        if (existingRoundCount > 0) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    round.getDescription() + "已经存在，不能重复创建"
            );
        }

        if (round.getOrder() == 1) {
            return;
        }

        InterviewRound previousRound = InterviewRound.fromOrder(
                round.getOrder() - 1
        );
        List<Interview> completedPreviousInterviews =
                interviewMapper.selectList(
                        new LambdaQueryWrapper<Interview>()
                                .eq(
                                        Interview::getApplicationId,
                                        application.getId()
                                )
                                .eq(
                                        Interview::getRound,
                                        previousRound.name()
                                )
                                .eq(
                                        Interview::getStatus,
                                        InterviewStatus.COMPLETED.name()
                                )
                );
        if (completedPreviousInterviews.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "请先完成" + previousRound.getDescription()
            );
        }

        Set<Long> completedInterviewIds = completedPreviousInterviews.stream()
                .map(Interview::getId)
                .collect(Collectors.toSet());
        Long submittedFeedbackCount = interviewFeedbackMapper.selectCount(
                new LambdaQueryWrapper<InterviewFeedback>()
                        .in(
                                InterviewFeedback::getInterviewId,
                                completedInterviewIds
                        )
                        .eq(InterviewFeedback::getState, "SUBMITTED")
        );
        if (submittedFeedbackCount == 0) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "请先提交" + previousRound.getDescription() + "反馈"
            );
        }
    }

    @Override
    public InterviewDetailVO getDetail(Long id) {
        Interview interview = requireInterview(id);
        checkInterviewAccess(interview);

        JobApplication application = jobApplicationMapper.selectById(
                interview.getApplicationId()
        );
        JobPosition job = application == null
                ? null
                : jobPositionMapper.selectById(application.getJobId());
        Candidate candidate = application == null
                ? null
                : candidateMapper.selectById(application.getCandidateId());
        Resume resume = application == null
                ? null
                : resumeMapper.selectById(application.getResumeId());
        SysUser interviewer = sysUserMapper.selectById(
                interview.getInterviewerId()
        );

        InterviewDetailVO vo = new InterviewDetailVO();
        vo.setId(interview.getId());
        vo.setApplicationId(interview.getApplicationId());
        if (application != null) {
            vo.setApplicationStatus(application.getStatus());
            vo.setJobId(application.getJobId());
            vo.setCandidateId(application.getCandidateId());
            vo.setResumeId(application.getResumeId());
        }
        if (job != null) {
            vo.setJobTitle(job.getTitle());
            vo.setDepartment(job.getDepartment());
        }
        vo.setCandidateName(candidate == null ? null : candidate.getName());
        vo.setResumeName(resume == null ? null : resume.getResumeName());
        vo.setInterviewerId(interview.getInterviewerId());
        vo.setInterviewerName(
                interviewer == null ? null : interviewer.getRealName()
        );
        vo.setRound(interview.getRound());
        vo.setInterviewTime(interview.getInterviewTime());
        vo.setMethod(interview.getMethod());
        vo.setLocation(interview.getLocation());
        vo.setStatus(interview.getStatus());
        vo.setAssignedAt(interview.getAssignedAt());
        vo.setScheduledAt(interview.getScheduledAt());
        vo.setCreatedAt(interview.getCreatedAt());
        vo.setUpdatedAt(interview.getUpdatedAt());

        InterviewRound round = InterviewRound.fromCode(interview.getRound());
        vo.setRoundText(
                round == null ? "未知轮次" : round.getDescription()
        );
        InterviewMethod method = InterviewMethod.fromCode(
                interview.getMethod()
        );
        vo.setMethodText(
                method == null ? "待确认" : method.getDescription()
        );
        InterviewStatus status = InterviewStatus.fromCode(
                interview.getStatus()
        );
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInterview(Long id, InterviewUpdateDTO dto) {
        Interview interview = requireInterview(id);
        if (!InterviewStatus.ASSIGNED.name()
                .equals(interview.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "只有待预约状态可以重新指派面试官"
            );
        }

        SysUser interviewer = requireInterviewer(dto.getInterviewerId());
        if (dto.getInterviewerId().equals(interview.getInterviewerId())) {
            return;
        }

        int updated = interviewMapper.update(
                null,
                new LambdaUpdateWrapper<Interview>()
                        .eq(Interview::getId, id)
                        .eq(
                                Interview::getStatus,
                                InterviewStatus.ASSIGNED.name()
                        )
                        .set(
                                Interview::getInterviewerId,
                                dto.getInterviewerId()
                        )
                        .set(Interview::getAssignedAt, LocalDateTime.now())
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "修改面试安排失败，记录可能已被其他人处理"
            );
        }

        processEventService.record(
                interview.getApplicationId(),
                ProcessEventType.INTERVIEW_UPDATED,
                InterviewStatus.ASSIGNED.name(),
                InterviewStatus.ASSIGNED.name(),
                "重新指派" + interviewer.getRealName()
                        + "负责" + interview.getRound() + "轮面试",
                ProcessRelatedType.INTERVIEW,
                interview.getId()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scheduleInterview(Long id, InterviewScheduleDTO dto) {
        Interview interview = requireInterview(id);
        if (!UserContext.getUserId().equals(interview.getInterviewerId())) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "只有被指派的面试官可以预约面试"
            );
        }

        boolean firstSchedule = InterviewStatus.ASSIGNED.name()
                .equals(interview.getStatus());
        boolean reschedule = InterviewStatus.SCHEDULED.name()
                .equals(interview.getStatus());
        if (!firstSchedule && !reschedule) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "当前面试状态不允许预约或调整时间"
            );
        }

        Long timeConflictCount = interviewMapper.selectCount(
                new LambdaQueryWrapper<Interview>()
                        .eq(
                                Interview::getInterviewerId,
                                interview.getInterviewerId()
                        )
                        .eq(
                                Interview::getInterviewTime,
                                dto.getInterviewTime()
                        )
                        .eq(
                                Interview::getStatus,
                                InterviewStatus.SCHEDULED.name()
                        )
                        .ne(Interview::getId, id)
        );
        if (timeConflictCount > 0) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "面试官该时间已有其他面试安排"
            );
        }

        LocalDateTime scheduledAt = LocalDateTime.now();
        int updated = interviewMapper.update(
                null,
                new LambdaUpdateWrapper<Interview>()
                        .eq(Interview::getId, id)
                        .eq(Interview::getStatus, interview.getStatus())
                        .eq(
                                Interview::getInterviewerId,
                                UserContext.getUserId()
                        )
                        .set(
                                Interview::getInterviewTime,
                                dto.getInterviewTime()
                        )
                        .set(Interview::getMethod, dto.getMethod())
                        .set(
                                Interview::getLocation,
                                dto.getLocation().trim()
                        )
                        .set(
                                Interview::getStatus,
                                InterviewStatus.SCHEDULED.name()
                        )
                        .set(Interview::getScheduledAt, scheduledAt)
        );
        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "确认面试预约失败，记录可能已被其他人处理"
            );
        }

        processEventService.record(
                interview.getApplicationId(),
                firstSchedule
                        ? ProcessEventType.INTERVIEW_SCHEDULED
                        : ProcessEventType.INTERVIEW_UPDATED,
                interview.getStatus(),
                InterviewStatus.SCHEDULED.name(),
                (firstSchedule ? "确认面试预约：" : "调整面试预约：")
                        + dto.getInterviewTime() + "，方式："
                        + dto.getMethod(),
                ProcessRelatedType.INTERVIEW,
                interview.getId()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelInterview(Long id) {
        Interview interview = requireInterview(id);
        String currentStatus = interview.getStatus();
        boolean canCancel = InterviewStatus.ASSIGNED.name()
                .equals(currentStatus)
                || InterviewStatus.SCHEDULED.name().equals(currentStatus);
        if (!canCancel) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "只有待预约或待面试状态可以取消"
            );
        }

        int updated = interviewMapper.update(
                null,
                new LambdaUpdateWrapper<Interview>()
                        .eq(Interview::getId, id)
                        .eq(
                                Interview::getStatus,
                                currentStatus
                        )
                        .set(
                                Interview::getStatus,
                                InterviewStatus.CANCELED.name()
                        )
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "取消面试失败，记录可能已被其他人处理"
            );
        }

        processEventService.record(
                interview.getApplicationId(),
                ProcessEventType.INTERVIEW_CANCELED,
                currentStatus,
                InterviewStatus.CANCELED.name(),
                "取消" + interview.getRound() + "轮面试",
                ProcessRelatedType.INTERVIEW,
                interview.getId()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeInterview(Long id) {
        Interview interview = requireInterview(id);
        String roleCode = UserContext.getRoleCode();

        if (!"ADMIN".equals(roleCode)
                && !"HR".equals(roleCode)
                && !"INTERVIEWER".equals(roleCode)) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "无权完成该面试"
            );
        }

        checkInterviewAccess(interview);

        if (!InterviewStatus.SCHEDULED.name()
                .equals(interview.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "只有待面试状态可以标记为完成"
            );
        }

        int updated = interviewMapper.update(
                null,
                new LambdaUpdateWrapper<Interview>()
                        .eq(Interview::getId, id)
                        .eq(
                                Interview::getStatus,
                                InterviewStatus.SCHEDULED.name()
                        )
                        .set(
                                Interview::getStatus,
                                InterviewStatus.COMPLETED.name()
                        )
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "完成面试失败，记录可能已被其他人处理"
            );
        }

        processEventService.record(
                interview.getApplicationId(),
                ProcessEventType.INTERVIEW_COMPLETED,
                InterviewStatus.SCHEDULED.name(),
                InterviewStatus.COMPLETED.name(),
                "面试已标记完成，等待面试反馈",
                ProcessRelatedType.INTERVIEW,
                interview.getId()
        );
    }

    @Override
    public PageResult<InterviewSummaryVO> listMyCandidateInterviews(
            InterviewQueryDTO dto
    ) {
        Candidate candidate = candidateMapper.selectOne(
                new LambdaQueryWrapper<Candidate>()
                        .eq(Candidate::getUserId, UserContext.getUserId())
        );
        if (candidate == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "该账号没有绑定候选人档案"
            );
        }

        Set<Long> applicationIds = jobApplicationMapper.selectList(
                        new LambdaQueryWrapper<JobApplication>()
                                .eq(
                                        JobApplication::getCandidateId,
                                        candidate.getId()
                                )
                )
                .stream()
                .map(JobApplication::getId)
                .collect(Collectors.toSet());

        if (applicationIds.isEmpty()) {
            return new PageResult<>(0L, List.of());
        }

        LambdaQueryWrapper<Interview> wrapper =
                new LambdaQueryWrapper<Interview>()
                        .in(Interview::getApplicationId, applicationIds)
                        .isNotNull(Interview::getScheduledAt);

        return queryInterviews(wrapper, dto);
    }

    @Override
    public PageResult<InterviewSummaryVO> listMyInterviewerInterviews(
            InterviewQueryDTO dto
    ) {
        LambdaQueryWrapper<Interview> wrapper =
                new LambdaQueryWrapper<Interview>()
                        .eq(
                                Interview::getInterviewerId,
                                UserContext.getUserId()
                        );

        return queryInterviews(wrapper, dto);
    }

    private PageResult<InterviewSummaryVO> queryInterviews(
            LambdaQueryWrapper<Interview> wrapper,
            InterviewQueryDTO dto
    ) {
        InterviewQueryDTO query = dto == null
                ? new InterviewQueryDTO()
                : dto;

        if (query.getStartTime() != null
                && query.getEndTime() != null
                && query.getStartTime().isAfter(query.getEndTime())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "开始时间不能晚于结束时间"
            );
        }

        int pageNum = query.getPageNum() == null || query.getPageNum() < 1
                ? 1
                : query.getPageNum();
        int pageSize = query.getPageSize() == null
                || query.getPageSize() < 1
                || query.getPageSize() > 100
                ? 10
                : query.getPageSize();

        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(Interview::getStatus, query.getStatus());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(Interview::getInterviewTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(Interview::getInterviewTime, query.getEndTime());
        }
        wrapper.orderByAsc(Interview::getInterviewTime)
                .orderByAsc(Interview::getId);

        Page<Interview> result = interviewMapper.selectPage(
                new Page<>(pageNum, pageSize),
                wrapper
        );

        return buildPageResult(result);
    }

    private PageResult<InterviewSummaryVO> buildPageResult(
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

        Map<Long, JobApplication> applicationMap =
                jobApplicationMapper.selectBatchIds(applicationIds)
                        .stream()
                        .collect(Collectors.toMap(
                                JobApplication::getId,
                                Function.identity()
                        ));

        Set<Long> jobIds = applicationMap.values().stream()
                .map(JobApplication::getJobId)
                .collect(Collectors.toSet());
        Set<Long> candidateIds = applicationMap.values().stream()
                .map(JobApplication::getCandidateId)
                .collect(Collectors.toSet());

        Map<Long, JobPosition> jobMap = jobIds.isEmpty()
                ? Map.of()
                : jobPositionMapper.selectBatchIds(jobIds)
                .stream()
                .collect(Collectors.toMap(
                        JobPosition::getId,
                        Function.identity()
                ));
        Map<Long, Candidate> candidateMap = candidateIds.isEmpty()
                ? Map.of()
                : candidateMapper.selectBatchIds(candidateIds)
                .stream()
                .collect(Collectors.toMap(
                        Candidate::getId,
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

        List<InterviewSummaryVO> records = interviews.stream()
                .map(interview -> toSummaryVO(
                        interview,
                        applicationMap,
                        jobMap,
                        candidateMap,
                        interviewerMap
                ))
                .toList();

        return new PageResult<>(result.getTotal(), records);
    }

    private InterviewSummaryVO toSummaryVO(
            Interview interview,
            Map<Long, JobApplication> applicationMap,
            Map<Long, JobPosition> jobMap,
            Map<Long, Candidate> candidateMap,
            Map<Long, SysUser> interviewerMap
    ) {
        JobApplication application = applicationMap.get(
                interview.getApplicationId()
        );
        JobPosition job = application == null
                ? null
                : jobMap.get(application.getJobId());
        Candidate candidate = application == null
                ? null
                : candidateMap.get(application.getCandidateId());
        SysUser interviewer = interviewerMap.get(interview.getInterviewerId());

        InterviewSummaryVO vo = new InterviewSummaryVO();
        vo.setId(interview.getId());
        vo.setApplicationId(interview.getApplicationId());
        if (application != null) {
            vo.setJobId(application.getJobId());
            vo.setCandidateId(application.getCandidateId());
        }
        vo.setJobTitle(job == null ? null : job.getTitle());
        vo.setCandidateName(candidate == null ? null : candidate.getName());
        vo.setInterviewerId(interview.getInterviewerId());
        vo.setInterviewerName(
                interviewer == null ? null : interviewer.getRealName()
        );
        vo.setRound(interview.getRound());
        vo.setInterviewTime(interview.getInterviewTime());
        vo.setMethod(interview.getMethod());
        vo.setLocation(interview.getLocation());
        vo.setStatus(interview.getStatus());
        vo.setAssignedAt(interview.getAssignedAt());
        vo.setScheduledAt(interview.getScheduledAt());

        InterviewRound round = InterviewRound.fromCode(interview.getRound());
        vo.setRoundText(
                round == null ? "未知轮次" : round.getDescription()
        );
        InterviewMethod method = InterviewMethod.fromCode(
                interview.getMethod()
        );
        vo.setMethodText(
                method == null ? "待确认" : method.getDescription()
        );
        InterviewStatus status = InterviewStatus.fromCode(
                interview.getStatus()
        );
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );

        return vo;
    }

    private Interview requireInterview(Long id) {
        Interview interview = interviewMapper.selectById(id);
        if (interview == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "面试记录不存在"
            );
        }
        return interview;
    }

    private SysUser requireInterviewer(Long interviewerId) {
        SysUser interviewer = sysUserMapper.selectById(interviewerId);
        if (interviewer == null
                || !Integer.valueOf(1).equals(interviewer.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "面试官不存在或已被禁用"
            );
        }

        SysRole role = sysRoleMapper.selectById(interviewer.getRoleId());
        if (role == null
                || !"INTERVIEWER".equals(role.getRoleCode())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "指定用户不是面试官角色"
            );
        }

        return interviewer;
    }

    private void checkInterviewAccess(Interview interview) {
        String roleCode = UserContext.getRoleCode();

        if ("ADMIN".equals(roleCode) || "HR".equals(roleCode)) {
            return;
        }

        if ("INTERVIEWER".equals(roleCode)) {
            if (!UserContext.getUserId().equals(interview.getInterviewerId())) {
                throw new BusinessException(
                        ErrorCode.FORBIDDEN,
                        "无权访问该面试记录"
                );
            }
            return;
        }

        if ("CANDIDATE".equals(roleCode)) {
            if (interview.getScheduledAt() == null) {
                throw new BusinessException(
                        ErrorCode.FORBIDDEN,
                        "面试尚未确认预约，候选人暂不可查看"
                );
            }
            JobApplication application = jobApplicationMapper.selectById(
                    interview.getApplicationId()
            );
            Candidate candidate = candidateMapper.selectOne(
                    new LambdaQueryWrapper<Candidate>()
                            .eq(
                                    Candidate::getUserId,
                                    UserContext.getUserId()
                            )
            );

            if (application == null
                    || candidate == null
                    || !candidate.getId().equals(
                    application.getCandidateId()
            )) {
                throw new BusinessException(
                        ErrorCode.FORBIDDEN,
                        "无权访问该面试记录"
                );
            }
            return;
        }

        throw new BusinessException(
                ErrorCode.FORBIDDEN,
                "无权访问该面试记录"
        );
    }
}
