package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.CandidateCreateDTO;
import com.recruit.biz.dto.CandidateQueryDTO;
import com.recruit.biz.dto.CandidateSelfUpdateDTO;
import com.recruit.biz.dto.CandidateUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.enums.ResumeParseStatus;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.CandidateService;
import com.recruit.biz.vo.CandidateDetailVO;
import com.recruit.biz.vo.CandidateVO;
import com.recruit.biz.vo.ResumeSummaryVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CandidateServiceImpl implements CandidateService {
    @Resource
    private CandidateMapper candidateMapper;
    @Resource
    private ResumeMapper resumeMapper;
    @Override
    public Long createCandidate(CandidateCreateDTO dto) {
        Long phoneCount = candidateMapper.selectCount(
                new LambdaQueryWrapper<Candidate>()
                        .eq(Candidate::getPhone, dto.getPhone())
        );

        if (phoneCount > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "手机号已存在");
        }

        Candidate candidate = new Candidate();
        candidate.setName(dto.getName().trim());
        candidate.setGender(dto.getGender());
        candidate.setAge(dto.getAge());
        candidate.setPhone(dto.getPhone());
        candidate.setEmail(dto.getEmail());
        candidate.setEducation(dto.getEducation());
        candidate.setSchool(dto.getSchool());
        candidate.setMajor(dto.getMajor());
        candidate.setYearsOfExperience(
                dto.getYearsOfExperience() == null
                        ? 0 : dto.getYearsOfExperience()
        );
        candidate.setSource(
                StringUtils.hasText(dto.getSource())
                        ? dto.getSource()
                        : "HR_IMPORT"
        );
        candidate.setCurrentStatus("AVAILABLE");
        candidate.setCreatedBy(UserContext.getUserId());

        candidateMapper.insert(candidate);
        return candidate.getId();
    }
    @Override
    public void updateCandidate(Long id, CandidateUpdateDTO dto) {
        Candidate candidate = candidateMapper.selectById(id);

        if (candidate == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "候选人不存在"
            );
        }

        if (StringUtils.hasText(dto.getPhone())) {
            Long phoneCount = candidateMapper.selectCount(
                    new LambdaQueryWrapper<Candidate>()
                            .eq(Candidate::getPhone, dto.getPhone())
                            .ne(Candidate::getId, id)
            );

            if (phoneCount > 0) {
                throw new BusinessException(
                        ErrorCode.PARAM_ERROR,
                        "手机号已被其他候选人使用"
                );
            }
        }

        if (dto.getName() != null) {
            if (!StringUtils.hasText(dto.getName())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "姓名不能为空");
            }
            candidate.setName(dto.getName().trim());
        }

        if (StringUtils.hasText(dto.getGender())) {
            candidate.setGender(dto.getGender());
        }

        if (dto.getAge() != null) {
            candidate.setAge(dto.getAge());
        }

        if (StringUtils.hasText(dto.getPhone())) {
            candidate.setPhone(dto.getPhone());
        }

        if (StringUtils.hasText(dto.getEmail())) {
            candidate.setEmail(dto.getEmail());
        }

        if (StringUtils.hasText(dto.getEducation())) {
            candidate.setEducation(dto.getEducation());
        }

        if (StringUtils.hasText(dto.getSchool())) {
            candidate.setSchool(dto.getSchool());
        }

        if (StringUtils.hasText(dto.getMajor())) {
            candidate.setMajor(dto.getMajor());
        }

        if (dto.getYearsOfExperience() != null) {
            candidate.setYearsOfExperience(
                    dto.getYearsOfExperience()
            );
        }

        if (StringUtils.hasText(dto.getSource())) {
            candidate.setSource(dto.getSource());
        }

        candidateMapper.updateById(candidate);
    }

    @Override
    public CandidateDetailVO getCandidateDetail(Long id) {
        Candidate candidate = candidateMapper.selectById(id);
        if (candidate == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "候选人不存在");
        }
        return toDetailVO(candidate);
    }

    @Override
    public CandidateDetailVO getCurrentCandidate() {
        return toDetailVO(getCurrentCandidateEntity());
    }

    @Override
    public void updateCurrentCandidate(CandidateSelfUpdateDTO dto) {
        if (dto.getGender() == null
                && dto.getAge() == null
                && dto.getEducation() == null
                && dto.getSchool() == null
                && dto.getMajor() == null
                && dto.getYearsOfExperience() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "没有需要修改的求职资料");
        }

        Candidate candidate = getCurrentCandidateEntity();

        if (dto.getGender() != null) {
            candidate.setGender(dto.getGender());
        }
        if (dto.getAge() != null) {
            candidate.setAge(dto.getAge());
        }
        if (dto.getEducation() != null) {
            candidate.setEducation(requireText(dto.getEducation(), "学历"));
        }
        if (dto.getSchool() != null) {
            candidate.setSchool(requireText(dto.getSchool(), "学校"));
        }
        if (dto.getMajor() != null) {
            candidate.setMajor(requireText(dto.getMajor(), "专业"));
        }
        if (dto.getYearsOfExperience() != null) {
            candidate.setYearsOfExperience(dto.getYearsOfExperience());
        }

        candidateMapper.updateById(candidate);
    }
    @Override
    public PageResult<CandidateVO> pageCandidate(CandidateQueryDTO dto) {
        CandidateQueryDTO query = dto == null
                ? new CandidateQueryDTO()
                : dto;

        Integer pageNum = query.getPageNum() == null || query.getPageNum() < 1
                ? 1 : query.getPageNum();

        Integer pageSize = query.getPageSize() == null
                || query.getPageSize() < 1
                || query.getPageSize() > 100
                ? 10 : query.getPageSize();

        if (query.getMinYearsOfExperience() != null
                && query.getMaxYearsOfExperience() != null
                && query.getMinYearsOfExperience()
                > query.getMaxYearsOfExperience()) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "最低工作年限不能大于最高工作年限"
            );
        }

        Page<Candidate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Candidate> wrapper =
                new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();

            wrapper.and(w -> w
                    .like(Candidate::getName, keyword)
                    .or()
                    .like(Candidate::getPhone, keyword)
                    .or()
                    .like(Candidate::getEmail, keyword)
            );
        }

        if (StringUtils.hasText(query.getGender())) {
            wrapper.eq(Candidate::getGender, query.getGender());
        }

        if (StringUtils.hasText(query.getEducation())) {
            wrapper.eq(Candidate::getEducation, query.getEducation());
        }

        if (StringUtils.hasText(query.getSchool())) {
            wrapper.like(Candidate::getSchool, query.getSchool());
        }

        if (StringUtils.hasText(query.getMajor())) {
            wrapper.like(Candidate::getMajor, query.getMajor());
        }

        if (StringUtils.hasText(query.getCurrentStatus())) {
            wrapper.eq(
                    Candidate::getCurrentStatus,
                    query.getCurrentStatus()
            );
        }

        if (StringUtils.hasText(query.getSource())) {
            wrapper.eq(Candidate::getSource, query.getSource());
        }

        if (query.getMinYearsOfExperience() != null) {
            wrapper.ge(
                    Candidate::getYearsOfExperience,
                    query.getMinYearsOfExperience()
            );
        }

        if (query.getMaxYearsOfExperience() != null) {
            wrapper.le(
                    Candidate::getYearsOfExperience,
                    query.getMaxYearsOfExperience()
            );
        }

        wrapper.orderByDesc(Candidate::getCreatedAt);

        Page<Candidate> result =
                candidateMapper.selectPage(page, wrapper);

        List<CandidateVO> voList = result.getRecords()
                .stream()
                .map(this::toVO)
                .toList();

        return new PageResult<>(
                result.getTotal(),
                voList
        );
    }
    private CandidateVO toVO(Candidate candidate){
        if(candidate==null){
            return null;
        }
        CandidateVO vo=new CandidateVO();
        vo.setId(candidate.getId());
        vo.setName(candidate.getName());
        vo.setAge(candidate.getAge());
        vo.setGender(candidate.getGender());
        vo.setPhone(candidate.getPhone());
        vo.setEmail(candidate.getEmail());
        vo.setEducation(candidate.getEducation());
        vo.setSchool(candidate.getSchool());
        vo.setMajor(candidate.getMajor());
        vo.setYearsOfExperience(candidate.getYearsOfExperience());
        vo.setSource(candidate.getSource());
        vo.setCurrentStatus(candidate.getCurrentStatus());
        vo.setCreatedAt(candidate.getCreatedAt());
        vo.setHasAccount(candidate.getUserId() != null);
        return vo;
    }

    private Candidate getCurrentCandidateEntity() {
        Candidate candidate = candidateMapper.selectOne(
                new LambdaQueryWrapper<Candidate>()
                        .eq(Candidate::getUserId, UserContext.getUserId())
        );
        if (candidate == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "当前账号未绑定候选人档案");
        }
        return candidate;
    }

    private CandidateDetailVO toDetailVO(Candidate candidate) {
        CandidateDetailVO vo = new CandidateDetailVO();
        vo.setId(candidate.getId());
        vo.setName(candidate.getName());
        vo.setGender(candidate.getGender());
        vo.setAge(candidate.getAge());
        vo.setPhone(candidate.getPhone());
        vo.setEmail(candidate.getEmail());
        vo.setEducation(candidate.getEducation());
        vo.setSchool(candidate.getSchool());
        vo.setMajor(candidate.getMajor());
        vo.setYearsOfExperience(candidate.getYearsOfExperience());
        vo.setCurrentStatus(candidate.getCurrentStatus());
        vo.setSource(candidate.getSource());
        vo.setHasAccount(candidate.getUserId() != null);
        vo.setCreatedAt(candidate.getCreatedAt());
        vo.setUpdatedAt(candidate.getUpdatedAt());

        List<ResumeSummaryVO> resumes = resumeMapper.selectList(
                        new LambdaQueryWrapper<Resume>()
                                .eq(Resume::getCandidateId, candidate.getId())
                                .orderByDesc(Resume::getIsDefault)
                                .orderByDesc(Resume::getCreatedAt)
                ).stream()
                .map(this::toResumeSummaryVO)
                .toList();
        vo.setResumes(resumes);
        return vo;
    }

    private ResumeSummaryVO toResumeSummaryVO(Resume resume) {
        ResumeSummaryVO vo = new ResumeSummaryVO();
        vo.setId(resume.getId());
        vo.setResumeName(resume.getResumeName());
        vo.setFileUrl(resume.getFileUrl());
        vo.setFileType(resume.getFileType());
        vo.setIsDefault(resume.getIsDefault());
        vo.setCreatedAt(resume.getCreatedAt());
        vo.setParseStatus(resume.getParseStatus());
        ResumeParseStatus status =ResumeParseStatus.fromCode(resume.getParseStatus());
        if(status!=null){
            vo.setParseStatusText(status.getDescription());
        }else{
            vo.setParseStatusText("未知状态");
        }
        return vo;
    }

    private String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, fieldName + "不能为空");
        }
        return value.trim();
    }
}
