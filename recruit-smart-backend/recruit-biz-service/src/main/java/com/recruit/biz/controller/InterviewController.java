package com.recruit.biz.controller;

import com.recruit.biz.dto.InterviewCreateDTO;
import com.recruit.biz.dto.InterviewFeedbackCreateDTO;
import com.recruit.biz.dto.InterviewQueryDTO;
import com.recruit.biz.dto.InterviewUpdateDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.InterviewService;
import com.recruit.biz.service.InterviewFeedbackService;
import com.recruit.biz.vo.InterviewSummaryVO;
import com.recruit.biz.vo.InterviewDetailVO;
import com.recruit.biz.vo.InterviewFeedbackVO;
import com.recruit.common.result.PageResult;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "面试安排接口")
@RestController
@RequestMapping("/interviews")
public class InterviewController {

    @Resource
    private InterviewService interviewService;

    @Resource
    private InterviewFeedbackService interviewFeedbackService;

    @PostMapping
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "创建面试安排")
    public Result<Long> create(
            @Valid @RequestBody InterviewCreateDTO dto
    ) {
        return Result.success(
                interviewService.createInterview(dto)
        );
    }

    @GetMapping("/me")
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "候选人查看本人面试列表")
    public Result<PageResult<InterviewSummaryVO>> listMyCandidateInterviews(
            @Valid @ModelAttribute InterviewQueryDTO dto
    ) {
        return Result.success(
                interviewService.listMyCandidateInterviews(dto)
        );
    }

    @GetMapping("/interviewer/me")
    @RequireRoles({"INTERVIEWER"})
    @Operation(summary = "面试官查看本人面试列表")
    public Result<PageResult<InterviewSummaryVO>> listMyInterviewerInterviews(
            @Valid @ModelAttribute InterviewQueryDTO dto
    ) {
        return Result.success(
                interviewService.listMyInterviewerInterviews(dto)
        );
    }

    @GetMapping("/{id}")
    @RequireRoles({"CANDIDATE", "INTERVIEWER", "HR", "ADMIN"})
    @Operation(summary = "查询面试详情")
    public Result<InterviewDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(interviewService.getDetail(id));
    }

    @PutMapping("/{id}")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "修改面试安排")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody InterviewUpdateDTO dto
    ) {
        interviewService.updateInterview(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "取消面试")
    public Result<Void> cancel(@PathVariable Long id) {
        interviewService.cancelInterview(id);
        return Result.success();
    }

    @PutMapping("/{id}/complete")
    @RequireRoles({"ADMIN", "HR", "INTERVIEWER"})
    @Operation(summary = "完成面试")
    public Result<Void> complete(@PathVariable Long id) {
        interviewService.completeInterview(id);
        return Result.success();
    }

    @PostMapping("/{id}/feedback")
    @RequireRoles({"INTERVIEWER"})
    @Operation(summary = "提交面试反馈")
    public Result<Long> submitFeedback(
            @PathVariable Long id,
            @Valid @RequestBody InterviewFeedbackCreateDTO dto
    ) {
        return Result.success(
                interviewFeedbackService.submitFeedback(id, dto)
        );
    }

    @GetMapping("/{id}/feedback")
    @RequireRoles({"ADMIN", "HR", "INTERVIEWER"})
    @Operation(summary = "查询面试反馈")
    public Result<InterviewFeedbackVO> getFeedback(
            @PathVariable Long id
    ) {
        return Result.success(
                interviewFeedbackService.getFeedback(id)
        );
    }
}
