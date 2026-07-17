package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.assembler.PipelineAssembler;
import com.recruit.biz.dto.PipelineApplicationQueryDTO;
import com.recruit.biz.entity.AiMatchResult;
import com.recruit.biz.entity.ApplicationProcessEvent;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Offer;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.AiMatchResultMapper;
import com.recruit.biz.mapper.ApplicationProcessEventMapper;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.OfferMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.service.PipelineService;
import com.recruit.biz.vo.PipelineApplicationDetailVO;
import com.recruit.biz.vo.PipelineApplicationSummaryVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PipelineServiceImpl implements PipelineService {

    @Resource
    private JobApplicationMapper jobApplicationMapper;
    @Resource
    private CandidateMapper candidateMapper;
    @Resource
    private JobPositionMapper jobPositionMapper;
    @Resource
    private ResumeMapper resumeMapper;
    @Resource
    private AiMatchResultMapper aiMatchResultMapper;
    @Resource
    private ApplicationProcessEventMapper applicationProcessEventMapper;
    @Resource
    private InterviewMapper interviewMapper;
    @Resource
    private InterviewFeedbackMapper interviewFeedbackMapper;
    @Resource
    private OfferMapper offerMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private PipelineAssembler pipelineAssembler;

    @Override
    public PageResult<PipelineApplicationSummaryVO> listPipeline(
            PipelineApplicationQueryDTO dto
    ) {
        PipelineApplicationQueryDTO query = dto == null
                ? new PipelineApplicationQueryDTO()
                : dto;
        LambdaQueryWrapper<JobApplication> wrapper =
                new LambdaQueryWrapper<>();
        if (hasText(query.getStatus())) {
            wrapper.eq(JobApplication::getStatus, query.getStatus());
        }
        if (query.getJobId() != null) {
            wrapper.eq(JobApplication::getJobId, query.getJobId());
        }
        if (hasText(query.getKeyword())
                && !applyKeywordFilter(wrapper, query.getKeyword().trim())) {
            return new PageResult<>(0L, List.of());
        }

        wrapper.orderByDesc(JobApplication::getUpdatedAt)
                .orderByDesc(JobApplication::getAppliedAt)
                .orderByDesc(JobApplication::getId);
        Page<JobApplication> result = jobApplicationMapper.selectPage(
                new Page<>(
                        normalizePageNum(query.getPageNum()),
                        normalizePageSize(query.getPageSize())
                ),
                wrapper
        );
        List<JobApplication> applications = result.getRecords();
        if (applications.isEmpty()) {
            return new PageResult<>(result.getTotal(), List.of());
        }

        PipelineBatchData batchData = loadBatchData(applications);
        List<PipelineApplicationSummaryVO> records = applications.stream()
                .map(application -> pipelineAssembler.toSummary(
                        application,
                        batchData.candidateMap().get(
                                application.getCandidateId()
                        ),
                        batchData.jobMap().get(application.getJobId()),
                        batchData.aiMatchMap().get(application.getId()),
                        batchData.ownerMap().get(application.getReviewedBy()),
                        batchData.interviewMap().getOrDefault(
                                application.getId(),
                                List.of()
                        ),
                        batchData.offerMap().get(application.getId())
                ))
                .toList();
        return new PageResult<>(result.getTotal(), records);
    }

    @Override
    public PipelineApplicationDetailVO getPipelineDetail(
            Long applicationId
    ) {
        JobApplication application = jobApplicationMapper.selectById(
                applicationId
        );
        if (application == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "投递记录不存在"
            );
        }

        Candidate candidate = candidateMapper.selectById(
                application.getCandidateId()
        );
        JobPosition job = jobPositionMapper.selectById(application.getJobId());
        Resume resume = resumeMapper.selectById(application.getResumeId());
        AiMatchResult aiMatch = aiMatchResultMapper.selectOne(
                new LambdaQueryWrapper<AiMatchResult>()
                        .eq(AiMatchResult::getApplicationId, applicationId)
        );
        List<Interview> interviews = interviewMapper.selectList(
                new LambdaQueryWrapper<Interview>()
                        .eq(Interview::getApplicationId, applicationId)
                        .orderByAsc(Interview::getInterviewTime)
                        .orderByAsc(Interview::getId)
        );
        Offer offer = offerMapper.selectOne(
                new LambdaQueryWrapper<Offer>()
                        .eq(Offer::getApplicationId, applicationId)
        );
        List<ApplicationProcessEvent> processEvents =
                applicationProcessEventMapper.selectList(
                        new LambdaQueryWrapper<ApplicationProcessEvent>()
                                .eq(
                                        ApplicationProcessEvent::getApplicationId,
                                        applicationId
                                )
                                .orderByAsc(
                                        ApplicationProcessEvent::getOccurredAt
                                )
                                .orderByAsc(ApplicationProcessEvent::getId)
                );

        Set<Long> userIds = collectRelatedUserIds(application, interviews, offer);
        processEvents.stream()
                .map(ApplicationProcessEvent::getOperatorId)
                .filter(Objects::nonNull)
                .forEach(userIds::add);
        Map<Long, SysUser> userMap = userIds.isEmpty()
                ? Map.of()
                : sysUserMapper.selectBatchIds(userIds)
                .stream()
                .collect(Collectors.toMap(
                        SysUser::getId,
                        Function.identity()
                ));
        Interview currentInterview = pipelineAssembler.selectCurrentInterview(
                interviews
        );
        InterviewFeedback feedback = currentInterview == null
                ? null
                : interviewFeedbackMapper.selectOne(
                        new LambdaQueryWrapper<InterviewFeedback>()
                                .eq(
                                        InterviewFeedback::getInterviewId,
                                        currentInterview.getId()
                                )
                                .and(condition -> condition
                                        .eq(
                                                InterviewFeedback::getState,
                                                "SUBMITTED"
                                        )
                                        .or()
                                        .isNull(InterviewFeedback::getState)
                                )
                );

        return pipelineAssembler.toDetail(
                application,
                candidate,
                job,
                resume,
                aiMatch,
                interviews,
                currentInterview,
                feedback,
                offer,
                processEvents,
                userMap
        );
    }

    private boolean applyKeywordFilter(
            LambdaQueryWrapper<JobApplication> wrapper,
            String keyword
    ) {
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

        if (candidateIds.isEmpty() && jobIds.isEmpty()) {
            return false;
        }
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
        return true;
    }

    private PipelineBatchData loadBatchData(
            List<JobApplication> applications
    ) {
        Set<Long> applicationIds = applications.stream()
                .map(JobApplication::getId)
                .collect(Collectors.toSet());
        Set<Long> candidateIds = applications.stream()
                .map(JobApplication::getCandidateId)
                .collect(Collectors.toSet());
        Set<Long> jobIds = applications.stream()
                .map(JobApplication::getJobId)
                .collect(Collectors.toSet());
        Set<Long> ownerIds = applications.stream()
                .map(JobApplication::getReviewedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Candidate> candidateMap = candidateMapper
                .selectBatchIds(candidateIds)
                .stream()
                .collect(Collectors.toMap(
                        Candidate::getId,
                        Function.identity()
                ));
        Map<Long, JobPosition> jobMap = jobPositionMapper
                .selectBatchIds(jobIds)
                .stream()
                .collect(Collectors.toMap(
                        JobPosition::getId,
                        Function.identity()
                ));
        Map<Long, AiMatchResult> aiMatchMap = aiMatchResultMapper.selectList(
                        new LambdaQueryWrapper<AiMatchResult>()
                                .in(
                                        AiMatchResult::getApplicationId,
                                        applicationIds
                                )
                )
                .stream()
                .collect(Collectors.toMap(
                        AiMatchResult::getApplicationId,
                        Function.identity()
                ));
        Map<Long, SysUser> ownerMap = ownerIds.isEmpty()
                ? Map.of()
                : sysUserMapper.selectBatchIds(ownerIds)
                .stream()
                .collect(Collectors.toMap(
                        SysUser::getId,
                        Function.identity()
                ));

        List<Interview> interviews = interviewMapper.selectList(
                new LambdaQueryWrapper<Interview>()
                        .in(Interview::getApplicationId, applicationIds)
        );
        Map<Long, List<Interview>> interviewMap = interviews.stream()
                .collect(Collectors.groupingBy(Interview::getApplicationId));
        Map<Long, Offer> offerMap = offerMapper.selectList(
                        new LambdaQueryWrapper<Offer>()
                                .in(Offer::getApplicationId, applicationIds)
                )
                .stream()
                .collect(Collectors.toMap(
                        Offer::getApplicationId,
                        Function.identity()
                ));

        return new PipelineBatchData(
                candidateMap,
                jobMap,
                aiMatchMap,
                ownerMap,
                interviewMap,
                offerMap
        );
    }

    private Set<Long> collectRelatedUserIds(
            JobApplication application,
            List<Interview> interviews,
            Offer offer
    ) {
        Set<Long> userIds = interviews.stream()
                .flatMap(interview -> Stream.of(
                                interview.getInterviewerId(),
                                interview.getCreatedBy()
                        )
                        .filter(Objects::nonNull))
                .collect(Collectors.toSet());
        if (application.getReviewedBy() != null) {
            userIds.add(application.getReviewedBy());
        }
        if (offer != null && offer.getCreatedBy() != null) {
            userIds.add(offer.getCreatedBy());
        }
        return userIds;
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

    private record PipelineBatchData(
            Map<Long, Candidate> candidateMap,
            Map<Long, JobPosition> jobMap,
            Map<Long, AiMatchResult> aiMatchMap,
            Map<Long, SysUser> ownerMap,
            Map<Long, List<Interview>> interviewMap,
            Map<Long, Offer> offerMap
    ) {
    }
}
