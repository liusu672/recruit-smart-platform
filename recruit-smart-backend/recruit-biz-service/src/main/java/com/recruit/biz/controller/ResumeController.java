package com.recruit.biz.controller;

import com.recruit.biz.dto.ResumeRenameDTO;
import com.recruit.biz.dto.ResumeUploadDTO;
import com.recruit.biz.security.RequireRoles;
import com.recruit.biz.service.ResumeService;
import com.recruit.biz.storage.ResumeFileResource;
import com.recruit.biz.vo.ResumeDetailVO;
import com.recruit.biz.vo.ResumeSummaryVO;
import com.recruit.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "简历管理接口")
@RestController
@RequestMapping("/resumes")
public class ResumeController {
    @Resource
    private ResumeService resumeService;
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "简历上传接口")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Long> upload(@Valid @ModelAttribute ResumeUploadDTO dto){
        return Result.success(resumeService.upload(dto));
    }
    @GetMapping("/me")
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "查询本人简历列表")
    public Result<List<ResumeSummaryVO>> listMyResumes() {
        return Result.success(resumeService.listMyResumes());
    }
    @GetMapping("/{id}")
    @RequireRoles({
            "CANDIDATE",
            "HR",
            "ADMIN",
            "INTERVIEWER"
    })
    @Operation(summary = "查询简历详情")
    public Result<ResumeDetailVO> getDetail(
            @PathVariable Long id
    ) {
        return Result.success(resumeService.getDetail(id));
    }
    @GetMapping("/{id}/download")
    @RequireRoles({
            "CANDIDATE",
            "HR",
            "ADMIN",
            "INTERVIEWER"
    })
    @Operation(summary = "下载简历文件")
    public ResponseEntity<org.springframework.core.io.Resource> download(
            @PathVariable Long id
    ) {
        ResumeFileResource file = resumeService.download(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.getFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .contentLength(file.getContentLength())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .body(file.getResource());
    }
    @GetMapping("/{id}/preview")
    @RequireRoles({
            "CANDIDATE",
            "HR",
            "ADMIN",
            "INTERVIEWER"
    })
    @Operation(summary = "在线预览PDF简历")
    public ResponseEntity<org.springframework.core.io.Resource> preview(
            @PathVariable Long id
    ) {
        ResumeFileResource file = resumeService.preview(id);
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(file.getFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .contentLength(file.getContentLength())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .header("X-Content-Type-Options", "nosniff")
                .body(file.getResource());
    }
    @PutMapping("/{id}/default")
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "设置默认简历")
    public Result<Void> setDefault(@PathVariable Long id) {
        resumeService.setDefault(id);
        return Result.success();
    }
    @PutMapping("/{id}/name")
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "修改简历名称")
    public Result<Void> rename(
            @PathVariable Long id,
            @Valid @RequestBody ResumeRenameDTO dto
    ) {
        resumeService.rename(id, dto);
        return Result.success();
    }
    @DeleteMapping("/{id}")
    @RequireRoles({"CANDIDATE"})
    @Operation(summary = "删除简历")
    public Result<Void> delete(@PathVariable Long id) {
        resumeService.delete(id);
        return Result.success();
    }
}
