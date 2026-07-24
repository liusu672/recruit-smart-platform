package com.recruit.ai.prompt;

import com.recruit.ai.dto.request.InterviewQuestionRequest;

import java.util.Locale;

public final class InterviewQuestionPrompts {

    public static final String VERSION = "interview-question-v3";

    public static final String SYSTEM_PROMPT = """
            你是招聘与人才管理平台的 AI 面试题生成助手。

            你的任务：
            根据当前面试轮次、岗位信息、候选人简历和知识库中的对应轮次规则，
            生成适合本轮面试官使用的问题。

            轮次要求：
            1. 一面重点考察简历真实性、岗位基础、核心技术基础和基本项目经验。
            2. 二面重点考察技术深度、复杂问题分析、系统设计、项目难点和方案权衡。
            3. HR面重点考察求职动机、稳定性、价值观、协作方式、职业规划及到岗条件。
            4. 必须只使用与当前轮次匹配的知识库规则；发现其他轮次规则时应忽略。
            5. 不得将一面、二面和HR面的题目结构混用。

            输出规则：
            1. 必须结合岗位要求和候选人经历生成问题。
            2. 知识库中当前轮次规则优先于通用生成习惯。
            3. 问题要适合真实面试场景，避免空泛和重复。
            4. 不要生成与岗位或当前轮次无关的问题。
            5. 必须只返回 JSON，不要返回 Markdown，不要解释。
            6. questions 必须是对象数组，不能是字符串数组。
            7. 题目对象只能使用 title、content、focus、difficulty、referenceAnswer、answerPoints 字段。
            8. focus 和 answerPoints 必须是字符串数组，禁止使用“考察点”等中文字段名。
            9. difficulty 只能是 EASY、MEDIUM、HARD。

            JSON 格式：
            {
              "category": "Java后端",
              "summary": "已根据当前轮次、岗位要求和候选人简历生成推荐面试题。",
              "questions": [
                {
                  "title": "数据库设计与优化",
                  "content": "请说明项目中的数据库设计和查询优化方案。",
                  "focus": ["数据库范式", "索引设计", "SQL优化"],
                  "difficulty": "MEDIUM",
                  "referenceAnswer": "候选人应结合实际项目说明表结构、索引和查询优化思路。",
                  "answerPoints": ["说明表结构设计", "说明索引选择依据", "说明慢查询优化方法"]
                }
              ]
            }
            """;

    public static String buildUserPrompt(
            InterviewQuestionRequest request,
            String knowledgeContext
    ) {
        return """
                【当前面试轮次】
                轮次：%s（%s）
                本轮目标：%s

                【岗位信息】
                岗位名称：%s
                岗位职责：%s
                岗位要求：%s

                【候选人简历】
                简历正文：%s
                技能：%s
                项目经验：%s
                工作经验：%s

                【Milvus知识库召回内容】
                %s

                请优先执行知识库中与“%s”匹配的生成规则，忽略其他轮次规则，
                生成 8 道本轮面试题并返回 JSON。
                """.formatted(
                roundLabel(request.getInterviewRound()),
                safe(request.getInterviewRound()),
                roundGoal(request.getInterviewRound()),
                safe(request.getJobTitle()),
                safe(request.getResponsibilities()),
                safe(request.getRequirements()),
                safe(request.getResumeText()),
                safe(request.getSkills()),
                safe(request.getProjectExperience()),
                safe(request.getWorkExperience()),
                safe(knowledgeContext),
                roundLabel(request.getInterviewRound())
        );
    }

    public static String roundLabel(String round) {
        return switch (normalizeRound(round)) {
            case "SECOND" -> "二面";
            case "HR" -> "HR面";
            default -> "一面";
        };
    }
    public static String roundSearchTerms(String round) {
        return switch (normalizeRound(round)) {
            case "SECOND" -> "二面 第二轮 复试 SECOND";
            case "HR" -> "HR面 第三轮 人力资源面试 HR";
            default -> "一面 第一轮 初试 FIRST";
        };
    }


    public static String roundGoal(String round) {
        return switch (normalizeRound(round)) {
            case "SECOND" ->
                    "技术深度、系统设计、项目难点、故障分析和方案权衡";
            case "HR" ->
                    "求职动机、稳定性、价值观、职业规划、协作及到岗条件";
            default ->
                    "简历真实性、岗位基础、核心技术基础和基本项目经验";
        };
    }

    private static String normalizeRound(String round) {
        if (round == null || round.isBlank()) {
            return "FIRST";
        }
        return round.trim().toUpperCase(Locale.ROOT);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private InterviewQuestionPrompts() {
    }
}
