package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InterviewMethod {
    ONLINE("线上"),
    OFFLINE("线下"),
    PHONE("电话");

    private final String description;

    public static InterviewMethod fromCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return InterviewMethod.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
