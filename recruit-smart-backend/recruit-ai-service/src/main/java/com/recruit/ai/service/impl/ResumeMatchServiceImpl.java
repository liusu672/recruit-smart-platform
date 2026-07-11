package com.recruit.ai.service.impl;

import com.recruit.ai.algorithm.match.ResumeMatchAlgorithm;
import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import com.recruit.ai.service.ResumeMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResumeMatchServiceImpl implements ResumeMatchService {

    private final ResumeMatchAlgorithm resumeMatchAlgorithm;

    @Override
    public ResumeMatchResponse matchResume(ResumeMatchRequest request) {
        return resumeMatchAlgorithm.match(request);
    }
}
