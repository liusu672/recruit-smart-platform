package com.recruit.biz.assembler;

import com.recruit.biz.entity.AiMatchResult;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.enums.InterviewMethod;
import com.recruit.biz.enums.InterviewRound;
import com.recruit.biz.enums.InterviewStatus;
import com.recruit.biz.vo.InterviewCandidateBriefVO;
import com.recruit.biz.vo.InterviewDetailVO;
import com.recruit.biz.vo.InterviewScoreItemVO;
import com.recruit.biz.vo.InterviewTaskSummaryVO;
import com.recruit.biz.vo.InterviewWorkspaceFeedbackVO;
import com.recruit.biz.vo.InterviewWorkspaceVO;
import com.recruit.biz.support.InterviewScorecardCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class InterviewWorkspaceAssembler {

    private final InterviewScorecardCodec scorecardCodec;

    private static final Pattern SKILL_SEPARATOR = Pattern.compile(
            "[,，;；\\r\\n]+"
    );
    private static final Pattern RISK_SEPARATOR = Pattern.compile(
            "[;；\\r\\n]+"
    );

    public InterviewTaskSummaryVO toTaskSummary(
            Interview interview,
            JobApplication application,
            Candidate candidate,
            JobPosition job,
            SysUser interviewer,
            InterviewFeedback feedback
    ) {
        InterviewTaskSummaryVO vo = new InterviewTaskSummaryVO();
        vo.setId(interview.getId());
        vo.setApplicationId(interview.getApplicationId());
        if (application != null) {
            vo.setCandidateId(application.getCandidateId());
        }
        vo.setCandidateName(candidate == null ? null : candidate.getName());
        if (job != null) {
            vo.setJobTitle(job.getTitle());
            vo.setDepartment(job.getDepartment());
        }
        vo.setRound(interview.getRound());
        InterviewRound round = InterviewRound.fromCode(interview.getRound());
        vo.setRoundText(
                round == null ? "未知轮次" : round.getDescription()
        );
        vo.setInterviewTime(interview.getInterviewTime());
        vo.setMethod(interview.getMethod());
        InterviewMethod method = InterviewMethod.fromCode(
                interview.getMethod()
        );
        vo.setMethodText(
                method == null ? "未知方式" : method.getDescription()
        );
        vo.setLocation(interview.getLocation());
        vo.setStatus(interview.getStatus());
        InterviewStatus status = InterviewStatus.fromCode(
                interview.getStatus()
        );
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );
        vo.setInterviewerId(interview.getInterviewerId());
        vo.setInterviewerName(
                interviewer == null ? null : interviewer.getRealName()
        );
        fillFeedbackState(vo, feedback);
        return vo;
    }

    public InterviewWorkspaceVO toWorkspace(
            InterviewDetailVO detail,
            Candidate candidate,
            Resume resume,
            AiMatchResult aiMatch,
            InterviewFeedback feedback
    ) {
        InterviewWorkspaceVO vo = new InterviewWorkspaceVO();
        copyTaskSummary(detail, vo);

        fillFeedbackState(vo, feedback);
        String feedbackState = vo.getFeedbackState();
        vo.setCandidateBrief(toCandidateBrief(candidate, resume, aiMatch));
        List<InterviewScoreItemVO> savedScorecard = feedback == null
                ? List.of()
                : scorecardCodec.read(feedback.getScorecardJson());
        vo.setScorecard(
                savedScorecard.isEmpty()
                        ? defaultScorecard()
                        : savedScorecard
        );
        vo.setQuestions(List.of());
        vo.setFeedback(toFeedback(detail, feedback, feedbackState));
        return vo;
    }

    private void copyTaskSummary(
            InterviewDetailVO detail,
            InterviewTaskSummaryVO vo
    ) {
        vo.setId(detail.getId());
        vo.setApplicationId(detail.getApplicationId());
        vo.setCandidateId(detail.getCandidateId());
        vo.setCandidateName(detail.getCandidateName());
        vo.setJobTitle(detail.getJobTitle());
        vo.setDepartment(detail.getDepartment());
        vo.setRound(detail.getRound());
        vo.setRoundText(detail.getRoundText());
        vo.setInterviewTime(detail.getInterviewTime());
        vo.setMethod(detail.getMethod());
        vo.setMethodText(detail.getMethodText());
        vo.setLocation(detail.getLocation());
        vo.setStatus(detail.getStatus());
        vo.setStatusText(detail.getStatusText());
        vo.setInterviewerId(detail.getInterviewerId());
        vo.setInterviewerName(detail.getInterviewerName());
    }

    private void fillFeedbackState(
            InterviewTaskSummaryVO vo,
            InterviewFeedback feedback
    ) {
        String state = feedbackState(feedback);
        vo.setFeedbackState(state);
        vo.setFeedbackStateText(switch (state) {
            case "DRAFT" -> "草稿";
            case "SUBMITTED" -> "已提交";
            default -> "未填写";
        });
    }

    private InterviewCandidateBriefVO toCandidateBrief(
            Candidate candidate,
            Resume resume,
            AiMatchResult aiMatch
    ) {
        InterviewCandidateBriefVO vo = new InterviewCandidateBriefVO();
        if (candidate != null) {
            vo.setEducation(candidate.getEducation());
            vo.setSchool(candidate.getSchool());
            vo.setYearsOfExperience(
                    candidate.getYearsOfExperience() == null
                            ? 0
                            : candidate.getYearsOfExperience()
            );
        } else {
            vo.setYearsOfExperience(0);
        }

        if (resume != null) {
            vo.setResumeName(resume.getResumeName());
            vo.setSkills(splitText(resume.getSkills(), SKILL_SEPARATOR));
            vo.setWorkExperience(resume.getWorkExperience());
            vo.setProjectExperience(resume.getProjectExperience());
        } else {
            vo.setSkills(List.of());
        }

        if (aiMatch != null) {
            vo.setMatchScore(aiMatch.getMatchScore());
            vo.setMatchSummary(aiMatch.getRecommendReason());
            vo.setRiskPoints(splitText(
                    aiMatch.getRiskSummary(),
                    RISK_SEPARATOR
            ));
        } else {
            vo.setRiskPoints(List.of());
        }
        return vo;
    }

    private InterviewWorkspaceFeedbackVO toFeedback(
            InterviewDetailVO detail,
            InterviewFeedback feedback,
            String state
    ) {
        InterviewWorkspaceFeedbackVO vo =
                new InterviewWorkspaceFeedbackVO();
        vo.setInterviewId(detail.getId());
        vo.setInterviewerId(detail.getInterviewerId());
        vo.setState(state);
        if (feedback == null) {
            vo.setComment("");
            return vo;
        }

        vo.setId(feedback.getId());
        vo.setInterviewerId(feedback.getInterviewerId());
        vo.setScore(feedback.getScore());
        vo.setComment(
                feedback.getComment() == null ? "" : feedback.getComment()
        );
        vo.setSuggestion(feedback.getSuggestion());
        vo.setAiSummary(feedback.getAiSummary());
        vo.setSubmittedAt(
                feedback.getSubmittedAt() != null
                        ? feedback.getSubmittedAt()
                        : "SUBMITTED".equals(state)
                        ? feedback.getCreatedAt()
                        : null
        );
        return vo;
    }

    private String feedbackState(InterviewFeedback feedback) {
        if (feedback == null) {
            return "EMPTY";
        }
        return feedback.getState() == null
                ? "SUBMITTED"
                : feedback.getState();
    }

    private List<InterviewScoreItemVO> defaultScorecard() {
        return List.of(
                new InterviewScoreItemVO(
                        "professional",
                        "专业能力",
                        "是否具备岗位要求的核心知识、方法与实践深度。",
                        null,
                        ""
                ),
                new InterviewScoreItemVO(
                        "problem-solving",
                        "问题解决",
                        "能否拆解问题、说明判断依据并形成可执行方案。",
                        null,
                        ""
                ),
                new InterviewScoreItemVO(
                        "collaboration",
                        "协作与影响力",
                        "能否清晰沟通、推动协作并对结果负责。",
                        null,
                        ""
                ),
                new InterviewScoreItemVO(
                        "reflection",
                        "结果与复盘",
                        "能否量化结果、识别不足并沉淀后续改进。",
                        null,
                        ""
                )
        );
    }

    private List<String> splitText(String value, Pattern separator) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(separator.split(value))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .distinct()
                .toList();
    }
}
