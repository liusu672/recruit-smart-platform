package com.recruit.ai.knowledge.service;

import com.recruit.ai.knowledge.dto.KnowledgeBuildRequest;
import com.recruit.ai.knowledge.dto.KnowledgeBuildResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResponse;

public interface KnowledgeBaseService {
    KnowledgeBuildResponse buildKnowledgeBase(KnowledgeBuildRequest request);

    KnowledgeSearchResponse searchKnowledge(String query, Integer topK);
}