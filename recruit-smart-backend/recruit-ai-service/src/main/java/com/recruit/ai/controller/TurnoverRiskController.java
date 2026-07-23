package com.recruit.ai.controller;

import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import com.recruit.ai.service.TurnoverRiskService;
import com.recruit.ai.service.AiTurnoverRiskResultService;
import com.recruit.feign.dto.response.TurnoverRiskHistoryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI离职风险预测接口", description = "根据员工绩效、考勤和反馈信息预测离职风险")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class TurnoverRiskController {

    private final TurnoverRiskService turnoverRiskService;
    private final AiTurnoverRiskResultService
            aiTurnoverRiskResultService;

    @Operation(summary = "预测离职风险", description = "返回员工离职风险等级、风险原因和干预建议")
    @PostMapping("/turnover-risk")
    public TurnoverRiskResponse predictRisk(@RequestBody TurnoverRiskRequest request) {
        return turnoverRiskService.predictRisk(request);
    }

    @GetMapping(
            "/turnover-risk/employees/{employeeId}/history"
    )
    @Operation(summary = "查询员工历史离职风险结果")
    public List<TurnoverRiskHistoryResponse> listHistory(
            @PathVariable("employeeId") Long employeeId
    ) {
        return aiTurnoverRiskResultService
                .listByEmployeeId(employeeId);
    }
}
