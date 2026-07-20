/*
 Navicat Premium Dump SQL

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : recruit_smart

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 18/07/2026 16:03:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_feedback_summary
-- ----------------------------
DROP TABLE IF EXISTS `ai_feedback_summary`;
CREATE TABLE `ai_feedback_summary`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'AI面试反馈摘要ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT 'AI任务ID',
  `interview_id` bigint NULL DEFAULT NULL COMMENT '面试ID',
  `candidate_id` bigint NULL DEFAULT NULL COMMENT '候选人ID',
  `job_id` bigint NULL DEFAULT NULL COMMENT '岗位ID',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '反馈摘要',
  `advantages` json NULL COMMENT '优势列表，JSON数组',
  `risks` json NULL COMMENT '风险点列表，JSON数组',
  `suggestion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '建议',
  `source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '结果来源：LLM/RULE',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型名称',
  `prompt_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Prompt版本',
  `generated_at` datetime NULL DEFAULT NULL COMMENT '生成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_feedback_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_ai_feedback_interview_id`(`interview_id` ASC) USING BTREE,
  INDEX `idx_ai_feedback_candidate_id`(`candidate_id` ASC) USING BTREE,
  INDEX `idx_ai_feedback_job_id`(`job_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI面试反馈摘要结果表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_interview_question
-- ----------------------------
DROP TABLE IF EXISTS `ai_interview_question`;
CREATE TABLE `ai_interview_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'AI面试题ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT 'AI任务ID',
  `job_id` bigint NULL DEFAULT NULL COMMENT '岗位ID',
  `candidate_id` bigint NULL DEFAULT NULL COMMENT '候选人ID',
  `resume_id` bigint NULL DEFAULT NULL COMMENT '简历ID',
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目类别',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '生成摘要',
  `questions` json NULL COMMENT '面试题列表，JSON数组',
  `source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '结果来源：LLM/RULE',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型名称',
  `prompt_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Prompt版本',
  `generated_at` datetime NULL DEFAULT NULL COMMENT '生成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_question_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_ai_question_job_id`(`job_id` ASC) USING BTREE,
  INDEX `idx_ai_question_candidate_id`(`candidate_id` ASC) USING BTREE,
  INDEX `idx_ai_question_resume_id`(`resume_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI面试题生成结果表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_match_result
-- ----------------------------
DROP TABLE IF EXISTS `ai_match_result`;
CREATE TABLE `ai_match_result`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'AI匹配结果ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT 'AI任务ID',
  `application_id` bigint NOT NULL COMMENT '投递记录ID',
  `job_id` bigint NOT NULL COMMENT '职位ID',
  `candidate_id` bigint NOT NULL COMMENT '候选人ID',
  `resume_id` bigint NOT NULL COMMENT '简历ID',
  `match_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '匹配分，0-100',
  `recommend_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '推荐等级：HIGH，MEDIUM，LOW',
  `recommend_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '推荐理由',
  `highlight_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '候选人亮点摘要',
  `matched_points` json NULL COMMENT '匹配点，JSON数组',
  `risk_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '风险点摘要',
  `risk_points` json NULL COMMENT '风险点，JSON数组',
  `suggestion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI建议',
  `source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '结果来源：LLM/RULE',
  `model_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型名称',
  `prompt_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Prompt版本',
  `generated_at` datetime NULL DEFAULT NULL COMMENT '生成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ai_match_application_id`(`application_id` ASC) USING BTREE,
  INDEX `idx_ai_match_job_id`(`job_id` ASC) USING BTREE,
  INDEX `idx_ai_match_candidate_id`(`candidate_id` ASC) USING BTREE,
  INDEX `idx_ai_match_score`(`match_score` ASC) USING BTREE,
  INDEX `idx_ai_match_task_id`(`task_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI简历职位匹配结果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_task
-- ----------------------------
DROP TABLE IF EXISTS `ai_task`;
CREATE TABLE `ai_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'AI任务ID',
  `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务类型：RESUME_MATCH、INTERVIEW_QUESTION、FEEDBACK_SUMMARY、TURNOVER_RISK',
  `biz_id` bigint NULL DEFAULT NULL COMMENT '业务ID，例如投递ID、面试ID、员工ID',
  `biz_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务类型：APPLICATION、INTERVIEW、EMPLOYEE',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'RUNNING' COMMENT '状态：RUNNING、SUCCESS、FAILED',
  `source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '执行来源：LLM、RULE',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型名称',
  `prompt_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Prompt版本',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '失败原因',
  `started_at` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `finished_at` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_task_type`(`task_type` ASC) USING BTREE,
  INDEX `idx_ai_task_biz`(`biz_type` ASC, `biz_id` ASC) USING BTREE,
  INDEX `idx_ai_task_status`(`status` ASC) USING BTREE,
  INDEX `idx_ai_task_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI任务记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_turnover_risk_result
-- ----------------------------
DROP TABLE IF EXISTS `ai_turnover_risk_result`;
CREATE TABLE `ai_turnover_risk_result`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'AI离职风险预测结果ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT 'AI任务ID',
  `employee_id` bigint NULL DEFAULT NULL COMMENT '员工ID',
  `risk_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '风险等级：LOW/MEDIUM/HIGH',
  `risk_score` int NULL DEFAULT NULL COMMENT '风险分数，0-100',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '风险摘要',
  `risk_reasons` json NULL COMMENT '风险原因列表，JSON数组',
  `suggestions` json NULL COMMENT '干预建议列表，JSON数组',
  `source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '结果来源：LLM/RULE',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型名称',
  `prompt_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Prompt版本',
  `generated_at` datetime NULL DEFAULT NULL COMMENT '生成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_turnover_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_ai_turnover_employee_id`(`employee_id` ASC) USING BTREE,
  INDEX `idx_ai_turnover_risk_level`(`risk_level` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI离职风险预测结果表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for application_process_event
-- ----------------------------
DROP TABLE IF EXISTS `application_process_event`;
CREATE TABLE `application_process_event`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint NOT NULL,
  `event_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `from_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `to_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `operator_id` bigint NULL DEFAULT NULL,
  `operator_role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'BUSINESS',
  `related_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `related_id` bigint NULL DEFAULT NULL,
  `occurred_at` datetime NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_event_application_time`(`application_id` ASC, `occurred_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for candidate
-- ----------------------------
DROP TABLE IF EXISTS `candidate`;
CREATE TABLE `candidate`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '候选人ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '绑定的系统用户ID，可为空',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '候选人姓名',
  `gender` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `education` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最高学历',
  `school` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '毕业学校',
  `major` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业',
  `years_of_experience` int NULL DEFAULT 0 COMMENT '工作年限',
  `current_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AVAILABLE' COMMENT '当前状态：AVAILABLE可应聘，INTERVIEWING面试中，HIRED已入职',
  `source` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源：SELF_REGISTER自注册，HR_IMPORT手动录入等',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_candidate_user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_candidate_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_candidate_name`(`name` ASC) USING BTREE,
  INDEX `idx_candidate_education`(`education` ASC) USING BTREE,
  INDEX `idx_candidate_status`(`current_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '候选人表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for employee_profile
-- ----------------------------
DROP TABLE IF EXISTS `employee_profile`;
CREATE TABLE `employee_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '员工档案ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '员工系统用户ID',
  `candidate_id` bigint NOT NULL COMMENT '来源候选人ID',
  `onboarding_id` bigint NULL DEFAULT NULL COMMENT '入职记录ID',
  `employee_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '员工编号',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '员工姓名',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `department` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门',
  `position` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位',
  `entry_date` date NOT NULL COMMENT '入职日期',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PROBATION' COMMENT '状态：PROBATION试用，ACTIVE在职，LEFT离职',
  `performance_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '绩效摘要',
  `attendance_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '考勤摘要',
  `satisfaction_feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '满意度或访谈反馈',
  `turnover_risk_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '离职风险等级：LOW，MEDIUM，HIGH',
  `risk_assessed_at` datetime NULL DEFAULT NULL COMMENT '鏈?繎涓??绂昏亴椋庨櫓璇勪及鏃堕棿',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_employee_no`(`employee_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_employee_candidate_id`(`candidate_id` ASC) USING BTREE,
  INDEX `idx_employee_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_employee_department`(`department` ASC) USING BTREE,
  INDEX `idx_employee_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '员工档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for interview
-- ----------------------------
DROP TABLE IF EXISTS `interview`;
CREATE TABLE `interview`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '面试ID',
  `application_id` bigint NOT NULL COMMENT '投递记录ID',
  `interviewer_id` bigint NOT NULL COMMENT '面试官用户ID',
  `round` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'FIRST' COMMENT '面试轮次：FIRST一面，SECOND二面，HR_HR面',
  `interview_time` datetime NULL DEFAULT NULL COMMENT '闈㈣瘯瀹樼‘璁ょ殑闈㈣瘯鏃堕棿锛屽緟棰勭害鏃朵负绌',
  `method` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '闈㈣瘯鏂瑰紡锛歄NLINE绾夸笂锛孫FFLINE绾夸笅锛孭HONE鐢佃瘽',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '面试地点或会议链接',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ASSIGNED' COMMENT '鐘舵?锛欰SSIGNED寰呴?绾︼紝SCHEDULED寰呴潰璇曪紝COMPLETED宸插畬鎴愶紝CANCELED宸插彇娑堬紝REINTERVIEW涓烘棫鐗堝?璇曠姸鎬',
  `created_by` bigint NOT NULL COMMENT '创建人用户ID',
  `assigned_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'HR鎸囨淳鏃堕棿',
  `scheduled_at` datetime NULL DEFAULT NULL COMMENT '闈㈣瘯瀹樼‘璁ら?绾︽椂闂',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_interview_application_id`(`application_id` ASC) USING BTREE,
  INDEX `idx_interview_interviewer_id`(`interviewer_id` ASC) USING BTREE,
  INDEX `idx_interview_time`(`interview_time` ASC) USING BTREE,
  INDEX `idx_interview_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '面试安排表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for interview_feedback
-- ----------------------------
DROP TABLE IF EXISTS `interview_feedback`;
CREATE TABLE `interview_feedback`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '面试反馈ID',
  `interview_id` bigint NOT NULL COMMENT '面试ID',
  `interviewer_id` bigint NOT NULL COMMENT '面试官用户ID',
  `state` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT' COMMENT '反馈状态：DRAFT草稿，SUBMITTED已提交',
  `scorecard_json` json NULL COMMENT '结构化评分卡JSON',
  `score` int NULL DEFAULT NULL COMMENT '面试评分，0-100',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '面试评价',
  `suggestion` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '录用建议：PASS通过，REJECT拒绝，PENDING待定',
  `ai_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI反馈摘要',
  `submitted_at` datetime NULL DEFAULT NULL COMMENT '正式提交时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_feedback_interview_id`(`interview_id` ASC) USING BTREE,
  INDEX `idx_feedback_interviewer_id`(`interviewer_id` ASC) USING BTREE,
  INDEX `idx_feedback_suggestion`(`suggestion` ASC) USING BTREE,
  INDEX `idx_feedback_state`(`state` ASC) USING BTREE,
  INDEX `idx_feedback_submitted_at`(`submitted_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '面试反馈表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for job_application
-- ----------------------------
DROP TABLE IF EXISTS `job_application`;
CREATE TABLE `job_application`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '投递记录ID',
  `job_id` bigint NOT NULL COMMENT '职位ID',
  `candidate_id` bigint NOT NULL COMMENT '候选人ID',
  `resume_id` bigint NOT NULL COMMENT '投递使用的简历ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SUBMITTED' COMMENT '投递状态',
  `allow_adjustment` tinyint NOT NULL DEFAULT 0 COMMENT '是否接受岗位调剂：1是，0否',
  `adjusted_job_id` bigint NULL DEFAULT NULL COMMENT '调剂职位ID',
  `source` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'ONLINE' COMMENT '投递来源',
  `hr_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'HR备注',
  `reject_reason_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '拒绝原因编码',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '拒绝原因说明',
  `reviewed_by` bigint NULL DEFAULT NULL COMMENT '筛选人用户ID',
  `reviewed_at` datetime NULL DEFAULT NULL COMMENT '筛选时间',
  `ai_match_score` int NULL DEFAULT NULL COMMENT 'AI匹配分数',
  `recommend_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'AI推荐等级：HIGH/MEDIUM/LOW',
  `recommend_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI推荐理由',
  `highlight_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI候选人亮点摘要',
  `risk_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI风险点摘要',
  `ai_analyzed_at` datetime NULL DEFAULT NULL COMMENT 'AI分析时间',
  `applied_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_application_candidate_job`(`candidate_id` ASC, `job_id` ASC) USING BTREE,
  INDEX `idx_application_job_id`(`job_id` ASC) USING BTREE,
  INDEX `idx_application_candidate_id`(`candidate_id` ASC) USING BTREE,
  INDEX `idx_application_resume_id`(`resume_id` ASC) USING BTREE,
  INDEX `idx_application_status`(`status` ASC) USING BTREE,
  INDEX `idx_application_applied_at`(`applied_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '职位投递记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for job_position
-- ----------------------------
DROP TABLE IF EXISTS `job_position`;
CREATE TABLE `job_position`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '职位ID',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '职位名称',
  `department` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属部门',
  `location` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工作地点',
  `salary_min` decimal(10, 2) NULL DEFAULT NULL COMMENT '最低薪资',
  `salary_max` decimal(10, 2) NULL DEFAULT NULL COMMENT '最高薪资',
  `headcount` int NOT NULL DEFAULT 1 COMMENT '招聘人数',
  `required_interview_rounds` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '瑕佹眰闈㈣瘯杞?暟锛?涓?潰锛?涓?潰鍔犱簩闈?紝3涓?潰鍔犱簩闈㈠姞HR闈',
  `responsibilities` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '岗位职责',
  `requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '任职要求',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT' COMMENT '职位状态：DRAFT草稿，OPEN招聘中，PAUSED暂停，CLOSED关闭',
  `created_by` bigint NOT NULL COMMENT '创建人用户ID',
  `published_at` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `closed_at` datetime NULL DEFAULT NULL COMMENT '关闭时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_job_position_status`(`status` ASC) USING BTREE,
  INDEX `idx_job_position_department`(`department` ASC) USING BTREE,
  INDEX `idx_job_position_created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `chk_job_required_interview_rounds` CHECK (`required_interview_rounds` between 1 and 3)
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '职位表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_conversation
-- ----------------------------
DROP TABLE IF EXISTS `message_conversation`;
CREATE TABLE `message_conversation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '浼氳瘽ID',
  `application_id` bigint NOT NULL COMMENT '鍏宠仈鎶曢?璁板綍ID',
  `last_message_preview` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '鏈?悗涓?潯娑堟伅鎽樿?',
  `last_message_at` datetime NULL DEFAULT NULL COMMENT '鏈?悗涓?潯娑堟伅鏃堕棿',
  `created_by` bigint NULL DEFAULT NULL COMMENT '浼氳瘽鍒涘缓浜虹敤鎴稩D锛岀郴缁熻嚜鍔ㄥ垱寤烘椂涓虹┖',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_conversation_application_id`(`application_id` ASC) USING BTREE,
  INDEX `idx_message_conversation_last_message_at`(`last_message_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鎷涜仒娴佺▼娑堟伅浼氳瘽琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_conversation_member
-- ----------------------------
DROP TABLE IF EXISTS `message_conversation_member`;
CREATE TABLE `message_conversation_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '浼氳瘽鎴愬憳ID',
  `conversation_id` bigint NOT NULL COMMENT '浼氳瘽ID',
  `user_id` bigint NOT NULL COMMENT '鎴愬憳鐢ㄦ埛ID',
  `role_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍔犲叆浼氳瘽鏃剁殑瑙掕壊缂栫爜',
  `last_read_at` datetime NULL DEFAULT NULL COMMENT '鏈?悗宸茶?鏃堕棿',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_member_conversation_user`(`conversation_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_message_member_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '娑堟伅浼氳瘽鎴愬憳琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_record
-- ----------------------------
DROP TABLE IF EXISTS `message_record`;
CREATE TABLE `message_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '娑堟伅ID',
  `conversation_id` bigint NOT NULL COMMENT '浼氳瘽ID',
  `sender_id` bigint NULL DEFAULT NULL COMMENT '鍙戦?浜虹敤鎴稩D锛岀郴缁熸秷鎭?负绌',
  `sender_role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍙戦?浜鸿?鑹茬紪鐮',
  `message_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TEXT' COMMENT '娑堟伅绫诲瀷锛歍EXT鏂囨湰锛孲YSTEM绯荤粺閫氱煡锛孎ILE闄勪欢',
  `content` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '娑堟伅姝ｆ枃',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍙戦?鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_message_record_conversation_id`(`conversation_id` ASC, `id` ASC) USING BTREE,
  INDEX `idx_message_record_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_message_record_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '绔欏唴娑堟伅璁板綍琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for offer
-- ----------------------------
DROP TABLE IF EXISTS `offer`;
CREATE TABLE `offer`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Offer ID',
  `application_id` bigint NOT NULL COMMENT '投递记录ID',
  `salary` decimal(10, 2) NULL DEFAULT NULL COMMENT '录用薪资',
  `entry_date` date NULL DEFAULT NULL COMMENT '预计入职日期',
  `probation_months` int NULL DEFAULT 3 COMMENT '试用期月数',
  `work_location` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工作地点',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT草稿，SENT已发送，ACCEPTED已接受，REJECTED已拒绝，REVOKED已撤回',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `sent_at` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `accepted_at` datetime NULL DEFAULT NULL COMMENT '接受时间',
  `created_by` bigint NOT NULL COMMENT '创建人用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_offer_application_id`(`application_id` ASC) USING BTREE,
  INDEX `idx_offer_status`(`status` ASC) USING BTREE,
  INDEX `idx_offer_entry_date`(`entry_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Offer表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for onboarding
-- ----------------------------
DROP TABLE IF EXISTS `onboarding`;
CREATE TABLE `onboarding`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '入职记录ID',
  `offer_id` bigint NOT NULL COMMENT 'Offer ID',
  `candidate_id` bigint NOT NULL COMMENT '候选人ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING待提交，REVIEWING审核中，APPROVED已通过，ONBOARDED已入职，CANCELED已取消',
  `current_step` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前入职节点',
  `material_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '材料状态：PENDING待提交，REVIEWING审核中，APPROVED通过，REJECTED驳回',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_onboarding_offer_id`(`offer_id` ASC) USING BTREE,
  INDEX `idx_onboarding_candidate_id`(`candidate_id` ASC) USING BTREE,
  INDEX `idx_onboarding_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '入职流程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for resume
-- ----------------------------
DROP TABLE IF EXISTS `resume`;
CREATE TABLE `resume`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '简历ID',
  `candidate_id` bigint NOT NULL COMMENT '候选人ID',
  `resume_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '简历名称',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '简历文件地址',
  `file_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件类型',
  `parsed_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '解析后的简历文本',
  `skills` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '技能关键词',
  `project_experience` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '项目经历摘要',
  `work_experience` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '工作经历摘要',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认简历：1是，0否',
  `parse_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '解析状态：PENDING、PROCESSING、SUCCESS、FAILED',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resume_candidate_id`(`candidate_id` ASC) USING BTREE,
  INDEX `idx_resume_is_default`(`is_default` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '简历表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录密码，正式项目应存储BCrypt密文',
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  `last_login_at` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_sys_user_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_sys_user_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_sys_user_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
