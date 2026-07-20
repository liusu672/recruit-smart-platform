package com.recruit.common.exception;

import com.recruit.common.enums.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {
    @Test
    void storesCodeAndMessage() {
        BusinessException ex = new BusinessException(
                ErrorCode.PARAM_ERROR, "用户名或密码错误"
        );
        assertEquals(400, ex.getCode());
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    @Test
    void isRuntimeException() {
        BusinessException ex = new BusinessException(
                ErrorCode.UNAUTHORIZED, "登录失效"
        );
        assertInstanceOf(RuntimeException.class, ex);
    }
}
