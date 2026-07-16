package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResumeFileType {
    PDF("pdf","application/pdf"),
    DOC("doc","application/msword"),
    DOCX("docx","application/vnd.openxmlformats-officedocument"+".wordprocessingml.document");
    private final String extension;
    private final String contentType;
    public static ResumeFileType fromExtension(String extension){
        if(extension==null){
            return null;
        }
        for (ResumeFileType type : values()) {
            if (type.extension.equalsIgnoreCase(extension)) {
                return type;
            }
        }
        return null;
    }
    public static ResumeFileType fromCode(String code) {
        if (code == null) {
            return null;
        }

        try {
            return ResumeFileType.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
