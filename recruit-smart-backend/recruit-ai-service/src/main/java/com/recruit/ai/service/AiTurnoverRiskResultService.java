package com.recruit.ai.service;

import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import com.recruit.feign.dto.response.TurnoverRiskHistoryResponse;

import java.util.List;

public interface AiTurnoverRiskResultService {

    void saveResult(
            Long taskId,
            TurnoverRiskRequest request,
            TurnoverRiskResponse response,
            String source
    );

    List<TurnoverRiskHistoryResponse> listByEmployeeId(
            Long employeeId
    );
}