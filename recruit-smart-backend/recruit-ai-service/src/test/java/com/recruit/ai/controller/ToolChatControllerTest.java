package com.recruit.ai.controller;

import com.recruit.ai.exception.AiExceptionHandler;
import com.recruit.ai.service.ToolChatService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ToolChatControllerTest {

    private final ToolChatService toolChatService = request -> "2026-07-22";

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ToolChatController(toolChatService))
            .setControllerAdvice(new AiExceptionHandler())
            .setValidator(validator())
            .build();

    @Test
    void chatReturnsRawStringResponse() throws Exception {
        mockMvc.perform(post("/api/ai/tool-chat")
                .contentType("application/json")
                        .content("{\"message\":\"今天几号？\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("2026-07-22"));
    }

    @Test
    void chatRejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/api/ai/tool-chat")
                .contentType("application/json")
                        .content("{\"message\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return validator;
    }
}
