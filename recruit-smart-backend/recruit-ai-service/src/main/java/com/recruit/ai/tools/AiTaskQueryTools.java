package com.recruit.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.ai.entity.AiTask;
import com.recruit.ai.mapper.AiTaskMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AiTaskQueryTools {

    private final AiTaskMapper aiTaskMapper;

    public AiTaskQueryTools(AiTaskMapper aiTaskMapper) {
        this.aiTaskMapper = aiTaskMapper;
    }

    @Tool(name = "queryRecentAiTasks", description = "查询最近的AI任务记录，可指定查询数量")
    public String queryRecentAiTasks(
            @ToolParam(description = "查询数量，默认5条，最大10条") Integer limit
    ) {
        int size = limit == null ? 5 : limit;
        size = Math.max(1, Math.min(size, 10));

        LambdaQueryWrapper<AiTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AiTask::getCreatedAt).last("limit " + size);

        List<AiTask> tasks = aiTaskMapper.selectList(wrapper);

        if (tasks == null || tasks.isEmpty()) {
            return "当前没有AI任务记录。";
        }

        return tasks.stream()
                .map(this::formatTask)
                .collect(Collectors.joining("\n"));
    }

    @Tool(name = "queryAiTaskById", description = "根据AI任务ID查询任务详情")
    public String queryAiTaskById(
            @ToolParam(description = "AI任务ID") Long taskId
    ) {
        if (taskId == null) {
            return "任务ID不能为空。";
        }

        AiTask task = aiTaskMapper.selectById(taskId);
        if (task == null) {
            return "未查询到ID为 " + taskId + " 的AI任务。";
        }

        return formatTask(task);
    }

    private String formatTask(AiTask task) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String startedAt = task.getStartedAt() == null
                ? "-"
                : task.getStartedAt().format(formatter);

        String finishedAt = task.getFinishedAt() == null
                ? "-"
                : task.getFinishedAt().format(formatter);

        return "任务ID：" + task.getId()
                + "，任务类型：" + safe(task.getTaskType())
                + "，业务类型：" + safe(task.getBizType())
                + "，业务ID：" + task.getBizId()
                + "，状态：" + safe(task.getStatus())
                + "，来源：" + safe(task.getSource())
                + "，模型：" + safe(task.getModelName())
                + "，开始时间：" + startedAt
                + "，结束时间：" + finishedAt;
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }
}