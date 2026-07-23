package com.recruit.feign.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class TurnoverRiskResponse {

    /*
     * 新增情感分析字段。
     */
    private String sentimentLabel;
    private Integer sentimentRiskScore;
    private String sentimentSummary;

    /*
     * 原有综合风险字段。
     */
    private String riskLevel;
    private Integer riskScore;
    private String summary;
    private List<String> riskReasons;
    private List<String> suggestions;
}