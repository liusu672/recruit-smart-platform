package com.recruit.biz;

import com.recruit.biz.dto.LoginDTO;
import com.recruit.common.result.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 会议"场景一"：依赖容器的集成测试
 * 验证 Controller → Service → Mapper 全链路
 * 需要 MySQL 和 Nacos 已启动
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecruitBizIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    // ==================== 冒烟验证（核心接口能否返回200） ====================

    @Test
    void loginSmokeTest() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        ResponseEntity<Result> resp = restTemplate.postForEntity(
                "/api/auth/login", dto, Result.class);

        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
    }

    @Test
    void swaggerApiDocsAccessible() {
        ResponseEntity<String> resp = restTemplate.getForEntity(
                "/v3/api-docs", String.class);
        // Swagger可能返回200或302
        assertTrue(resp.getStatusCodeValue() == 200 || resp.getStatusCodeValue() == 302);
    }

    @Test
    void candidatePageAccessible() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        ResponseEntity<Result> loginResp = restTemplate.postForEntity(
                "/api/auth/login", dto, Result.class);
        String token = (String) ((java.util.Map) loginResp.getBody().getData()).get("token");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> resp = restTemplate.postForEntity(
                "/api/candidates/page", entity, String.class);
        assertEquals(200, resp.getStatusCodeValue());
    }
}
