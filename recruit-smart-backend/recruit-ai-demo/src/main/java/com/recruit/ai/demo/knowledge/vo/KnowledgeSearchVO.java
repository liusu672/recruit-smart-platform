package com.recruit.ai.demo.knowledge.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KnowledgeSearchVO {

    private Boolean success;

    private String query;

    private Integer total;

    private List<KnowledgeSearchResultVO> results;
}