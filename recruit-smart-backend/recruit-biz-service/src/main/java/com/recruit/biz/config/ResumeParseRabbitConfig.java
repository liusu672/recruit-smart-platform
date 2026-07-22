package com.recruit.biz.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class ResumeParseRabbitConfig {

    public static final String EXCHANGE = "recruit.resume.exchange";
    public static final String ROUTING_KEY = "resume.parse";
    public static final String QUEUE = "recruit.resume.parse.queue";

    public static final String DEAD_EXCHANGE = "recruit.resume.dlx";
    public static final String DEAD_ROUTING_KEY = "resume.parse.dead";
    public static final String DEAD_QUEUE =
            "recruit.resume.parse.dead.queue";

    @Bean
    public DirectExchange resumeParseExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange resumeParseDeadExchange() {
        return new DirectExchange(DEAD_EXCHANGE, true, false);
    }

    @Bean
    public Queue resumeParseQueue() {
        return QueueBuilder.durable(QUEUE)
                .deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey(DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue resumeParseDeadQueue() {
        return QueueBuilder.durable(DEAD_QUEUE).build();
    }

    @Bean
    public Binding resumeParseBinding(
            @Qualifier("resumeParseQueue") Queue resumeParseQueue,
            @Qualifier("resumeParseExchange")
            DirectExchange resumeParseExchange
    ) {
        return BindingBuilder.bind(resumeParseQueue)
                .to(resumeParseExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public Binding resumeParseDeadBinding(
            @Qualifier("resumeParseDeadQueue") Queue resumeParseDeadQueue,
            @Qualifier("resumeParseDeadExchange")
            DirectExchange resumeParseDeadExchange
    ) {
        return BindingBuilder.bind(resumeParseDeadQueue)
                .to(resumeParseDeadExchange)
                .with(DEAD_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter(
            ObjectMapper objectMapper
    ) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
