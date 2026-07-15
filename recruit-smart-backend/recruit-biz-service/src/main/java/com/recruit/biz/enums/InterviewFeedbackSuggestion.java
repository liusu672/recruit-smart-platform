package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InterviewFeedbackSuggestion {
    PASS("建议通过"),
    REJECT("建议拒绝"),
    PENDING("待定");

    private final String description;

    public static InterviewFeedbackSuggestion fromCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return InterviewFeedbackSuggestion.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
