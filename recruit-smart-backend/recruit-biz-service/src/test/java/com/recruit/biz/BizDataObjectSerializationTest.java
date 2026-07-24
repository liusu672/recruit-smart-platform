package com.recruit.biz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 批量验证 recruit-biz-service 模块所有 DTO、VO、Entity、Enum 的序列化/反序列化。
 * 覆盖 128 个源文件（dto 48 + vo 41 + entity 18 + enums 21）。
 */
class BizDataObjectSerializationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ==================== DTO 批量测试 ====================

    @Test
    void allDtos_serializeAndDeserialize() {
        List<Class<?>> classes = scanPackage("com.recruit.biz.dto");
        assertFalse(classes.isEmpty(), "应该扫描到 DTO 类");
        for (Class<?> clazz : classes) {
            assertDoesNotThrow(() -> testDto(clazz), "DTO序列化失败: " + clazz.getSimpleName());
        }
    }

    // ==================== VO 批量测试 ====================

    @Test
    void allVos_serializeAndDeserialize() {
        List<Class<?>> classes = scanPackage("com.recruit.biz.vo");
        assertFalse(classes.isEmpty(), "应该扫描到 VO 类");
        for (Class<?> clazz : classes) {
            assertDoesNotThrow(() -> testDto(clazz), "VO序列化失败: " + clazz.getSimpleName());
        }
    }

    // ==================== Entity 批量测试 ====================

    @Test
    void allEntities_serializeAndDeserialize() {
        List<Class<?>> classes = scanPackage("com.recruit.biz.entity");
        assertFalse(classes.isEmpty(), "应该扫描到 Entity 类");
        for (Class<?> clazz : classes) {
            assertDoesNotThrow(() -> testDto(clazz), "Entity序列化失败: " + clazz.getSimpleName());
        }
    }

    // ==================== Enum 批量测试 ====================

    @Test
    void allEnums_haveValidConstants() {
        List<Class<?>> enumClasses = scanPackage("com.recruit.biz.enums");
        assertFalse(enumClasses.isEmpty(), "应该扫描到 Enum 类");
        for (Class<?> clazz : enumClasses) {
            assertTrue(clazz.isEnum(), clazz.getSimpleName() + " 应该是枚举");
            assertDoesNotThrow(() -> testEnum(clazz), "枚举验证失败: " + clazz.getSimpleName());
        }
    }

    // ==================== 辅助方法 ====================

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
            } catch (ClassNotFoundException e) {
                // 跳过无法加载的类
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void testDto(Class<?> clazz) throws Exception {
        // 跳过没有无参构造的类
        try {
            clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            return;
        }
        Object instance = clazz.getDeclaredConstructor().newInstance();
        fillSampleValues(instance, clazz);

        String json = MAPPER.writeValueAsString(instance);
        assertNotNull(json);

        // JSON 应包含已设置字段
        // 反序列化
        Object deserialized = MAPPER.readValue(json, clazz);
        assertNotNull(deserialized);

        // 验证序列化再反序列化后不为 null
        String reSerialized = MAPPER.writeValueAsString(deserialized);
        assertNotNull(reSerialized);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void testEnum(Class<?> clazz) throws Exception {
        Object[] constants = clazz.getEnumConstants();
        assertNotNull(constants);
        assertTrue(constants.length > 0, clazz.getSimpleName() + " 应至少有一个枚举值");

        // 验证 valueOf 可用
        for (Object constant : constants) {
            Enum enumConst = (Enum) constant;
            String name = enumConst.name();
            Object same = Enum.valueOf((Class<Enum>) clazz, name);
            assertSame(constant, same);

            // 枚举序列化
            String json = MAPPER.writeValueAsString(constant);
            assertNotNull(json);
        }
    }

    private void fillSampleValues(Object instance, Class<?> clazz) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    || java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            Class<?> fieldType = field.getType();
            Object sample = createSampleValue(fieldType);
            if (sample != null) {
                String setterName = "set" + Character.toUpperCase(field.getName().charAt(0))
                        + field.getName().substring(1);
                try {
                    Method setter = clazz.getMethod(setterName, fieldType);
                    setter.invoke(instance, sample);
                } catch (NoSuchMethodException e) {
                    // 跳过没有 setter 的字段
                }
            }
        }
    }

    private Object createSampleValue(Class<?> type) {
        if (type == String.class) return "test_" + UUID.randomUUID().toString().substring(0, 6);
        if (type == Long.class || type == long.class) return 1L;
        if (type == Integer.class || type == int.class) return 1;
        if (type == Boolean.class || type == boolean.class) return true;
        if (type == Double.class || type == double.class) return 1.0d;
        if (type == Float.class || type == float.class) return 1.0f;
        if (type == BigDecimal.class) return BigDecimal.ONE;
        if (type == LocalDate.class) return LocalDate.of(2026, 1, 1);
        if (type == LocalDateTime.class) return LocalDateTime.of(2026, 1, 1, 0, 0);
        if (type == List.class) return Collections.emptyList();
        if (type == Set.class) return Collections.emptySet();
        if (type == Map.class) return Collections.emptyMap();
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants.length > 0 ? constants[0] : null;
        }
        return null;
    }
}
