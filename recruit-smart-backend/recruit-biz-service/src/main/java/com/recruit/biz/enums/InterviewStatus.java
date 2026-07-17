package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InterviewStatus {
    ASSIGNED("待面试官预约"),
    SCHEDULED("待面试"),
    COMPLETED("已完成"),
    CANCELED("已取消"),
    REINTERVIEW("需要复试");

    private final String description;

    public static InterviewStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return InterviewStatus.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
