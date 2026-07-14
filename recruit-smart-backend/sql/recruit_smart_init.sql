-- Recruit Smart Platform initial database schema and seed data.
-- MySQL 8.x

CREATE DATABASE IF NOT EXISTS recruit_smart
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE recruit_smart;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS employee_profile;
DROP TABLE IF EXISTS onboarding;
DROP TABLE IF EXISTS offer;
DROP TABLE IF EXISTS interview_feedback;
DROP TABLE IF EXISTS interview;
DROP TABLE IF EXISTS ai_match_result;
DROP TABLE IF EXISTS job_application;
DROP TABLE IF EXISTS resume;
DROP TABLE IF EXISTS candidate;
DROP TABLE IF EXISTS job_position;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. Role table.
CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
  role_code VARCHAR(32) NOT NULL COMMENT '角色编码',
  role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
  description VARCHAR(255) DEFAULT NULL COMMENT '角色说明',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- 2. User account table.
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(64) NOT NULL COMMENT '登录账号',
  password VARCHAR(255) NOT NULL COMMENT '登录密码，正式项目应存储BCrypt密文',
  real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
  phone VARCHAR(32) DEFAULT NULL COMMENT '手机号',
  email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_sys_user_username (username),
  UNIQUE KEY uk_sys_user_phone (phone),
  KEY idx_sys_user_role_id (role_id),
  KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 3. Job position table.
