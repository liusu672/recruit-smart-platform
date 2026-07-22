package com.recruit.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiStreamEventResponse {
    String type;
    String requestId;
    String content;
    String code;
    String message;

    public static AiStreamEventResponse meta(String requestId) {
        return AiStreamEventResponse.builder()
                .type("meta")
                .requestId(requestId)
                .build();
    }

    public static AiStreamEventResponse delta(String content) {
        return AiStreamEventResponse.builder()
                .type("delta")
                .content(content)
                .build();
    }

    public static AiStreamEventResponse done() {
        return AiStreamEventResponse.builder()
                .type("done")
                .build();
    }

    public static AiStreamEventResponse error(String code, String message) {
        return AiStreamEventResponse.builder()
                .type("error")
                .code(code)
                .message(message)
                .build();
    }
}
