package com.recruit.biz.assembler;

import com.recruit.biz.entity.AiMatchResult;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Offer;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.enums.InterviewMethod;
import com.recruit.biz.enums.InterviewRound;
import com.recruit.biz.enums.InterviewStatus;
import com.recruit.biz.enums.JobApplicationStatus;
import com.recruit.biz.enums.OfferStatus;
import com.recruit.biz.vo.AiMatchSummaryVO;
import com.recruit.biz.vo.InterviewSummaryVO;
import com.recruit.biz.vo.OfferSummaryVO;
import com.recruit.biz.vo.PipelineApplicationDetailVO;
import com.recruit.biz.vo.PipelineApplicationSummaryVO;
import com.recruit.biz.vo.PipelineTimelineEventVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class PipelineAssembler {

    public PipelineApplicationSummaryVO toSummary(
            JobApplication application,
            Candidate candidate,
            JobPosition job,
            AiMatchResult aiMatch,
            SysUser owner,
            List<Interview> interviews,
            Offer offer
    ) {
        PipelineApplicationSummaryVO vo =
                new PipelineApplicationSummaryVO();
        vo.setId(application.getId());
        vo.setCandidateId(application.getCandidateId());
        if (candidate != null) {
            vo.setCandidateName(candidate.getName());
            vo.setEducation(candidate.getEducation());
            vo.setYearsOfExperience(candidate.getYearsOfExperience());
        }
        vo.setJobId(application.getJobId());
        if (job != null) {
            vo.setJobTitle(job.getTitle());
            vo.setDepartment(job.getDepartment());
        }
        vo.setStatus(application.getStatus());
        vo.setStatusText(applicationStatusText(application.getStatus()));
        if (aiMatch != null) {
            vo.setMatchScore(aiMatch.getMatchScore());
            vo.setRecommendLevel(aiMatch.getRecommendLevel());
        }
        vo.setOwnerId(application.getReviewedBy());
        vo.setOwnerName(displayName(owner));
        vo.setSource(application.getSource());
        vo.setSourceText(sourceText(application.getSource()));
        vo.setReviewDecision(reviewDecision(application));
        vo.setAppliedAt(application.getAppliedAt());
        vo.setLastActivityAt(calculateLastActivityAt(
                application,
                aiMatch,
                interviews,
                offer
        ));
        return vo;
    }

    public PipelineApplicationDetailVO toDetail(
            JobApplication application,
            Candidate candidate,
            JobPosition job,
            Resume resume,
            AiMatchResult aiMatch,
            List<Interview> interviews,
            Interview currentInterview,
            InterviewFeedback feedback,
            Offer offer,
            Map<Long, SysUser> userMap
    ) {
        PipelineApplicationSummaryVO summary = toSummary(
                application,
                candidate,
                job,
                aiMatch,
                userMap.get(application.getReviewedBy()),
                interviews,
                offer
        );
        PipelineApplicationDetailVO vo = new PipelineApplicationDetailVO();
        BeanUtils.copyProperties(summary, vo);

        if (candidate != null) {
            vo.setCandidatePhone(candidate.getPhone());
            vo.setCandidateEmail(candidate.getEmail());
            vo.setSchool(candidate.getSchool());
        }
        vo.setResumeId(application.getResumeId());
        if (resume != null) {
            vo.setResumeName(resume.getResumeName());
            vo.setResumeFileType(resume.getFileType());
        }
        vo.setAllowAdjustment(application.getAllowAdjustment() != null
                && application.getAllowAdjustment() == 1);
        vo.setAdjustedJobId(application.getAdjustedJobId());
        vo.setHrNote(application.getHrNote());
        vo.setRejectReasonCode(application.getRejectReasonCode());
        vo.setRejectReason(application.getRejectReason());
        vo.setReviewedAt(application.getReviewedAt());
        vo.setAiMatch(toAiMatchSummary(aiMatch));
        vo.setInterview(toInterviewSummary(
                currentInterview,
                application,
                candidate,
                job,
                currentInterview == null
                        ? null
                        : userMap.get(currentInterview.getInterviewerId()),
                feedback
        ));
        vo.setOffer(toOfferSummary(offer, application, job));
        vo.setTimeline(buildTimeline(
                application,
                candidate,
                job,
                aiMatch,
                interviews,
                offer,
                userMap
        ));
        return vo;
    }

    public Interview selectCurrentInterview(List<Interview> interviews) {
        return interviews.stream()
                .filter(interview -> InterviewStatus.SCHEDULED.name()
                        .equals(interview.getStatus()))
                .min(Comparator.comparing(
                        Interview::getInterviewTime,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .orElseGet(() -> interviews.stream()
                        .max(Comparator.comparing(
                                Interview::getInterviewTime,
                                Comparator.nullsFirst(
                                        Comparator.naturalOrder()
                                )
                        ))
                        .orElse(null));
    }

    private AiMatchSummaryVO toAiMatchSummary(AiMatchResult aiMatch) {
        if (aiMatch == null) {
            return null;
        }
        AiMatchSummaryVO vo = new AiMatchSummaryVO();
        vo.setMatchScore(aiMatch.getMatchScore());
        vo.setRecommendLevel(aiMatch.getRecommendLevel());
        vo.setRecommendReason(aiMatch.getRecommendReason());
        vo.setHighlightSummary(aiMatch.getHighlightSummary());
        vo.setRiskSummary(aiMatch.getRiskSummary());
        vo.setModelName(aiMatch.getModelName());
        vo.setGeneratedAt(aiMatch.getGeneratedAt());
        return vo;
    }

    private InterviewSummaryVO toInterviewSummary(
            Interview interview,
            JobApplication application,
            Candidate candidate,
            JobPosition job,
            SysUser interviewer,
            InterviewFeedback feedback
    ) {
        if (interview == null) {
            return null;
        }
        InterviewSummaryVO vo = new InterviewSummaryVO();
        vo.setId(interview.getId());
        vo.setApplicationId(interview.getApplicationId());
        vo.setJobId(application.getJobId());
        vo.setJobTitle(job == null ? null : job.getTitle());
        vo.setCandidateId(application.getCandidateId());
        vo.setCandidateName(candidate == null ? null : candidate.getName());
        vo.setInterviewerId(interview.getInterviewerId());
        vo.setInterviewerName(displayName(interviewer));
        vo.setRound(interview.getRound());
        InterviewRound round = InterviewRound.fromCode(interview.getRound());
        vo.setRoundText(
                round == null ? "未知轮次" : round.getDescription()
        );
        vo.setInterviewTime(interview.getInterviewTime());
        vo.setMethod(interview.getMethod());
        vo.setMethodText(interviewMethodText(interview.getMethod()));
        vo.setLocation(interview.getLocation());
        vo.setStatus(interview.getStatus());
        vo.setStatusText(interviewStatusText(interview.getStatus()));
        if (feedback != null) {
            vo.setFeedbackScore(feedback.getScore());
            vo.setFeedbackSuggestion(feedback.getSuggestion());
        }
        return vo;
    }

    private OfferSummaryVO toOfferSummary(
            Offer offer,
            JobApplication application,
            JobPosition job
    ) {
        if (offer == null) {
            return null;
        }
        OfferSummaryVO vo = new OfferSummaryVO();
        vo.setId(offer.getId());
        vo.setApplicationId(offer.getApplicationId());
        vo.setJobId(application.getJobId());
        if (job != null) {
            vo.setJobTitle(job.getTitle());
            vo.setDepartment(job.getDepartment());
        }
        vo.setSalary(offer.getSalary());
        vo.setEntryDate(offer.getEntryDate());
        vo.setWorkLocation(offer.getWorkLocation());
        vo.setStatus(offer.getStatus());
        OfferStatus status = OfferStatus.fromCode(offer.getStatus());
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );
        vo.setSentAt(offer.getSentAt());
        vo.setAcceptedAt(offer.getAcceptedAt());
        return vo;
    }

    private List<PipelineTimelineEventVO> buildTimeline(
            JobApplication application,
            Candidate candidate,
            JobPosition job,
            AiMatchResult aiMatch,
            List<Interview> interviews,
            Offer offer,
            Map<Long, SysUser> userMap
    ) {
        List<PipelineTimelineEventVO> timeline = new ArrayList<>();
        String candidateName = candidate == null
                ? "候选人"
                : candidate.getName();
        String jobTitle = job == null ? "职位" : job.getTitle();
        addEvent(
                timeline,
                "application-" + application.getId() + "-submitted",
                "候选人提交投递",
                "使用简历投递该职位。",
                candidateName,
                firstNonNull(application.getAppliedAt(), application.getCreatedAt()),
                "BUSINESS",
                jobTitle
        );
        if (aiMatch != null) {
            addEvent(
                    timeline,
                    "ai-match-" + aiMatch.getId(),
                    "生成岗位匹配参考",
                    "生成匹配分和候选人优势、风险摘要。",
                    hasText(aiMatch.getModelName())
                            ? aiMatch.getModelName()
                            : "AI 服务",
                    firstNonNull(aiMatch.getGeneratedAt(), aiMatch.getCreatedAt()),
                    "AI",
                    jobTitle
            );
        }
        if (application.getReviewedAt() != null) {
            addEvent(
                    timeline,
                    "application-" + application.getId() + "-reviewed",
                    "HR 处理投递",
                    "当前投递状态为"
                            + applicationStatusText(application.getStatus())
                            + "。",
                    defaultActorName(
                            userMap.get(application.getReviewedBy()),
                            "HR"
                    ),
                    application.getReviewedAt(),
                    "BUSINESS",
                    "投递 #" + application.getId()
            );
        }
        for (Interview interview : interviews) {
            InterviewRound round = InterviewRound.fromCode(interview.getRound());
            String roundText = round == null ? "面试" : round.getDescription();
            addEvent(
                    timeline,
                    "interview-" + interview.getId(),
                    "创建" + roundText + "安排",
                    "面试方式：" + interviewMethodText(interview.getMethod())
                            + "，当前状态："
                            + interviewStatusText(interview.getStatus())
                            + "。",
                    defaultActorName(
                            userMap.get(interview.getCreatedBy()),
                            "HR"
                    ),
                    firstNonNull(interview.getCreatedAt(), interview.getInterviewTime()),
                    "BUSINESS",
                    "面试 #" + interview.getId()
            );
        }
        if (offer != null) {
            String actor = defaultActorName(
                    userMap.get(offer.getCreatedBy()),
                    "HR"
            );
            addEvent(
                    timeline,
                    "offer-" + offer.getId() + "-created",
                    "创建 Offer",
                    "录用方案已创建。",
                    actor,
                    offer.getCreatedAt(),
                    "BUSINESS",
                    "Offer #" + offer.getId()
            );
            addEvent(
                    timeline,
                    "offer-" + offer.getId() + "-sent",
                    "发送 Offer",
                    "录用方案已发送给候选人。",
                    actor,
                    offer.getSentAt(),
                    "BUSINESS",
                    "Offer #" + offer.getId()
            );
            addEvent(
                    timeline,
                    "offer-" + offer.getId() + "-accepted",
                    "候选人接受 Offer",
                    "候选人确认接受录用方案。",
                    candidateName,
                    offer.getAcceptedAt(),
                    "BUSINESS",
                    "Offer #" + offer.getId()
            );
        }
        timeline.sort(Comparator.comparing(
                PipelineTimelineEventVO::getOccurredAt
        ));
        return timeline;
    }

    private LocalDateTime calculateLastActivityAt(
            JobApplication application,
            AiMatchResult aiMatch,
            List<Interview> interviews,
            Offer offer
    ) {
        List<LocalDateTime> times = new ArrayList<>();
        times.add(application.getAppliedAt());
        times.add(application.getCreatedAt());
        times.add(application.getUpdatedAt());
        if (aiMatch != null) {
            times.add(aiMatch.getGeneratedAt());
            times.add(aiMatch.getCreatedAt());
            times.add(aiMatch.getUpdatedAt());
        }
        for (Interview interview : interviews) {
            times.add(interview.getCreatedAt());
            times.add(interview.getUpdatedAt());
        }
        if (offer != null) {
            times.add(offer.getCreatedAt());
            times.add(offer.getUpdatedAt());
            times.add(offer.getSentAt());
            times.add(offer.getAcceptedAt());
        }
        return times.stream()
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(application.getAppliedAt());
    }

    private void addEvent(
            List<PipelineTimelineEventVO> timeline,
            String id,
            String title,
            String description,
            String actorName,
            LocalDateTime occurredAt,
            String source,
            String relatedObject
    ) {
        if (occurredAt != null) {
            timeline.add(new PipelineTimelineEventVO(
                    id,
                    title,
                    description,
                    actorName,
                    occurredAt,
                    source,
                    relatedObject
            ));
        }
    }

    private String applicationStatusText(String code) {
        JobApplicationStatus status = JobApplicationStatus.fromCode(code);
        return status == null ? "未知状态" : status.getDescription();
    }

    private String interviewMethodText(String code) {
        InterviewMethod method = InterviewMethod.fromCode(code);
        return method == null ? "未知方式" : method.getDescription();
    }

    private String interviewStatusText(String code) {
        InterviewStatus status = InterviewStatus.fromCode(code);
        return status == null ? "未知状态" : status.getDescription();
    }

    private String sourceText(String code) {
        if (code == null) {
            return "未知来源";
        }
        return switch (code) {
            case "ONLINE" -> "在线投递";
            case "HR_IMPORT" -> "HR录入";
            case "REFERRAL" -> "员工推荐";
            default -> code;
        };
    }

    private String reviewDecision(JobApplication application) {
        String status = application.getStatus();
        if (JobApplicationStatus.SCREEN_REJECT.name().equals(status)) {
            return "REJECT";
        }
        if (JobApplicationStatus.SCREENING.name().equals(status)
                && application.getReviewedAt() != null
                && hasText(application.getHrNote())) {
            return "PENDING";
        }
        if (JobApplicationStatus.SCREEN_PASSED.name().equals(status)
                || JobApplicationStatus.INTERVIEWING.name().equals(status)
                || JobApplicationStatus.OFFERED.name().equals(status)
                || JobApplicationStatus.HIRED.name().equals(status)
                || JobApplicationStatus.REJECTED.name().equals(status)) {
            return "PASS";
        }
        return null;
    }

    private String displayName(SysUser user) {
        if (user == null) {
            return null;
        }
        return hasText(user.getRealName())
                ? user.getRealName()
                : user.getUsername();
    }

    private String defaultActorName(SysUser user, String defaultName) {
        String name = displayName(user);
        return name == null ? defaultName : name;
    }

    private LocalDateTime firstNonNull(
            LocalDateTime first,
            LocalDateTime second
    ) {
        return first == null ? second : first;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
