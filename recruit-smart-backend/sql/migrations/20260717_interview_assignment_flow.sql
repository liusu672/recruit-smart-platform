USE recruit_smart;

-- Existing databases only: split HR assignment from interviewer scheduling.
ALTER TABLE interview
  MODIFY interview_time DATETIME NULL COMMENT '面试官确认的面试时间，待预约时为空',
  MODIFY method VARCHAR(32) NULL COMMENT '面试方式：ONLINE线上，OFFLINE线下，PHONE电话',
  MODIFY status VARCHAR(32) NOT NULL DEFAULT 'ASSIGNED' COMMENT '状态：ASSIGNED待预约，SCHEDULED待面试，COMPLETED已完成，CANCELED已取消，REINTERVIEW为旧版复试状态',
  ADD COLUMN assigned_at DATETIME NULL COMMENT 'HR指派时间' AFTER created_by,
  ADD COLUMN scheduled_at DATETIME NULL COMMENT '面试官确认预约时间' AFTER assigned_at;

UPDATE interview
SET assigned_at = COALESCE(assigned_at, created_at),
    scheduled_at = CASE
      WHEN status = 'ASSIGNED' THEN NULL
      ELSE COALESCE(scheduled_at, updated_at, created_at)
    END;

ALTER TABLE interview
  MODIFY assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'HR指派时间';
