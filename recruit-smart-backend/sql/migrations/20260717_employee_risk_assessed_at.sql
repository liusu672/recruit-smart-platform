USE recruit_smart;

ALTER TABLE employee_profile
  ADD COLUMN risk_assessed_at DATETIME NULL COMMENT '最近一次离职风险评估时间'
  AFTER turnover_risk_level;

UPDATE employee_profile
SET risk_assessed_at = COALESCE(updated_at, created_at)
WHERE turnover_risk_level IS NOT NULL
  AND risk_assessed_at IS NULL;
