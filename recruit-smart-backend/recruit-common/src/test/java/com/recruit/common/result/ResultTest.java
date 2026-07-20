package com.recruit.common.result;

import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
    @Test
    void successReturns200WithNullData() {
        Result<Void> result = Result.success();
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void successWithDataReturnsData() {
        Result<String> result = Result.success("hello");
        assertEquals("hello", result.getData());
        assertEquals(200, result.getCode());
    }

    @Test
    void failReturnsGivenCodeAndMessage() {
        Result<Void> result = Result.fail(400, "参数错误");
        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
        assertNull(result.getData());
    }
}

class PageResultTest {
    @Test
    void storesTotalAndRecords() {
        List<String> records = List.of("a", "b");
        PageResult<String> result = new PageResult<>(2L, records);
        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getRecords().size());
    }

    @Test
    void emptyRecords() {
        PageResult<String> result = new PageResult<>(0L, List.of());
        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }
}
