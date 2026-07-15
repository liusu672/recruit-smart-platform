package com.recruit.biz.storage;

import com.recruit.biz.enums.ResumeFileType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoredResumeFile {
    private String relativePath;
    private ResumeFileType fileType;
}
