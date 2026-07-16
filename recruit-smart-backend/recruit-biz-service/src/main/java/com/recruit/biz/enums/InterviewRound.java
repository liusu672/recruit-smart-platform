package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InterviewRound {
    FIRST("一面"),
    SECOND("二面"),
    HR_HR("HR面");

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
}
