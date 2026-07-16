package com.recruit.biz.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PipelineApplicationDetailVO
        extends PipelineApplicationSummaryVO {
    private String candidatePhone;
    private String candidateEmail;
    private String school;
    private Long resumeId;
    private String resumeName;
    private String resumeFileType;
    private Boolean allowAdjustment;
    private Long adjustedJobId;
    private String hrNote;
    private String rejectReasonCode;
    private String rejectReason;
    private LocalDateTime reviewedAt;
    private AiMatchSummaryVO aiMatch;
    private InterviewSummaryVO interview;
    private OfferSummaryVO offer;
    private List<PipelineTimelineEventVO> timeline;
}
