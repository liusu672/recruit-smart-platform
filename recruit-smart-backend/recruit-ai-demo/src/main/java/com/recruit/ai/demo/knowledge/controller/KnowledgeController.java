package com.recruit.ai.demo.knowledge.controller;

import com.recruit.ai.demo.knowledge.dto.KnowledgeBuildRequest;
import com.recruit.ai.demo.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.demo.knowledge.vo.KnowledgeBuildVO;
import com.recruit.ai.demo.knowledge.vo.KnowledgeSearchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/build")
    public KnowledgeBuildVO build(@RequestBody KnowledgeBuildRequest request) throws IOException {
        return knowledgeBaseService.buildKnowledgeBase(request);
    }

    @GetMapping("/search")
    public KnowledgeSearchVO search(@RequestParam("query") String query,
                                    @RequestParam(value = "topK", defaultValue = "3") Integer topK) {
        return knowledgeBaseService.search(query, topK);
    }
}