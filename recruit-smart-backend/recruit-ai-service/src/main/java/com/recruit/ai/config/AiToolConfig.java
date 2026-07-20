package com.recruit.ai.config;

import com.recruit.ai.tools.AiInterviewQuestionQueryTools;
import com.recruit.ai.tools.AiMatchResultQueryTools;
import com.recruit.ai.tools.AiTaskQueryTools;
import com.recruit.ai.tools.DateTimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiToolConfig {

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel,
                                 DateTimeTools dateTimeTools,
                                 AiTaskQueryTools aiTaskQueryTools,
                                 AiMatchResultQueryTools aiMatchResultQueryTools,
                                 AiInterviewQuestionQueryTools aiInterviewQuestionQueryTools) {
        return ChatClient.builder(chatModel)
                .defaultTools(
                        dateTimeTools,
                        aiTaskQueryTools,
                        aiMatchResultQueryTools,
                        aiInterviewQuestionQueryTools
                )
                .build();
    }
}