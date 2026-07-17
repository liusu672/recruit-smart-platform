package com.recruit.common.exception;

import com.recruit.common.enums.ErrorCode;
import com.recruit.common.result.Result;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void businessExceptionKeepsBusinessCodeAndMessage() {
        Result<Void> result = handler.handleBusinessException(
                new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "候选人不存在"
                )
        );

        assertEquals(ErrorCode.NOT_FOUND.getCode(), result.getCode());
        assertEquals("候选人不存在", result.getMessage());
    }

    @Test
    void bindExceptionReturnsFirstValidationMessage() {
        BindException exception = new BindException(
                new ValidationTarget(),
                "dto"
        );
        exception.rejectValue(
                "name",
                "NotBlank",
                "姓名不能为空"
        );

        Result<Void> result = handler.handleBindException(exception);

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), result.getCode());
        assertEquals("姓名不能为空", result.getMessage());
    }

    @Test
    void uploadLimitReturnsParameterError() {
        Result<Void> result = handler.handleMaxUploadSizeExceeded(
                new MaxUploadSizeExceededException(10L)
        );

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), result.getCode());
        assertEquals("上传文件大小超过限制", result.getMessage());
    }

    @Test
    void databaseConflictDoesNotExposeDatabaseMessage() {
        Result<Void> result = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException(
                        "Duplicate entry 'secret' for key 'uk_phone'"
                )
        );

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), result.getCode());
        assertFalse(result.getMessage().contains("secret"));
    }

    @Test
    void unexpectedExceptionDoesNotExposeInternalMessage() {
        Result<Void> result = handler.handleUnexpectedException(
                new IllegalStateException("internal details")
        );

        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), result.getCode());
        assertEquals("系统繁忙，请稍后重试", result.getMessage());
    }

    private static class ValidationTarget {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
