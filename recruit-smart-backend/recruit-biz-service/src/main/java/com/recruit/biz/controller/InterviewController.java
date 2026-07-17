package com.recruit.biz.controller;

import com.recruit.biz.dto.InterviewCreateDTO;
import com.recruit.biz.dto.InterviewFeedbackCreateDTO;
import com.recruit.biz.dto.InterviewFeedbackDraftDTO;
import com.recruit.biz.dto.InterviewQueryDTO;
import com.recruit.biz.dto.InterviewScheduleDTO;
import com.recruit.biz.dto.InterviewTaskQueryDTO;
import com.recruit.biz.dto.InterviewUpdateDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.InterviewService;
import com.recruit.biz.service.InterviewFeedbackService;
import com.recruit.biz.service.InterviewWorkspaceService;
import com.recruit.biz.vo.InterviewSummaryVO;
import com.recruit.biz.vo.InterviewTaskSummaryVO;
import com.recruit.biz.vo.InterviewDetailVO;
import com.recruit.biz.vo.InterviewFeedbackVO;
import com.recruit.biz.vo.InterviewWorkspaceVO;
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

    @Resource
    private InterviewWorkspaceService interviewWorkspaceService;

    @PostMapping
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "HR指派面试官")
    public Result<Long> create(
            @Valid @RequestBody InterviewCreateDTO dto
    ) {
        return Result.success(
                interviewService.createInterview(dto)
        );
    }

    @PutMapping("/{id}/schedule")
    @RequireRoles({"INTERVIEWER"})
    @Operation(summary = "面试官预约并确认面试")
    public Result<Void> schedule(
            @PathVariable("id") Long id,
            @Valid @RequestBody InterviewScheduleDTO dto
    ) {
        interviewService.scheduleInterview(id, dto);
        return Result.success();
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

    @GetMapping("/tasks")
    @RequireRoles({"INTERVIEWER", "HR", "ADMIN"})
    @Operation(summary = "分页查询面试工作台任务")
    public Result<PageResult<InterviewTaskSummaryVO>> listTasks(
            @Valid @ModelAttribute InterviewTaskQueryDTO dto
    ) {
        return Result.success(
                interviewWorkspaceService.listTasks(dto)
        );
    }

    @GetMapping("/{id}")
    @RequireRoles({"CANDIDATE", "INTERVIEWER", "HR", "ADMIN"})
    @Operation(summary = "查询面试详情")
    public Result<InterviewDetailVO> getDetail(@PathVariable("id") Long id) {
        return Result.success(interviewService.getDetail(id));
    }

    @GetMapping("/{id}/workspace")
    @RequireRoles({"INTERVIEWER", "HR", "ADMIN"})
    @Operation(summary = "查询面试工作台聚合数据")
    public Result<InterviewWorkspaceVO> getWorkspace(
            @PathVariable("id") Long id
    ) {
        return Result.success(
                interviewWorkspaceService.getWorkspace(id)
        );
    }

    @PutMapping("/{id}")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "HR重新指派面试官")
    public Result<Void> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody InterviewUpdateDTO dto
    ) {
        interviewService.updateInterview(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "取消面试")
    public Result<Void> cancel(@PathVariable("id") Long id) {
        interviewService.cancelInterview(id);
        return Result.success();
    }

    @PutMapping("/{id}/complete")
    @RequireRoles({"ADMIN", "HR", "INTERVIEWER"})
    @Operation(summary = "完成面试")
    public Result<Void> complete(@PathVariable("id") Long id) {
        interviewService.completeInterview(id);
        return Result.success();
    }

    @PostMapping("/{id}/feedback")
    @RequireRoles({"INTERVIEWER"})
    @Operation(summary = "提交面试反馈")
    public Result<Long> submitFeedback(
            @PathVariable("id") Long id,
            @Valid @RequestBody InterviewFeedbackCreateDTO dto
    ) {
        return Result.success(
                interviewFeedbackService.submitFeedback(id, dto)
        );
    }

    @PutMapping("/{id}/feedback/draft")
    @RequireRoles({"INTERVIEWER"})
    @Operation(summary = "保存面试反馈草稿")
    public Result<Void> saveFeedbackDraft(
            @PathVariable("id") Long id,
            @Valid @RequestBody InterviewFeedbackDraftDTO dto
    ) {
        interviewFeedbackService.saveDraft(id, dto);
        return Result.success();
    }

    @GetMapping("/{id}/feedback")
    @RequireRoles({"ADMIN", "HR", "INTERVIEWER"})
    @Operation(summary = "查询面试反馈")
    public Result<InterviewFeedbackVO> getFeedback(
            @PathVariable("id") Long id
    ) {
        return Result.success(
                interviewFeedbackService.getFeedback(id)
        );
    }
}
