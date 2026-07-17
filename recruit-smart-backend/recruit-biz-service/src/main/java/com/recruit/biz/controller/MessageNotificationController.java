package com.recruit.biz.controller;

import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.MessageRealtimeService;
import com.recruit.biz.service.MessageService;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "消息通知接口")
@RestController
@RequestMapping("/messages")
@RequireRoles({"ADMIN", "HR", "INTERVIEWER", "CANDIDATE"})
public class MessageNotificationController {

    @Resource
    private MessageService messageService;

    @Resource
    private MessageRealtimeService messageRealtimeService;

    @GetMapping("/unread-count")
    @Operation(summary = "查询当前用户全部未读消息数量")
    public Result<Long> countUnreadMessages() {
        return Result.success(messageService.countUnreadMessages());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "订阅消息变化SSE事件")
    public SseEmitter subscribe() {
        return messageRealtimeService.subscribe(UserContext.getUserId());
    }
}
