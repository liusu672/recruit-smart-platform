package com.recruit.ai.prompt;

import com.recruit.ai.dto.request.TurnoverRiskRequest;

public final class TurnoverRiskPrompts {

    public static final String VERSION = "turnover-risk-v1";

    public static final String SYSTEM_PROMPT = """
            你是招聘与人才管理平台的 AI 员工离职风险分析助手。

            你的任务：
            根据员工基础信息、绩效情况、考勤情况、满意度反馈、访谈反馈和知识库参考内容，
            分析员工当前离职风险。

            规则：
            1. AI 结果只作为 HR 参考，不能自动改变员工状态。
            2. 必须基于输入信息分析，不要凭空编造。
            3. 知识库内容只是参考标准，不是硬性结论。
            4. riskScore 范围是 0 到 100，分数越高表示离职风险越高。
            5. riskLevel 只能是 LOW、MEDIUM、HIGH。
            6. riskReasons 要说明风险原因。
            7. suggestions 要给出 HR 可执行的干预建议。
            8. 必须只返回 JSON，不要返回 Markdown，不要解释。

            JSON 格式：
            {
              "riskLevel": "LOW",
              "riskScore": 0,
              "summary": "整体摘要",
              "riskReasons": [],
              "suggestions": []
            }
            """;

    public static String buildUserPrompt(TurnoverRiskRequest request, String knowledgeContext) {
        return """
                【员工基础信息】
                员工ID：%s
                员工姓名：%s
                部门：%s
                岗位：%s

                【评分信息】
                绩效评分：%s
                考勤评分：%s
                满意度评分：%s

                【文本信息】
                绩效摘要：%s
                考勤摘要：%s
                满意度反馈：%s
                访谈反馈：%s

                【知识库参考内容】
                %s

                请分析员工离职风险，并返回 JSON。
                """.formatted(
                request.getEmployeeId() == null ? "" : request.getEmployeeId().toString(),
                safe(request.getEmployeeName()),
                safe(request.getDepartment()),
                safe(request.getPosition()),
                request.getPerformanceScore() == null ? "未提供" : request.getPerformanceScore().toString(),
                request.getAttendanceScore() == null ? "未提供" : request.getAttendanceScore().toString(),
                request.getSatisfactionScore() == null ? "未提供" : request.getSatisfactionScore().toString(),
                safe(request.getPerformanceSummary()),
                safe(request.getAttendanceSummary()),
                safe(request.getSatisfactionFeedback()),
                safe(request.getInterviewFeedback()),
                safe(knowledgeContext)
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private TurnoverRiskPrompts() {
    }
}