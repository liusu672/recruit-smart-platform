package com.recruit.ai.prompt;

import com.recruit.ai.dto.request.InterviewQuestionRequest;

public final class InterviewQuestionPrompts {

    public static final String VERSION = "interview-question-v1";

    public static final String SYSTEM_PROMPT = """
            你是招聘与人才管理平台的 AI 面试题生成助手。

            你的任务：
            根据岗位信息、候选人简历和知识库参考内容，生成适合面试官使用的面试问题。

            规则：
            1. 必须结合岗位要求和候选人经历生成问题。
            2. 知识库内容只是参考，不是硬性要求。
            3. 问题要适合真实面试场景，避免空泛。
            4. 优先生成技术能力、项目经验、岗位匹配、综合素质相关问题。
            5. 不要生成与岗位无关的问题。
            6. 必须只返回 JSON，不要返回 Markdown，不要解释。

            JSON 格式：
            {
              "category": "Java后端",
              "summary": "已根据岗位要求和候选人简历生成推荐面试题。",
              "questions": []
            }
            """;

    public static String buildUserPrompt(InterviewQuestionRequest request, String knowledgeContext) {
        return """
                【岗位信息】
                岗位名称：%s
                岗位职责：%s
                岗位要求：%s

                【候选人简历】
                简历正文：%s
                技能：%s
                项目经验：%s
                工作经验：%s

                【知识库参考内容】
                %s

                请生成 8 道面试题，返回 JSON。
                """.formatted(
                safe(request.getJobTitle()),
                safe(request.getResponsibilities()),
                safe(request.getRequirements()),
                safe(request.getResumeText()),
                safe(request.getSkills()),
                safe(request.getProjectExperience()),
                safe(request.getWorkExperience()),
                safe(knowledgeContext)
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private InterviewQuestionPrompts() {
    }
}