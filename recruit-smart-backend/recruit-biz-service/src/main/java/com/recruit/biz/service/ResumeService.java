package com.recruit.biz.service;

import com.recruit.biz.dto.ResumeRenameDTO;
import com.recruit.biz.dto.ResumeUploadDTO;
import com.recruit.biz.storage.ResumeFileResource;
import com.recruit.biz.vo.ResumeDetailVO;
import com.recruit.biz.vo.ResumeSummaryVO;

import java.util.List;

public interface ResumeService {
    Long upload(ResumeUploadDTO dto);
    List<ResumeSummaryVO> listMyResumes();
    ResumeDetailVO getDetail(Long id);
    ResumeFileResource download(Long id);
    ResumeFileResource preview(Long id);
    void setDefault(Long id);
    void rename(Long id, ResumeRenameDTO dto);
    void delete(Long id);
}
