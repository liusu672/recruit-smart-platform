package com.recruit.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorCodeTest {
    @Test
    void valuesHaveCorrectCodeAndMessage() {
        assertEquals(200, ErrorCode.SUCCESS.getCode());
        assertEquals("success", ErrorCode.SUCCESS.getMessage());
        assertEquals(400, ErrorCode.PARAM_ERROR.getCode());
        assertEquals("参数错误", ErrorCode.PARAM_ERROR.getMessage());
        assertEquals(401, ErrorCode.UNAUTHORIZED.getCode());
        assertEquals("未登录或登录状态失效", ErrorCode.UNAUTHORIZED.getMessage());
        assertEquals(403, ErrorCode.FORBIDDEN.getCode());
        assertEquals("没有操作权限", ErrorCode.FORBIDDEN.getMessage());
        assertEquals(404, ErrorCode.NOT_FOUND.getCode());
        assertEquals("数据不存在", ErrorCode.NOT_FOUND.getMessage());
        assertEquals(500, ErrorCode.BUSINESS_ERROR.getCode());
        assertEquals("业务处理失败", ErrorCode.BUSINESS_ERROR.getMessage());
    }
}
