package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.OfferCreateDTO;
import com.recruit.biz.dto.OfferHRQueryDTO;
import com.recruit.biz.dto.OfferQueryDTO;
import com.recruit.biz.dto.OfferUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Offer;
import com.recruit.biz.entity.Onboarding;
import com.recruit.biz.enums.JobApplicationStatus;
import com.recruit.biz.enums.OfferStatus;
import com.recruit.biz.enums.OnboardingMaterialStatus;
import com.recruit.biz.enums.OnboardingStatus;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.OfferMapper;
import com.recruit.biz.mapper.OnboardingMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.OfferService;
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
    private JobPositionMapper jobPositionMapper;

    @Resource
    private OnboardingMapper onboardingMapper;

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

        return offer.getId();
    }

    @Override
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
