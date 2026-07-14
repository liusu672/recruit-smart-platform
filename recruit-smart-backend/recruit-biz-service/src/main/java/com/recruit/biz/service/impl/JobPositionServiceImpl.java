package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.JobPositionCreateDTO;
import com.recruit.biz.dto.JobPositionQueryDTO;
import com.recruit.biz.dto.JobPositionUpdateDTO;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.enums.JobPositionStatus;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.JobPositionService;
import com.recruit.biz.vo.JobPositionVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobPositionServiceImpl implements JobPositionService {
    @Resource
    private JobPositionMapper jobPositionMapper;

    @Override
    public Long createJob(JobPositionCreateDTO dto){
        JobPosition job=new JobPosition();
        //重复判断
        Long count=jobPositionMapper.selectCount(
                new LambdaQueryWrapper<JobPosition>()
                        .eq(JobPosition::getTitle,dto.getTitle())
                        .eq(JobPosition::getDepartment,dto.getDepartment())
                        .ne(JobPosition::getStatus, JobPositionStatus.CLOSED.name())
        );
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该部门下已存在相同职位");
        }
        job.setTitle(dto.getTitle());
        job.setDepartment(dto.getDepartment());
        job.setLocation(dto.getLocation());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
        if(job.getSalaryMax().compareTo(job.getSalaryMin())<0){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"最高薪资不能低于最低薪资");
        }
        job.setHeadcount(dto.getHeadcount());
        job.setCreatedBy(UserContext.getUserId());
        job.setResponsibilities(dto.getResponsibilities());
        job.setRequirements(dto.getRequirements());

        job.setStatus(JobPositionStatus.DRAFT.name());

        jobPositionMapper.insert(job);
        return job.getId();
    }

    @Override
    public void updateJob(Long id, JobPositionUpdateDTO dto) {
        JobPosition oldJob = jobPositionMapper.selectById(id);
        if (oldJob == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "职位不存在");
        }

        Long count = jobPositionMapper.selectCount(
                new LambdaQueryWrapper<JobPosition>()
                        .eq(JobPosition::getTitle, dto.getTitle())
                        .eq(JobPosition::getDepartment, dto.getDepartment())
                        .ne(JobPosition::getStatus, JobPositionStatus.CLOSED.name())
                        .ne(JobPosition::getId, id)
        );
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该部门下已存在相同职位");
        }

        if (dto.getSalaryMax().compareTo(dto.getSalaryMin()) < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "最高薪资不能低于最低薪资");
        }

        JobPosition job = new JobPosition();
        job.setId(id);
        job.setTitle(dto.getTitle());
        job.setDepartment(dto.getDepartment());
        job.setLocation(dto.getLocation());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
        job.setHeadcount(dto.getHeadcount());
        job.setResponsibilities(dto.getResponsibilities());
        job.setRequirements(dto.getRequirements());

        jobPositionMapper.updateById(job);
    }

    @Override
    public JobPositionVO getById(Long id){
        JobPosition job = jobPositionMapper.selectById(id);
        if (job == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "职位不存在");
        }
        return toVO(job);
    }

    @Override
    public JobPositionVO getOpenById(Long id) {
        JobPosition job = jobPositionMapper.selectById(id);
        if (job == null || !JobPositionStatus.OPEN.name().equals(job.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "招聘中的职位不存在");
        }
        return toVO(job);
    }

    @Override
    public PageResult<JobPositionVO> openJobPages(JobPositionQueryDTO dto) {
        JobPositionQueryDTO query = dto == null
                ? new JobPositionQueryDTO()
                : dto;
        query.setStatus(JobPositionStatus.OPEN.name());
        return jobPages(query);
    }

    @Override
    public PageResult<JobPositionVO> jobPages(JobPositionQueryDTO dto){
        if (dto == null) {
            dto = new JobPositionQueryDTO();
        }

        Integer pageNum=dto.getPageNum()==null||dto.getPageNum()<1
                ?1
                :dto.getPageNum();
        Integer pageSize=dto.getPageSize()==null||dto.getPageSize()<1||dto.getPageSize()>100
                ?10
                :dto.getPageSize();
        Page<JobPosition> page=new Page<>(pageNum,pageSize);

        LambdaQueryWrapper<JobPosition> wrapper = new LambdaQueryWrapper<>();

        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.like(JobPosition::getTitle, dto.getKeyword());
        }

        if (dto.getDepartment() != null && !dto.getDepartment().isBlank()) {
            wrapper.eq(JobPosition::getDepartment, dto.getDepartment());
        }

        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            wrapper.eq(JobPosition::getStatus, dto.getStatus());
        }

        wrapper.orderByDesc(JobPosition::getCreatedAt);

        Page<JobPosition> result = jobPositionMapper.selectPage(page, wrapper);

        List<JobPositionVO> voList = result.getRecords()
                .stream()
                .map(this::toVO)
                .toList();

        return new PageResult<>(
                result.getTotal(),
                voList
        );
    }

    private JobPositionVO toVO(JobPosition job) {
        if (job == null) {
            return null;
        }

        JobPositionVO vo = new JobPositionVO();
        vo.setId(job.getId());
        vo.setTitle(job.getTitle());
        vo.setDepartment(job.getDepartment());
        vo.setLocation(job.getLocation());
        vo.setHeadcount(job.getHeadcount());
        vo.setStatus(job.getStatus());
        vo.setCreatedAt(job.getCreatedAt());
        vo.setUpdatedAt(job.getUpdatedAt());
        vo.setDescription(job.getResponsibilities());
        vo.setRequirement(job.getRequirements());

        if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
            vo.setSalaryRange(job.getSalaryMin() + "-" + job.getSalaryMax());
        }

        JobPositionStatus status = JobPositionStatus.fromCode(job.getStatus());
        if (status != null) {
            vo.setStatusText(status.getDescription());
        }

        return vo;
    }
    @Override
    public void publishJob(Long id){
        JobPosition jobPosition= jobPositionMapper.selectById(id);
        if(jobPosition==null){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"该职位不存在");
        }
        if(!JobPositionStatus.DRAFT.name().equals(jobPosition.getStatus())){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"非草稿职位不可发布");
        }
            jobPosition.setStatus(JobPositionStatus.OPEN.name());
            jobPosition.setPublishedAt(LocalDateTime.now());
            jobPositionMapper.updateById(jobPosition);
    }

    @Override
    public void pauseJob(Long id) {
        JobPosition jobPosition = getJob(id);
        if (!JobPositionStatus.OPEN.name().equals(jobPosition.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "只有招聘中的职位可以暂停");
        }
        jobPosition.setStatus(JobPositionStatus.PAUSED.name());
        jobPositionMapper.updateById(jobPosition);
    }

    @Override
    public void resumeJob(Long id) {
        JobPosition jobPosition = getJob(id);
        if (!JobPositionStatus.PAUSED.name().equals(jobPosition.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "只有已暂停的职位可以恢复");
        }
        jobPosition.setStatus(JobPositionStatus.OPEN.name());
        jobPositionMapper.updateById(jobPosition);
    }

    @Override
    public void closeJob(Long id){
        JobPosition jobPosition = getJob(id);
        boolean canClose = JobPositionStatus.OPEN.name().equals(jobPosition.getStatus())
                || JobPositionStatus.PAUSED.name().equals(jobPosition.getStatus());
        if(!canClose){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"只有招聘中或已暂停的职位可以关闭");
        }
            jobPosition.setStatus(JobPositionStatus.CLOSED.name());
            jobPosition.setClosedAt(LocalDateTime.now());
            jobPositionMapper.updateById(jobPosition);

    }

    private JobPosition getJob(Long id) {
        JobPosition jobPosition = jobPositionMapper.selectById(id);
        if (jobPosition == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "职位不存在");
        }
        return jobPosition;
    }
}
