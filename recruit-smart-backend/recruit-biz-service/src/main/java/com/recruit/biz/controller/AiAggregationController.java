package com.recruit.biz.controller;

import com.recruit.biz.dto.InterviewAiQuestionDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.AiAggregationService;
import com.recruit.biz.vo.AiMatchSummaryVO;
import com.recruit.common.result.Result;
import com.recruit.feign.dto.response.FeedbackSummaryResponse;
import com.recruit.feign.dto.response.InterviewQuestionResponse;
import com.recruit.feign.dto.response.TurnoverRiskResponse;
import com.recruit.feign.dto.response.TurnoverRiskHistoryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI业务聚合接口")
@RestController
@RequiredArgsConstructor
public class AiAggregationController {

    private final AiAggregationService aiAggregationService;

    @PostMapping("/applications/{id}/ai-match")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "生成简历与职位匹配结果")
    public Result<AiMatchSummaryVO> generateResumeMatch(
            @PathVariable("id") Long id
    ) {
        return Result.success(
                aiAggregationService.generateResumeMatch(id)
        );
    }

    @PostMapping("/interviews/{id}/ai-questions")
    @RequireRoles({"ADMIN", "HR", "INTERVIEWER"})
    @Operation(summary = "生成面试问题")
    public Result<InterviewQuestionResponse> generateInterviewQuestions(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) InterviewAiQuestionDTO dto
    ) {
        return Result.success(
                aiAggregationService.generateInterviewQuestions(id, dto)
        );
    }

    @PostMapping("/interviews/{id}/ai-summary")
    @RequireRoles({"ADMIN", "HR", "INTERVIEWER"})
    @Operation(summary = "生成面试反馈摘要")
    public Result<FeedbackSummaryResponse> generateFeedbackSummary(
            @PathVariable("id") Long id
    ) {
        return Result.success(
                aiAggregationService.generateFeedbackSummary(id)
        );
    }

    @PostMapping("/employees/{id}/turnover-risk")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "评估员工离职风险")
    public Result<TurnoverRiskResponse> assessTurnoverRisk(
            @PathVariable("id") Long id
    ) {
        return Result.success(
                aiAggregationService.assessTurnoverRisk(id)
        );
    }

    @GetMapping("/employees/{id}/turnover-risks")
    @RequireRoles({"ADMIN", "HR"})
    @Operation(summary = "查询员工历史离职风险结果")
    public Result<List<TurnoverRiskHistoryResponse>>
    listTurnoverRiskHistory(
            @PathVariable("id") Long id
    ) {
        return Result.success(
                aiAggregationService
                        .listTurnoverRiskHistory(id)
        );
    }
}
