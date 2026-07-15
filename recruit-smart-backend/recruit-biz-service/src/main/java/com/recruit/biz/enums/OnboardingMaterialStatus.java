package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OnboardingMaterialStatus {
    PENDING("待提交"),
    REVIEWING("审核中"),
    APPROVED("已通过"),
    REJECTED("已驳回");

    private final String description;

    public static OnboardingMaterialStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return OnboardingMaterialStatus.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