CREATE TABLE job_position (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '职位ID',
  title VARCHAR(128) NOT NULL COMMENT '职位名称',
  department VARCHAR(128) NOT NULL COMMENT '所属部门',
  location VARCHAR(128) DEFAULT NULL COMMENT '工作地点',
  salary_min DECIMAL(10,2) DEFAULT NULL COMMENT '最低薪资',
  salary_max DECIMAL(10,2) DEFAULT NULL COMMENT '最高薪资',
  headcount INT NOT NULL DEFAULT 1 COMMENT '招聘人数',
  responsibilities TEXT COMMENT '岗位职责',
  requirements TEXT COMMENT '任职要求',
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '职位状态：DRAFT草稿，OPEN招聘中，PAUSED暂停，CLOSED关闭',
  created_by BIGINT NOT NULL COMMENT '创建人用户ID',
  published_at DATETIME DEFAULT NULL COMMENT '发布时间',
  closed_at DATETIME DEFAULT NULL COMMENT '关闭时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_job_position_status (status),
  KEY idx_job_position_department (department),
  KEY idx_job_position_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职位表';

-- 4. Candidate table.
CREATE TABLE candidate (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '候选人ID',
  user_id BIGINT DEFAULT NULL COMMENT '绑定的系统用户ID，可为空',
  name VARCHAR(64) NOT NULL COMMENT '候选人姓名',
  gender VARCHAR(16) DEFAULT NULL COMMENT '性别',
  age INT DEFAULT NULL COMMENT '年龄',
  phone VARCHAR(32) DEFAULT NULL COMMENT '手机号',
  email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  education VARCHAR(64) DEFAULT NULL COMMENT '最高学历',
  school VARCHAR(128) DEFAULT NULL COMMENT '毕业学校',
  major VARCHAR(128) DEFAULT NULL COMMENT '专业',
  years_of_experience INT DEFAULT 0 COMMENT '工作年限',
  current_status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE' COMMENT '当前状态：AVAILABLE可应聘，INTERVIEWING面试中，HIRED已入职',
  source VARCHAR(64) DEFAULT NULL COMMENT '来源：SELF_REGISTER自注册，HR_IMPORT手动录入等',
  created_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_candidate_user_id (user_id),
  UNIQUE KEY uk_candidate_phone (phone),
  KEY idx_candidate_name (name),
  KEY idx_candidate_education (education),
  KEY idx_candidate_status (current_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='候选人表';

-- 5. Resume table.
CREATE TABLE resume (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '简历ID',
  candidate_id BIGINT NOT NULL COMMENT '候选人ID',
  resume_name VARCHAR(128) NOT NULL COMMENT '简历名称',
  file_url VARCHAR(255) DEFAULT NULL COMMENT '简历文件地址',
  file_type VARCHAR(32) DEFAULT NULL COMMENT '文件类型',
  parsed_content TEXT COMMENT '解析后的简历文本',
  skills TEXT COMMENT '技能关键词',
  project_experience TEXT COMMENT '项目经历摘要',
  work_experience TEXT COMMENT '工作经历摘要',
  is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认简历：1是，0否',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_resume_candidate_id (candidate_id),
  KEY idx_resume_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历表';

-- 6. Job application table. This is the center table of recruitment workflow.
CREATE TABLE job_application (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '投递记录ID',
  job_id BIGINT NOT NULL COMMENT '职位ID',
  candidate_id BIGINT NOT NULL COMMENT '候选人ID',
  resume_id BIGINT NOT NULL COMMENT '投递使用的简历ID',
  status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED' COMMENT '投递状态',
  allow_adjustment TINYINT NOT NULL DEFAULT 0 COMMENT '是否接受岗位调剂：1是，0否',
  adjusted_job_id BIGINT DEFAULT NULL COMMENT '调剂职位ID',
  source VARCHAR(64) DEFAULT 'ONLINE' COMMENT '投递来源',
  hr_note VARCHAR(500) DEFAULT NULL COMMENT 'HR备注',
  reject_reason_code VARCHAR(64) DEFAULT NULL COMMENT '拒绝原因编码',
  reject_reason VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因说明',
  reviewed_by BIGINT DEFAULT NULL COMMENT '筛选人用户ID',
  reviewed_at DATETIME DEFAULT NULL COMMENT '筛选时间',
  applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_application_candidate_job (candidate_id, job_id),
  KEY idx_application_job_id (job_id),
  KEY idx_application_candidate_id (candidate_id),
  KEY idx_application_resume_id (resume_id),
  KEY idx_application_status (status),
  KEY idx_application_applied_at (applied_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职位投递记录表';

-- 7. AI resume matching result table.
CREATE TABLE ai_match_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'AI匹配结果ID',
  application_id BIGINT NOT NULL COMMENT '投递记录ID',
  job_id BIGINT NOT NULL COMMENT '职位ID',
  candidate_id BIGINT NOT NULL COMMENT '候选人ID',
  resume_id BIGINT NOT NULL COMMENT '简历ID',
  match_score DECIMAL(5,2) DEFAULT NULL COMMENT '匹配分，0-100',
  recommend_level VARCHAR(32) DEFAULT NULL COMMENT '推荐等级：HIGH，MEDIUM，LOW',
  recommend_reason TEXT COMMENT '推荐理由',
  highlight_summary TEXT COMMENT '候选人亮点摘要',
  risk_summary TEXT COMMENT '风险点摘要',
  model_name VARCHAR(128) DEFAULT NULL COMMENT '模型名称',
  generated_at DATETIME DEFAULT NULL COMMENT '生成时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_ai_match_application_id (application_id),
  KEY idx_ai_match_job_id (job_id),
  KEY idx_ai_match_candidate_id (candidate_id),
  KEY idx_ai_match_score (match_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI简历职位匹配结果表';

-- 8. Interview schedule table.
CREATE TABLE interview (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '面试ID',
  application_id BIGINT NOT NULL COMMENT '投递记录ID',
  interviewer_id BIGINT NOT NULL COMMENT '面试官用户ID',
  round VARCHAR(32) NOT NULL DEFAULT 'FIRST' COMMENT '面试轮次：FIRST一面，SECOND二面，HR_HR面',
  interview_time DATETIME NOT NULL COMMENT '面试时间',
  method VARCHAR(32) NOT NULL DEFAULT 'ONLINE' COMMENT '面试方式：ONLINE线上，OFFLINE线下，PHONE电话',
  location VARCHAR(255) DEFAULT NULL COMMENT '面试地点或会议链接',
  status VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED' COMMENT '状态：SCHEDULED待面试，COMPLETED已完成，CANCELED已取消，REINTERVIEW需复试',
  created_by BIGINT NOT NULL COMMENT '创建人用户ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_interview_application_id (application_id),
  KEY idx_interview_interviewer_id (interviewer_id),
  KEY idx_interview_time (interview_time),
  KEY idx_interview_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试安排表';

-- 9. Interview feedback table.
CREATE TABLE interview_feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '面试反馈ID',
  interview_id BIGINT NOT NULL COMMENT '面试ID',
  interviewer_id BIGINT NOT NULL COMMENT '面试官用户ID',
  score INT DEFAULT NULL COMMENT '面试评分，0-100',
  comment TEXT COMMENT '面试评价',
  suggestion VARCHAR(32) DEFAULT NULL COMMENT '录用建议：PASS通过，REJECT拒绝，PENDING待定',
  ai_summary TEXT COMMENT 'AI反馈摘要',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_feedback_interview_id (interview_id),
  KEY idx_feedback_interviewer_id (interviewer_id),
  KEY idx_feedback_suggestion (suggestion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试反馈表';

-- 10. Offer table.
CREATE TABLE offer (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Offer ID',
  application_id BIGINT NOT NULL COMMENT '投递记录ID',
  salary DECIMAL(10,2) DEFAULT NULL COMMENT '录用薪资',
  entry_date DATE DEFAULT NULL COMMENT '预计入职日期',
  probation_months INT DEFAULT 3 COMMENT '试用期月数',
  work_location VARCHAR(128) DEFAULT NULL COMMENT '工作地点',
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT草稿，SENT已发送，ACCEPTED已接受，REJECTED已拒绝，REVOKED已撤回',
  remark TEXT COMMENT '备注',
  sent_at DATETIME DEFAULT NULL COMMENT '发送时间',
  accepted_at DATETIME DEFAULT NULL COMMENT '接受时间',
  created_by BIGINT NOT NULL COMMENT '创建人用户ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_offer_application_id (application_id),
  KEY idx_offer_status (status),
  KEY idx_offer_entry_date (entry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Offer表';

-- 11. Onboarding workflow table.
CREATE TABLE onboarding (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '入职记录ID',
  offer_id BIGINT NOT NULL COMMENT 'Offer ID',
  candidate_id BIGINT NOT NULL COMMENT '候选人ID',
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING待提交，REVIEWING审核中，APPROVED已通过，ONBOARDED已入职，CANCELED已取消',
  current_step VARCHAR(64) DEFAULT NULL COMMENT '当前入职节点',
  material_status VARCHAR(32) DEFAULT 'PENDING' COMMENT '材料状态：PENDING待提交，REVIEWING审核中，APPROVED通过，REJECTED驳回',
  remark TEXT COMMENT '备注',
  completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_onboarding_offer_id (offer_id),
  KEY idx_onboarding_candidate_id (candidate_id),
  KEY idx_onboarding_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入职流程表';

-- 12. Employee profile table.
CREATE TABLE employee_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '员工档案ID',
  user_id BIGINT DEFAULT NULL COMMENT '员工系统用户ID',
  candidate_id BIGINT NOT NULL COMMENT '来源候选人ID',
  onboarding_id BIGINT DEFAULT NULL COMMENT '入职记录ID',
  employee_no VARCHAR(64) NOT NULL COMMENT '员工编号',
  name VARCHAR(64) NOT NULL COMMENT '员工姓名',
  phone VARCHAR(32) DEFAULT NULL COMMENT '手机号',
  email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  department VARCHAR(128) NOT NULL COMMENT '部门',
  position VARCHAR(128) NOT NULL COMMENT '岗位',
  entry_date DATE NOT NULL COMMENT '入职日期',
  status VARCHAR(32) NOT NULL DEFAULT 'PROBATION' COMMENT '状态：PROBATION试用，ACTIVE在职，LEFT离职',
  performance_summary TEXT COMMENT '绩效摘要',
  attendance_summary TEXT COMMENT '考勤摘要',
  satisfaction_feedback TEXT COMMENT '满意度或访谈反馈',
  turnover_risk_level VARCHAR(32) DEFAULT NULL COMMENT '离职风险等级：LOW，MEDIUM，HIGH',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_employee_no (employee_no),
  UNIQUE KEY uk_employee_candidate_id (candidate_id),
  KEY idx_employee_user_id (user_id),
  KEY idx_employee_department (department),
  KEY idx_employee_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工档案表';

-- Seed data.
-- The BCrypt hashes below correspond to the demo password: 123456.

INSERT INTO sys_role (id, role_code, role_name, description) VALUES
(1, 'ADMIN', '系统管理员', '维护系统用户、角色和基础配置'),
(2, 'HR', '招聘负责人', '负责职位发布、简历筛选、面试安排、Offer和入职流程'),
(3, 'INTERVIEWER', '面试官', '负责查看面试任务并提交面试反馈'),
(4, 'CANDIDATE', '候选人', '负责维护个人资料、上传简历和投递职位');

INSERT INTO sys_user (id, username, password, real_name, phone, email, role_id, status) VALUES
(1, 'admin', '$2a$10$DXeibuKFK47H.le87RVs2uwMeTrqyf0OJm5MMFx6twyLyQah4HdT6', '系统管理员', '13800000001', 'admin@recruit.com', 1, 1),
(2, 'hr01', '$2a$10$DXeibuKFK47H.le87RVs2uwMeTrqyf0OJm5MMFx6twyLyQah4HdT6', '李HR', '13800000002', 'hr01@recruit.com', 2, 1),
(3, 'interviewer01', '$2a$10$DXeibuKFK47H.le87RVs2uwMeTrqyf0OJm5MMFx6twyLyQah4HdT6', '王面试官', '13800000003', 'interviewer01@recruit.com', 3, 1),
(4, 'candidate01', '$2a$10$DXeibuKFK47H.le87RVs2uwMeTrqyf0OJm5MMFx6twyLyQah4HdT6', '张三', '13800000004', 'zhangsan@example.com', 4, 1);

INSERT INTO job_position (
  id, title, department, location, salary_min, salary_max, headcount,
  responsibilities, requirements, status, created_by, published_at
) VALUES
(1, 'Java后端开发工程师', '研发部', '武汉', 9000.00, 15000.00, 3,
 '负责招聘平台后端接口开发、业务流程实现和数据库设计。',
 '熟悉Java、Spring Boot、MySQL，了解微服务架构，有项目经验优先。',
 'OPEN', 2, '2026-07-09 09:00:00'),
(2, 'AI算法实习生', 'AI实验室', '武汉', 5000.00, 9000.00, 2,
 '参与简历匹配、语义检索和面试反馈摘要等AI能力建设。',
 '了解Python、机器学习、向量检索或大模型应用，有RAG经验优先。',
 'OPEN', 2, '2026-07-09 09:30:00'),
(3, '人力资源专员', '人力资源部', '武汉', 6000.00, 9000.00, 1,
 '负责招聘流程跟进、候选人沟通和入职材料审核。',
 '具备良好的沟通能力，熟悉招聘流程，有校园招聘经验优先。',
 'DRAFT', 2, NULL);

INSERT INTO candidate (
  id, user_id, name, gender, age, phone, email, education, school, major,
  years_of_experience, current_status, source, created_by
) VALUES
(1, 4, '张三', '男', 24, '13900000001', 'zhangsan@example.com', '本科', '武汉理工大学', '软件工程', 2, 'HIRED', 'SELF_REGISTER', NULL),
(2, NULL, '李四', '女', 25, '13900000002', 'lisi@example.com', '硕士', '华中科技大学', '计算机科学与技术', 1, 'INTERVIEWING', 'HR_IMPORT', 2),
(3, NULL, '王五', '男', 26, '13900000003', 'wangwu@example.com', '本科', '湖北大学', '人力资源管理', 3, 'AVAILABLE', 'HR_IMPORT', 2);

INSERT INTO resume (
  id, candidate_id, resume_name, file_url, file_type, parsed_content,
  skills, project_experience, work_experience, is_default
) VALUES
(1, 1, '张三-Java后端简历', '/files/resume/zhangsan-java.pdf', 'PDF',
 '张三，本科，软件工程专业，熟悉Java、Spring Boot、MySQL，参与过招聘管理系统开发。',
 'Java,Spring Boot,MySQL,Redis,Git',
 '参与招聘管理系统后端开发，负责职位、投递、面试模块接口。',
 '2年Java后端开发经验。', 1),
(2, 2, '李四-AI算法简历', '/files/resume/lisi-ai.pdf', 'PDF',
 '李四，硕士，熟悉Python、机器学习、文本向量化和语义检索。',
 'Python,Machine Learning,RAG,Qdrant,PyTorch',
 '参与简历语义匹配实验项目，负责文本向量化和相似度计算。',
 '1年AI算法实习经验。', 1),
(3, 3, '王五-HR简历', '/files/resume/wangwu-hr.pdf', 'PDF',
 '王五，本科，人力资源管理专业，熟悉招聘流程和候选人沟通。',
 '招聘流程,候选人沟通,Offer管理,入职办理',
 '参与校园招聘活动组织和候选人跟进。',
 '3年人力资源相关经验。', 1);

INSERT INTO job_application (
  id, job_id, candidate_id, resume_id, status, allow_adjustment, adjusted_job_id,
  source, hr_note, reject_reason_code, reject_reason, reviewed_by, reviewed_at, applied_at
) VALUES
(1, 1, 1, 1, 'HIRED', 1, NULL, 'ONLINE', 'Java基础较好，项目经历匹配，已完成入职。', NULL, NULL, 2, '2026-07-09 10:30:00', '2026-07-09 10:00:00'),
(2, 2, 2, 2, 'INTERVIEWING', 1, NULL, 'ONLINE', 'AI方向匹配度较高，安排一面。', NULL, NULL, 2, '2026-07-09 11:00:00', '2026-07-09 10:20:00'),
(3, 1, 3, 3, 'SCREEN_REJECT', 0, NULL, 'HR_IMPORT', '与Java后端岗位技能不匹配。', 'SKILL_NOT_MATCH', '候选人主要经历为HR方向，与后端开发岗位不匹配。', 2, '2026-07-09 11:20:00', '2026-07-09 10:40:00');

INSERT INTO ai_match_result (
  id, application_id, job_id, candidate_id, resume_id, match_score,
  recommend_level, recommend_reason, highlight_summary, risk_summary,
  model_name, generated_at
) VALUES
(1, 1, 1, 1, 1, 88.50, 'HIGH',
 '候选人具备Java后端开发经验，技术栈与岗位要求匹配度高。',
 '熟悉Spring Boot和MySQL，有招聘管理系统相关项目经历。',
 '工作年限较短，需要进一步确认复杂业务建模能力。',
 'demo-model', '2026-07-09 10:05:00'),
(2, 2, 2, 2, 2, 91.00, 'HIGH',
 '候选人具备机器学习、RAG和向量检索经验，适合AI算法实习生岗位。',
 '具备语义匹配实验项目经验，技能方向清晰。',
 '缺少完整工程落地经验，需要面试进一步确认。',
 'demo-model', '2026-07-09 10:25:00'),
(3, 3, 1, 3, 3, 35.00, 'LOW',
 '候选人主要经历集中在人力资源方向，与Java后端岗位要求差距较大。',
 '沟通与招聘流程经验较好。',
 '缺少Java、Spring Boot和数据库开发经验。',
 'demo-model', '2026-07-09 10:45:00');

INSERT INTO interview (
  id, application_id, interviewer_id, round, interview_time,
  method, location, status, created_by
) VALUES
(1, 1, 3, 'FIRST', '2026-07-09 14:00:00', 'ONLINE', '腾讯会议：123-456-789', 'COMPLETED', 2),
(2, 2, 3, 'FIRST', '2026-07-10 10:00:00', 'ONLINE', '腾讯会议：987-654-321', 'SCHEDULED', 2);

INSERT INTO interview_feedback (
  id, interview_id, interviewer_id, score, comment, suggestion, ai_summary
) VALUES
(1, 1, 3, 86,
 '候选人Java基础扎实，能说明项目中的职位、投递和面试模块设计，沟通表达较清晰。',
 'PASS',
 '候选人技术栈与岗位匹配，项目经历相关，建议进入Offer阶段。');

INSERT INTO offer (
  id, application_id, salary, entry_date, probation_months, work_location,
  status, remark, sent_at, accepted_at, created_by
) VALUES
(1, 1, 12000.00, '2026-07-15', 3, '武汉',
 'ACCEPTED', 'Java后端开发工程师岗位Offer，候选人已接受。', '2026-07-09 16:00:00', '2026-07-09 17:00:00', 2);

INSERT INTO onboarding (
  id, offer_id, candidate_id, status, current_step, material_status,
  remark, completed_at
) VALUES
(1, 1, 1, 'ONBOARDED', '入职完成', 'APPROVED',
 '候选人已提交入职材料并完成入职办理。', '2026-07-15 09:00:00');

INSERT INTO employee_profile (
  id, user_id, candidate_id, onboarding_id, employee_no, name, phone, email,
  department, position, entry_date, status, performance_summary,
  attendance_summary, satisfaction_feedback, turnover_risk_level
) VALUES
(1, NULL, 1, 1, 'EMP20260709001', '张三', '13900000001', 'zhangsan@example.com',
 '研发部', 'Java后端开发工程师', '2026-07-15', 'PROBATION',
 '初始入职员工，暂无绩效记录。',
 '初始入职员工，暂无考勤记录。',
 '入职意愿稳定，对岗位内容认可。',
 'LOW');
