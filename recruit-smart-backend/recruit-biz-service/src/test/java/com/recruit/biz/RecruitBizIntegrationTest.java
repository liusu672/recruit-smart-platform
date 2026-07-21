package com.recruit.biz;

import com.recruit.biz.dto.LoginDTO;
import com.recruit.common.result.Result;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 会议"场景一"：依赖容器的集成测试
 * 验证 Controller → Service → Mapper 全链路
 *
 * 前置条件：MySQL 已运行 + 数据库已初始化 + Nacos 已启动(standalone模式)
 *
 * biz 模块没有 /api 前缀（/api 是 Gateway 加的），路径以 controller 的 @RequestMapping 为准：
 * - AuthController:  /auth/login (POST), /auth/me (GET)
 * - CandidateController: /candidate (GET分页), /candidate/{id} (GET详情)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("需要 MySQL + Nacos 已启动才能运行")
class RecruitBizIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void loginSmokeTest() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        // biz 模块路径没有 /api 前缀
        ResponseEntity<Result> resp = restTemplate.postForEntity(
                "/auth/login", dto, Result.class);

        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals(200, resp.getBody().getCode());
    }

    @Test
    void swaggerApiDocsAccessible() {
        ResponseEntity<String> resp = restTemplate.getForEntity(
                "/v3/api-docs", String.class);
        assertTrue(resp.getStatusCodeValue() == 200,
                "Swagger API文档应可访问");
    }

    @Test
    void candidatePageAccessible() {
        // 1. 先登录获取 token
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        ResponseEntity<Result> loginResp = restTemplate.postForEntity(
                "/auth/login", dto, Result.class);
        assertEquals(200, loginResp.getStatusCodeValue());

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> dataMap =
                (java.util.Map<String, Object>) loginResp.getBody().getData();
        String token = (String) dataMap.get("token");

        // 2. 带 token 访问候选人分页（GET 方法）
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                "/candidate?pageNum=1&pageSize=5",
                HttpMethod.GET, entity, String.class);
        assertEquals(200, resp.getStatusCodeValue());
    }
}
