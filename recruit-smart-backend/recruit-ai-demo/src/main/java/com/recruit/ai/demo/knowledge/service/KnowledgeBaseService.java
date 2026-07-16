package com.recruit.ai.demo.knowledge.service;

import com.recruit.ai.demo.knowledge.dto.KnowledgeBuildRequest;
import com.recruit.ai.demo.knowledge.model.DocumentChunk;
import com.recruit.ai.demo.knowledge.vo.KnowledgeBuildVO;
import com.recruit.ai.demo.knowledge.vo.KnowledgeSearchResultVO;
import com.recruit.ai.demo.knowledge.vo.KnowledgeSearchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final DocumentParserService documentParserService;

    private final TextSplitterService textSplitterService;

    private final VectorStore vectorStore;

    public KnowledgeBuildVO buildKnowledgeBase(KnowledgeBuildRequest request) throws IOException {
        if (request.getDocumentPath() == null || request.getDocumentPath().isBlank()) {
            throw new IllegalArgumentException("documentPath 不能为空");
        }

        Path sourcePath = Path.of(request.getDocumentPath());
        List<Path> files = collectFiles(sourcePath);

        if (files.isEmpty()) {
            return KnowledgeBuildVO.builder()
                    .success(false)
                    .totalChunks(0)
                    .totalDocuments(0)
                    .sourcePath(sourcePath.toString())
                    .message("没有找到可解析的文档")
                    .build();
        }

        List<Document> documents = new ArrayList<>();
        int totalChunks = 0;

        for (Path file : files) {
            String text = documentParserService.parseDocument(file);

            List<DocumentChunk> chunks = textSplitterService.splitText(
                    text,
                    request.getChunkSize(),
                    request.getChunkOverlap(),
                    file.getFileName().toString()
            );

            totalChunks += chunks.size();

            for (DocumentChunk chunk : chunks) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("sourceDocument", chunk.getSourceDocument());
                metadata.put("chunkIndex", chunk.getChunkIndex());
                metadata.put("filePath", file.toString());

                documents.add(new Document(chunk.getContent(), metadata));
            }
        }

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }

        return KnowledgeBuildVO.builder()
                .success(true)
                .totalChunks(totalChunks)
                .totalDocuments(files.size())
                .sourcePath(sourcePath.toString())
                .message("知识库构建成功")
                .build();
    }

    public KnowledgeSearchVO search(String query, Integer topK) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query 不能为空");
        }

        int limit = topK == null || topK <= 0 ? 3 : topK;

        List<Document> documents = vectorStore.similaritySearch(query);

        if (documents == null) {
            documents = List.of();
        }

        List<KnowledgeSearchResultVO> results = documents.stream()
                .limit(limit)
                .map(this::toSearchResult)
                .toList();

        return KnowledgeSearchVO.builder()
                .success(true)
                .query(query)
                .total(results.size())
                .results(results)
                .build();
    }

    private List<Path> collectFiles(Path sourcePath) throws IOException {
        if (!Files.exists(sourcePath)) {
            throw new IOException("路径不存在: " + sourcePath);
        }

        if (Files.isRegularFile(sourcePath)) {
            if (documentParserService.isSupported(sourcePath)) {
                return List.of(sourcePath);
            }
            return List.of();
        }

        try (var stream = Files.walk(sourcePath)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(documentParserService::isSupported)
                    .toList();
        }
    }

    private KnowledgeSearchResultVO toSearchResult(Document document) {
        Map<String, Object> metadata = document.getMetadata();

        return KnowledgeSearchResultVO.builder()
                .content(document.getText())
                .sourceDocument(String.valueOf(metadata.get("sourceDocument")))
                .chunkIndex(toInteger(metadata.get("chunkIndex")))
                .metadata(metadata)
                .build();
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer integer) {
            return integer;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        return Integer.valueOf(value.toString());
    }
}