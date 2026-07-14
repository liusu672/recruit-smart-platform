package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobPositionStatus {

    DRAFT("草稿"),
    OPEN("招聘中"),
    PAUSED("已暂停"),
    CLOSED("已关闭");

    private final String description;

    public static JobPositionStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return JobPositionStatus.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
