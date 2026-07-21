package com.recruit.biz.service;

public interface ResumeParsingService {
    void parse(Long resumeId);

    void parseAutomatically(Long resumeId);
}
