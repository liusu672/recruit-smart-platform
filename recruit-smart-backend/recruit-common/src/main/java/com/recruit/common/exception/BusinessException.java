package com.recruit.common.exception;

import com.recruit.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{
    private final Integer code;
    public BusinessException(ErrorCode errorCode,String message){
        super(message);
        this.code= errorCode.getCode();
    }

}
