package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OnboardingStatus {
    PENDING("待提交"),
    REVIEWING("审核中"),
    APPROVED("已通过"),
    ONBOARDED("已入职"),
    CANCELED("已取消");

    private final String description;

    public static OnboardingStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return OnboardingStatus.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
