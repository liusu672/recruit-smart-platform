package com.recruit.ai.demo.knowledge.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class DocumentParserService {

    public String parseDocument(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("文件不存在: " + path);
        }

        String fileName = path.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".txt")) {
            return parseTxt(path);
        }

        if (fileName.endsWith(".pdf")) {
            return parsePdf(path);
        }

        if (fileName.endsWith(".docx")) {
            return parseDocx(path);
        }

        throw new IllegalArgumentException("不支持的文档格式: " + fileName);
    }

    public boolean isSupported(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".txt")
                || fileName.endsWith(".pdf")
                || fileName.endsWith(".docx");
    }

    private String parseTxt(Path path) throws IOException {
        return cleanText(Files.readString(path, StandardCharsets.UTF_8));
    }

    private String parsePdf(Path path) throws IOException {
        try (PDDocument document = PDDocument.load(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return cleanText(stripper.getText(document));
        }
    }

    private String parseDocx(Path path) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(path.toFile());
             XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return cleanText(extractor.getText());
        }
    }

    private String cleanText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        text = text.replace("\r\n", "\n").replace("\r", "\n");
        text = text.replaceAll("\\n{3,}", "\n\n");

        StringBuilder result = new StringBuilder();
        String[] lines = text.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                result.append(trimmed).append("\n");
            }
        }

        return result.toString().trim();
    }
}