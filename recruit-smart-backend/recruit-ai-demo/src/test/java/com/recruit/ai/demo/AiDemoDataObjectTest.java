package com.recruit.ai.demo;

import com.recruit.ai.demo.config.CorsConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiDemoDataObjectTest {

    @Test
    void corsConfig_classLoads() {
        assertNotNull(CorsConfig.class);
    }

    @Test
    void appClass_classLoads() {
        assertNotNull(com.recruit.ai.demo.AiDemoApp.class);
    }

    @Test
    void controllerClasses_exist() {
        assertNotNull(com.recruit.ai.demo.controller.ChatController.class);
        assertNotNull(com.recruit.ai.demo.knowledge.controller.KnowledgeController.class);
    }

    @Test
    void serviceInterfaces_exist() {
        assertNotNull(com.recruit.ai.demo.service.ChatService.class);
        assertNotNull(com.recruit.ai.demo.service.impl.ChatServiceImpl.class);
        assertNotNull(com.recruit.ai.demo.knowledge.service.DocumentParserService.class);
        assertNotNull(com.recruit.ai.demo.knowledge.service.KnowledgeBaseService.class);
        assertNotNull(com.recruit.ai.demo.knowledge.service.TextSplitterService.class);
    }

    @Test
    void dtoAndVoClasses_exist() {
        assertNotNull(com.recruit.ai.demo.dto.ChatRequest.class);
        assertNotNull(com.recruit.ai.demo.knowledge.dto.KnowledgeBuildRequest.class);
        assertNotNull(com.recruit.ai.demo.knowledge.model.DocumentChunk.class);
        assertNotNull(com.recruit.ai.demo.knowledge.vo.KnowledgeBuildVO.class);
        assertNotNull(com.recruit.ai.demo.knowledge.vo.KnowledgeSearchResultVO.class);
        assertNotNull(com.recruit.ai.demo.knowledge.vo.KnowledgeSearchVO.class);
    }
}
