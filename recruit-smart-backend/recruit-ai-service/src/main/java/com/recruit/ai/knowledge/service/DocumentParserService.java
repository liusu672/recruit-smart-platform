package com.recruit.ai.knowledge.service;

import java.io.IOException;

public interface DocumentParserService {
    String parseDocument(String filePath) throws IOException;
}