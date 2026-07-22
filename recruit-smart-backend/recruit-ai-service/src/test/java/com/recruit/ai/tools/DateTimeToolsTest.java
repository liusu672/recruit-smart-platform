package com.recruit.ai.tools;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateTimeToolsTest {

    private final DateTimeTools tools = new DateTimeTools();

    @Test
    void getCurrentDateUsesDefaultPattern() {
        assertEquals(
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                tools.getCurrentDate(null)
        );
    }

    @Test
    void getCurrentDayOfWeekUsesChineseLocale() {
        assertEquals(
                LocalDateTime.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.CHINA),
                tools.getCurrentDayOfWeek()
        );
    }

    @Test
    void getCurrentDateTimeUsesDateTimePattern() {
        assertTrue(tools.getCurrentDateTime().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
}
