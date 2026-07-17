package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InterviewRound {
    FIRST(1, "一面"),
    SECOND(2, "二面"),
    HR(3, "HR面");

    private final int order;
    private final String description;

    public static InterviewRound fromCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return InterviewRound.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static InterviewRound fromOrder(int order) {
        for (InterviewRound round : values()) {
            if (round.order == order) {
                return round;
            }
        }
        return null;
    }
}
