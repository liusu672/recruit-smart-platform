package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.OfferCreateDTO;
import com.recruit.biz.dto.OfferHRQueryDTO;
import com.recruit.biz.dto.OfferQueryDTO;
import com.recruit.biz.dto.OfferUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Offer;
import com.recruit.biz.entity.Onboarding;
import com.recruit.biz.enums.JobApplicationStatus;
import com.recruit.biz.enums.InterviewRound;
import com.recruit.biz.enums.InterviewStatus;
import com.recruit.biz.enums.OfferStatus;
import com.recruit.biz.enums.OnboardingMaterialStatus;
import com.recruit.biz.enums.OnboardingStatus;
import com.recruit.biz.enums.ProcessEventType;
import com.recruit.biz.enums.ProcessRelatedType;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.OfferMapper;
import com.recruit.biz.mapper.OnboardingMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.OfferService;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.biz.vo.OfferCandidateOptionVO;
import com.recruit.biz.vo.OfferDetailVO;
import com.recruit.biz.vo.OfferHRSummaryVO;
import com.recruit.biz.vo.OfferSummaryVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OfferServiceImpl implements OfferService {

    @Resource
    private OfferMapper offerMapper;

    @Resource
    private JobApplicationMapper jobApplicationMapper;

    @Resource
    private CandidateMapper candidateMapper;

    @Resource
    private InterviewMapper interviewMapper;

    @Resource
    private InterviewFeedbackMapper interviewFeedbackMapper;

    @Resource
    private JobPositionMapper jobPositionMapper;

    @Resource
    private OnboardingMapper onboardingMapper;

    @Resource
    private ApplicationProcessEventService processEventService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOffer(OfferCreateDTO dto) {
        JobApplication application = jobApplicationMapper.selectById(
                dto.getApplicationId()
        );
        if (application == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "投递记录不存在"
            );
        }

        if (!JobApplicationStatus.INTERVIEWING.name()
                .equals(application.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "只有面试中的投递可以创建Offer"
            );
        }

        validateRequiredInterviewsCompleted(application);

        Long offerCount = offerMapper.selectCount(
                new LambdaQueryWrapper<Offer>()
                        .eq(
                                Offer::getApplicationId,
                                application.getId()
                        )
        );
        if (offerCount > 0) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "该投递记录已经存在Offer"
            );
        }

        Offer offer = new Offer();
        offer.setApplicationId(application.getId());
        offer.setSalary(dto.getSalary());
        offer.setEntryDate(dto.getEntryDate());
        offer.setProbationMonths(
                dto.getProbationMonths() == null
                        ? 3
                        : dto.getProbationMonths()
        );
        offer.setWorkLocation(dto.getWorkLocation().trim());
        offer.setStatus(OfferStatus.DRAFT.name());
        offer.setRemark(
                dto.getRemark() == null
                        ? null
                        : dto.getRemark().trim()
        );
        offer.setCreatedBy(UserContext.getUserId());

        try {
            offerMapper.insert(offer);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "该投递记录已经存在Offer"
            );
        }

        processEventService.record(
                application.getId(),
                ProcessEventType.OFFER_CREATED,
                null,
                OfferStatus.DRAFT.name(),
                "创建 Offer 草稿",
                ProcessRelatedType.OFFER,
                offer.getId()
        );

        return offer.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOffer(Long id, OfferUpdateDTO dto) {
        Offer offer = requireOffer(id);
        if (!OfferStatus.DRAFT.name().equals(offer.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "只有Offer草稿可以修改"
            );
        }

        String workLocation = dto.getWorkLocation().trim();
        String remark = dto.getRemark() == null
                ? null
                : dto.getRemark().trim();

        boolean unchanged = Objects.equals(offer.getSalary(), dto.getSalary())
                && Objects.equals(offer.getEntryDate(), dto.getEntryDate())
                && Objects.equals(
                offer.getProbationMonths(),
                dto.getProbationMonths()
        )
                && Objects.equals(offer.getWorkLocation(), workLocation)
                && Objects.equals(offer.getRemark(), remark);
        if (unchanged) {
            return;
        }

        int updated = offerMapper.update(
                null,
                new LambdaUpdateWrapper<Offer>()
                        .eq(Offer::getId, id)
                        .eq(Offer::getStatus, OfferStatus.DRAFT.name())
                        .set(Offer::getSalary, dto.getSalary())
                        .set(Offer::getEntryDate, dto.getEntryDate())
                        .set(
                                Offer::getProbationMonths,
                                dto.getProbationMonths()
                        )
                        .set(Offer::getWorkLocation, workLocation)
                        .set(Offer::getRemark, remark)
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "修改Offer失败，记录可能已被其他人处理"
            );
        }

        processEventService.record(
                offer.getApplicationId(),
                ProcessEventType.OFFER_UPDATED,
                OfferStatus.DRAFT.name(),
                OfferStatus.DRAFT.name(),
                "修改 Offer 草稿内容",
                ProcessRelatedType.OFFER,
                offer.getId()
        );
    }

    @Override
    public OfferDetailVO getDetail(Long id) {
        Offer offer = requireOffer(id);
        JobApplication application = jobApplicationMapper.selectById(
                offer.getApplicationId()
        );
        if (application == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "Offer关联的投递记录不存在"
            );
        }

        checkOfferAccess(offer, application);

        JobPosition job = jobPositionMapper.selectById(application.getJobId());
        Candidate candidate = candidateMapper.selectById(
                application.getCandidateId()
        );

        OfferDetailVO vo = new OfferDetailVO();
        vo.setId(offer.getId());
        vo.setApplicationId(offer.getApplicationId());
        vo.setApplicationStatus(application.getStatus());
        vo.setJobId(application.getJobId());
        vo.setJobTitle(job == null ? null : job.getTitle());
        vo.setDepartment(job == null ? null : job.getDepartment());
        vo.setCandidateId(application.getCandidateId());
        vo.setCandidateName(candidate == null ? null : candidate.getName());
        vo.setPhone(candidate == null ? null : candidate.getPhone());
        vo.setEmail(candidate == null ? null : candidate.getEmail());
        vo.setSalary(offer.getSalary());
        vo.setEntryDate(offer.getEntryDate());
        vo.setProbationMonths(offer.getProbationMonths());
        vo.setWorkLocation(offer.getWorkLocation());
        vo.setStatus(offer.getStatus());
        vo.setRemark(offer.getRemark());
        vo.setSentAt(offer.getSentAt());
        vo.setAcceptedAt(offer.getAcceptedAt());
        vo.setCreatedAt(offer.getCreatedAt());
        vo.setUpdatedAt(offer.getUpdatedAt());

        OfferStatus status = OfferStatus.fromCode(offer.getStatus());
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendOffer(Long id) {
        Offer offer = requireOffer(id);
        if (!OfferStatus.DRAFT.name().equals(offer.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "只有Offer草稿可以发送"
            );
        }

        JobApplication application = jobApplicationMapper.selectById(
                offer.getApplicationId()
        );
        if (application == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "Offer关联的投递记录不存在"
            );
        }
        if (!JobApplicationStatus.INTERVIEWING.name()
                .equals(application.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "当前投递状态不能发送Offer"
            );
        }

        validateRequiredInterviewsCompleted(application);

        LocalDateTime now = LocalDateTime.now();
        int offerUpdated = offerMapper.update(
                null,
                new LambdaUpdateWrapper<Offer>()
                        .eq(Offer::getId, id)
                        .eq(Offer::getStatus, OfferStatus.DRAFT.name())
                        .set(Offer::getStatus, OfferStatus.SENT.name())
                        .set(Offer::getSentAt, now)
        );
        if (offerUpdated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "发送Offer失败，记录可能已被其他人处理"
            );
        }

        int applicationUpdated = jobApplicationMapper.update(
                null,
                new LambdaUpdateWrapper<JobApplication>()
                        .eq(JobApplication::getId, application.getId())
                        .eq(
                                JobApplication::getStatus,
                                JobApplicationStatus.INTERVIEWING.name()
                        )
                        .set(
                                JobApplication::getStatus,
                                JobApplicationStatus.OFFERED.name()
                        )
                        .set(
                                JobApplication::getReviewedBy,
                                UserContext.getUserId()
                        )
                        .set(JobApplication::getReviewedAt, now)
        );
        if (applicationUpdated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "更新投递状态失败，记录可能已被其他人处理"
            );
        }

        processEventService.record(
                application.getId(),
                ProcessEventType.OFFER_SENT,
                OfferStatus.DRAFT.name(),
                OfferStatus.SENT.name(),
                "Offer 已发送给候选人",
                ProcessRelatedType.OFFER,
                offer.getId()
        );
    }

    @Override
    public PageResult<OfferSummaryVO> listMyOffers(OfferQueryDTO dto) {
        Candidate candidate = getCurrentCandidate();
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

        OfferQueryDTO query = dto == null ? new OfferQueryDTO() : dto;
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1
                ? 1
                : query.getPageNum();
        int pageSize = query.getPageSize() == null
                || query.getPageSize() < 1
                || query.getPageSize() > 100
                ? 10
                : query.getPageSize();

        LambdaQueryWrapper<Offer> wrapper =
                new LambdaQueryWrapper<Offer>()
                        .in(Offer::getApplicationId, applicationIds)
                        .ne(Offer::getStatus, OfferStatus.DRAFT.name())
                        .orderByDesc(Offer::getSentAt)
                        .orderByDesc(Offer::getId);
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(Offer::getStatus, query.getStatus());
        }

        Page<Offer> result = offerMapper.selectPage(
                new Page<>(pageNum, pageSize),
                wrapper
        );
        List<Offer> offers = result.getRecords();
        if (offers.isEmpty()) {
            return new PageResult<>(result.getTotal(), List.of());
        }

        Set<Long> resultApplicationIds = offers.stream()
                .map(Offer::getApplicationId)
                .collect(Collectors.toSet());
        Map<Long, JobApplication> applicationMap =
                jobApplicationMapper.selectBatchIds(resultApplicationIds)
                        .stream()
                        .collect(Collectors.toMap(
                                JobApplication::getId,
                                Function.identity()
                        ));
        Set<Long> jobIds = applicationMap.values().stream()
                .map(JobApplication::getJobId)
                .collect(Collectors.toSet());
        Map<Long, JobPosition> jobMap = jobIds.isEmpty()
                ? Map.of()
                : jobPositionMapper.selectBatchIds(jobIds)
                .stream()
                .collect(Collectors.toMap(
                        JobPosition::getId,
                        Function.identity()
                ));

        List<OfferSummaryVO> records = offers.stream()
                .map(offer -> toSummaryVO(
                        offer,
                        applicationMap.get(offer.getApplicationId()),
                        jobMap
                ))
                .toList();

        return new PageResult<>(result.getTotal(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptOffer(Long id) {
        Offer offer = requireOffer(id);
        JobApplication application = requireOwnedApplication(offer);

        if (OfferStatus.ACCEPTED.name().equals(offer.getStatus())) {
            return;
        }
        if (!OfferStatus.SENT.name().equals(offer.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "只有已发送的Offer可以接受"
            );
        }
        if (!JobApplicationStatus.OFFERED.name()
                .equals(application.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "当前投递状态不能接受Offer"
            );
        }

        Long activeOnboardingCount = onboardingMapper.selectCount(
                new LambdaQueryWrapper<Onboarding>()
                        .eq(
                                Onboarding::getCandidateId,
                                application.getCandidateId()
                        )
                        .ne(
                                Onboarding::getStatus,
                                OnboardingStatus.CANCELED.name()
                        )
        );
        if (activeOnboardingCount > 0) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "当前候选人已经存在有效入职流程"
            );
        }

        Long onboardingCount = onboardingMapper.selectCount(
                new LambdaQueryWrapper<Onboarding>()
                        .eq(Onboarding::getOfferId, offer.getId())
        );
        if (onboardingCount > 0) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "该Offer已经存在入职记录"
            );
        }

        int updated = offerMapper.update(
                null,
                new LambdaUpdateWrapper<Offer>()
                        .eq(Offer::getId, id)
                        .eq(Offer::getStatus, OfferStatus.SENT.name())
                        .set(Offer::getStatus, OfferStatus.ACCEPTED.name())
                        .set(Offer::getAcceptedAt, LocalDateTime.now())
        );
        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "接受Offer失败，记录可能已被其他人处理"
            );
        }

        Onboarding onboarding = new Onboarding();
        onboarding.setOfferId(offer.getId());
        onboarding.setCandidateId(application.getCandidateId());
        onboarding.setStatus(OnboardingStatus.PENDING.name());
        onboarding.setCurrentStep("材料提交");
        onboarding.setMaterialStatus(
                OnboardingMaterialStatus.PENDING.name()
        );

        try {
            onboardingMapper.insert(onboarding);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "该Offer已经存在入职记录"
            );
        }

        processEventService.record(
                application.getId(),
                ProcessEventType.OFFER_ACCEPTED,
                OfferStatus.SENT.name(),
                OfferStatus.ACCEPTED.name(),
                "候选人确认接受 Offer",
                ProcessRelatedType.OFFER,
                offer.getId()
        );
        processEventService.record(
                application.getId(),
                ProcessEventType.ONBOARDING_CREATED,
                null,
                OnboardingStatus.PENDING.name(),
                "接受 Offer 后自动创建入职流程",
                ProcessRelatedType.ONBOARDING,
                onboarding.getId()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectOffer(Long id) {
        Offer offer = requireOffer(id);
        JobApplication application = requireOwnedApplication(offer);

        if (OfferStatus.REJECTED.name().equals(offer.getStatus())) {
            return;
        }
        if (!OfferStatus.SENT.name().equals(offer.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "只有已发送的Offer可以拒绝"
            );
        }
        if (!JobApplicationStatus.OFFERED.name()
                .equals(application.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "当前投递状态不能拒绝Offer"
            );
        }

        int offerUpdated = offerMapper.update(
                null,
                new LambdaUpdateWrapper<Offer>()
                        .eq(Offer::getId, id)
                        .eq(Offer::getStatus, OfferStatus.SENT.name())
                        .set(Offer::getStatus, OfferStatus.REJECTED.name())
        );
        if (offerUpdated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "拒绝Offer失败，记录可能已被其他人处理"
            );
        }

        int applicationUpdated = jobApplicationMapper.update(
                null,
                new LambdaUpdateWrapper<JobApplication>()
                        .eq(JobApplication::getId, application.getId())
                        .eq(
                                JobApplication::getStatus,
                                JobApplicationStatus.OFFERED.name()
                        )
                        .set(
                                JobApplication::getStatus,
                                JobApplicationStatus.REJECTED.name()
                        )
                        .set(
                                JobApplication::getRejectReasonCode,
                                "OFFER_DECLINED"
                        )
                        .set(
                                JobApplication::getRejectReason,
                                "候选人拒绝Offer"
                        )
        );
        if (applicationUpdated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "更新投递状态失败，记录可能已被其他人处理"
            );
        }

        processEventService.record(
                application.getId(),
                ProcessEventType.OFFER_REJECTED,
                OfferStatus.SENT.name(),
                OfferStatus.REJECTED.name(),
                "候选人拒绝 Offer",
                ProcessRelatedType.OFFER,
                offer.getId()
        );
    }

    @Override
    public PageResult<OfferHRSummaryVO> listOffers(OfferHRQueryDTO dto) {
        OfferHRQueryDTO query = dto == null ? new OfferHRQueryDTO() : dto;
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1
                ? 1
                : query.getPageNum();
        int pageSize = query.getPageSize() == null
                || query.getPageSize() < 1
                || query.getPageSize() > 100
                ? 10
                : query.getPageSize();

        LambdaQueryWrapper<Offer> offerWrapper =
                new LambdaQueryWrapper<Offer>()
                        .orderByDesc(Offer::getCreatedAt)
                        .orderByDesc(Offer::getId);
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            offerWrapper.eq(Offer::getStatus, query.getStatus());
        }

        boolean hasApplicationFilter = query.getJobId() != null
                || query.getCandidateKeyword() != null
                && !query.getCandidateKeyword().isBlank();
        if (hasApplicationFilter) {
            LambdaQueryWrapper<JobApplication> applicationWrapper =
                    new LambdaQueryWrapper<>();

            if (query.getJobId() != null) {
                applicationWrapper.eq(
                        JobApplication::getJobId,
                        query.getJobId()
                );
            }

            if (query.getCandidateKeyword() != null
                    && !query.getCandidateKeyword().isBlank()) {
                String keyword = query.getCandidateKeyword().trim();
                Set<Long> candidateIds = candidateMapper.selectList(
                                new LambdaQueryWrapper<Candidate>()
                                        .and(condition -> condition
                                                .like(
                                                        Candidate::getName,
                                                        keyword
                                                )
                                                .or()
                                                .like(
                                                        Candidate::getPhone,
                                                        keyword
                                                )
                                                .or()
                                                .like(
                                                        Candidate::getEmail,
                                                        keyword
                                                )
                                        )
                        )
                        .stream()
                        .map(Candidate::getId)
                        .collect(Collectors.toSet());

                if (candidateIds.isEmpty()) {
                    return new PageResult<>(0L, List.of());
                }
                applicationWrapper.in(
                        JobApplication::getCandidateId,
                        candidateIds
                );
            }

            Set<Long> applicationIds = jobApplicationMapper.selectList(
                            applicationWrapper
                    )
                    .stream()
                    .map(JobApplication::getId)
                    .collect(Collectors.toSet());
            if (applicationIds.isEmpty()) {
                return new PageResult<>(0L, List.of());
            }
            offerWrapper.in(Offer::getApplicationId, applicationIds);
        }

        Page<Offer> result = offerMapper.selectPage(
                new Page<>(pageNum, pageSize),
                offerWrapper
        );
        List<Offer> offers = result.getRecords();
        if (offers.isEmpty()) {
            return new PageResult<>(result.getTotal(), List.of());
        }

        Set<Long> applicationIds = offers.stream()
                .map(Offer::getApplicationId)
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

        List<OfferHRSummaryVO> records = offers.stream()
                .map(offer -> toHRSummaryVO(
                        offer,
                        applicationMap.get(offer.getApplicationId()),
                        jobMap,
                        candidateMap
                ))
                .toList();

        return new PageResult<>(result.getTotal(), records);
    }

    @Override
    public List<OfferCandidateOptionVO> listEligibleApplications() {
        List<JobApplication> applications = jobApplicationMapper.selectList(
                new LambdaQueryWrapper<JobApplication>()
                        .eq(
                                JobApplication::getStatus,
                                JobApplicationStatus.INTERVIEWING.name()
                        )
                        .orderByDesc(JobApplication::getReviewedAt)
                        .orderByDesc(JobApplication::getUpdatedAt)
                        .orderByDesc(JobApplication::getId)
        );
        if (applications.isEmpty()) {
            return List.of();
        }

        Set<Long> applicationIds = applications.stream()
                .map(JobApplication::getId)
                .collect(Collectors.toSet());
        Set<Long> applicationIdsWithOffer = offerMapper.selectList(
                        new LambdaQueryWrapper<Offer>()
                                .in(Offer::getApplicationId, applicationIds)
                )
                .stream()
                .map(Offer::getApplicationId)
                .collect(Collectors.toSet());

        Set<Long> jobIds = applications.stream()
                .map(JobApplication::getJobId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, JobPosition> jobMap = jobIds.isEmpty()
                ? Map.of()
                : jobPositionMapper.selectBatchIds(jobIds)
                .stream()
                .collect(Collectors.toMap(JobPosition::getId, Function.identity()));

        Set<Long> candidateIds = applications.stream()
                .map(JobApplication::getCandidateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Candidate> candidateMap = candidateIds.isEmpty()
                ? Map.of()
                : candidateMapper.selectBatchIds(candidateIds)
                .stream()
                .collect(Collectors.toMap(Candidate::getId, Function.identity()));

        List<Interview> completedInterviews = interviewMapper.selectList(
                new LambdaQueryWrapper<Interview>()
                        .in(Interview::getApplicationId, applicationIds)
                        .eq(Interview::getStatus, InterviewStatus.COMPLETED.name())
        );
        Map<Long, List<Interview>> completedInterviewMap = completedInterviews
                .stream()
                .collect(Collectors.groupingBy(Interview::getApplicationId));

        Set<Long> completedInterviewIds = completedInterviews.stream()
                .map(Interview::getId)
                .collect(Collectors.toSet());
        Map<Long, InterviewFeedback> submittedFeedbackMap =
                completedInterviewIds.isEmpty()
                        ? Map.of()
                        : interviewFeedbackMapper.selectList(
                                new LambdaQueryWrapper<InterviewFeedback>()
                                        .in(
                                                InterviewFeedback::getInterviewId,
                                                completedInterviewIds
                                        )
                                        .eq(
                                                InterviewFeedback::getState,
                                                "SUBMITTED"
                                        )
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                InterviewFeedback::getInterviewId,
                                Function.identity(),
                                (left, right) -> left
                        ));

        return applications.stream()
                .filter(application ->
                        !applicationIdsWithOffer.contains(application.getId()))
                .map(application -> toOfferCandidateOption(
                        application,
                        jobMap.get(application.getJobId()),
                        candidateMap.get(application.getCandidateId()),
                        completedInterviewMap.getOrDefault(
                                application.getId(),
                                List.of()
                        ),
                        submittedFeedbackMap
                ))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeOffer(Long id) {
        Offer offer = requireOffer(id);
        if (OfferStatus.REVOKED.name().equals(offer.getStatus())) {
            return;
        }
        if (!OfferStatus.SENT.name().equals(offer.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "只有已发送的Offer可以撤回"
            );
        }

        JobApplication application = jobApplicationMapper.selectById(
                offer.getApplicationId()
        );
        if (application == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "Offer关联的投递记录不存在"
            );
        }
        if (!JobApplicationStatus.OFFERED.name()
                .equals(application.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "当前投递状态不能撤回Offer"
            );
        }

        int offerUpdated = offerMapper.update(
                null,
                new LambdaUpdateWrapper<Offer>()
                        .eq(Offer::getId, id)
                        .eq(Offer::getStatus, OfferStatus.SENT.name())
                        .set(Offer::getStatus, OfferStatus.REVOKED.name())
        );
        if (offerUpdated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "撤回Offer失败，记录可能已被其他人处理"
            );
        }

        LocalDateTime now = LocalDateTime.now();
        int applicationUpdated = jobApplicationMapper.update(
                null,
                new LambdaUpdateWrapper<JobApplication>()
                        .eq(JobApplication::getId, application.getId())
                        .eq(
                                JobApplication::getStatus,
                                JobApplicationStatus.OFFERED.name()
                        )
                        .set(
                                JobApplication::getStatus,
                                JobApplicationStatus.REJECTED.name()
                        )
                        .set(
                                JobApplication::getRejectReasonCode,
                                "OFFER_REVOKED"
                        )
                        .set(
                                JobApplication::getRejectReason,
                                "HR撤回Offer"
                        )
                        .set(
                                JobApplication::getReviewedBy,
                                UserContext.getUserId()
                        )
                        .set(JobApplication::getReviewedAt, now)
        );
        if (applicationUpdated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "更新投递状态失败，记录可能已被其他人处理"
            );
        }

        processEventService.record(
                application.getId(),
                ProcessEventType.OFFER_REVOKED,
                OfferStatus.SENT.name(),
                OfferStatus.REVOKED.name(),
                "HR 撤回 Offer",
                ProcessRelatedType.OFFER,
                offer.getId()
        );
    }

    private OfferHRSummaryVO toHRSummaryVO(
            Offer offer,
            JobApplication application,
            Map<Long, JobPosition> jobMap,
            Map<Long, Candidate> candidateMap
    ) {
        JobPosition job = application == null
                ? null
                : jobMap.get(application.getJobId());
        Candidate candidate = application == null
                ? null
                : candidateMap.get(application.getCandidateId());

        OfferHRSummaryVO vo = new OfferHRSummaryVO();
        vo.setId(offer.getId());
        vo.setApplicationId(offer.getApplicationId());
        if (application != null) {
            vo.setJobId(application.getJobId());
            vo.setCandidateId(application.getCandidateId());
        }
        if (job != null) {
            vo.setJobTitle(job.getTitle());
            vo.setDepartment(job.getDepartment());
        }
        if (candidate != null) {
            vo.setCandidateName(candidate.getName());
            vo.setPhone(candidate.getPhone());
            vo.setEmail(candidate.getEmail());
        }
        vo.setSalary(offer.getSalary());
        vo.setEntryDate(offer.getEntryDate());
        vo.setWorkLocation(offer.getWorkLocation());
        vo.setStatus(offer.getStatus());
        vo.setSentAt(offer.getSentAt());
        vo.setAcceptedAt(offer.getAcceptedAt());
        vo.setCreatedAt(offer.getCreatedAt());

        OfferStatus status = OfferStatus.fromCode(offer.getStatus());
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );

        return vo;
    }

    private OfferSummaryVO toSummaryVO(
            Offer offer,
            JobApplication application,
            Map<Long, JobPosition> jobMap
    ) {
        JobPosition job = application == null
                ? null
                : jobMap.get(application.getJobId());

        OfferSummaryVO vo = new OfferSummaryVO();
        vo.setId(offer.getId());
        vo.setApplicationId(offer.getApplicationId());
        if (application != null) {
            vo.setJobId(application.getJobId());
        }
        if (job != null) {
            vo.setJobTitle(job.getTitle());
            vo.setDepartment(job.getDepartment());
        }
        vo.setSalary(offer.getSalary());
        vo.setEntryDate(offer.getEntryDate());
        vo.setWorkLocation(offer.getWorkLocation());
        vo.setStatus(offer.getStatus());
        vo.setSentAt(offer.getSentAt());
        vo.setAcceptedAt(offer.getAcceptedAt());

        OfferStatus status = OfferStatus.fromCode(offer.getStatus());
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );

        return vo;
    }

    private OfferCandidateOptionVO toOfferCandidateOption(
            JobApplication application,
            JobPosition job,
            Candidate candidate,
            List<Interview> completedInterviews,
            Map<Long, InterviewFeedback> submittedFeedbackMap
    ) {
        if (job == null || candidate == null) {
            return null;
        }
        int requiredRounds = job.getRequiredInterviewRounds() == null
                ? 1
                : job.getRequiredInterviewRounds();
        List<Interview> requiredCompletedInterviews = completedInterviews
                .stream()
                .filter(interview -> isRequiredCompletedWithFeedback(
                        interview,
                        requiredRounds,
                        submittedFeedbackMap
                ))
                .toList();
        if (!hasAllRequiredRounds(
                requiredCompletedInterviews,
                requiredRounds
        )) {
            return null;
        }

        List<InterviewFeedback> submittedFeedbacks = requiredCompletedInterviews
                .stream()
                .map(interview -> submittedFeedbackMap.get(interview.getId()))
                .filter(Objects::nonNull)
                .toList();
        Integer averageScore = submittedFeedbacks.stream()
                .map(InterviewFeedback::getScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .stream()
                .mapToInt(score -> (int) Math.round(score))
                .findFirst()
                .orElse(0);
        Interview latestRequiredInterview = requiredCompletedInterviews.stream()
                .max(Comparator.comparingInt(interview -> {
                    InterviewRound round = InterviewRound.fromCode(
                            interview.getRound()
                    );
                    return round == null ? 0 : round.getOrder();
                }))
                .orElse(null);
        InterviewFeedback latestFeedback = latestRequiredInterview == null
                ? null
                : submittedFeedbackMap.get(latestRequiredInterview.getId());

        OfferCandidateOptionVO vo = new OfferCandidateOptionVO();
        vo.setApplicationId(application.getId());
        vo.setCandidateId(application.getCandidateId());
        vo.setCandidateName(candidate.getName());
        vo.setJobId(application.getJobId());
        vo.setJobTitle(job.getTitle());
        vo.setDepartment(job.getDepartment());
        vo.setInterviewScore(averageScore);
        vo.setInterviewSuggestion(
                latestFeedback == null ? null : latestFeedback.getSuggestion()
        );
        return vo;
    }

    private boolean isRequiredCompletedWithFeedback(
            Interview interview,
            int requiredRounds,
            Map<Long, InterviewFeedback> submittedFeedbackMap
    ) {
        InterviewRound round = InterviewRound.fromCode(interview.getRound());
        return round != null
                && round.getOrder() <= requiredRounds
                && submittedFeedbackMap.containsKey(interview.getId());
    }

    private boolean hasAllRequiredRounds(
            List<Interview> completedInterviews,
            int requiredRounds
    ) {
        Set<Integer> completedRoundOrders = completedInterviews.stream()
                .map(interview -> InterviewRound.fromCode(interview.getRound()))
                .filter(Objects::nonNull)
                .map(InterviewRound::getOrder)
                .collect(Collectors.toSet());
        for (int order = 1; order <= requiredRounds; order++) {
            if (!completedRoundOrders.contains(order)) {
                return false;
            }
        }
        return true;
    }

    private Offer requireOffer(Long id) {
        Offer offer = offerMapper.selectById(id);
        if (offer == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "Offer不存在"
            );
        }
        return offer;
    }

    private void validateRequiredInterviewsCompleted(
            JobApplication application
    ) {
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
        List<Interview> completedInterviews = interviewMapper.selectList(
                new LambdaQueryWrapper<Interview>()
                        .eq(
                                Interview::getApplicationId,
                                application.getId()
                        )
                        .eq(
                                Interview::getStatus,
                                InterviewStatus.COMPLETED.name()
                        )
        );
        if (completedInterviews.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "请先完成该职位要求的全部面试"
            );
        }

        Set<Long> completedInterviewIds = completedInterviews.stream()
                .map(Interview::getId)
                .collect(Collectors.toSet());
        Set<Long> submittedFeedbackInterviewIds = interviewFeedbackMapper
                .selectList(
                        new LambdaQueryWrapper<InterviewFeedback>()
                                .in(
                                        InterviewFeedback::getInterviewId,
                                        completedInterviewIds
                                )
                                .eq(
                                        InterviewFeedback::getState,
                                        "SUBMITTED"
                                )
                )
                .stream()
                .map(InterviewFeedback::getInterviewId)
                .collect(Collectors.toSet());

        for (int order = 1; order <= requiredRounds; order++) {
            InterviewRound requiredRound = InterviewRound.fromOrder(order);
            boolean completedWithFeedback = completedInterviews.stream()
                    .anyMatch(interview ->
                            requiredRound.name().equals(interview.getRound())
                                    && submittedFeedbackInterviewIds.contains(
                                    interview.getId()
                            )
                    );
            if (!completedWithFeedback) {
                throw new BusinessException(
                        ErrorCode.PARAM_ERROR,
                        requiredRound.getDescription()
                                + "尚未完成或反馈尚未提交"
                );
            }
        }
    }

    private Candidate getCurrentCandidate() {
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
        return candidate;
    }

    private JobApplication requireOwnedApplication(Offer offer) {
        JobApplication application = jobApplicationMapper.selectById(
                offer.getApplicationId()
        );
        if (application == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "Offer关联的投递记录不存在"
            );
        }

        Candidate candidate = getCurrentCandidate();
        if (!candidate.getId().equals(application.getCandidateId())) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "无权操作该Offer"
            );
        }

        return application;
    }

    private void checkOfferAccess(
            Offer offer,
            JobApplication application
    ) {
        String roleCode = UserContext.getRoleCode();
        if ("ADMIN".equals(roleCode) || "HR".equals(roleCode)) {
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
            boolean ownsOffer = candidate != null
                    && candidate.getId().equals(application.getCandidateId());
            boolean visibleStatus = !OfferStatus.DRAFT.name()
                    .equals(offer.getStatus());

            if (ownsOffer && visibleStatus) {
                return;
            }
        }

        throw new BusinessException(
                ErrorCode.FORBIDDEN,
                "无权访问该Offer"
        );
    }
}
