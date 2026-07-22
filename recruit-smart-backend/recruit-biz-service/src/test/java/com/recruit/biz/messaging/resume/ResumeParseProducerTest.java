package com.recruit.biz.messaging.resume;

import com.recruit.biz.config.ResumeParseRabbitConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResumeParseProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ResumeParseProducer producer;

    @Test
    void sendsResumeIdWithoutFileContent() {
        assertTrue(producer.send(100L));

        ArgumentCaptor<ResumeParseMessage> captor =
                ArgumentCaptor.forClass(ResumeParseMessage.class);
        verify(rabbitTemplate).convertAndSend(
                eq(ResumeParseRabbitConfig.EXCHANGE),
                eq(ResumeParseRabbitConfig.ROUTING_KEY),
                captor.capture()
        );
        ResumeParseMessage message = captor.getValue();
        assertEquals(100L, message.resumeId());
        assertNotNull(message.requestId());
        assertNotNull(message.createdAt());
    }

    @Test
    void keepsUploadRecoverableWhenBrokerIsUnavailable() {
        doThrow(new AmqpException("broker unavailable"))
                .when(rabbitTemplate)
                .convertAndSend(
                        any(String.class),
                        any(String.class),
                        any(ResumeParseMessage.class)
                );

        assertFalse(producer.send(100L));
    }
}
