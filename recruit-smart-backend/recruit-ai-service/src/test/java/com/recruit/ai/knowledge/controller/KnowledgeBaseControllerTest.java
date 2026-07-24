package com.recruit.ai.knowledge.controller;

import com.recruit.ai.knowledge.dto.*;
import com.recruit.ai.knowledge.model.DocumentChunk;
import com.recruit.ai.knowledge.service.DocumentParserService;
import com.recruit.ai.knowledge.service.KnowledgeBaseService;
import com.recruit.ai.knowledge.service.TextSplitterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeBaseControllerTest {

    @Mock
    private TextSplitterService textSplitterService;
    @Mock
    private DocumentParserService documentParserService;
    @Mock
    private KnowledgeBaseService knowledgeBaseService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new KnowledgeBaseController(
                        textSplitterService, documentParserService, knowledgeBaseService))
                .build();
    }

    @Test
    void parseDocument_returnsContent() throws Exception {
        when(documentParserService.parseDocument(anyString())).thenReturn("解析后的文档内容");

        mockMvc.perform(post("/api/knowledge/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentPath\":\"/path/to/doc.pdf\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.content").value("解析后的文档内容"))
                .andExpect(jsonPath("$.message").value("解析成功"));
    }

    @Test
    void parseDocument_whenParserFails_returnsError() throws Exception {
        when(documentParserService.parseDocument(anyString()))
                .thenThrow(new IllegalArgumentException("不支持的文件格式"));

        mockMvc.perform(post("/api/knowledge/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentPath\":\"/bad/file.xyz\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("解析失败")));
    }

    @Test
    void splitDocument_withDefaults_returnsChunks() throws Exception {
        String content = "文档内容 ".repeat(200);
        when(documentParserService.parseDocument(anyString())).thenReturn(content);

        DocumentChunk chunk = new DocumentChunk();
        chunk.setContent("分块1");
        chunk.setChunkIndex(0);
        when(textSplitterService.splitText(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(List.of(chunk));

        mockMvc.perform(post("/api/knowledge/split")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentPath\":\"/path/to/doc.pdf\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalChunks").value(1))
                .andExpect(jsonPath("$.chunks[0].content").value("分块1"));
    }

    @Test
    void splitDocument_withCustomParams_usesThem() throws Exception {
        when(documentParserService.parseDocument(anyString())).thenReturn("some content");
        when(textSplitterService.splitText(anyString(), eq(1000), eq(100), anyString()))
                .thenReturn(List.of(new DocumentChunk()));

        mockMvc.perform(post("/api/knowledge/split")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentPath\":\"/doc.pdf\",\"chunkSize\":1000,\"chunkOverlap\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void splitDocument_whenParserFails_returnsError() throws Exception {
        when(documentParserService.parseDocument(anyString()))
                .thenThrow(new RuntimeException("文件不存在"));

        mockMvc.perform(post("/api/knowledge/split")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentPath\":\"/missing.pdf\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("分块失败")));
    }

    @Test
    void buildKnowledgeBase_returnsResult() throws Exception {
        KnowledgeBuildResponse buildResponse = new KnowledgeBuildResponse();
        buildResponse.setSuccess(true);
        buildResponse.setTotalChunks(10);
        when(knowledgeBaseService.buildKnowledgeBase(any())).thenReturn(buildResponse);

        mockMvc.perform(post("/api/knowledge/build")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentPath\":\"/path/to/doc.pdf\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalChunks").value(10));
    }

    @Test
    void searchKnowledge_returnsResults() throws Exception {
        KnowledgeSearchResponse searchResponse = new KnowledgeSearchResponse();
        searchResponse.setSuccess(true);
        searchResponse.setTotal(2);
        KnowledgeSearchResult result = new KnowledgeSearchResult();
        result.setContent("相关文档片段");
        searchResponse.setResults(List.of(result));

        when(knowledgeBaseService.searchKnowledge(eq("查询内容"), eq(5)))
                .thenReturn(searchResponse);

        mockMvc.perform(get("/api/knowledge/search")
                        .param("query", "查询内容")
                        .param("topK", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.results[0].content").value("相关文档片段"));
    }

    @Test
    void searchKnowledge_withDefaultTopK() throws Exception {
        when(knowledgeBaseService.searchKnowledge(anyString(), eq(3)))
                .thenReturn(new KnowledgeSearchResponse());

        mockMvc.perform(get("/api/knowledge/search")
                        .param("query", "查询内容"))
                .andExpect(status().isOk());
    }
}
