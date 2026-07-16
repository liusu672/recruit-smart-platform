package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.OnboardingMaterialRejectDTO;
import com.recruit.biz.dto.OnboardingCancelDTO;
import com.recruit.biz.dto.OnboardingQueryDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.EmployeeProfile;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Offer;
import com.recruit.biz.entity.Onboarding;
import com.recruit.biz.enums.CandidateStatus;
import com.recruit.biz.enums.EmployeeStatus;
import com.recruit.biz.enums.JobApplicationStatus;
import com.recruit.biz.enums.OfferStatus;
import com.recruit.biz.enums.OnboardingMaterialStatus;
import com.recruit.biz.enums.OnboardingStatus;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.EmployeeProfileMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.OfferMapper;
import com.recruit.biz.mapper.OnboardingMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.OnboardingService;
import com.recruit.biz.vo.OnboardingDetailVO;
import com.recruit.biz.vo.OnboardingHRSummaryVO;
import com.recruit.biz.vo.OnboardingSummaryVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OnboardingServiceImpl implements OnboardingService {

    @Resource
    private OnboardingMapper onboardingMapper;

    @Resource
    private CandidateMapper candidateMapper;

    @Resource
    private OfferMapper offerMapper;

    @Resource
    private JobApplicationMapper jobApplicationMapper;

    @Resource
    private JobPositionMapper jobPositionMapper;

    @Resource
    private EmployeeProfileMapper employeeProfileMapper;

    @Override
    public List<OnboardingSummaryVO> listMyOnboarding() {
        Candidate candidate = getCurrentCandidate();
        List<Onboarding> onboardingList = onboardingMapper.selectList(
                new LambdaQueryWrapper<Onboarding>()
                        .eq(Onboarding::getCandidateId, candidate.getId())
                        .orderByDesc(Onboarding::getCreatedAt)
                        .orderByDesc(Onboarding::getId)
        );

        if (onboardingList.isEmpty()) {
            return List.of();
        }

        Set<Long> offerIds = onboardingList.stream()
                .map(Onboarding::getOfferId)
                .collect(Collectors.toSet());
        Map<Long, Offer> offerMap = offerMapper.selectBatchIds(offerIds)
                .stream()
                .collect(Collectors.toMap(
                        Offer::getId,
                        Function.identity()
                ));

        Set<Long> applicationIds = offerMap.values().stream()
                .map(Offer::getApplicationId)
                .collect(Collectors.toSet());
        Map<Long, JobApplication> applicationMap = applicationIds.isEmpty()
                ? Map.of()
                : jobApplicationMapper.selectBatchIds(applicationIds)
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

        return onboardingList.stream()
                .map(onboarding -> toSummaryVO(
                        onboarding,
                        offerMap,
                        applicationMap,
                        jobMap
                ))
                .toList();
    }

    @Override
    public OnboardingDetailVO getDetail(Long id) {
        Onboarding onboarding = onboardingMapper.selectById(id);
        if (onboarding == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "入职流程不存在"
            );
        }

        checkAccess(onboarding);

        Offer offer = offerMapper.selectById(onboarding.getOfferId());
        JobApplication application = offer == null
                ? null
                : jobApplicationMapper.selectById(offer.getApplicationId());
        JobPosition job = application == null
                ? null
                : jobPositionMapper.selectById(application.getJobId());
        Candidate candidate = candidateMapper.selectById(
                onboarding.getCandidateId()
        );

        OnboardingDetailVO vo = new OnboardingDetailVO();
        vo.setId(onboarding.getId());
        vo.setOfferId(onboarding.getOfferId());
        if (offer != null) {
            vo.setOfferStatus(offer.getStatus());
            vo.setApplicationId(offer.getApplicationId());
            vo.setSalary(offer.getSalary());
            vo.setEntryDate(offer.getEntryDate());
            vo.setProbationMonths(offer.getProbationMonths());
            vo.setWorkLocation(offer.getWorkLocation());
        }
        if (application != null) {
            vo.setApplicationStatus(application.getStatus());
            vo.setJobId(application.getJobId());
        }
        if (job != null) {
            vo.setJobTitle(job.getTitle());
            vo.setDepartment(job.getDepartment());
        }
        vo.setCandidateId(onboarding.getCandidateId());
        if (candidate != null) {
            vo.setCandidateName(candidate.getName());
            vo.setPhone(candidate.getPhone());
            vo.setEmail(candidate.getEmail());
        }
        vo.setStatus(onboarding.getStatus());
        vo.setCurrentStep(onboarding.getCurrentStep());
        vo.setMaterialStatus(onboarding.getMaterialStatus());
        vo.setRemark(onboarding.getRemark());
        vo.setCompletedAt(onboarding.getCompletedAt());
        vo.setCreatedAt(onboarding.getCreatedAt());
        vo.setUpdatedAt(onboarding.getUpdatedAt());
        fillStatusText(
                onboarding.getStatus(),
                onboarding.getMaterialStatus(),
                vo
        );

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitMaterials(Long id) {
        if (!"CANDIDATE".equals(UserContext.getRoleCode())) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "只有候选人可以提交入职材料"
            );
        }

        Onboarding onboarding = requireOnboarding(id);
        checkAccess(onboarding);

        boolean canSubmit = OnboardingStatus.PENDING.name()
                .equals(onboarding.getStatus())
                && (OnboardingMaterialStatus.PENDING.name()
                .equals(onboarding.getMaterialStatus())
                || OnboardingMaterialStatus.REJECTED.name()
                .equals(onboarding.getMaterialStatus()));
        if (!canSubmit) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "当前入职流程不能提交材料"
            );
        }

        int updated = onboardingMapper.update(
                null,
                new LambdaUpdateWrapper<Onboarding>()
                        .eq(Onboarding::getId, id)
                        .eq(
                                Onboarding::getStatus,
                                OnboardingStatus.PENDING.name()
                        )
                        .in(
                                Onboarding::getMaterialStatus,
                                OnboardingMaterialStatus.PENDING.name(),
                                OnboardingMaterialStatus.REJECTED.name()
                        )
                        .set(
                                Onboarding::getStatus,
                                OnboardingStatus.REVIEWING.name()
                        )
                        .set(
                                Onboarding::getMaterialStatus,
                                OnboardingMaterialStatus.REVIEWING.name()
                        )
                        .set(Onboarding::getCurrentStep, "HR材料审核")
                        .set(Onboarding::getRemark, null)
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "提交材料失败，记录可能已被其他人处理"
            );
        }
    }

    @Override
    public PageResult<OnboardingHRSummaryVO> listOnboarding(
            OnboardingQueryDTO dto
    ) {
        requireStaffRole();

        OnboardingQueryDTO query = dto == null
                ? new OnboardingQueryDTO()
                : dto;
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1
                ? 1
                : query.getPageNum();
        int pageSize = query.getPageSize() == null
                || query.getPageSize() < 1
                || query.getPageSize() > 100
                ? 10
                : query.getPageSize();

        LambdaQueryWrapper<Onboarding> wrapper =
                new LambdaQueryWrapper<Onboarding>()
                        .orderByDesc(Onboarding::getCreatedAt)
                        .orderByDesc(Onboarding::getId);
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(Onboarding::getStatus, query.getStatus());
        }
        if (query.getMaterialStatus() != null
                && !query.getMaterialStatus().isBlank()) {
            wrapper.eq(
                    Onboarding::getMaterialStatus,
                    query.getMaterialStatus()
            );
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
            wrapper.in(Onboarding::getCandidateId, candidateIds);
        }

        Page<Onboarding> result = onboardingMapper.selectPage(
                new Page<>(pageNum, pageSize),
                wrapper
        );
        List<Onboarding> onboardingList = result.getRecords();
        if (onboardingList.isEmpty()) {
            return new PageResult<>(result.getTotal(), List.of());
        }

        Set<Long> candidateIds = onboardingList.stream()
                .map(Onboarding::getCandidateId)
                .collect(Collectors.toSet());
        Set<Long> offerIds = onboardingList.stream()
                .map(Onboarding::getOfferId)
                .collect(Collectors.toSet());

        Map<Long, Candidate> candidateMap = candidateMapper.selectBatchIds(
                        candidateIds
                )
                .stream()
                .collect(Collectors.toMap(
                        Candidate::getId,
                        Function.identity()
                ));
        Map<Long, Offer> offerMap = offerMapper.selectBatchIds(offerIds)
                .stream()
                .collect(Collectors.toMap(
                        Offer::getId,
                        Function.identity()
                ));
        Set<Long> applicationIds = offerMap.values().stream()
                .map(Offer::getApplicationId)
                .collect(Collectors.toSet());
        Map<Long, JobApplication> applicationMap = applicationIds.isEmpty()
                ? Map.of()
                : jobApplicationMapper.selectBatchIds(applicationIds)
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

        List<OnboardingHRSummaryVO> records = onboardingList.stream()
                .map(onboarding -> toHRSummaryVO(
                        onboarding,
                        candidateMap,
                        offerMap,
                        applicationMap,
                        jobMap
                ))
                .toList();

        return new PageResult<>(result.getTotal(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveMaterials(Long id) {
        requireStaffRole();
        Onboarding onboarding = requireOnboarding(id);

        boolean canApprove = OnboardingStatus.REVIEWING.name()
                .equals(onboarding.getStatus())
                && OnboardingMaterialStatus.REVIEWING.name()
                .equals(onboarding.getMaterialStatus());
        if (!canApprove) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "当前入职流程不能审核通过"
            );
        }

        int updated = onboardingMapper.update(
                null,
                new LambdaUpdateWrapper<Onboarding>()
                        .eq(Onboarding::getId, id)
                        .eq(
                                Onboarding::getStatus,
                                OnboardingStatus.REVIEWING.name()
                        )
                        .eq(
                                Onboarding::getMaterialStatus,
                                OnboardingMaterialStatus.REVIEWING.name()
                        )
                        .set(
                                Onboarding::getStatus,
                                OnboardingStatus.APPROVED.name()
                        )
                        .set(
                                Onboarding::getMaterialStatus,
                                OnboardingMaterialStatus.APPROVED.name()
                        )
                        .set(Onboarding::getCurrentStep, "等待入职")
                        .set(Onboarding::getRemark, null)
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "材料审核通过失败，记录可能已被其他人处理"
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectMaterials(
            Long id,
            OnboardingMaterialRejectDTO dto
    ) {
        requireStaffRole();
        Onboarding onboarding = requireOnboarding(id);

        boolean canReject = OnboardingStatus.REVIEWING.name()
                .equals(onboarding.getStatus())
                && OnboardingMaterialStatus.REVIEWING.name()
                .equals(onboarding.getMaterialStatus());
        if (!canReject) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "当前入职流程不能驳回材料"
            );
        }

        int updated = onboardingMapper.update(
                null,
                new LambdaUpdateWrapper<Onboarding>()
                        .eq(Onboarding::getId, id)
                        .eq(
                                Onboarding::getStatus,
                                OnboardingStatus.REVIEWING.name()
                        )
                        .eq(
                                Onboarding::getMaterialStatus,
                                OnboardingMaterialStatus.REVIEWING.name()
                        )
                        .set(
                                Onboarding::getStatus,
                                OnboardingStatus.PENDING.name()
                        )
                        .set(
                                Onboarding::getMaterialStatus,
                                OnboardingMaterialStatus.REJECTED.name()
                        )
                        .set(Onboarding::getCurrentStep, "重新提交材料")
                        .set(
                                Onboarding::getRemark,
                                dto.getReason().trim()
                        )
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "驳回材料失败，记录可能已被其他人处理"
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOnboarding(Long id) {
        requireStaffRole();
        Onboarding onboarding = requireOnboarding(id);

        if (OnboardingStatus.ONBOARDED.name()
                .equals(onboarding.getStatus())) {
            Long employeeCount = employeeProfileMapper.selectCount(
                    new LambdaQueryWrapper<EmployeeProfile>()
                            .eq(
                                    EmployeeProfile::getOnboardingId,
                                    onboarding.getId()
                            )
            );
            if (employeeCount > 0) {
                return;
            }
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "入职流程已完成但员工档案不存在"
            );
        }

        boolean canComplete = OnboardingStatus.APPROVED.name()
                .equals(onboarding.getStatus())
                && OnboardingMaterialStatus.APPROVED.name()
                .equals(onboarding.getMaterialStatus());
        if (!canComplete) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "只有审核通过的入职流程可以完成入职"
            );
        }

        Offer offer = offerMapper.selectById(onboarding.getOfferId());
        if (offer == null
                || !OfferStatus.ACCEPTED.name().equals(offer.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "Offer不存在或候选人尚未接受"
            );
        }

        JobApplication application = jobApplicationMapper.selectById(
                offer.getApplicationId()
        );
        if (application == null
                || !JobApplicationStatus.OFFERED.name()
                .equals(application.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "投递记录不存在或当前状态不能完成入职"
            );
        }

        Candidate candidate = candidateMapper.selectById(
                onboarding.getCandidateId()
        );
        if (candidate == null
                || !candidate.getId().equals(application.getCandidateId())) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "入职流程关联的候选人信息不正确"
            );
        }

        JobPosition job = jobPositionMapper.selectById(application.getJobId());
        if (job == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "入职流程关联的职位不存在"
            );
        }

        Long employeeCount = employeeProfileMapper.selectCount(
                new LambdaQueryWrapper<EmployeeProfile>()
                        .eq(
                                EmployeeProfile::getCandidateId,
                                candidate.getId()
                        )
        );
        if (employeeCount > 0) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "该候选人已经存在员工档案"
            );
        }

        LocalDateTime now = LocalDateTime.now();
        EmployeeProfile employee = new EmployeeProfile();
        employee.setUserId(candidate.getUserId());
        employee.setCandidateId(candidate.getId());
        employee.setOnboardingId(onboarding.getId());
        employee.setEmployeeNo(generateEmployeeNo(candidate.getId()));
        employee.setName(candidate.getName());
        employee.setPhone(candidate.getPhone());
        employee.setEmail(candidate.getEmail());
        employee.setDepartment(job.getDepartment());
        employee.setPosition(job.getTitle());
        employee.setEntryDate(offer.getEntryDate());
        employee.setStatus(EmployeeStatus.PROBATION.name());

        try {
            employeeProfileMapper.insert(employee);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "该候选人已经存在员工档案"
            );
        }

        int onboardingUpdated = onboardingMapper.update(
                null,
                new LambdaUpdateWrapper<Onboarding>()
                        .eq(Onboarding::getId, onboarding.getId())
                        .eq(
                                Onboarding::getStatus,
                                OnboardingStatus.APPROVED.name()
                        )
                        .eq(
                                Onboarding::getMaterialStatus,
                                OnboardingMaterialStatus.APPROVED.name()
                        )
                        .set(
                                Onboarding::getStatus,
                                OnboardingStatus.ONBOARDED.name()
                        )
                        .set(Onboarding::getCurrentStep, "入职完成")
                        .set(Onboarding::getCompletedAt, now)
        );
        if (onboardingUpdated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "完成入职失败，流程可能已被其他人处理"
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
                                JobApplicationStatus.HIRED.name()
                        )
        );
        if (applicationUpdated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "更新投递状态失败，记录可能已被其他人处理"
            );
        }

        int candidateUpdated = candidateMapper.update(
                null,
                new LambdaUpdateWrapper<Candidate>()
                        .eq(Candidate::getId, candidate.getId())
                        .set(
                                Candidate::getCurrentStatus,
                                CandidateStatus.HIRED.name()
                        )
        );
        if (candidateUpdated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "更新候选人状态失败"
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOnboarding(Long id, OnboardingCancelDTO dto) {
        requireStaffRole();
        Onboarding onboarding = requireOnboarding(id);

        if (OnboardingStatus.CANCELED.name().equals(onboarding.getStatus())) {
            return;
        }
        if (OnboardingStatus.ONBOARDED.name().equals(onboarding.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "已完成入职的流程不能取消"
            );
        }

        Offer offer = offerMapper.selectById(onboarding.getOfferId());
        JobApplication application = offer == null
                ? null
                : jobApplicationMapper.selectById(offer.getApplicationId());
        if (offer == null
                || !OfferStatus.ACCEPTED.name().equals(offer.getStatus())
                || application == null
                || !JobApplicationStatus.OFFERED.name()
                .equals(application.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "当前入职流程状态不允许取消"
            );
        }

        int onboardingUpdated = onboardingMapper.update(
                null,
                new LambdaUpdateWrapper<Onboarding>()
                        .eq(Onboarding::getId, id)
                        .eq(Onboarding::getStatus, onboarding.getStatus())
                        .ne(
                                Onboarding::getStatus,
                                OnboardingStatus.ONBOARDED.name()
                        )
                        .set(
                                Onboarding::getStatus,
                                OnboardingStatus.CANCELED.name()
                        )
                        .set(Onboarding::getCurrentStep, "流程取消")
                        .set(
                                Onboarding::getRemark,
                                dto.getReason().trim()
                        )
        );
        if (onboardingUpdated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "取消入职流程失败，记录可能已被其他人处理"
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
                                "ONBOARDING_CANCELED"
                        )
                        .set(
                                JobApplication::getRejectReason,
                                dto.getReason().trim()
                        )
        );
        if (applicationUpdated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "更新投递状态失败，记录可能已被其他人处理"
            );
        }
    }

    private String generateEmployeeNo(Long candidateId) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "EMP" + date + String.format("%06d", candidateId);
    }

    private OnboardingSummaryVO toSummaryVO(
            Onboarding onboarding,
            Map<Long, Offer> offerMap,
            Map<Long, JobApplication> applicationMap,
            Map<Long, JobPosition> jobMap
    ) {
        Offer offer = offerMap.get(onboarding.getOfferId());
        JobApplication application = offer == null
                ? null
                : applicationMap.get(offer.getApplicationId());
        JobPosition job = application == null
                ? null
                : jobMap.get(application.getJobId());

        OnboardingSummaryVO vo = new OnboardingSummaryVO();
        vo.setId(onboarding.getId());
        vo.setOfferId(onboarding.getOfferId());
        if (offer != null) {
            vo.setApplicationId(offer.getApplicationId());
            vo.setEntryDate(offer.getEntryDate());
        }
        if (application != null) {
            vo.setJobId(application.getJobId());
        }
        if (job != null) {
            vo.setJobTitle(job.getTitle());
            vo.setDepartment(job.getDepartment());
        }
        vo.setStatus(onboarding.getStatus());
        vo.setCurrentStep(onboarding.getCurrentStep());
        vo.setMaterialStatus(onboarding.getMaterialStatus());
        vo.setCompletedAt(onboarding.getCompletedAt());
        vo.setCreatedAt(onboarding.getCreatedAt());

        OnboardingStatus status = OnboardingStatus.fromCode(
                onboarding.getStatus()
        );
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );
        OnboardingMaterialStatus materialStatus =
                OnboardingMaterialStatus.fromCode(
                        onboarding.getMaterialStatus()
                );
        vo.setMaterialStatusText(
                materialStatus == null
                        ? "未知状态"
                        : materialStatus.getDescription()
        );

        return vo;
    }

    private void fillStatusText(
            String statusCode,
            String materialStatusCode,
            OnboardingDetailVO vo
    ) {
        OnboardingStatus status = OnboardingStatus.fromCode(statusCode);
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );
        OnboardingMaterialStatus materialStatus =
                OnboardingMaterialStatus.fromCode(materialStatusCode);
        vo.setMaterialStatusText(
                materialStatus == null
                        ? "未知状态"
                        : materialStatus.getDescription()
        );
    }

    private OnboardingHRSummaryVO toHRSummaryVO(
            Onboarding onboarding,
            Map<Long, Candidate> candidateMap,
            Map<Long, Offer> offerMap,
            Map<Long, JobApplication> applicationMap,
            Map<Long, JobPosition> jobMap
    ) {
        Candidate candidate = candidateMap.get(onboarding.getCandidateId());
        Offer offer = offerMap.get(onboarding.getOfferId());
        JobApplication application = offer == null
                ? null
                : applicationMap.get(offer.getApplicationId());
        JobPosition job = application == null
                ? null
                : jobMap.get(application.getJobId());

        OnboardingHRSummaryVO vo = new OnboardingHRSummaryVO();
        vo.setId(onboarding.getId());
        vo.setOfferId(onboarding.getOfferId());
        if (offer != null) {
            vo.setApplicationId(offer.getApplicationId());
            vo.setEntryDate(offer.getEntryDate());
        }
        if (application != null) {
            vo.setJobId(application.getJobId());
        }
        if (job != null) {
            vo.setJobTitle(job.getTitle());
            vo.setDepartment(job.getDepartment());
        }
        vo.setCandidateId(onboarding.getCandidateId());
        if (candidate != null) {
            vo.setCandidateName(candidate.getName());
            vo.setPhone(candidate.getPhone());
            vo.setEmail(candidate.getEmail());
        }
        vo.setStatus(onboarding.getStatus());
        vo.setCurrentStep(onboarding.getCurrentStep());
        vo.setMaterialStatus(onboarding.getMaterialStatus());
        vo.setRemark(onboarding.getRemark());
        vo.setCompletedAt(onboarding.getCompletedAt());
        vo.setCreatedAt(onboarding.getCreatedAt());

        OnboardingStatus status = OnboardingStatus.fromCode(
                onboarding.getStatus()
        );
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );
        OnboardingMaterialStatus materialStatus =
                OnboardingMaterialStatus.fromCode(
                        onboarding.getMaterialStatus()
                );
        vo.setMaterialStatusText(
                materialStatus == null
                        ? "未知状态"
                        : materialStatus.getDescription()
        );

        return vo;
    }

    private Onboarding requireOnboarding(Long id) {
        Onboarding onboarding = onboardingMapper.selectById(id);
        if (onboarding == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "入职流程不存在"
            );
        }
        return onboarding;
    }

    private void requireStaffRole() {
        String roleCode = UserContext.getRoleCode();
        if (!"ADMIN".equals(roleCode) && !"HR".equals(roleCode)) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "没有入职流程管理权限"
            );
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

    private void checkAccess(Onboarding onboarding) {
        String roleCode = UserContext.getRoleCode();
        if ("ADMIN".equals(roleCode) || "HR".equals(roleCode)) {
            return;
        }

        if ("CANDIDATE".equals(roleCode)) {
            Candidate candidate = getCurrentCandidate();
            if (candidate.getId().equals(onboarding.getCandidateId())) {
                return;
            }
        }

        throw new BusinessException(
                ErrorCode.FORBIDDEN,
                "无权访问该入职流程"
        );
    }
}
