package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobApplicationStatus {
    SUBMITTED("已投递"),
    SCREENING("筛选中"),
    SCREEN_PASSED("筛选通过"),
    INTERVIEWING("面试中"),
    OFFERED("已发放 Offer"),
    HIRED("已入职"),
    SCREEN_REJECT("简历筛选未通过"),
    REJECTED("已拒绝"),
    WITHDRAWN("已撤回");

    private final String description;

    public static JobApplicationStatus fromCode(String code) {
        if (code == null) {
            return null;
        }

        try {
            return JobApplicationStatus.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
