package com.recruit.biz.vo;

import lombok.Data;

import java.util.List;

@Data
public class InterviewWorkspaceVO extends InterviewTaskSummaryVO {
    private InterviewCandidateBriefVO candidateBrief;
    private List<InterviewScoreItemVO> scorecard;
    private List<InterviewQuestionVO> questions;
    private InterviewWorkspaceFeedbackVO feedback;
}
