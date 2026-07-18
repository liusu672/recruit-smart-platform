package com.recruit.ai.service;

import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;

public interface AiTurnoverRiskResultService {

    void saveResult(Long taskId,
                    TurnoverRiskRequest request,
                    TurnoverRiskResponse response,
                    String source);
}