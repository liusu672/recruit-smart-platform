package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OfferStatus {
    DRAFT("草稿"),
    SENT("已发送"),
    ACCEPTED("候选人已接受"),
    REJECTED("候选人已拒绝"),
    REVOKED("已撤回");

    private final String description;

    public static OfferStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return OfferStatus.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
