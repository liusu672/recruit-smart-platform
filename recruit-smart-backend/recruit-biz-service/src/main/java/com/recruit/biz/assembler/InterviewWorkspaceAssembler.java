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
                method == null ? "待确认" : method.getDescription()
        );
        vo.setLocation(interview.getLocation());
        vo.setStatus(interview.getStatus());
        InterviewStatus status = InterviewStatus.fromCode(
                interview.getStatus()
        );
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );
        vo.setAssignedAt(interview.getAssignedAt());
        vo.setScheduledAt(interview.getScheduledAt());
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
                        ? defaultScorecard(detail.getRound())
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
        vo.setAssignedAt(detail.getAssignedAt());
        vo.setScheduledAt(detail.getScheduledAt());
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

    private List<InterviewScoreItemVO> defaultScorecard(String roundCode) {
        InterviewRound round = InterviewRound.fromCode(roundCode);
        if (InterviewRound.SECOND.equals(round)) {
            return secondRoundScorecard();
        }
        if (InterviewRound.HR.equals(round)) {
            return hrRoundScorecard();
        }
        return firstRoundScorecard();
    }

    private List<InterviewScoreItemVO> firstRoundScorecard() {
        return List.of(
                scoreItem(
                        "resume-verification",
                        "简历与经历真实性",
                        "候选人描述的职责、技术细节与成果是否真实且前后一致。"
                ),
                scoreItem(
                        "fundamentals",
                        "核心基础能力",
                        "是否掌握岗位要求的基础知识、常用方法与基本实践。"
                ),
                scoreItem(
                        "basic-problem-solving",
                        "基础问题解决",
                        "能否理解问题、拆解步骤并给出清晰可执行的处理思路。"
                ),
                scoreItem(
                        "communication-motivation",
                        "沟通与岗位动机",
                        "表达是否清晰，求职动机与岗位方向是否基本匹配。"
                )
        );
    }

    private List<InterviewScoreItemVO> secondRoundScorecard() {
        return List.of(
                scoreItem(
                        "professional-depth",
                        "专业深度",
                        "能否深入解释关键原理、边界条件与实际应用经验。"
                ),
                scoreItem(
                        "solution-design",
                        "方案设计与取舍",
                        "能否针对复杂场景设计方案并说明成本、风险与取舍依据。"
                ),
                scoreItem(
                        "ownership-results",
                        "项目主导与结果",
                        "是否真正承担关键责任，并能用事实说明推进过程和最终结果。"
                ),
                scoreItem(
                        "collaboration-influence",
                        "协作与影响力",
                        "能否处理分歧、推动跨角色协作并对团队结果产生积极影响。"
                )
        );
    }

    private List<InterviewScoreItemVO> hrRoundScorecard() {
        return List.of(
                scoreItem(
                        "career-motivation",
                        "求职动机",
                        "离职原因、求职诉求与选择本岗位的动机是否合理一致。"
                ),
                scoreItem(
                        "stability-planning",
                        "稳定性与职业规划",
                        "职业规划是否清晰，与岗位发展路径和预期任职周期是否匹配。"
                ),
                scoreItem(
                        "values-fit",
                        "价值观与团队匹配",
                        "工作方式、责任意识和协作理念是否与团队要求相符。"
                ),
                scoreItem(
                        "offer-risk",
                        "薪资、到岗与录用风险",
                        "薪资期望、到岗时间及其他可能影响录用或入职的风险是否可控。"
                )
        );
    }

    private InterviewScoreItemVO scoreItem(
            String key,
            String label,
            String description
    ) {
        return new InterviewScoreItemVO(
                key,
                label,
                description,
                null,
                ""
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
