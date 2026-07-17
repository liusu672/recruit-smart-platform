package com.recruit.biz.vo;

import lombok.Data;

import java.util.List;

@Data
public class DashboardOverviewVO {
    private DashboardMetricsVO metrics;
    private List<DashboardTaskVO> tasks;
}
