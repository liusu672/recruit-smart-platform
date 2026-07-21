package com.recruit.biz.messaging.resume;

import java.time.LocalDateTime;

public record ResumeParseMessage(
        Long resumeId,
        String requestId,
        LocalDateTime createdAt
) {
}
