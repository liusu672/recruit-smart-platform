package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.recruit.biz.dto.InterviewFeedbackCreateDTO;
import com.recruit.biz.dto.InterviewFeedbackDraftDTO;
import com.recruit.biz.dto.InterviewScoreItemDTO;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.enums.InterviewFeedbackSuggestion;
import com.recruit.biz.enums.InterviewStatus;
import com.recruit.biz.enums.ProcessEventType;
import com.recruit.biz.enums.ProcessRelatedType;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.InterviewFeedbackService;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.biz.support.InterviewScorecardCodec;
import com.recruit.biz.vo.InterviewFeedbackVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InterviewFeedbackServiceImpl
        implements InterviewFeedbackService {

    @Resource
    private InterviewMapper interviewMapper;

    @Resource
    private InterviewFeedbackMapper interviewFeedbackMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private ApplicationProcessEventService processEventService;

    @Resource
    private InterviewScorecardCodec scorecardCodec;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDraft(
            Long interviewId,
            InterviewFeedbackDraftDTO dto
    ) {
        Interview interview = requireOwnedInterview(interviewId);
        if (InterviewStatus.CANCELED.name().equals(interview.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "已取消的面试不能保存反馈草稿"
            );
        }

        InterviewFeedback feedback = findFeedback(interviewId);
        if (feedback != null && "SUBMITTED".equals(stateOf(feedback))) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "已提交的反馈不能覆盖"
            );
        }

        String scorecardJson = scorecardCodec.write(dto.getScorecard());
        String comment = trimToNull(dto.getComment());
        String suggestion = trimToNull(dto.getSuggestion());
        if (feedback == null) {
            InterviewFeedback draft = new InterviewFeedback();
            draft.setInterviewId(interviewId);
            draft.setInterviewerId(UserContext.getUserId());
            draft.setState("DRAFT");
            draft.setScorecardJson(scorecardJson);
            draft.setScore(dto.getScore());
            draft.setComment(comment);
            draft.setSuggestion(suggestion);
            try {
                interviewFeedbackMapper.insert(draft);
            } catch (DuplicateKeyException e) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_ERROR,
                        "草稿保存失败，记录可能已被其他请求处理"
                );
            }
            return;
        }

        int updated = interviewFeedbackMapper.update(
                null,
                new LambdaUpdateWrapper<InterviewFeedback>()
                        .eq(InterviewFeedback::getId, feedback.getId())
                        .eq(InterviewFeedback::getState, "DRAFT")
                        .set(InterviewFeedback::getScorecardJson, scorecardJson)
                        .set(InterviewFeedback::getScore, dto.getScore())
                        .set(InterviewFeedback::getComment, comment)
                        .set(InterviewFeedback::getSuggestion, suggestion)
        );
        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "草稿保存失败，记录可能已被其他请求处理"
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitFeedback(
            Long interviewId,
            InterviewFeedbackCreateDTO dto
    ) {
        Interview interview = requireOwnedInterview(interviewId);

        if (!InterviewStatus.COMPLETED.name()
                .equals(interview.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "只有已完成的面试可以提交反馈"
            );
        }

        InterviewFeedbackSuggestion suggestion;
        try {
            suggestion = InterviewFeedbackSuggestion.valueOf(
                    dto.getSuggestion()
            );
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "录用建议不正确"
            );
        }
        validateSubmittedScorecard(dto);
        String scorecardJson = scorecardCodec.write(dto.getScorecard());
        LocalDateTime submittedAt = LocalDateTime.now();
        InterviewFeedback existing = findFeedback(interviewId);
        if (existing != null && "SUBMITTED".equals(stateOf(existing))) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "该面试已经提交过反馈"
            );
        }

        Long feedbackId;
        if (existing == null) {
            InterviewFeedback feedback = new InterviewFeedback();
            feedback.setInterviewId(interviewId);
            feedback.setInterviewerId(UserContext.getUserId());
            feedback.setState("SUBMITTED");
            feedback.setScorecardJson(scorecardJson);
            feedback.setScore(dto.getScore());
            feedback.setComment(dto.getComment().trim());
            feedback.setSuggestion(suggestion.name());
            feedback.setSubmittedAt(submittedAt);
            try {
                interviewFeedbackMapper.insert(feedback);
            } catch (DuplicateKeyException e) {
                throw new BusinessException(
                        ErrorCode.PARAM_ERROR,
                        "该面试反馈正在被其他请求处理"
                );
            }
            feedbackId = feedback.getId();
        } else {
            int updated = interviewFeedbackMapper.update(
                    null,
                    new LambdaUpdateWrapper<InterviewFeedback>()
                            .eq(InterviewFeedback::getId, existing.getId())
                            .eq(InterviewFeedback::getState, "DRAFT")
                            .set(InterviewFeedback::getState, "SUBMITTED")
                            .set(
                                    InterviewFeedback::getScorecardJson,
                                    scorecardJson
                            )
                            .set(InterviewFeedback::getScore, dto.getScore())
                            .set(
                                    InterviewFeedback::getComment,
                                    dto.getComment().trim()
                            )
                            .set(
                                    InterviewFeedback::getSuggestion,
                                    suggestion.name()
                            )
                            .set(
                                    InterviewFeedback::getSubmittedAt,
                                    submittedAt
                            )
            );
            if (updated != 1) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_ERROR,
                        "提交反馈失败，草稿可能已被其他请求处理"
                );
            }
            feedbackId = existing.getId();
        }

        processEventService.record(
                interview.getApplicationId(),
                ProcessEventType.INTERVIEW_FEEDBACK_SUBMITTED,
                null,
                suggestion.name(),
                "面试评分：" + dto.getScore()
                        + "，录用建议：" + suggestion.getDescription(),
                ProcessRelatedType.INTERVIEW,
                interview.getId()
        );

        return feedbackId;
    }

    @Override
    public InterviewFeedbackVO getFeedback(Long interviewId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "面试记录不存在"
            );
        }

        String roleCode = UserContext.getRoleCode();
        boolean staffAllowed = "ADMIN".equals(roleCode)
                || "HR".equals(roleCode);
        boolean interviewerAllowed = "INTERVIEWER".equals(roleCode)
                && UserContext.getUserId().equals(
                interview.getInterviewerId()
        );
        if (!staffAllowed && !interviewerAllowed) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "无权查看该面试反馈"
            );
        }

        InterviewFeedback feedback = interviewFeedbackMapper.selectOne(
                new LambdaQueryWrapper<InterviewFeedback>()
                        .eq(
                                InterviewFeedback::getInterviewId,
                                interviewId
                        )
        );
        if (feedback == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "该面试尚未提交反馈"
            );
        }

        SysUser interviewer = sysUserMapper.selectById(
                feedback.getInterviewerId()
        );

        InterviewFeedbackVO vo = new InterviewFeedbackVO();
        vo.setId(feedback.getId());
        vo.setInterviewId(feedback.getInterviewId());
        vo.setInterviewerId(feedback.getInterviewerId());
        vo.setInterviewerName(
                interviewer == null ? null : interviewer.getRealName()
        );
        vo.setScore(feedback.getScore());
        vo.setComment(feedback.getComment());
        vo.setSuggestion(feedback.getSuggestion());
        vo.setAiSummary(feedback.getAiSummary());
        vo.setState(stateOf(feedback));
        vo.setSubmittedAt(feedback.getSubmittedAt());
        vo.setCreatedAt(feedback.getCreatedAt());
        vo.setUpdatedAt(feedback.getUpdatedAt());

        InterviewFeedbackSuggestion suggestion =
                InterviewFeedbackSuggestion.fromCode(
                        feedback.getSuggestion()
                );
        vo.setSuggestionText(
                suggestion == null
                        ? "未知建议"
                        : suggestion.getDescription()
        );

        return vo;
    }

    private Interview requireOwnedInterview(Long interviewId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "面试记录不存在"
            );
        }
        if (!UserContext.getUserId().equals(interview.getInterviewerId())) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "只有本场面试官可以操作反馈"
            );
        }
        return interview;
    }

    private InterviewFeedback findFeedback(Long interviewId) {
        return interviewFeedbackMapper.selectOne(
                new LambdaQueryWrapper<InterviewFeedback>()
                        .eq(InterviewFeedback::getInterviewId, interviewId)
        );
    }

    private void validateSubmittedScorecard(
            InterviewFeedbackCreateDTO dto
    ) {
        boolean incomplete = dto.getScorecard().stream()
                .anyMatch(this::isIncompleteScoreItem);
        if (incomplete) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "请完成所有评分并填写评价证据"
            );
        }
    }

    private boolean isIncompleteScoreItem(InterviewScoreItemDTO item) {
        return item.getScore() == null
                || item.getEvidence() == null
                || item.getEvidence().isBlank();
    }

    private String stateOf(InterviewFeedback feedback) {
        return feedback.getState() == null
                ? "SUBMITTED"
                : feedback.getState();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
