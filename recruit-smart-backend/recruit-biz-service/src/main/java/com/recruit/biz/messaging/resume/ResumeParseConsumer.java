package com.recruit.biz.messaging.resume;

import com.recruit.biz.config.ResumeParseRabbitConfig;
import com.recruit.biz.service.ResumeParsingService;
import com.recruit.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeParseConsumer {

    private final ResumeParsingService resumeParsingService;

    @RabbitListener(queues = ResumeParseRabbitConfig.QUEUE)
    public void consume(ResumeParseMessage message) {
        if (message == null || message.resumeId() == null) {
            throw new AmqpRejectAndDontRequeueException(
                    "简历解析消息缺少resumeId"
            );
        }

        try {
            resumeParsingService.parseAutomatically(message.resumeId());
        } catch (BusinessException exception) {
            if (exception.getCode() < 500) {
                log.warn(
                        "简历解析消息不可重试，resumeId={}, code={}",
                        message.resumeId(),
                        exception.getCode()
                );
                throw new AmqpRejectAndDontRequeueException(
                        "简历解析任务不可重试",
                        exception
                );
            }
            throw exception;
        }
    }
}
