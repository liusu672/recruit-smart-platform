package com.recruit.biz.service;

import com.recruit.biz.dto.MessageConversationCreateDTO;
import com.recruit.biz.dto.MessagePageQueryDTO;
import com.recruit.biz.dto.MessageSendDTO;
import com.recruit.biz.vo.MessageConversationSummaryVO;
import com.recruit.biz.vo.MessageRecordVO;
import com.recruit.common.result.PageResult;

public interface MessageService {
    Long getOrCreateConversation(MessageConversationCreateDTO dto);

    PageResult<MessageConversationSummaryVO> listConversations(
            MessagePageQueryDTO dto
    );

    PageResult<MessageRecordVO> listMessages(
            Long conversationId,
            MessagePageQueryDTO dto
    );

    Long sendMessage(Long conversationId, MessageSendDTO dto);

    void markRead(Long conversationId);

    Long countUnreadMessages();

    Long sendSystemMessage(Long applicationId, String content);
}
