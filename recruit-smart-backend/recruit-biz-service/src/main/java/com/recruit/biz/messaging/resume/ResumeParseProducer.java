package com.recruit.biz.messaging.resume;

import com.recruit.biz.config.ResumeParseRabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeParseProducer {

    private final RabbitTemplate rabbitTemplate;

    public boolean send(Long resumeId) {
        ResumeParseMessage message = new ResumeParseMessage(
                resumeId,
                UUID.randomUUID().toString(),
                LocalDateTime.now()
        );
        try {
            rabbitTemplate.convertAndSend(
                    ResumeParseRabbitConfig.EXCHANGE,
                    ResumeParseRabbitConfig.ROUTING_KEY,
                    message
            );
            return true;
        } catch (AmqpException exception) {
            log.error(
                    "发送简历解析消息失败，resumeId={}",
                    resumeId,
                    exception
            );
            return false;
        }
    }
}
