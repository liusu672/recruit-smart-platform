package com.recruit.ai.service.impl;

import com.recruit.ai.dto.response.ResumeParseResponse;
import com.recruit.ai.knowledge.service.DocumentParserService;
import com.recruit.ai.service.ResumeParseService;
import com.recruit.ai.service.llm.LlmResumeParseService;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ResumeParseServiceImpl implements ResumeParseService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final int MAX_LLM_TEXT_LENGTH = 50_000;
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "pdf",
            "doc",
            "docx",
            "txt"
    );

    private final DocumentParserService documentParserService;
    private final LlmResumeParseService llmResumeParseService;

    @Override
    public ResumeParseResponse parseResume(MultipartFile file) {
        String fileName = validateAndGetFileName(file);
        String parsedContent = extractText(file, fileName);
        if (!StringUtils.hasText(parsedContent)) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "未能从简历中提取文本，请确认文件不是扫描图片"
            );
        }

        boolean truncated = parsedContent.length() > MAX_LLM_TEXT_LENGTH;
        String textForExtraction = truncated
                ? parsedContent.substring(0, MAX_LLM_TEXT_LENGTH)
                : parsedContent;

        ResumeParseResponse response;
        try {
            response = llmResumeParseService.extract(textForExtraction);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI简历结构化解析失败，请稍后重试"
            );
        }

        if (response == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI简历结构化解析结果为空"
            );
        }

        response.setParsedContent(parsedContent);
        response.setSkills(normalizeValues(response.getSkills()));

        List<String> warnings = new ArrayList<>(
                normalizeValues(response.getWarnings())
        );
        if (truncated) {
            warnings.add("简历正文过长，结构化提取仅分析前50000个字符");
        }
        response.setWarnings(List.copyOf(new LinkedHashSet<>(warnings)));
        return response;
    }

    private String validateAndGetFileName(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "简历文件不能为空"
            );
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "简历文件不能超过10MB"
            );
        }

        String originalFileName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFileName)) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "简历文件名不能为空"
            );
        }

        String fileName = StringUtils.cleanPath(originalFileName);
        if (fileName.contains("..")) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "简历文件名不合法"
            );
        }

        String extension = StringUtils.getFilenameExtension(fileName);
        if (extension == null
                || !SUPPORTED_EXTENSIONS.contains(
                        extension.toLowerCase(Locale.ROOT)
                )) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "仅支持PDF、DOC、DOCX和TXT格式的简历"
            );
        }
        return fileName;
    }

    private String extractText(MultipartFile file, String fileName) {
        try (InputStream inputStream = file.getInputStream()) {
            return documentParserService.parseDocument(
                    fileName,
                    inputStream
            );
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    e.getMessage()
            );
        } catch (IOException e) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "读取简历文件失败"
            );
        }
    }

    private List<String> normalizeValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }
}
