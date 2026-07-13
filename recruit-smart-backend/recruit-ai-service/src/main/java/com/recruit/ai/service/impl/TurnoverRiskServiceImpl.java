package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.risk.TurnoverRiskAlgorithm;
import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import com.recruit.ai.service.TurnoverRiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TurnoverRiskServiceImpl implements TurnoverRiskService {

    private final TurnoverRiskAlgorithm turnoverRiskAlgorithm;

    @Override
    public TurnoverRiskResponse predictRisk(TurnoverRiskRequest request) {
        return turnoverRiskAlgorithm.predict(request);
    }
}
