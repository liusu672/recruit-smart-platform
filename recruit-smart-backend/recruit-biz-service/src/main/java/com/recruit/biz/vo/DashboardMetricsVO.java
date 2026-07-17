package com.recruit.biz.vo;

import lombok.Data;

@Data
public class DashboardMetricsVO {
    private Long pendingScreening;
    private Long pendingFeedback;
    private Long activeOffers;
    private Long reviewingOnboardings;
}
