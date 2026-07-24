package com.recruit.ai;

import com.recruit.ai.config.AiHttpClientConfig;
import com.recruit.ai.config.AiToolConfig;
import com.recruit.ai.config.Knife4jConfig;
import com.recruit.ai.exception.AiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.junit.jupiter.api.Assertions.*;

class AiInfrastructureTest {

    @Test
    void aiExceptionHandler_hasRestControllerAdvice() {
        assertTrue(AiExceptionHandler.class.isAnnotationPresent(RestControllerAdvice.class));
    }

    @Test
    void aiHttpClientConfig_classLoads() {
        assertNotNull(AiHttpClientConfig.class);
    }

    @Test
    void aiToolConfig_classLoads() {
        assertNotNull(AiToolConfig.class);
    }

    @Test
    void knife4jConfig_classLoads() {
        assertNotNull(Knife4jConfig.class);
    }
}
