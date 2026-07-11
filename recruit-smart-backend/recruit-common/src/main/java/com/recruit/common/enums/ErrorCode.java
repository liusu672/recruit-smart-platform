package com.recruit.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    SUCCESS(200,"success"),
    PARAM_ERROR(400,"参数错误"),
    NOT_FOUND(404,"数据不存在"),
    BUSINESS_ERROR(500,"业务处理失败");
    private final Integer code;
    private final String message;
}
