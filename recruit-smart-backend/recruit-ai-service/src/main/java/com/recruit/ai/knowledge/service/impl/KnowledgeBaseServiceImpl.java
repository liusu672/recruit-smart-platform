package com.recruit.ai.knowledge.service.impl;

import com.recruit.ai.knowledge.dto.KnowledgeBuildRequest;
import com.recruit.ai.knowledge.dto.KnowledgeBuildResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResponse;
import com.recruit.ai.knowledge.dto.KnowledgeSearchResult;
import com.recruit.ai.knowledge.model.DocumentChunk;
import com.recruit.ai.knowledge.service.DocumentParserService;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.knowledge.service.TextSplitterService;
import com.recruit.ai.mapper.AiKnowledgeDocumentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.ai.entity.AiKnowledgeDocument;
import com.recruit.ai.mapper.AiKnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.UUID;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_CHUNK_OVERLAP = 50;

    private final DocumentParserService documentParserService;
    private final TextSplitterService textSplitterService;
    private final VectorStore vectorStore;

    private final AiKnowledgeDocumentMapper
            aiKnowledgeDocumentMapper;

    @Value("${spring.ai.openai.embedding.options.model}")
    private String embeddingModel;

    @Value("${spring.ai.vectorstore.milvus.collectionName}")
    private String collectionName;

    @Override
    public KnowledgeBuildResponse buildKnowledgeBase(
            KnowledgeBuildRequest request
    ) {
        KnowledgeBuildResponse response =
                new KnowledgeBuildResponse();

        AiKnowledgeDocument record = null;

        try {
            validateBuildRequest(request);

            String documentKey = request.getDocumentKey().trim();
            String documentPath = request.getDocumentPath().trim();

            int chunkSize = request.getChunkSize() == null
                    ? DEFAULT_CHUNK_SIZE
                    : request.getChunkSize();

            int chunkOverlap = request.getChunkOverlap() == null
                    ? DEFAULT_CHUNK_OVERLAP
                    : request.getChunkOverlap();

            validateChunkConfig(chunkSize, chunkOverlap);

            response.setDocumentKey(documentKey);
            response.setSourcePath(documentPath);

            // 仍需解析文件，解析后才能判断内容是否变化
            String content = documentParserService.parseDocument(
                    documentPath
            );

            if (content == null || content.isBlank()) {
                throw new IllegalArgumentException("文档内容不能为空");
            }

            /*
             * 以下任意一项变化，都应该重新构建：
             * 1. 文档正文
             * 2. 分块大小
             * 3. 重叠大小
             * 4. Embedding模型
             * 5. Milvus collection
             */
            String fingerprint = sha256(
                    content
                            + "|chunkSize=" + chunkSize
                            + "|chunkOverlap=" + chunkOverlap
                            + "|embeddingModel=" + embeddingModel
                            + "|collection=" + collectionName
            );

            response.setContentHash(fingerprint);

            record = findByDocumentKey(documentKey);

            // 内容及构建配置未变化，直接跳过Embedding
            if (canSkip(
                    record,
                    fingerprint,
                    chunkSize,
                    chunkOverlap
            )) {
                response.setSuccess(true);
                response.setSkipped(true);
                response.setTotalChunks(record.getChunkCount());
                response.setTotalDocuments(record.getChunkCount());
                response.setMessage(
                        "文档内容和构建配置未变化，已跳过向量化"
                );
                return response;
            }

            // 标记正在构建，但暂时不覆盖最后成功的contentHash
            record = markBuilding(
                    record,
                    documentKey,
                    documentPath,
                    chunkSize,
                    chunkOverlap
            );

            List<DocumentChunk> chunks = textSplitterService.splitText(
                    content,
                    chunkSize,
                    chunkOverlap,
                    documentPath
            );

            if (chunks == null || chunks.isEmpty()) {
                throw new IllegalArgumentException(
                        "文档分块结果为空"
                );
            }

            List<Document> documents = chunks.stream()
                    .map(chunk -> toSpringAiDocument(
                            chunk,
                            documentKey,
                            fingerprint
                    ))
                    .toList();

            /*
             * 保留当前旧方案：
             * 删除该documentKey旧向量，再写入新向量。
             */
            deleteByDocumentKey(documentKey);
            vectorStore.add(documents);

            markSuccess(
                    record,
                    fingerprint,
                    chunkSize,
                    chunkOverlap,
                    chunks.size()
            );

            response.setSuccess(true);
            response.setSkipped(false);
            response.setTotalChunks(chunks.size());
            response.setTotalDocuments(documents.size());
            response.setMessage(
                    "知识库构建成功，文档编号: " + documentKey
            );
        } catch (Exception e) {
            log.error("知识库构建失败", e);

            markFailed(record, getRootMessage(e));

            response.setSuccess(false);
            response.setSkipped(false);
            response.setTotalChunks(0);
            response.setTotalDocuments(0);

            if (request != null) {
                response.setDocumentKey(request.getDocumentKey());
                response.setSourcePath(request.getDocumentPath());
            }

            response.setMessage(
                    "知识库构建失败: " + getRootMessage(e)
            );
        }

        return response;
    }

    @Override
    public KnowledgeSearchResponse searchKnowledge(
            String query,
            Integer topK
    ) {
        KnowledgeSearchResponse response =
                new KnowledgeSearchResponse();

        try {
            if (query == null || query.isBlank()) {
                throw new IllegalArgumentException(
                        "检索内容不能为空"
                );
            }

            int finalTopK = topK == null || topK <= 0
                    ? 3
                    : topK;

            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query.trim())
                    .topK(finalTopK)
                    .build();

            List<Document> documents =
                    vectorStore.similaritySearch(searchRequest);

            if (documents == null) {
                documents = List.of();
            }

            List<KnowledgeSearchResult> results =
                    new ArrayList<>();

            for (Document document : documents) {
                results.add(toSearchResult(document));
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
            response.setMessage(
                    "检索失败: " + e.getMessage()
            );
        }

        return response;
    }

    private void validateBuildRequest(
            KnowledgeBuildRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "构建知识库请求不能为空"
            );
        }

        if (request.getDocumentKey() == null
                || request.getDocumentKey().isBlank()) {
            throw new IllegalArgumentException(
                    "documentKey不能为空"
            );
        }

        if (request.getDocumentPath() == null
                || request.getDocumentPath().isBlank()) {
            throw new IllegalArgumentException(
                    "documentPath不能为空"
            );
        }

        if (request.getDocumentKey().length() > 128) {
            throw new IllegalArgumentException(
                    "documentKey长度不能超过128个字符"
            );
        }
    }

    private void validateChunkConfig(
            int chunkSize,
            int chunkOverlap
    ) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException(
                    "chunkSize必须大于0"
            );
        }

        if (chunkOverlap < 0) {
            throw new IllegalArgumentException(
                    "chunkOverlap不能小于0"
            );
        }

        if (chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException(
                    "chunkOverlap必须小于chunkSize"
            );
        }
    }

    private void deleteByDocumentKey(String documentKey) {
        FilterExpressionBuilder builder =
                new FilterExpressionBuilder();

        vectorStore.delete(
                builder.eq(
                        "documentKey",
                        documentKey
                ).build()
        );
    }

    private Document toSpringAiDocument(
            DocumentChunk chunk,
            String documentKey,
            String fingerprint
    ) {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("documentKey", documentKey);
        metadata.put("documentVersion", fingerprint);
        metadata.put(
                "sourceDocument",
                chunk.getSourceDocument()
        );
        metadata.put("chunkIndex", chunk.getChunkIndex());
        metadata.put("startOffset", chunk.getStartOffset());
        metadata.put("endOffset", chunk.getEndOffset());

        String vectorId = buildVectorId(
                documentKey,
                fingerprint,
                chunk.getChunkIndex()
        );

        return new Document(
                vectorId,
                chunk.getContent(),
                metadata
        );
    }

    private KnowledgeSearchResult toSearchResult(
            Document document
    ) {
        Map<String, Object> metadata =
                document.getMetadata();

        KnowledgeSearchResult result =
                new KnowledgeSearchResult();

        result.setContent(document.getText());
        result.setMetadata(metadata);
        result.setSourceDocument(
                toStringValue(
                        metadata.get("sourceDocument")
                )
        );
        result.setChunkIndex(
                toInteger(metadata.get("chunkIndex"))
        );
        result.setStartOffset(
                toInteger(metadata.get("startOffset"))
        );
        result.setEndOffset(
                toInteger(metadata.get("endOffset"))
        );

        return result;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "计算SHA-256失败",
                    e
            );
        }
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

    private String toStringValue(Object value) {
        return value == null
                ? null
                : value.toString();
    }

    private String buildVectorId(
            String documentKey,
            String fingerprint,
            Integer chunkIndex
    ) {
        String source = documentKey
                + "|" + fingerprint
                + "|" + chunkIndex;

        return UUID.nameUUIDFromBytes(
                source.getBytes(StandardCharsets.UTF_8)
        ).toString();
    }

    private AiKnowledgeDocument findByDocumentKey(
            String documentKey
    ) {
        LambdaQueryWrapper<AiKnowledgeDocument> wrapper =
                new LambdaQueryWrapper<>();

        wrapper.eq(
                AiKnowledgeDocument::getDocumentKey,
                documentKey
        );

        return aiKnowledgeDocumentMapper.selectOne(wrapper);
    }

    private boolean canSkip(
            AiKnowledgeDocument record,
            String fingerprint,
            int chunkSize,
            int chunkOverlap
    ) {
        return record != null
                && "SUCCESS".equals(record.getStatus())
                && fingerprint.equals(record.getContentHash())
                && Integer.valueOf(chunkSize).equals(
                record.getChunkSize()
        )
                && Integer.valueOf(chunkOverlap).equals(
                record.getChunkOverlap()
        );
    }

    private AiKnowledgeDocument markBuilding(
            AiKnowledgeDocument record,
            String documentKey,
            String documentPath,
            int chunkSize,
            int chunkOverlap
    ) {
        if (record == null) {
            record = new AiKnowledgeDocument();
            record.setDocumentKey(documentKey);
            record.setSourcePath(documentPath);
            record.setChunkSize(chunkSize);
            record.setChunkOverlap(chunkOverlap);
            record.setChunkCount(0);
            record.setStatus("BUILDING");
            record.setLastError(null);

            aiKnowledgeDocumentMapper.insert(record);
            return record;
        }

        record.setSourcePath(documentPath);
        record.setChunkSize(chunkSize);
        record.setChunkOverlap(chunkOverlap);
        record.setStatus("BUILDING");
        record.setLastError(null);

        aiKnowledgeDocumentMapper.updateById(record);
        return record;
    }

    private void markSuccess(
            AiKnowledgeDocument record,
            String fingerprint,
            int chunkSize,
            int chunkOverlap,
            int chunkCount
    ) {
        record.setContentHash(fingerprint);
        record.setChunkSize(chunkSize);
        record.setChunkOverlap(chunkOverlap);
        record.setChunkCount(chunkCount);
        record.setStatus("SUCCESS");
        record.setLastError(null);

        int rows = aiKnowledgeDocumentMapper.updateById(record);

        if (rows != 1) {
            throw new IllegalStateException(
                    "更新知识文档成功状态失败"
            );
        }
    }

    private void markFailed(
            AiKnowledgeDocument record,
            String errorMessage
    ) {
        if (record == null || record.getId() == null) {
            return;
        }

        try {
            record.setStatus("FAILED");
            record.setLastError(truncate(errorMessage, 1000));
            aiKnowledgeDocumentMapper.updateById(record);
        } catch (Exception databaseException) {
            log.error(
                    "记录知识库构建失败状态时发生异常",
                    databaseException
            );
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }

    private String getRootMessage(Throwable throwable) {
        Throwable current = throwable;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage() == null
                ? current.getClass().getSimpleName()
                : current.getMessage();
    }
}