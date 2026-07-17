package com.recruit.common.exception;

import com.recruit.common.enums.ErrorCode;
import com.recruit.common.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(
            GlobalExceptionHandler.class
    );

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e
    ) {
        return parameterError(firstValidationMessage(e.getBindingResult()));
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        return parameterError(firstValidationMessage(e.getBindingResult()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(
            ConstraintViolationException e
    ) {
        String message = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .filter(this::hasText)
                .findFirst()
                .orElse(ErrorCode.PARAM_ERROR.getMessage());
        return parameterError(message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(
            MethodArgumentTypeMismatchException e
    ) {
        return parameterError("参数“" + e.getName() + "”格式不正确");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingRequestParameter(
            MissingServletRequestParameterException e
    ) {
        return parameterError("缺少请求参数“" + e.getParameterName() + "”");
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public Result<Void> handleRequestBinding(
            ServletRequestBindingException e
    ) {
        return parameterError("请求参数绑定失败");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(
            HttpMessageNotReadableException e
    ) {
        return parameterError("请求体格式不正确");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<Void> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException e
    ) {
        return parameterError("不支持当前请求数据格式");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e
    ) {
        return parameterError("不支持当前请求方式");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException e
    ) {
        return parameterError("上传文件大小超过限制");
    }

    @ExceptionHandler(MultipartException.class)
    public Result<Void> handleMultipartException(MultipartException e) {
        return parameterError("文件上传请求格式不正确");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolation(
            DataIntegrityViolationException e
    ) {
        log.warn(
                "Database constraint conflict: {}",
                e.getClass().getSimpleName()
        );
        return parameterError("数据已存在或关联数据发生冲突");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnexpectedException(Exception e) {
        log.error("Unexpected request processing error", e);
        return Result.fail(
                ErrorCode.BUSINESS_ERROR.getCode(),
                "系统繁忙，请稍后重试"
        );
    }

    private Result<Void> parameterError(String message) {
        return Result.fail(
                ErrorCode.PARAM_ERROR.getCode(),
                message
        );
    }

    private String firstValidationMessage(BindingResult bindingResult) {
        return bindingResult.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(this::hasText)
                .findFirst()
                .orElse(ErrorCode.PARAM_ERROR.getMessage());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
