package com.recruit.ai.service;

import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;

public interface TurnoverRiskService {
    TurnoverRiskResponse predictRisk(TurnoverRiskRequest request);
}
