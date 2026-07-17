USE recruit_smart;

ALTER TABLE job_position
  ADD COLUMN required_interview_rounds TINYINT UNSIGNED NOT NULL DEFAULT 1
  COMMENT '要求面试轮数：1一面，2一面加二面，3一面加二面加HR面'
  AFTER headcount;

ALTER TABLE job_position
  ADD CONSTRAINT chk_job_required_interview_rounds
  CHECK (required_interview_rounds BETWEEN 1 AND 3);

UPDATE job_position
SET required_interview_rounds = CASE id
  WHEN 2 THEN 2
  ELSE 1
END
WHERE id IN (1, 2, 3);
