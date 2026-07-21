package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.enums.ResumeFileType;
import com.recruit.biz.enums.ResumeParseStatus;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ResumeParsingService;
import com.recruit.biz.storage.ResumeFileResource;
import com.recruit.biz.storage.ResumeFileStorage;
import com.recruit.biz.storage.StoredResumeMultipartFile;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.feign.client.AiServiceClient;
import com.recruit.feign.dto.response.ResumeParseResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeParsingServiceImpl implements ResumeParsingService {

    private final ResumeMapper resumeMapper;
    private final CandidateMapper candidateMapper;
    private final ResumeFileStorage resumeFileStorage;
    private final AiServiceClient aiServiceClient;

    @Override
    public void parse(Long resumeId) {
        Resume resume = requireResume(resumeId);
        requireParseAccess(resume);

        if (ResumeParseStatus.PROCESSING.name()
                .equals(resume.getParseStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "简历正在解析，请勿重复提交"
            );
        }

        if (!markProcessing(resumeId, false)) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "简历正在解析，请勿重复提交"
            );
        }
        executeParse(resume);
    }

    @Override
    public void parseAutomatically(Long resumeId) {
        Resume resume = requireResume(resumeId);
        String status = resume.getParseStatus();
        if (ResumeParseStatus.SUCCESS.name().equals(status)
                || ResumeParseStatus.PROCESSING.name().equals(status)) {
            return;
        }
        if (!markProcessing(resumeId, true)) {
            log.info(
                    "跳过不需要自动解析的简历，resumeId={}, status={}",
                    resumeId,
                    status
            );
            return;
        }
        executeParse(resume);
    }

    private Resume requireResume(Long resumeId) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "简历不存在"
            );
        }
        return resume;
    }

    private void executeParse(Resume resume) {
        Long resumeId = resume.getId();
        try {
            ResumeFileResource file = loadResumeFile(resume);
            ResumeParseResponse response = aiServiceClient.parseResume(
                    new StoredResumeMultipartFile(file)
            );
            saveParseResult(resumeId, response);
            backfillCandidateProfileSafely(
                    resume.getCandidateId(),
                    response
            );
        } catch (Exception exception) {
            markFailed(resumeId);
            if (exception instanceof BusinessException businessException) {
                throw businessException;
            }
            if (exception instanceof FeignException feignException
                    && feignException.status() >= 400
                    && feignException.status() < 500) {
                throw new BusinessException(
                        ErrorCode.PARAM_ERROR,
                        "AI服务无法解析该简历文件"
                );
            }
            log.warn(
                    "简历AI解析失败，resumeId={}, error={}",
                    resumeId,
                    exception.getClass().getSimpleName()
            );
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "简历AI解析失败，请稍后重试"
            );
        }
    }

    private boolean markProcessing(Long resumeId, boolean automatic) {
        LambdaUpdateWrapper<Resume> wrapper =
                new LambdaUpdateWrapper<Resume>()
                        .eq(Resume::getId, resumeId);
        if (automatic) {
            wrapper.and(condition -> condition
                    .isNull(Resume::getParseStatus)
                    .or()
                    .in(
                            Resume::getParseStatus,
                            ResumeParseStatus.PENDING.name(),
                            ResumeParseStatus.FAILED.name()
                    ));
        } else {
            wrapper.and(condition -> condition
                    .isNull(Resume::getParseStatus)
                    .or()
                    .ne(
                            Resume::getParseStatus,
                            ResumeParseStatus.PROCESSING.name()
                    ));
        }
        wrapper.set(
                Resume::getParseStatus,
                ResumeParseStatus.PROCESSING.name()
        );
        return resumeMapper.update(null, wrapper) == 1;
    }

    private ResumeFileResource loadResumeFile(Resume resume) {
        ResumeFileType fileType = ResumeFileType.fromCode(
                resume.getFileType()
        );
        if (fileType == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "简历文件类型不正确"
            );
        }
        return resumeFileStorage.load(
                resume.getFileUrl(),
                "resume-" + resume.getId() + "." + fileType.getExtension(),
                fileType
        );
    }

    private void saveParseResult(
            Long resumeId,
            ResumeParseResponse response
    ) {
        if (response == null
                || !StringUtils.hasText(response.getParsedContent())) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI未返回有效的简历正文"
            );
        }

        int updated = resumeMapper.update(
                null,
                new LambdaUpdateWrapper<Resume>()
                        .eq(Resume::getId, resumeId)
                        .eq(
                                Resume::getParseStatus,
                                ResumeParseStatus.PROCESSING.name()
                        )
                        .set(
                                Resume::getParsedContent,
                                response.getParsedContent()
                        )
                        .set(
                                Resume::getSkills,
                                joinValues(response.getSkills())
                        )
                        .set(
                                Resume::getProjectExperience,
                                emptyToNull(response.getProjectExperience())
                        )
                        .set(
                                Resume::getWorkExperience,
                                emptyToNull(response.getWorkExperience())
                        )
                        .set(
                                Resume::getParseStatus,
                                ResumeParseStatus.SUCCESS.name()
                        )
        );
        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "保存简历解析结果失败"
            );
        }
    }

    private void markFailed(Long resumeId) {
        try {
            resumeMapper.update(
                    null,
                    new LambdaUpdateWrapper<Resume>()
                            .eq(Resume::getId, resumeId)
                            .eq(
                                    Resume::getParseStatus,
                                    ResumeParseStatus.PROCESSING.name()
                            )
                            .set(
                                    Resume::getParseStatus,
                                    ResumeParseStatus.FAILED.name()
                            )
            );
        } catch (Exception updateException) {
            log.error(
                    "更新简历解析失败状态异常，resumeId={}",
                    resumeId,
                    updateException
            );
        }
    }

    private void backfillCandidateProfileSafely(
            Long candidateId,
            ResumeParseResponse response
    ) {
        if (candidateId == null || response == null
                || (!StringUtils.hasText(response.getEducation())
                && !StringUtils.hasText(response.getSchool())
                && !StringUtils.hasText(response.getMajor()))) {
            return;
        }

        try {
            Candidate candidate = candidateMapper.selectById(candidateId);
            if (candidate == null) {
                log.warn(
                        "简历解析成功，但候选人不存在，candidateId={}",
                        candidateId
                );
                return;
            }

            Candidate update = new Candidate();
            update.setId(candidateId);
            boolean changed = false;

            if (!StringUtils.hasText(candidate.getEducation())
                    && StringUtils.hasText(response.getEducation())) {
                update.setEducation(response.getEducation().trim());
                changed = true;
            }
            if (!StringUtils.hasText(candidate.getSchool())
                    && StringUtils.hasText(response.getSchool())) {
                update.setSchool(response.getSchool().trim());
                changed = true;
            }
            if (!StringUtils.hasText(candidate.getMajor())
                    && StringUtils.hasText(response.getMajor())) {
                update.setMajor(response.getMajor().trim());
                changed = true;
            }

            if (changed && candidateMapper.updateById(update) != 1) {
                log.warn(
                        "AI教育信息未能补充到候选人资料，candidateId={}",
                        candidateId
                );
            }
        } catch (Exception exception) {
            log.warn(
                    "AI解析成功，但候选人教育信息补充失败，candidateId={}",
                    candidateId,
                    exception
            );
        }
    }

    private void requireParseAccess(Resume resume) {
        String roleCode = UserContext.getRoleCode();
        if ("ADMIN".equals(roleCode) || "HR".equals(roleCode)) {
            return;
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
                "无权解析该简历"
        );
    }

    private String joinValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        String joined = values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .reduce((left, right) -> left + ", " + right)
                .orElse(null);
        return emptyToNull(joined);
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
