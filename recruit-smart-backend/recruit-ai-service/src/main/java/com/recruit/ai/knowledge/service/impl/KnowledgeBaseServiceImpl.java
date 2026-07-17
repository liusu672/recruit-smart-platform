package com.recruit.ai.knowledge.service.impl;

import com.recruit.ai.knowledge.dto.KnowledgeBuildRequest;
import com.recruit.ai.knowledge.dto.KnowledgeBuildResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResult;
import com.recruit.ai.knowledge.model.DocumentChunk;
import com.recruit.ai.knowledge.service.DocumentParserService;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.knowledge.service.TextSplitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_CHUNK_OVERLAP = 50;

    private final DocumentParserService documentParserService;
    private final TextSplitterService textSplitterService;
    private final VectorStore vectorStore;

    @Override
    public KnowledgeBuildResponse buildKnowledgeBase(KnowledgeBuildRequest request) {
        KnowledgeBuildResponse response = new KnowledgeBuildResponse();

        try {
            int chunkSize = request.getChunkSize() == null ? DEFAULT_CHUNK_SIZE : request.getChunkSize();
            int chunkOverlap = request.getChunkOverlap() == null ? DEFAULT_CHUNK_OVERLAP : request.getChunkOverlap();

            String content = documentParserService.parseDocument(request.getDocumentPath());

            List<DocumentChunk> chunks = textSplitterService.splitText(
                    content,
                    chunkSize,
                    chunkOverlap,
                    request.getDocumentPath()
            );

            List<Document> documents = chunks.stream()
                    .map(this::toSpringAiDocument)
                    .toList();

            vectorStore.add(documents);

            response.setSuccess(true);
            response.setTotalChunks(chunks.size());
            response.setTotalDocuments(documents.size());
            response.setSourcePath(request.getDocumentPath());
            response.setMessage("知识库构建成功");
        } catch (Exception e) {
            response.setSuccess(false);
            response.setTotalChunks(0);
            response.setTotalDocuments(0);
            response.setSourcePath(request.getDocumentPath());
            response.setMessage("知识库构建失败: " + e.getMessage());
        }

        return response;
    }

    @Override
    public KnowledgeSearchResponse searchKnowledge(String query, Integer topK) {
        KnowledgeSearchResponse response = new KnowledgeSearchResponse();

        try {
            int finalTopK = topK == null || topK <= 0 ? 3 : topK;

            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(finalTopK)
                    .build();

            List<Document> documents = vectorStore.similaritySearch(searchRequest);

            List<KnowledgeSearchResult> results = new ArrayList<>();
            for (Document document : documents) {
                KnowledgeSearchResult result = toSearchResult(document);
                results.add(result);
            }

            response.setSuccess(true);
            response.setQuery(query);
            response.setTotal(results.size());
            response.setResults(results);
            response.setMessage("检索成功");
        } catch (Exception e) {
            response.setSuccess(false);
            response.setQuery(query);
            response.setTotal(0);
            response.setResults(List.of());
            response.setMessage("检索失败: " + e.getMessage());
        }

        return response;
    }

    private Document toSpringAiDocument(DocumentChunk chunk) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceDocument", chunk.getSourceDocument());
        metadata.put("chunkIndex", chunk.getChunkIndex());
        metadata.put("startOffset", chunk.getStartOffset());
        metadata.put("endOffset", chunk.getEndOffset());

        return new Document(chunk.getContent(), metadata);
    }

    private KnowledgeSearchResult toSearchResult(Document document) {
        Map<String, Object> metadata = document.getMetadata();

        KnowledgeSearchResult result = new KnowledgeSearchResult();
        result.setContent(document.getText());
        result.setMetadata(metadata);
        result.setSourceDocument((String) metadata.get("sourceDocument"));
        result.setChunkIndex(toInteger(metadata.get("chunkIndex")));
        result.setStartOffset(toInteger(metadata.get("startOffset")));
        result.setEndOffset(toInteger(metadata.get("endOffset")));
        return result;
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