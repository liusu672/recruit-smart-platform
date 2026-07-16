package com.recruit.biz.storage;

import com.recruit.biz.config.ResumeStorageProperties;
import com.recruit.biz.enums.ResumeFileType;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class ResumeFileStorage {
    @Resource
    private ResumeStorageProperties properties;
    public StoredResumeFile store(MultipartFile file,Long candidateId){
        if(file==null|| file.isEmpty()){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"简历不能为空");
        }
        if (file.getSize() > properties.getMaxSize()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "简历文件不能超过10MB");
        }
        String extension= StringUtils.getFilenameExtension(file.getOriginalFilename());
        ResumeFileType fileType=ResumeFileType.fromExtension(extension);
        if(fileType==null){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"只允许上传PDF、DOC、DOCX文档格式");
        }
        validateContentType(file,fileType);
        Path root= Paths.get(properties.getRootPath())
                .toAbsolutePath()
                .normalize();
        Path candidateDirectory=root
                .resolve(candidateId.toString())
                .normalize();
        if(!candidateDirectory.startsWith(root)){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"文件存储路径不正确");
        }
        String storedFileName= UUID.randomUUID()+"."+fileType.getExtension();
        Path target=candidateDirectory.resolve(storedFileName).normalize();
        try{
            Files.createDirectories(candidateDirectory);
            file.transferTo(target);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR,"文件保存失败");
        }
        String relativePath = root
                .relativize(target)
                .toString()
                .replace('\\', '/');

        return new StoredResumeFile(
                relativePath,
                fileType
        );
    }
    public ResumeFileResource load(
            String relativePath,
            String fileName,
            ResumeFileType fileType
    ) {
        if (!StringUtils.hasText(relativePath)) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "简历文件路径不存在"
            );
        }

        Path root = Paths.get(properties.getRootPath())
                .toAbsolutePath()
                .normalize();
        Path target = root.resolve(relativePath).normalize();

        if (!target.startsWith(root)) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "文件访问路径不正确"
            );
        }

        if (!Files.isRegularFile(target) || !Files.isReadable(target)) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "简历文件不存在或无法读取"
            );
        }

        try {
            return new ResumeFileResource(
                    new FileSystemResource(target),
                    fileName,
                    fileType.getContentType(),
                    Files.size(target)
            );
        } catch (IOException e) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "读取简历文件失败"
            );
        }
    }
    public void delete(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return;
        }

        Path root = Paths.get(properties.getRootPath())
                .toAbsolutePath()
                .normalize();
        Path target = root.resolve(relativePath).normalize();

        if (!target.startsWith(root)) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "文件删除路径不正确"
            );
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "删除简历文件失败"
            );
        }
    }
    private void validateContentType(MultipartFile file,ResumeFileType fileType){
        String contentType = file.getContentType();

        if (!StringUtils.hasText(contentType)
                || "application/octet-stream"
                .equalsIgnoreCase(contentType)) {
            return;
        }

        if (!fileType.getContentType()
                .equalsIgnoreCase(contentType)) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "文件扩展名与文件类型不匹配"
            );
        }
    }
}
