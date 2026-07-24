package com.recruit.biz.messaging.resume;

import com.recruit.biz.service.ResumeParsingService;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResumeParseConsumerTest {

    @Mock
    private ResumeParsingService resumeParsingService;

    @InjectMocks
    private ResumeParseConsumer consumer;

    @Test
    void delegatesMessageToAutomaticParsing() {
        ResumeParseMessage message = message(100L);

        consumer.consume(message);

        verify(resumeParsingService).parseAutomatically(100L);
    }

    @Test
    void rejectsPermanentBusinessErrorWithoutRetry() {
        doThrow(new BusinessException(
                ErrorCode.PARAM_ERROR,
                "文件无法解析"
        )).when(resumeParsingService).parseAutomatically(100L);

        assertThrows(
                AmqpRejectAndDontRequeueException.class,
                () -> consumer.consume(message(100L))
        );
    }

    @Test
    void propagatesTransientErrorForConfiguredRetry() {
        BusinessException exception = new BusinessException(
                ErrorCode.BUSINESS_ERROR,
                "AI暂不可用"
        );
        doThrow(exception)
                .when(resumeParsingService)
                .parseAutomatically(100L);

        BusinessException actual = assertThrows(
                BusinessException.class,
                () -> consumer.consume(message(100L))
        );
        assertSame(exception, actual);
    }

    @Test
    void rejectsMalformedMessage() {
        assertThrows(
                AmqpRejectAndDontRequeueException.class,
                () -> consumer.consume(message(null))
        );
    }

    private ResumeParseMessage message(Long resumeId) {
        return new ResumeParseMessage(
                resumeId,
                "request-1",
                LocalDateTime.now()
        );
    }
}
