package com.recruit.biz.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class StoredResumeMultipartFile implements MultipartFile {

    private final Resource resource;
    private final String fileName;
    private final String contentType;
    private final long contentLength;

    public StoredResumeMultipartFile(ResumeFileResource file) {
        this.resource = file.getResource();
        this.fileName = file.getFileName();
        this.contentType = file.getContentType();
        this.contentLength = file.getContentLength();
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return contentLength == 0;
    }

    @Override
    public long getSize() {
        return contentLength;
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (InputStream inputStream = getInputStream()) {
            return inputStream.readAllBytes();
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return resource.getInputStream();
    }

    @Override
    public void transferTo(File destination) throws IOException {
        try (InputStream inputStream = getInputStream()) {
            Files.copy(
                    inputStream,
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}
