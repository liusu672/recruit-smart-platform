package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.biz.dto.InterviewFeedbackCreateDTO;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.enums.InterviewFeedbackSuggestion;
import com.recruit.biz.enums.InterviewStatus;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.InterviewFeedbackService;
import com.recruit.biz.vo.InterviewFeedbackVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterviewFeedbackServiceImpl
        implements InterviewFeedbackService {

    @Resource
    private InterviewMapper interviewMapper;

    @Resource
    private InterviewFeedbackMapper interviewFeedbackMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitFeedback(
            Long interviewId,
            InterviewFeedbackCreateDTO dto
    ) {
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
                    "只有本场面试官可以提交反馈"
            );
        }

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

        Long feedbackCount = interviewFeedbackMapper.selectCount(
                new LambdaQueryWrapper<InterviewFeedback>()
                        .eq(
                                InterviewFeedback::getInterviewId,
                                interviewId
                        )
        );
        if (feedbackCount > 0) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "该面试已经提交过反馈"
            );
        }

        InterviewFeedback feedback = new InterviewFeedback();
        feedback.setInterviewId(interviewId);
        feedback.setInterviewerId(UserContext.getUserId());
        feedback.setScore(dto.getScore());
        feedback.setComment(dto.getComment().trim());
        feedback.setSuggestion(suggestion.name());

        try {
            interviewFeedbackMapper.insert(feedback);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "该面试已经提交过反馈"
            );
        }

        return feedback.getId();
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
}
