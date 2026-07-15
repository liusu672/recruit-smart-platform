package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResumeParseStatus {
    PENDING("等待解析"),
    PROCESSING("解析中"),
    SUCCESS("解析成功"),
    FAILED("解析失败");
    private final String description;
    public static ResumeParseStatus fromCode(String code) {
        if (code == null) {
            return null;
        }

        try {
            return ResumeParseStatus.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
