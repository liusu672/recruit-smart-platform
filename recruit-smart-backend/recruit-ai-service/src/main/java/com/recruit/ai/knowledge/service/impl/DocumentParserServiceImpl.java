package com.recruit.ai.knowledge.service.impl;

import com.recruit.ai.knowledge.service.DocumentParserService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class DocumentParserServiceImpl implements DocumentParserService {

    @Override
    public String parseDocument(String filePath) throws IOException {
        Path path = Path.of(filePath);
        if (!Files.isRegularFile(path)) {
            throw new IOException("文件不存在: " + filePath);
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            return parseDocument(path.getFileName().toString(), inputStream);
        }
    }

    @Override
    public String parseDocument(
            String fileName,
            InputStream inputStream
    ) throws IOException {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("文件内容不能为空");
        }

        String normalizedFileName = fileName.toLowerCase();
        String content;

        if (normalizedFileName.endsWith(".txt")) {
            content = parseTxt(inputStream);
        } else if (normalizedFileName.endsWith(".pdf")) {
            content = parsePdf(inputStream);
        } else if (normalizedFileName.endsWith(".docx")) {
            content = parseDocx(inputStream);
        } else if (normalizedFileName.endsWith(".doc")) {
            content = parseDoc(inputStream);
        } else {
            throw new IllegalArgumentException(
                    "不支持的文件格式: " + normalizedFileName
            );
        }

        return cleanText(content);
    }

    private String parseTxt(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String parsePdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    private String parseDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String parseDoc(InputStream inputStream) throws IOException {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String cleanText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        text = text.replace("\r\n", "\n").replace("\r", "\n");
        text = text.replaceAll("[ \\t]+", " ");
        text = text.replaceAll("\\n{3,}", "\n\n");

        StringBuilder builder = new StringBuilder();
        String[] lines = text.split("\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                builder.append(trimmed).append("\n");
            }
        }
        return builder.toString().trim();
    }
}
