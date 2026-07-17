USE recruit_smart;

CREATE TABLE message_conversation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
  application_id BIGINT NOT NULL COMMENT '关联投递记录ID',
  last_message_preview VARCHAR(255) DEFAULT NULL COMMENT '最后一条消息摘要',
  last_message_at DATETIME DEFAULT NULL COMMENT '最后一条消息时间',
  created_by BIGINT NOT NULL COMMENT '会话创建人用户ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_message_conversation_application_id (application_id),
  KEY idx_message_conversation_last_message_at (last_message_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='招聘流程消息会话表';

CREATE TABLE message_conversation_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话成员ID',
  conversation_id BIGINT NOT NULL COMMENT '会话ID',
  user_id BIGINT NOT NULL COMMENT '成员用户ID',
  role_code VARCHAR(32) NOT NULL COMMENT '加入会话时的角色编码',
  last_read_at DATETIME DEFAULT NULL COMMENT '最后已读时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_message_member_conversation_user (conversation_id, user_id),
  KEY idx_message_member_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息会话成员表';

CREATE TABLE message_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
  conversation_id BIGINT NOT NULL COMMENT '会话ID',
  sender_id BIGINT NOT NULL COMMENT '发送人用户ID',
  sender_role VARCHAR(32) NOT NULL COMMENT '发送人角色编码',
  content VARCHAR(5000) NOT NULL COMMENT '消息正文',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  KEY idx_message_record_conversation_id (conversation_id, id),
  KEY idx_message_record_sender_id (sender_id),
  KEY idx_message_record_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内消息记录表';
