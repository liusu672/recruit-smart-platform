package com.recruit.ai.prompt;

import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.feign.dto.request.EmployeeBehaviorRecordDTO;

public final class TurnoverRiskPrompts {

    public static final String VERSION = "turnover-risk-v2";

    public static final String SYSTEM_PROMPT = """
            你是招聘与人才管理平台的员工离职风险分析助手。

            你的任务是根据员工最近多个周期的绩效、考勤、满意度、
            工作反馈和岗位信息，分析员工的情感状态和离职风险。

            必须遵守以下规则：

            1. AI结果只能作为HR参考，不能自动决定辞退、降薪、调岗或其他业务结果。
            2. 必须基于输入内容分析，不能凭空编造员工信息。
            3. 需要综合分析行为数据趋势，而不是只看最近一期数据。
            4. satisfactionScore越低，情绪风险通常越高。
            5. performanceScore持续下降时，需要增加离职风险判断。
            6. attendanceScore下降、迟到和缺勤增加时，需要增加风险判断。
            7. 反馈中出现工作压力、晋升疑虑、薪资不满、离职、跳槽、其他机会等内容时，
               需要重点分析情绪风险。
            8. riskScore必须是0到100之间的整数。
            9. sentimentRiskScore必须是0到100之间的整数。
            10. sentimentLabel只能是POSITIVE、NEUTRAL或NEGATIVE。
            11. riskLevel只能是LOW、MEDIUM或HIGH。
            12. riskReasons必须是字符串数组。
            13. suggestions必须是面向HR的可执行建议数组。
            14. 只能返回JSON，不要返回Markdown代码块，不要添加额外解释。

            必须严格按照以下JSON格式返回：

            {
              "sentimentLabel": "POSITIVE",
              "sentimentRiskScore": 0,
              "sentimentSummary": "情感分析说明",
              "riskLevel": "LOW",
              "riskScore": 0,
              "summary": "综合风险分析摘要",
              "riskReasons": [],
              "suggestions": []
            }
            """;

    public static String buildUserPrompt(
            TurnoverRiskRequest request,
            String knowledgeContext
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("【员工基础信息】\n");
        prompt.append("员工ID：")
                .append(value(request.getEmployeeId()))
                .append("\n");
        prompt.append("员工姓名：")
                .append(value(request.getEmployeeName()))
                .append("\n");
        prompt.append("部门：")
                .append(value(request.getDepartment()))
                .append("\n");
        prompt.append("岗位：")
                .append(value(request.getPosition()))
                .append("\n\n");

        prompt.append("【多周期趋势】\n");
        prompt.append("绩效趋势：")
                .append(value(request.getPerformanceTrend()))
                .append("\n");
        prompt.append("考勤趋势：")
                .append(value(request.getAttendanceTrend()))
                .append("\n");
        prompt.append("满意度趋势：")
                .append(value(request.getSatisfactionTrend()))
                .append("\n\n");

        prompt.append("【最近一期数据】\n");
        prompt.append("绩效分数：")
                .append(value(request.getPerformanceScore()))
                .append("\n");
        prompt.append("考勤分数：")
                .append(value(request.getAttendanceScore()))
                .append("\n");
        prompt.append("满意度分数：")
                .append(value(request.getSatisfactionScore()))
                .append("\n");
        prompt.append("绩效摘要：")
                .append(value(request.getPerformanceSummary()))
                .append("\n");
        prompt.append("考勤摘要：")
                .append(value(request.getAttendanceSummary()))
                .append("\n");
        prompt.append("满意度反馈：")
                .append(value(request.getSatisfactionFeedback()))
                .append("\n");
        prompt.append("最近反馈：")
                .append(value(request.getLatestFeedback()))
                .append("\n\n");

        prompt.append("【各周期行为数据】\n");

        if (request.getBehaviorRecords() == null
                || request.getBehaviorRecords().isEmpty()) {
            prompt.append("没有提供多周期行为数据\n");
        } else {
            for (EmployeeBehaviorRecordDTO record
                    : request.getBehaviorRecords()) {
                prompt.append("周期：")
                        .append(value(record.getPeriodStart()))
                        .append(" 至 ")
                        .append(value(record.getPeriodEnd()))
                        .append("\n");

                prompt.append("绩效：")
                        .append(value(record.getPerformanceScore()))
                        .append("\n");

                prompt.append("任务完成率：")
                        .append(value(record.getTaskCompletionRate()))
                        .append("\n");

                prompt.append("考勤：")
                        .append(value(record.getAttendanceScore()))
                        .append("\n");

                prompt.append("迟到次数：")
                        .append(value(record.getLateCount()))
                        .append("\n");

                prompt.append("缺勤天数：")
                        .append(value(record.getAbsenceDays()))
                        .append("\n");

                prompt.append("满意度：")
                        .append(value(record.getSatisfactionScore()))
                        .append("\n");

                prompt.append("员工反馈：")
                        .append(value(record.getFeedbackText()))
                        .append("\n\n");
            }
        }

        prompt.append("【知识库参考内容】\n");
        prompt.append(value(knowledgeContext))
                .append("\n\n");

        prompt.append("请根据以上信息分析情感状态和离职风险，只返回JSON。");

        return prompt.toString();
    }

    private static String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private TurnoverRiskPrompts() {
    }
}