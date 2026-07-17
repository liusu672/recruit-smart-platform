package com.recruit.ai.knowledge.controller;

import com.recruit.ai.knowledge.dto.*;
import com.recruit.ai.knowledge.model.DocumentChunk;
import com.recruit.ai.knowledge.service.DocumentParserService;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.knowledge.service.TextSplitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "知识库文档解析接口", description = "用于把 txt/pdf/docx 文档解析成纯文本")
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final TextSplitterService textSplitterService;

    private final DocumentParserService documentParserService;

    private final KnowledgeBaseService knowledgeBaseService;

    @Operation(summary = "解析文档", description = "根据文件路径解析 txt/pdf/docx 为纯文本")
    @PostMapping("/parse")
    public KnowledgeParseResponse parseDocument(@RequestBody KnowledgeBuildRequest request) {
        KnowledgeParseResponse response = new KnowledgeParseResponse();

        try {
            String content = documentParserService.parseDocument(request.getDocumentPath());
            response.setSuccess(true);
            response.setContent(content);
            response.setLength(content.length());
            response.setMessage("解析成功");
        } catch (Exception e) {
            response.setSuccess(false);
            response.setContent("");
            response.setLength(0);
            response.setMessage("解析失败: " + e.getMessage());
        }

        return response;
    }

    @Operation(summary = "解析并分块文档", description = "解析 txt/pdf/docx 文档，并按照指定大小切分为多个文本块")
    @PostMapping("/split")
    public KnowledgeSplitResponse splitDocument(@RequestBody KnowledgeBuildRequest request) {
        KnowledgeSplitResponse response = new KnowledgeSplitResponse();

        try {
            String content = documentParserService.parseDocument(request.getDocumentPath());

            int chunkSize = request.getChunkSize() == null ? 500 : request.getChunkSize();
            int chunkOverlap = request.getChunkOverlap() == null ? 50 : request.getChunkOverlap();

            List<DocumentChunk> chunks = textSplitterService.splitText(
                    content,
                    chunkSize,
                    chunkOverlap,
                    request.getDocumentPath()
            );

            response.setSuccess(true);
            response.setTotalChunks(chunks.size());
            response.setChunks(chunks);
            response.setMessage("文档分块成功");
        } catch (Exception e) {
            response.setSuccess(false);
            response.setTotalChunks(0);
            response.setChunks(List.of());
            response.setMessage("文档分块失败: " + e.getMessage());
        }

        return response;
    }

    @Operation(summary = "构建知识库", description = "解析文档、文本分块、向量化并写入向量库")
    @PostMapping("/build")
    public KnowledgeBuildResponse buildKnowledgeBase(@RequestBody KnowledgeBuildRequest request) {
        return knowledgeBaseService.buildKnowledgeBase(request);
    }

    @Operation(summary = "知识库检索", description = "根据问题从向量库中检索最相关的文本块")
    @GetMapping("/search")
    public KnowledgeSearchResponse searchKnowledge(
            @RequestParam("query") String query,
            @RequestParam(value = "topK", required = false, defaultValue = "3") Integer topK) {
        return knowledgeBaseService.searchKnowledge(query, topK);
    }
}