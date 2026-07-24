package com.recruit.ai.exception;

import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.Result;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AiExceptionHandlerTest {

    private final AiExceptionHandler handler = new AiExceptionHandler();

    @Test
    void mapsParameterErrorToHttpBadRequest() {
        ResponseEntity<Result<Void>> response =
                handler.handleBusinessException(
                        new BusinessException(
                                ErrorCode.PARAM_ERROR,
                                "文件格式错误"
                        )
                );

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCode());
        assertEquals("文件格式错误", response.getBody().getMessage());
    }
}
