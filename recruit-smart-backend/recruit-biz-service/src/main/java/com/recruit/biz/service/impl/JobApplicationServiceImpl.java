package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.JobApplicationCreateDTO;
import com.recruit.biz.dto.JobApplicationHRQueryDTO;
import com.recruit.biz.dto.JobApplicationQueryDTO;
import com.recruit.biz.dto.JobApplicationRejectDTO;
import com.recruit.biz.dto.JobApplicationScreeningDTO;
import com.recruit.biz.dto.JobApplicationStatusUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.enums.JobApplicationStatus;
import com.recruit.biz.enums.JobPositionStatus;
import com.recruit.biz.enums.ProcessEventType;
import com.recruit.biz.enums.ProcessRelatedType;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.JobApplicationService;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.biz.vo.JobApplicationSummaryVO;
import com.recruit.biz.vo.JobApplicationDetailVO;
import com.recruit.biz.vo.JobApplicationHRSummaryVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JobApplicationServiceImpl implements JobApplicationService {

    @Resource
    private CandidateMapper candidateMapper;

    @Resource
    private JobPositionMapper jobPositionMapper;

    @Resource
    private ResumeMapper resumeMapper;

    @Resource
    private JobApplicationMapper jobApplicationMapper;

    @Resource
    private InterviewMapper interviewMapper;

    @Resource
    private ApplicationProcessEventService processEventService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createApplication(JobApplicationCreateDTO dto) {
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

        JobPosition job = jobPositionMapper.selectById(dto.getJobId());
        if (job == null
                || !JobPositionStatus.OPEN.name().equals(job.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "该职位不存在或当前未开放招聘"
            );
        }

        Resume resume = resumeMapper.selectById(dto.getResumeId());
        if (resume == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "简历不存在"
            );
        }
        if (!candidate.getId().equals(resume.getCandidateId())) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "不能使用其他候选人的简历投递"
            );
        }

        Long applicationCount = jobApplicationMapper.selectCount(
                new LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getCandidateId, candidate.getId())
                        .eq(JobApplication::getJobId, job.getId())
        );
        if (applicationCount > 0) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "你已经投递过该职位"
            );
        }

        JobApplication application = new JobApplication();
        application.setJobId(job.getId());
        application.setCandidateId(candidate.getId());
        application.setResumeId(resume.getId());
        application.setStatus(JobApplicationStatus.SUBMITTED.name());
        application.setAllowAdjustment(
                Boolean.TRUE.equals(dto.getAllowAdjustment()) ? 1 : 0
        );
        application.setSource("ONLINE");

        try {
            jobApplicationMapper.insert(application);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "你已经投递过该职位"
            );
        }

        processEventService.record(
                application.getId(),
                ProcessEventType.APPLICATION_SUBMITTED,
                null,
                JobApplicationStatus.SUBMITTED.name(),
                "使用简历“" + resume.getResumeName()
                        + "”投递职位“" + job.getTitle() + "”",
                ProcessRelatedType.APPLICATION,
                application.getId()
        );

        return application.getId();
    }

    @Override
    public PageResult<JobApplicationSummaryVO> listMyApplications(
            JobApplicationQueryDTO dto
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

        JobApplicationQueryDTO query = dto == null
                ? new JobApplicationQueryDTO()
                : dto;
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1
                ? 1
                : query.getPageNum();
        int pageSize = query.getPageSize() == null
                || query.getPageSize() < 1
                || query.getPageSize() > 100
                ? 10
                : query.getPageSize();

        Page<JobApplication> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<JobApplication> wrapper =
                new LambdaQueryWrapper<JobApplication>()
                        .eq(
                                JobApplication::getCandidateId,
                                candidate.getId()
                        )
                        .orderByDesc(JobApplication::getAppliedAt)
                        .orderByDesc(JobApplication::getId);

        if (query.getStatus() != null
                && !query.getStatus().isBlank()) {
            wrapper.eq(JobApplication::getStatus, query.getStatus());
        }

        Page<JobApplication> result =
                jobApplicationMapper.selectPage(page, wrapper);
        List<JobApplication> applications = result.getRecords();

        Set<Long> jobIds = applications.stream()
                .map(JobApplication::getJobId)
                .collect(Collectors.toSet());
        Set<Long> resumeIds = applications.stream()
                .map(JobApplication::getResumeId)
                .collect(Collectors.toSet());

        Map<Long, JobPosition> jobMap = jobIds.isEmpty()
                ? Map.of()
                : jobPositionMapper.selectBatchIds(jobIds)
                .stream()
                .collect(Collectors.toMap(
                        JobPosition::getId,
                        Function.identity()
                ));
        Map<Long, Resume> resumeMap = resumeIds.isEmpty()
                ? Map.of()
                : resumeMapper.selectBatchIds(resumeIds)
                .stream()
                .collect(Collectors.toMap(
                        Resume::getId,
                        Function.identity()
                ));

        List<JobApplicationSummaryVO> records = applications.stream()
                .map(application -> toSummaryVO(
                        application,
                        jobMap.get(application.getJobId()),
                        resumeMap.get(application.getResumeId())
                ))
                .toList();

        return new PageResult<>(result.getTotal(), records);
    }

    @Override
    public JobApplicationDetailVO getDetail(Long id) {
        JobApplication application = jobApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "投递记录不存在"
            );
        }

        checkApplicationAccess(application);

        JobPosition job = jobPositionMapper.selectById(application.getJobId());
        Resume resume = resumeMapper.selectById(application.getResumeId());
        Candidate candidate = candidateMapper.selectById(
                application.getCandidateId()
        );

        JobApplicationDetailVO vo = new JobApplicationDetailVO();
        vo.setId(application.getId());
        vo.setJobId(application.getJobId());
        vo.setJobTitle(job == null ? null : job.getTitle());
        vo.setDepartment(job == null ? null : job.getDepartment());
        vo.setCandidateId(application.getCandidateId());
        vo.setCandidateName(candidate == null ? null : candidate.getName());
        vo.setResumeId(application.getResumeId());
        vo.setResumeName(resume == null ? null : resume.getResumeName());
        vo.setResumeFileType(resume == null ? null : resume.getFileType());
        vo.setStatus(application.getStatus());
        vo.setAllowAdjustment(application.getAllowAdjustment());
        vo.setAdjustedJobId(application.getAdjustedJobId());
        vo.setSource(application.getSource());
        vo.setRejectReasonCode(application.getRejectReasonCode());
        vo.setRejectReason(application.getRejectReason());
        vo.setAppliedAt(application.getAppliedAt());
        vo.setReviewedAt(application.getReviewedAt());
        vo.setCreatedAt(application.getCreatedAt());
        vo.setUpdatedAt(application.getUpdatedAt());

        if (!"CANDIDATE".equals(UserContext.getRoleCode())) {
            vo.setHrNote(application.getHrNote());
        }

        JobApplicationStatus status =
                JobApplicationStatus.fromCode(application.getStatus());
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long id) {
        JobApplication application = jobApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "投递记录不存在"
            );
        }

        checkApplicationAccess(application);

        String status = application.getStatus();
        boolean canWithdraw = JobApplicationStatus.SUBMITTED.name()
                .equals(status)
                || JobApplicationStatus.SCREENING.name().equals(status)
                || JobApplicationStatus.SCREEN_PASSED.name().equals(status)
                || JobApplicationStatus.INTERVIEWING.name().equals(status);

        if (!canWithdraw) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "当前投递状态不允许撤回"
            );
        }

        int updated = jobApplicationMapper.update(
                null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<JobApplication>()
                        .eq(JobApplication::getId, id)
                        .eq(
                                JobApplication::getCandidateId,
                                application.getCandidateId()
                        )
                        .set(
                                JobApplication::getStatus,
                                JobApplicationStatus.WITHDRAWN.name()
                        )
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "撤回投递失败"
            );
        }

        processEventService.record(
                application.getId(),
                ProcessEventType.APPLICATION_WITHDRAWN,
                status,
                JobApplicationStatus.WITHDRAWN.name(),
                "候选人主动撤回投递",
                ProcessRelatedType.APPLICATION,
                application.getId()
        );
    }

    @Override
    public PageResult<JobApplicationHRSummaryVO> listJobApplications(
            Long jobId,
            JobApplicationHRQueryDTO dto
    ) {
        JobPosition job = jobPositionMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "职位不存在"
            );
        }

        JobApplicationHRQueryDTO query = dto == null
                ? new JobApplicationHRQueryDTO()
                : dto;
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1
                ? 1
                : query.getPageNum();
        int pageSize = query.getPageSize() == null
                || query.getPageSize() < 1
                || query.getPageSize() > 100
                ? 10
                : query.getPageSize();

        LambdaQueryWrapper<JobApplication> wrapper =
                new LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getJobId, jobId)
                        .orderByDesc(JobApplication::getAppliedAt)
                        .orderByDesc(JobApplication::getId);

        if ("INTERVIEWER".equals(UserContext.getRoleCode())) {
            Set<Long> assignedApplicationIds = interviewMapper.selectList(
                            new LambdaQueryWrapper<Interview>()
                                    .eq(
                                            Interview::getInterviewerId,
                                            UserContext.getUserId()
                                    )
                    )
                    .stream()
                    .map(Interview::getApplicationId)
                    .collect(Collectors.toSet());

            if (assignedApplicationIds.isEmpty()) {
                return new PageResult<>(0L, List.of());
            }
            wrapper.in(JobApplication::getId, assignedApplicationIds);
        }

        if (query.getStatus() != null
                && !query.getStatus().isBlank()) {
            wrapper.eq(JobApplication::getStatus, query.getStatus());
        }

        if (query.getCandidateKeyword() != null
                && !query.getCandidateKeyword().isBlank()) {
            String keyword = query.getCandidateKeyword().trim();
            Set<Long> candidateIds = candidateMapper.selectList(
                            new LambdaQueryWrapper<Candidate>()
                                    .and(condition -> condition
                                            .like(Candidate::getName, keyword)
                                            .or()
                                            .like(Candidate::getPhone, keyword)
                                            .or()
                                            .like(Candidate::getEmail, keyword)
                                    )
                    )
                    .stream()
                    .map(Candidate::getId)
                    .collect(Collectors.toSet());

            if (candidateIds.isEmpty()) {
                return new PageResult<>(0L, List.of());
            }

            wrapper.in(JobApplication::getCandidateId, candidateIds);
        }

        Page<JobApplication> page = new Page<>(pageNum, pageSize);
        Page<JobApplication> result =
                jobApplicationMapper.selectPage(page, wrapper);
        List<JobApplication> applications = result.getRecords();

        Set<Long> candidateIds = applications.stream()
                .map(JobApplication::getCandidateId)
                .collect(Collectors.toSet());
        Set<Long> resumeIds = applications.stream()
                .map(JobApplication::getResumeId)
                .collect(Collectors.toSet());

        Map<Long, Candidate> candidateMap = candidateIds.isEmpty()
                ? Map.of()
                : candidateMapper.selectBatchIds(candidateIds)
                .stream()
                .collect(Collectors.toMap(
                        Candidate::getId,
                        Function.identity()
                ));
        Map<Long, Resume> resumeMap = resumeIds.isEmpty()
                ? Map.of()
                : resumeMapper.selectBatchIds(resumeIds)
                .stream()
                .collect(Collectors.toMap(
                        Resume::getId,
                        Function.identity()
                ));

        List<JobApplicationHRSummaryVO> records = applications.stream()
                .map(application -> toHRSummaryVO(
                        application,
                        job,
                        candidateMap.get(application.getCandidateId()),
                        resumeMap.get(application.getResumeId())
                ))
                .toList();

        return new PageResult<>(result.getTotal(), records);
    }

    private JobApplicationHRSummaryVO toHRSummaryVO(
            JobApplication application,
            JobPosition job,
            Candidate candidate,
            Resume resume
    ) {
        JobApplicationHRSummaryVO vo = new JobApplicationHRSummaryVO();
        vo.setId(application.getId());
        vo.setJobId(application.getJobId());
        vo.setJobTitle(job.getTitle());
        vo.setCandidateId(application.getCandidateId());

        if (candidate != null) {
            vo.setCandidateName(candidate.getName());
            vo.setGender(candidate.getGender());
            vo.setAge(candidate.getAge());
            vo.setEducation(candidate.getEducation());
            vo.setSchool(candidate.getSchool());
            vo.setMajor(candidate.getMajor());
            vo.setYearsOfExperience(candidate.getYearsOfExperience());
        }

        vo.setResumeId(application.getResumeId());
        if (resume != null) {
            vo.setResumeName(resume.getResumeName());
            vo.setResumeFileType(resume.getFileType());
        }

        vo.setStatus(application.getStatus());
        vo.setAllowAdjustment(application.getAllowAdjustment());
        vo.setAdjustedJobId(application.getAdjustedJobId());
        vo.setAppliedAt(application.getAppliedAt());
        vo.setReviewedAt(application.getReviewedAt());

        JobApplicationStatus status =
                JobApplicationStatus.fromCode(application.getStatus());
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, JobApplicationRejectDTO dto) {
        JobApplication application = requireApplication(id);
        String currentStatus = application.getStatus();
        String targetStatus;

        if (JobApplicationStatus.SUBMITTED.name().equals(currentStatus)
                || JobApplicationStatus.SCREENING.name()
                .equals(currentStatus)) {
            targetStatus = JobApplicationStatus.SCREEN_REJECT.name();
        } else if (JobApplicationStatus.SCREEN_PASSED.name()
                .equals(currentStatus)
                || JobApplicationStatus.INTERVIEWING.name()
                .equals(currentStatus)
                || JobApplicationStatus.OFFERED.name()
                .equals(currentStatus)) {
            targetStatus = JobApplicationStatus.REJECTED.name();
        } else {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "当前投递状态不能拒绝"
            );
        }

        int updated = jobApplicationMapper.update(
                null,
                new LambdaUpdateWrapper<JobApplication>()
                        .eq(JobApplication::getId, id)
                        .eq(JobApplication::getStatus, currentStatus)
                        .set(JobApplication::getStatus, targetStatus)
                        .set(
                                JobApplication::getRejectReasonCode,
                                dto.getReasonCode().trim()
                        )
                        .set(
                                JobApplication::getRejectReason,
                                dto.getReason().trim()
                        )
                        .set(
                                JobApplication::getReviewedBy,
                                UserContext.getUserId()
                        )
                        .set(
                                JobApplication::getReviewedAt,
                                LocalDateTime.now()
                        )
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "拒绝投递失败，记录可能已被其他人处理"
            );
        }

        processEventService.record(
                application.getId(),
                JobApplicationStatus.SCREEN_REJECT.name().equals(targetStatus)
                        ? ProcessEventType.SCREENING_REJECTED
                        : ProcessEventType.APPLICATION_REJECTED,
                currentStatus,
                targetStatus,
                dto.getReason().trim(),
                ProcessRelatedType.APPLICATION,
                application.getId()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewScreening(
            Long id,
            JobApplicationScreeningDTO dto
    ) {
        JobApplication application = requireApplication(id);
        if (!JobApplicationStatus.SCREENING.name()
                .equals(application.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "只有筛选中的投递可以提交筛选结论"
            );
        }

        String decision = dto.getDecision().trim();
        String note = StringUtils.hasText(dto.getNote())
                ? dto.getNote().trim()
                : null;
        String targetStatus = JobApplicationStatus.SCREENING.name();
        ProcessEventType eventType;
        LambdaUpdateWrapper<JobApplication> wrapper =
                new LambdaUpdateWrapper<JobApplication>()
                        .eq(JobApplication::getId, id)
                        .eq(
                                JobApplication::getStatus,
                                JobApplicationStatus.SCREENING.name()
                        )
                        .set(
                                JobApplication::getReviewedBy,
                                UserContext.getUserId()
                        )
                        .set(
                                JobApplication::getReviewedAt,
                                LocalDateTime.now()
                        );

        switch (decision) {
            case "PASS" -> {
                targetStatus = JobApplicationStatus.SCREEN_PASSED.name();
                eventType = ProcessEventType.SCREENING_PASSED;
                wrapper
                        .set(JobApplication::getStatus, targetStatus)
                        .set(
                                StringUtils.hasText(note),
                                JobApplication::getHrNote,
                                note
                        );
            }
            case "REJECT" -> {
                if (!StringUtils.hasText(dto.getRejectReasonCode())
                        || !StringUtils.hasText(note)) {
                    throw new BusinessException(
                            ErrorCode.PARAM_ERROR,
                            "拒绝时必须填写拒绝原因和说明"
                    );
                }
                targetStatus = JobApplicationStatus.SCREEN_REJECT.name();
                eventType = ProcessEventType.SCREENING_REJECTED;
                wrapper
                        .set(
                                JobApplication::getStatus,
                                targetStatus
                        )
                        .set(
                                JobApplication::getRejectReasonCode,
                                dto.getRejectReasonCode().trim()
                        )
                        .set(JobApplication::getRejectReason, note)
                        .set(JobApplication::getHrNote, note);
            }
            case "PENDING" -> {
                if (!StringUtils.hasText(note)) {
                    throw new BusinessException(
                            ErrorCode.PARAM_ERROR,
                            "保留待定时必须填写待核实事项"
                    );
                }
                eventType = ProcessEventType.SCREENING_PENDING;
                wrapper.set(JobApplication::getHrNote, note);
            }
            default -> throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "筛选结论不正确"
            );
        }

        int updated = jobApplicationMapper.update(null, wrapper);
        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "提交筛选结论失败，记录可能已被其他人处理"
            );
        }

        processEventService.record(
                application.getId(),
                eventType,
                JobApplicationStatus.SCREENING.name(),
                targetStatus,
                note,
                ProcessRelatedType.APPLICATION,
                application.getId()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(
            Long id,
            JobApplicationStatusUpdateDTO dto
    ) {
        JobApplication application = requireApplication(id);
        String currentStatus = application.getStatus();
        String targetStatus = dto.getStatus();

        if (targetStatus.equals(currentStatus)) {
            return;
        }

        if (!isAllowedTransition(currentStatus, targetStatus)) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "不允许从当前状态修改为目标状态"
            );
        }

        updateStatusInternal(application, targetStatus);
        processEventService.record(
                application.getId(),
                ProcessEventType.SCREENING_STARTED,
                currentStatus,
                targetStatus,
                "HR开始处理该投递",
                ProcessRelatedType.APPLICATION,
                application.getId()
        );
    }

    private boolean isAllowedTransition(
            String currentStatus,
            String targetStatus
    ) {
        return JobApplicationStatus.SUBMITTED.name().equals(currentStatus)
                && JobApplicationStatus.SCREENING.name()
                .equals(targetStatus);
    }

    private void updateStatusInternal(
            JobApplication application,
            String targetStatus
    ) {
        int updated = jobApplicationMapper.update(
                null,
                new LambdaUpdateWrapper<JobApplication>()
                        .eq(JobApplication::getId, application.getId())
                        .eq(
                                JobApplication::getStatus,
                                application.getStatus()
                        )
                        .set(JobApplication::getStatus, targetStatus)
                        .set(
                                JobApplication::getReviewedBy,
                                UserContext.getUserId()
                        )
                        .set(
                                JobApplication::getReviewedAt,
                                LocalDateTime.now()
                        )
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "修改投递状态失败，记录可能已被其他人处理"
            );
        }
    }

    private JobApplication requireApplication(Long id) {
        JobApplication application = jobApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "投递记录不存在"
            );
        }
        return application;
    }

    private void checkApplicationAccess(JobApplication application) {
        String roleCode = UserContext.getRoleCode();
        if ("ADMIN".equals(roleCode) || "HR".equals(roleCode)) {
            return;
        }

        if ("INTERVIEWER".equals(roleCode)) {
            Long assignedCount = interviewMapper.selectCount(
                    new LambdaQueryWrapper<Interview>()
                            .eq(
                                    Interview::getApplicationId,
                                    application.getId()
                            )
                            .eq(
                                    Interview::getInterviewerId,
                                    UserContext.getUserId()
                            )
            );
            if (assignedCount == null || assignedCount == 0) {
                throw new BusinessException(
                        ErrorCode.FORBIDDEN,
                        "无权访问未分配给自己的投递记录"
                );
            }
            return;
        }

        if ("CANDIDATE".equals(roleCode)) {
            Candidate candidate = candidateMapper.selectOne(
                    new LambdaQueryWrapper<Candidate>()
                            .eq(
                                    Candidate::getUserId,
                                    UserContext.getUserId()
                            )
            );

            if (candidate != null
                    && candidate.getId().equals(
                    application.getCandidateId()
            )) {
                return;
            }
        }

        throw new BusinessException(
                ErrorCode.FORBIDDEN,
                "无权访问该投递记录"
        );
    }

    private JobApplicationSummaryVO toSummaryVO(
            JobApplication application,
            JobPosition job,
            Resume resume
    ) {
        JobApplicationSummaryVO vo = new JobApplicationSummaryVO();
        vo.setId(application.getId());
        vo.setJobId(application.getJobId());
        vo.setJobTitle(job == null ? null : job.getTitle());
        vo.setDepartment(job == null ? null : job.getDepartment());
        vo.setResumeId(application.getResumeId());
        vo.setResumeName(resume == null ? null : resume.getResumeName());
        vo.setStatus(application.getStatus());
        vo.setAllowAdjustment(application.getAllowAdjustment());
        vo.setAdjustedJobId(application.getAdjustedJobId());
        vo.setAppliedAt(application.getAppliedAt());

        JobApplicationStatus status =
                JobApplicationStatus.fromCode(application.getStatus());
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );

        return vo;
    }
}
