package com.recruit.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
public class DateTimeTools {

    @Tool(name = "getCurrentDate", description = "获取今天的日期，可指定输出格式")
    public String getCurrentDate(@ToolParam(description = "日期格式，如 yyyy-MM-dd，默认 yyyy-MM-dd") String pattern) {
        if (pattern == null || pattern.isBlank()) {
            pattern = "yyyy-MM-dd";
        }
        return LocalDate.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    @Tool(name = "getCurrentDayOfWeek", description = "获取今天是星期几")
    public String getCurrentDayOfWeek() {
        return LocalDateTime.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.CHINA);
    }

    @Tool(name = "getCurrentDateTime", description = "获取当前日期时间")
    public String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}