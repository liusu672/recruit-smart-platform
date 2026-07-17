USE recruit_smart;

ALTER TABLE message_conversation
  MODIFY COLUMN created_by BIGINT NULL
  COMMENT '会话创建人用户ID，系统自动创建时为空';

ALTER TABLE message_record
  MODIFY COLUMN sender_id BIGINT NULL
  COMMENT '发送人用户ID，系统消息为空',
  ADD COLUMN message_type VARCHAR(32) NOT NULL DEFAULT 'TEXT'
  COMMENT '消息类型：TEXT文本，SYSTEM系统通知，FILE附件'
  AFTER sender_role;
