package com.recruit.ai.knowledge.service;

import java.io.IOException;
import java.io.InputStream;

public interface DocumentParserService {
    String parseDocument(String filePath) throws IOException;

    String parseDocument(String fileName, InputStream inputStream)
            throws IOException;
}
