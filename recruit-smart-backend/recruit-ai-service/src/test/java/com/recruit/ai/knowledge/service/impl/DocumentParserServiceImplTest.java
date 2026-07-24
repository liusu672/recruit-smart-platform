package com.recruit.ai.knowledge.service.impl;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentParserServiceImplTest {

    private final DocumentParserServiceImpl parser =
            new DocumentParserServiceImpl();

    @Test
    void parsesAndCleansUploadedText() throws Exception {
        String source = " Java  开发 \r\n\r\n\r\n Spring Boot \t 项目 ";

        String content = parser.parseDocument(
                "resume.txt",
                new ByteArrayInputStream(
                        source.getBytes(StandardCharsets.UTF_8)
                )
        );

        assertEquals("Java 开发\nSpring Boot 项目", content);
    }

    @Test
    void rejectsUnsupportedUploadedFileType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseDocument(
                        "resume.rtf",
                        new ByteArrayInputStream(new byte[]{1})
                )
        );
    }
}
