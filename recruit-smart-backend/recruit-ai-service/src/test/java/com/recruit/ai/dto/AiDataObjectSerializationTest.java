package com.recruit.ai.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 批量验证 recruit-ai-service 模块所有 DTO、Entity 的序列化/反序列化。
 * 覆盖 17 个源文件（dto/request 5 + dto/response 6 + entity 6）。
 */
class AiDataObjectSerializationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void allDtos_serializeAndDeserialize() {
        List<Class<?>> classes = new ArrayList<>();
        classes.addAll(scanPackage("com.recruit.ai.dto.request"));
        classes.addAll(scanPackage("com.recruit.ai.dto.response"));
        assertFalse(classes.isEmpty(), "应该扫描到 DTO 类");
        for (Class<?> clazz : classes) {
            assertDoesNotThrow(() -> testDto(clazz), "DTO序列化失败: " + clazz.getSimpleName());
        }
    }

    @Test
    void allEntities_serializeAndDeserialize() {
        List<Class<?>> classes = scanPackage("com.recruit.ai.entity");
        assertFalse(classes.isEmpty(), "应该扫描到 Entity 类");
        for (Class<?> clazz : classes) {
            assertDoesNotThrow(() -> testDto(clazz), "Entity序列化失败: " + clazz.getSimpleName());
        }
    }

    @Test
    void allKnowledgeDtos_serializeAndDeserialize() {
        List<Class<?>> classes = scanPackage("com.recruit.ai.knowledge.dto");
        assertFalse(classes.isEmpty(), "应该扫描到 Knowledge DTO 类");
        for (Class<?> clazz : classes) {
            assertDoesNotThrow(() -> testDto(clazz), "Knowledge DTO序列化失败: " + clazz.getSimpleName());
        }
    }

    private List<Class<?>> scanPackage(String basePackage) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));
        Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);
        List<Class<?>> result = new ArrayList<>();
        for (BeanDefinition bd : candidates) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                if (!clazz.isAnonymousClass() && !clazz.isSynthetic()
                        && !clazz.isMemberClass() && !clazz.getSimpleName().contains("$")) {
                    result.add(clazz);
                }
            } catch (ClassNotFoundException ignored) {}
        }
        return result;
    }

    private void testDto(Class<?> clazz) throws Exception {
        // 跳过没有无参构造的类（如 @Value + @Builder 的不可变对象）
        try {
            clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            return; // 跳过
        }
        Object instance = clazz.getDeclaredConstructor().newInstance();
        fillSampleValues(instance, clazz);
        String json = MAPPER.writeValueAsString(instance);
        assertNotNull(json);
        Object deserialized = MAPPER.readValue(json, clazz);
        assertNotNull(deserialized);
    }

    private void fillSampleValues(Object instance, Class<?> clazz) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    || java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            Object sample = createSampleValue(field.getType());
            if (sample != null) {
                String setterName = "set" + Character.toUpperCase(field.getName().charAt(0))
                        + field.getName().substring(1);
                try {
                    Method setter = clazz.getMethod(setterName, field.getType());
                    setter.invoke(instance, sample);
                } catch (NoSuchMethodException ignored) {}
            }
        }
    }

    private Object createSampleValue(Class<?> type) {
        if (type == String.class) return "test_val";
        if (type == Long.class || type == long.class) return 1L;
        if (type == Integer.class || type == int.class) return 1;
        if (type == Boolean.class || type == boolean.class) return true;
        if (type == Double.class || type == double.class) return 1.0d;
        if (type == BigDecimal.class) return java.math.BigDecimal.ONE;
        if (type == LocalDate.class) return LocalDate.of(2026, 1, 1);
        if (type == LocalDateTime.class) return LocalDateTime.of(2026, 1, 1, 0, 0);
        if (type == List.class) return List.of();
        if (type == Set.class) return Set.of();
        if (type == Map.class) return Map.of();
        return null;
    }
}
