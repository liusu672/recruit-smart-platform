package com.recruit.ai.exception;

import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice(basePackages = "com.recruit.ai")
@Slf4j
public class AiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(
            BusinessException exception
    ) {
        HttpStatus status = HttpStatus.resolve(exception.getCode());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (status.is5xxServerError()) {
            log.error("AI业务处理失败: {}", exception.getMessage());
        }
        return ResponseEntity.status(status).body(
                Result.fail(exception.getCode(), exception.getMessage())
        );
    }

    @ExceptionHandler({
            MissingServletRequestPartException.class,
            MaxUploadSizeExceededException.class,
            MultipartException.class
    })
    public ResponseEntity<Result<Void>> handleMultipartException(
            Exception exception
    ) {
        return ResponseEntity.badRequest().body(
                Result.fail(
                        ErrorCode.PARAM_ERROR.getCode(),
                        "简历文件上传请求不正确或文件超过大小限制"
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpectedException(
            Exception exception
    ) {
        log.error("AI服务发生未处理异常", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Result.fail(
                        ErrorCode.BUSINESS_ERROR.getCode(),
                        "AI服务繁忙，请稍后重试"
                )
        );
    }
}
