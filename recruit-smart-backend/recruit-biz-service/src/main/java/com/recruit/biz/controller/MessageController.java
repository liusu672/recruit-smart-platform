package com.recruit.biz.controller;

import com.recruit.biz.dto.MessageConversationCreateDTO;
import com.recruit.biz.dto.MessagePageQueryDTO;
import com.recruit.biz.dto.MessageSendDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.MessageService;
import com.recruit.biz.vo.MessageConversationSummaryVO;
import com.recruit.biz.vo.MessageRecordVO;
import com.recruit.common.result.PageResult;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "站内消息接口")
@RestController
@RequestMapping("/messages/conversations")
@RequireRoles({"ADMIN", "HR", "INTERVIEWER", "CANDIDATE"})
public class MessageController {

    @Resource
    private MessageService messageService;

    @PostMapping
    @Operation(summary = "创建或获取投递消息会话")
    public Result<Long> getOrCreateConversation(
            @Valid @RequestBody MessageConversationCreateDTO dto
    ) {
        return Result.success(messageService.getOrCreateConversation(dto));
    }

    @GetMapping
    @Operation(summary = "分页查询当前用户可见会话")
    public Result<PageResult<MessageConversationSummaryVO>> listConversations(
            @Valid @ModelAttribute MessagePageQueryDTO dto
    ) {
        return Result.success(messageService.listConversations(dto));
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "分页查询会话消息，按最新消息优先返回")
    public Result<PageResult<MessageRecordVO>> listMessages(
            @PathVariable("conversationId") Long conversationId,
            @Valid @ModelAttribute MessagePageQueryDTO dto
    ) {
        return Result.success(
                messageService.listMessages(conversationId, dto)
        );
    }

    @PostMapping("/{conversationId}/messages")
    @Operation(summary = "发送文本消息")
    public Result<Long> sendMessage(
            @PathVariable("conversationId") Long conversationId,
            @Valid @RequestBody MessageSendDTO dto
    ) {
        return Result.success(
                messageService.sendMessage(conversationId, dto)
        );
    }

    @PutMapping("/{conversationId}/read")
    @Operation(summary = "将当前会话标记为已读")
    public Result<Void> markRead(
            @PathVariable("conversationId") Long conversationId
    ) {
        messageService.markRead(conversationId);
        return Result.success();
    }
}
