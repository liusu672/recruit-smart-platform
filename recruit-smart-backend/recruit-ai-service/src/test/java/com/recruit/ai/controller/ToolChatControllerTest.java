package com.recruit.ai.controller;

import com.recruit.ai.dto.request.ToolChatRequest;
import com.recruit.ai.dto.response.AiStreamEventResponse;
import com.recruit.ai.exception.AiExceptionHandler;
import com.recruit.ai.service.ToolChatService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import reactor.core.publisher.Flux;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ToolChatControllerTest {

    private final ToolChatService toolChatService = new ToolChatService() {
        @Override
        public String chat(ToolChatRequest request) {
            return "2026-07-22";
        }

        @Override
        public Flux<ServerSentEvent<AiStreamEventResponse>> streamChat(
                ToolChatRequest request
        ) {
            return Flux.just(
                    event(AiStreamEventResponse.meta("request-1")),
                    event(AiStreamEventResponse.delta("2026")),
                    event(AiStreamEventResponse.delta("-07-22")),
                    event(AiStreamEventResponse.done())
            );
        }
    };

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

    @Test
    void streamChatReturnsTypedSseEvents() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/ai/tool-chat/stream")
                .contentType("application/json")
                .content("{\"message\":\"今天几号？\"}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("\"type\":\"meta\"")))
                .andExpect(content().string(containsString("\"requestId\":\"request-1\"")))
                .andExpect(content().string(containsString("\"type\":\"delta\"")))
                .andExpect(content().string(containsString("\"content\":\"2026\"")))
                .andExpect(content().string(containsString("\"type\":\"done\"")));
    }

    @Test
    void streamChatCanReturnErrorEvent() throws Exception {
        ToolChatService errorService = new ToolChatService() {
            @Override
            public String chat(ToolChatRequest request) {
                return "unused";
            }

            @Override
            public Flux<ServerSentEvent<AiStreamEventResponse>> streamChat(
                ToolChatRequest request
            ) {
                return Flux.just(event(AiStreamEventResponse.error("500", "stream failed")));
            }
        };
        MockMvc errorMockMvc = MockMvcBuilders
                .standaloneSetup(new ToolChatController(errorService))
                .setControllerAdvice(new AiExceptionHandler())
                .setValidator(validator())
                .build();

        MvcResult result = errorMockMvc.perform(post("/api/ai/tool-chat/stream")
                .contentType("application/json")
                .content("{\"message\":\"今天几号？\"}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        errorMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("\"type\":\"error\"")))
                .andExpect(content().string(containsString("\"message\":\"stream failed\"")));
    }

    private static LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return validator;
    }

    private static ServerSentEvent<AiStreamEventResponse> event(
            AiStreamEventResponse data
    ) {
        return ServerSentEvent.builder(data).build();
    }
}
