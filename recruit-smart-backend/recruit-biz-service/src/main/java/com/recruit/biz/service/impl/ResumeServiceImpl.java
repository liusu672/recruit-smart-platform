package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.recruit.biz.dto.ResumeRenameDTO;
import com.recruit.biz.dto.ResumeUploadDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.enums.ResumeFileType;
import com.recruit.biz.enums.ResumeParseStatus;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ResumeService;
import com.recruit.biz.storage.ResumeFileStorage;
import com.recruit.biz.storage.ResumeFileResource;
import com.recruit.biz.storage.StoredResumeFile;
import com.recruit.biz.vo.ResumeDetailVO;
import com.recruit.biz.vo.ResumeSummaryVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ResumeServiceImpl implements ResumeService {
    @Resource
    private ResumeMapper resumeMapper;
    @Resource
    private CandidateMapper candidateMapper;
    @Resource
    private JobApplicationMapper jobApplicationMapper;
    @Resource
    private InterviewMapper interviewMapper;
    @Resource
    private ResumeFileStorage resumeFileStorage;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upload(ResumeUploadDTO dto){
        Candidate candidate=candidateMapper.selectOne(
                new LambdaQueryWrapper<Candidate>()
                        .eq(Candidate::getUserId,
                                UserContext.getUserId())
        );
        if(candidate==null){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"该账号没有绑定候选人档案");
        }
        Long resumeCount=resumeMapper.selectCount(
                new LambdaQueryWrapper<Resume>()
                        .eq(Resume::getCandidateId,candidate.getId())
        );
        boolean shouldSetDefault=
                resumeCount==0||Boolean.TRUE.equals(dto.getSetDefault());
            if (shouldSetDefault) {
                resumeMapper.update(
                        null,
                        new LambdaUpdateWrapper<Resume>()
                                .eq(
                                        Resume::getCandidateId,
                                        candidate.getId()
                                )
                                .set(
                                        Resume::getIsDefault,
                                        0
                                )
                );
            }
        StoredResumeFile storedFile=resumeFileStorage.store(dto.getFile(),candidate.getId());
            Resume resume=new Resume();
            resume.setCandidateId(candidate.getId());
            resume.setResumeName(dto.getResumeName().trim());
            resume.setFileUrl(storedFile.getRelativePath());
            resume.setFileType(storedFile.getFileType().name());
        resume.setIsDefault(
                shouldSetDefault ? 1 : 0
        );
        resume.setParseStatus(
                ResumeParseStatus.PENDING.name()
        );

        resumeMapper.insert(resume);

        return resume.getId();
    }
    @Override
    public List<ResumeSummaryVO> listMyResumes(){
        Candidate candidate=candidateMapper.selectOne(
                new LambdaQueryWrapper<Candidate>()
                        .eq(Candidate::getUserId,UserContext.getUserId())
        );
        if(candidate==null){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"该账号没有绑定候选人档案");
        }
        List<Resume> resumes=resumeMapper.selectList(
                new LambdaQueryWrapper<Resume>()
                        .eq(Resume::getCandidateId,candidate.getId())
                        .orderByDesc(Resume::getIsDefault)
                        .orderByAsc(Resume::getCreatedAt)
        );
        return resumes.stream()
                .map(this::toSummaryVO)
                .toList();
    }
    private ResumeSummaryVO toSummaryVO(Resume resume) {
        ResumeSummaryVO vo = new ResumeSummaryVO();

        vo.setId(resume.getId());
        vo.setResumeName(resume.getResumeName());
        vo.setFileType(resume.getFileType());
        vo.setIsDefault(resume.getIsDefault());
        vo.setCreatedAt(resume.getCreatedAt());
        vo.setParseStatus(resume.getParseStatus());

        ResumeParseStatus status =
                ResumeParseStatus.fromCode(resume.getParseStatus());

        vo.setParseStatusText(
                status == null ? "未知状态" : status.getDescription()
        );

        return vo;
    }
    @Override
    public ResumeDetailVO getDetail(Long id){
        Resume resume = resumeMapper.selectById(id);

        if (resume == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "简历不存在"
            );
        }

        checkResumeAccess(resume);

        return toDetailVO(resume);
    }
    private ResumeDetailVO toDetailVO(Resume resume) {
        ResumeDetailVO vo = new ResumeDetailVO();

        vo.setId(resume.getId());
        vo.setCandidateId(resume.getCandidateId());
        vo.setResumeName(resume.getResumeName());
        vo.setFileType(resume.getFileType());
        vo.setIsDefault(resume.getIsDefault());
        vo.setParseStatus(resume.getParseStatus());
        vo.setParsedContent(resume.getParsedContent());
        vo.setSkills(resume.getSkills());
        vo.setProjectExperience(resume.getProjectExperience());
        vo.setWorkExperience(resume.getWorkExperience());
        vo.setCreatedAt(resume.getCreatedAt());
        vo.setUpdatedAt(resume.getUpdatedAt());

        ResumeParseStatus status =
                ResumeParseStatus.fromCode(resume.getParseStatus());

        vo.setParseStatusText(
                status == null ? "未知状态" : status.getDescription()
        );

        return vo;
    }
    @Override
    public ResumeFileResource download(Long id) {
        Resume resume = resumeMapper.selectById(id);

        if (resume == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "简历不存在"
            );
        }

        checkResumeAccess(resume);

        ResumeFileType fileType =
                ResumeFileType.fromCode(resume.getFileType());
        if (fileType == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "简历文件类型不正确"
            );
        }

        String fileName = buildDownloadFileName(resume, fileType);

        return resumeFileStorage.load(
                resume.getFileUrl(),
                fileName,
                fileType
        );
    }
    @Override
    public ResumeFileResource preview(Long id) {
        ResumeFileResource file = download(id);

        if (!ResumeFileType.PDF.getContentType()
                .equals(file.getContentType())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "当前仅支持PDF文件在线预览"
            );
        }

        return file;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        Resume resume = resumeMapper.selectById(id);

        if (resume == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "简历不存在"
            );
        }

        checkResumeAccess(resume);

        if (Integer.valueOf(1).equals(resume.getIsDefault())) {
            return;
        }

        resumeMapper.update(
                null,
                new LambdaUpdateWrapper<Resume>()
                        .eq(
                                Resume::getCandidateId,
                                resume.getCandidateId()
                        )
                        .set(Resume::getIsDefault, 0)
        );

        int updated = resumeMapper.update(
                null,
                new LambdaUpdateWrapper<Resume>()
                        .eq(Resume::getId, id)
                        .eq(
                                Resume::getCandidateId,
                                resume.getCandidateId()
                        )
                        .set(Resume::getIsDefault, 1)
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "设置默认简历失败"
            );
        }
    }
    @Override
    public void rename(Long id, ResumeRenameDTO dto) {
        Resume resume = resumeMapper.selectById(id);

        if (resume == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "简历不存在"
            );
        }

        checkResumeAccess(resume);

        String resumeName = dto.getResumeName().trim();
        if (resumeName.equals(resume.getResumeName())) {
            return;
        }

        int updated = resumeMapper.update(
                null,
                new LambdaUpdateWrapper<Resume>()
                        .eq(Resume::getId, id)
                        .eq(
                                Resume::getCandidateId,
                                resume.getCandidateId()
                        )
                        .set(Resume::getResumeName, resumeName)
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "修改简历名称失败"
            );
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Resume resume = resumeMapper.selectById(id);

        if (resume == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "简历不存在"
            );
        }

        checkResumeAccess(resume);

        Long applicationCount = jobApplicationMapper.selectCount(
                new LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getResumeId, id)
        );
        if (applicationCount > 0) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "该简历已用于职位投递，不能删除"
            );
        }

        int deleted = resumeMapper.delete(
                new LambdaQueryWrapper<Resume>()
                        .eq(Resume::getId, id)
                        .eq(
                                Resume::getCandidateId,
                                resume.getCandidateId()
                        )
        );
        if (deleted != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "删除简历记录失败"
            );
        }

        if (Integer.valueOf(1).equals(resume.getIsDefault())) {
            Resume replacement = resumeMapper.selectOne(
                    new LambdaQueryWrapper<Resume>()
                            .eq(
                                    Resume::getCandidateId,
                                    resume.getCandidateId()
                            )
                            .orderByDesc(Resume::getCreatedAt)
                            .orderByDesc(Resume::getId)
                            .last("LIMIT 1")
            );

            if (replacement != null) {
                int updated = resumeMapper.update(
                        null,
                        new LambdaUpdateWrapper<Resume>()
                                .eq(Resume::getId, replacement.getId())
                                .eq(
                                        Resume::getCandidateId,
                                        resume.getCandidateId()
                                )
                                .set(Resume::getIsDefault, 1)
                );

                if (updated != 1) {
                    throw new BusinessException(
                            ErrorCode.BUSINESS_ERROR,
                            "重新设置默认简历失败"
                    );
                }
            }
        }

        resumeFileStorage.delete(resume.getFileUrl());
    }
    private String buildDownloadFileName(
            Resume resume,
            ResumeFileType fileType
    ) {
        String fileName = resume.getResumeName();
        if (fileName == null || fileName.isBlank()) {
            fileName = "resume-" + resume.getId();
        }

        fileName = fileName.trim()
                .replace('\r', '_')
                .replace('\n', '_');

        String extension = "." + fileType.getExtension();
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(extension)) {
            fileName += extension;
        }

        return fileName;
    }
    private void checkResumeAccess(Resume resume) {
        String roleCode = UserContext.getRoleCode();
        if ("ADMIN".equals(roleCode) || "HR".equals(roleCode)) {
            return;
        }

        if ("INTERVIEWER".equals(roleCode)) {
            Set<Long> applicationIds = jobApplicationMapper.selectList(
                            new LambdaQueryWrapper<JobApplication>()
                                    .eq(
                                            JobApplication::getResumeId,
                                            resume.getId()
                                    )
                    )
                    .stream()
                    .map(JobApplication::getId)
                    .collect(Collectors.toSet());

            if (!applicationIds.isEmpty()) {
                Long assignedCount = interviewMapper.selectCount(
                        new LambdaQueryWrapper<Interview>()
                                .in(
                                        Interview::getApplicationId,
                                        applicationIds
                                )
                                .eq(
                                        Interview::getInterviewerId,
                                        UserContext.getUserId()
                                )
                );
                if (assignedCount != null && assignedCount > 0) {
                    return;
                }
            }

            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "无权访问未分配给自己的面试简历"
            );
        }

        if ("CANDIDATE".equals(roleCode)) {
            Candidate candidate = candidateMapper.selectOne(
                    new LambdaQueryWrapper<Candidate>()
                            .eq(
                                    Candidate::getUserId,
                                    UserContext.getUserId()
                            )
            );

            if (candidate != null
                    && candidate.getId().equals(resume.getCandidateId())) {
                return;
            }
        }

        throw new BusinessException(
                ErrorCode.FORBIDDEN,
                "无权访问该简历"
        );
    }
}
