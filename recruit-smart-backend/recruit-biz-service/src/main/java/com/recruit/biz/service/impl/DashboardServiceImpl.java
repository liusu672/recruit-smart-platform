package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Offer;
import com.recruit.biz.entity.Onboarding;
import com.recruit.biz.enums.DashboardTaskType;
import com.recruit.biz.enums.JobApplicationStatus;
import com.recruit.biz.enums.OfferStatus;
import com.recruit.biz.enums.OnboardingMaterialStatus;
import com.recruit.biz.enums.OnboardingStatus;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.DashboardMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.OfferMapper;
import com.recruit.biz.mapper.OnboardingMapper;
import com.recruit.biz.service.DashboardService;
import com.recruit.biz.vo.DashboardMetricsVO;
import com.recruit.biz.vo.DashboardOverviewVO;
import com.recruit.biz.vo.DashboardTaskVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int TASK_LIMIT = 10;

    @Resource
    private DashboardMapper dashboardMapper;
    @Resource
    private JobApplicationMapper jobApplicationMapper;
    @Resource
    private OfferMapper offerMapper;
    @Resource
    private OnboardingMapper onboardingMapper;
    @Resource
    private CandidateMapper candidateMapper;
    @Resource
    private JobPositionMapper jobPositionMapper;

    @Override
    public DashboardOverviewVO getOverview() {
        DashboardOverviewVO overview = new DashboardOverviewVO();
        overview.setMetrics(loadMetrics());

        List<JobApplication> screeningApplications =
                loadScreeningApplications();
        List<Interview> pendingFeedbackInterviews =
                dashboardMapper.selectPendingFeedbackInterviews(TASK_LIMIT);
        List<Offer> activeOffers = loadActiveOffers();
        List<Onboarding> reviewingOnboardings =
                loadReviewingOnboardings();

        Map<Long, Offer> offerMap = loadOfferMap(
                activeOffers,
                reviewingOnboardings
        );
        Map<Long, JobApplication> applicationMap = loadApplicationMap(
                screeningApplications,
                pendingFeedbackInterviews,
                offerMap.values()
        );
        Map<Long, Candidate> candidateMap = loadCandidateMap(
                applicationMap.values(),
                reviewingOnboardings
        );
        Map<Long, JobPosition> jobMap = loadJobMap(
                applicationMap.values()
        );

        List<DashboardTaskVO> tasks = new ArrayList<>();
        screeningApplications.forEach(application -> tasks.add(
                screeningTask(
                        application,
                        candidateMap,
                        jobMap
                )
        ));
        pendingFeedbackInterviews.forEach(interview -> tasks.add(
                feedbackTask(
                        interview,
                        applicationMap,
                        candidateMap,
                        jobMap
                )
        ));
        activeOffers.forEach(offer -> tasks.add(
                offerTask(
                        offer,
                        applicationMap,
                        candidateMap,
                        jobMap
                )
        ));
        reviewingOnboardings.forEach(onboarding -> tasks.add(
                onboardingTask(
                        onboarding,
                        offerMap,
                        applicationMap,
                        candidateMap,
                        jobMap
                )
        ));

        overview.setTasks(tasks.stream()
                .sorted(Comparator
                        .comparing(
                                DashboardTaskVO::getOccurredAt,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                        .thenComparing(
                                DashboardTaskVO::getRelatedId,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        ))
                .limit(TASK_LIMIT)
                .toList());
        return overview;
    }

    private DashboardMetricsVO loadMetrics() {
        DashboardMetricsVO metrics = new DashboardMetricsVO();
        metrics.setPendingScreening(zeroIfNull(
                dashboardMapper.countPendingScreening()
        ));
        metrics.setPendingFeedback(zeroIfNull(
                dashboardMapper.countPendingFeedback()
        ));
        metrics.setActiveOffers(zeroIfNull(
                dashboardMapper.countActiveOffers()
        ));
        metrics.setReviewingOnboardings(zeroIfNull(
                dashboardMapper.countReviewingOnboardings()
        ));
        return metrics;
    }

    private List<JobApplication> loadScreeningApplications() {
        return jobApplicationMapper.selectList(
                new LambdaQueryWrapper<JobApplication>()
                        .in(
                                JobApplication::getStatus,
                                JobApplicationStatus.SUBMITTED.name(),
                                JobApplicationStatus.SCREENING.name()
                        )
                        .orderByDesc(JobApplication::getAppliedAt)
                        .orderByDesc(JobApplication::getId)
                        .last("LIMIT " + TASK_LIMIT)
        );
    }

    private List<Offer> loadActiveOffers() {
        return offerMapper.selectList(
                new LambdaQueryWrapper<Offer>()
                        .in(
                                Offer::getStatus,
                                OfferStatus.DRAFT.name(),
                                OfferStatus.SENT.name()
                        )
                        .orderByDesc(Offer::getUpdatedAt)
                        .orderByDesc(Offer::getId)
                        .last("LIMIT " + TASK_LIMIT)
        );
    }

    private List<Onboarding> loadReviewingOnboardings() {
        return onboardingMapper.selectList(
                new LambdaQueryWrapper<Onboarding>()
                        .and(condition -> condition
                                .eq(
                                        Onboarding::getStatus,
                                        OnboardingStatus.REVIEWING.name()
                                )
                                .or()
                                .eq(
                                        Onboarding::getMaterialStatus,
                                        OnboardingMaterialStatus.REVIEWING.name()
                                ))
                        .orderByDesc(Onboarding::getUpdatedAt)
                        .orderByDesc(Onboarding::getId)
                        .last("LIMIT " + TASK_LIMIT)
        );
    }

    private Map<Long, Offer> loadOfferMap(
            List<Offer> activeOffers,
            List<Onboarding> onboardings
    ) {
        Map<Long, Offer> offerMap = activeOffers.stream()
                .collect(Collectors.toMap(
                        Offer::getId,
                        Function.identity(),
                        (left, right) -> left,
                        HashMap::new
                ));
        Set<Long> missingOfferIds = onboardings.stream()
                .map(Onboarding::getOfferId)
                .filter(id -> id != null && !offerMap.containsKey(id))
                .collect(Collectors.toSet());
        if (!missingOfferIds.isEmpty()) {
            offerMapper.selectBatchIds(missingOfferIds)
                    .forEach(offer -> offerMap.put(offer.getId(), offer));
        }
        return offerMap;
    }

    private Map<Long, JobApplication> loadApplicationMap(
            List<JobApplication> screeningApplications,
            List<Interview> interviews,
            java.util.Collection<Offer> offers
    ) {
        Map<Long, JobApplication> applicationMap = screeningApplications
                .stream()
                .collect(Collectors.toMap(
                        JobApplication::getId,
                        Function.identity(),
                        (left, right) -> left,
                        HashMap::new
                ));
        Set<Long> applicationIds = new HashSet<>();
        interviews.stream()
                .map(Interview::getApplicationId)
                .filter(id -> id != null && !applicationMap.containsKey(id))
                .forEach(applicationIds::add);
        offers.stream()
                .map(Offer::getApplicationId)
                .filter(id -> id != null && !applicationMap.containsKey(id))
                .forEach(applicationIds::add);
        if (!applicationIds.isEmpty()) {
            jobApplicationMapper.selectBatchIds(applicationIds)
                    .forEach(application -> applicationMap.put(
                            application.getId(),
                            application
                    ));
        }
        return applicationMap;
    }

    private Map<Long, Candidate> loadCandidateMap(
            java.util.Collection<JobApplication> applications,
            List<Onboarding> onboardings
    ) {
        Set<Long> candidateIds = new HashSet<>();
        applications.stream()
                .map(JobApplication::getCandidateId)
                .filter(id -> id != null)
                .forEach(candidateIds::add);
        onboardings.stream()
                .map(Onboarding::getCandidateId)
                .filter(id -> id != null)
                .forEach(candidateIds::add);
        if (candidateIds.isEmpty()) {
            return Map.of();
        }
        return candidateMapper.selectBatchIds(candidateIds)
                .stream()
                .collect(Collectors.toMap(
                        Candidate::getId,
                        Function.identity()
                ));
    }

    private Map<Long, JobPosition> loadJobMap(
            java.util.Collection<JobApplication> applications
    ) {
        Set<Long> jobIds = applications.stream()
                .map(JobApplication::getJobId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (jobIds.isEmpty()) {
            return Map.of();
        }
        return jobPositionMapper.selectBatchIds(jobIds)
                .stream()
                .collect(Collectors.toMap(
                        JobPosition::getId,
                        Function.identity()
                ));
    }

    private DashboardTaskVO screeningTask(
            JobApplication application,
            Map<Long, Candidate> candidateMap,
            Map<Long, JobPosition> jobMap
    ) {
        DashboardTaskVO task = baseTask(
                DashboardTaskType.SCREENING,
                application.getId(),
                application,
                candidateMap,
                jobMap
        );
        task.setStatus(application.getStatus());
        JobApplicationStatus status = JobApplicationStatus.fromCode(
                application.getStatus()
        );
        task.setStatusText(
                status == null ? "待筛选" : status.getDescription()
        );
        task.setTitle(contextTitle(task, "投递待筛选"));
        task.setOccurredAt(firstNonNull(
                application.getAppliedAt(),
                application.getUpdatedAt(),
                application.getCreatedAt()
        ));
        return task;
    }

    private DashboardTaskVO feedbackTask(
            Interview interview,
            Map<Long, JobApplication> applicationMap,
            Map<Long, Candidate> candidateMap,
            Map<Long, JobPosition> jobMap
    ) {
        JobApplication application = applicationMap.get(
                interview.getApplicationId()
        );
        DashboardTaskVO task = baseTask(
                DashboardTaskType.INTERVIEW_FEEDBACK,
                interview.getId(),
                application,
                candidateMap,
                jobMap
        );
        task.setStatus("PENDING_FEEDBACK");
        task.setStatusText("待补充反馈");
        task.setTitle(contextTitle(task, "面试待提交反馈"));
        task.setOccurredAt(firstNonNull(
                interview.getUpdatedAt(),
                interview.getInterviewTime(),
                interview.getCreatedAt()
        ));
        return task;
    }

    private DashboardTaskVO offerTask(
            Offer offer,
            Map<Long, JobApplication> applicationMap,
            Map<Long, Candidate> candidateMap,
            Map<Long, JobPosition> jobMap
    ) {
        JobApplication application = applicationMap.get(
                offer.getApplicationId()
        );
        DashboardTaskVO task = baseTask(
                DashboardTaskType.OFFER,
                offer.getId(),
                application,
                candidateMap,
                jobMap
        );
        task.setStatus(offer.getStatus());
        OfferStatus status = OfferStatus.fromCode(offer.getStatus());
        task.setStatusText(
                status == null ? "处理中" : status.getDescription()
        );
        task.setTitle(contextTitle(
                task,
                OfferStatus.DRAFT.name().equals(offer.getStatus())
                        ? "Offer草稿待发送"
                        : "Offer等待候选人回复"
        ));
        task.setOccurredAt(firstNonNull(
                offer.getUpdatedAt(),
                offer.getSentAt(),
                offer.getCreatedAt()
        ));
        return task;
    }

    private DashboardTaskVO onboardingTask(
            Onboarding onboarding,
            Map<Long, Offer> offerMap,
            Map<Long, JobApplication> applicationMap,
            Map<Long, Candidate> candidateMap,
            Map<Long, JobPosition> jobMap
    ) {
        Offer offer = offerMap.get(onboarding.getOfferId());
        JobApplication application = offer == null
                ? null
                : applicationMap.get(offer.getApplicationId());
        DashboardTaskVO task = baseTask(
                DashboardTaskType.ONBOARDING,
                onboarding.getId(),
                application,
                candidateMap,
                jobMap
        );
        if (task.getCandidateId() == null) {
            task.setCandidateId(onboarding.getCandidateId());
            Candidate candidate = candidateMap.get(
                    onboarding.getCandidateId()
            );
            task.setCandidateName(
                    candidate == null ? null : candidate.getName()
            );
        }
        task.setStatus(onboarding.getStatus());
        OnboardingStatus status = OnboardingStatus.fromCode(
                onboarding.getStatus()
        );
        task.setStatusText(
                status == null ? "材料审核中" : status.getDescription()
        );
        task.setTitle(contextTitle(task, "入职材料待审核"));
        task.setOccurredAt(firstNonNull(
                onboarding.getUpdatedAt(),
                onboarding.getCreatedAt()
        ));
        return task;
    }

    private DashboardTaskVO baseTask(
            DashboardTaskType type,
            Long relatedId,
            JobApplication application,
            Map<Long, Candidate> candidateMap,
            Map<Long, JobPosition> jobMap
    ) {
        DashboardTaskVO task = new DashboardTaskVO();
        task.setType(type.name());
        task.setRelatedId(relatedId);
        if (application == null) {
            return task;
        }
        task.setApplicationId(application.getId());
        task.setCandidateId(application.getCandidateId());
        Candidate candidate = candidateMap.get(application.getCandidateId());
        task.setCandidateName(candidate == null ? null : candidate.getName());
        task.setJobId(application.getJobId());
        JobPosition job = jobMap.get(application.getJobId());
        task.setJobTitle(job == null ? null : job.getTitle());
        return task;
    }

    private String contextTitle(DashboardTaskVO task, String action) {
        String candidateName = task.getCandidateName() == null
                ? "未知候选人"
                : task.getCandidateName();
        String jobTitle = task.getJobTitle() == null
                ? "未知职位"
                : task.getJobTitle();
        return candidateName + " · " + jobTitle + "：" + action;
    }

    private LocalDateTime firstNonNull(LocalDateTime... values) {
        for (LocalDateTime value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Long zeroIfNull(Long value) {
        return value == null ? 0L : value;
    }
}
