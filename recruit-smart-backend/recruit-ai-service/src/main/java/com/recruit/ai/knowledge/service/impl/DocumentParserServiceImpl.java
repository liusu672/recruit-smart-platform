package com.recruit.ai.knowledge.service.impl;

import com.recruit.ai.knowledge.service.DocumentParserService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class DocumentParserServiceImpl implements DocumentParserService {

    @Override
    public String parseDocument(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }

        String fileName = file.getName().toLowerCase();
        String content;

        if (fileName.endsWith(".txt")) {
            content = parseTxt(file);
        } else if (fileName.endsWith(".pdf")) {
            content = parsePdf(file);
        } else if (fileName.endsWith(".docx")) {
            content = parseDocx(file);
        } else {
            throw new IllegalArgumentException("不支持的文件格式: " + fileName);
        }

        return cleanText(content);
    }

    private String parseTxt(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    private String parsePdf(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    private String parseDocx(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
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