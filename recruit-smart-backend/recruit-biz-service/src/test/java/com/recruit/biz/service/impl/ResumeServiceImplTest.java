package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.dto.ResumeRenameDTO;
import com.recruit.biz.dto.ResumeUploadDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.enums.ResumeFileType;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.storage.ResumeFileStorage;
import com.recruit.biz.storage.StoredResumeFile;
import com.recruit.biz.vo.ResumeDetailVO;
import com.recruit.biz.vo.ResumeSummaryVO;
import com.recruit.common.exception.BusinessException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                Resume.class
        );
    }

    @Mock private ResumeMapper resumeMapper;
    @Mock private CandidateMapper candidateMapper;
    @Mock private JobApplicationMapper jobApplicationMapper;
    @Mock private InterviewMapper interviewMapper;
    @Mock private ResumeFileStorage resumeFileStorage;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    @BeforeEach
    void setUp() {
        UserContext.set(new CurrentUser(1L, "candidate", "CANDIDATE"));
    }

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    // ==================== upload() ====================

    @Test
    void uploadFirstResumeSetsDefault() {
        Candidate candidate = candidate(10L, 1L);
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(candidate);
        when(resumeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        MultipartFile file = mock(MultipartFile.class);
        ResumeUploadDTO dto = new ResumeUploadDTO();
        dto.setResumeName("我的简历");
        dto.setFile(file);

        when(resumeFileStorage.store(file, 10L))
                .thenReturn(new StoredResumeFile("resumes/10/uuid.pdf", ResumeFileType.PDF));
        doAnswer(inv -> {
            Resume r = inv.getArgument(0);
            r.setId(100L);
            return 1;
        }).when(resumeMapper).insert(any(Resume.class));

        Long id = resumeService.upload(dto);

        assertEquals(100L, id);
        verify(resumeMapper).update(eq(null), any(LambdaUpdateWrapper.class));

        ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);
        verify(resumeMapper).insert(captor.capture());
        Resume saved = captor.getValue();
        assertEquals("我的简历", saved.getResumeName());
        assertEquals(1, saved.getIsDefault().intValue());
        assertEquals("PENDING", saved.getParseStatus());
    }

    @Test
    void uploadWithExplicitSetDefault() {
        Candidate candidate = candidate(10L, 1L);
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(candidate);
        when(resumeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        MultipartFile file = mock(MultipartFile.class);
        ResumeUploadDTO dto = new ResumeUploadDTO();
        dto.setResumeName("新简历");
        dto.setFile(file);
        dto.setSetDefault(true);

        when(resumeFileStorage.store(file, 10L))
                .thenReturn(new StoredResumeFile("resumes/10/uuid.pdf", ResumeFileType.PDF));
        doAnswer(inv -> {
            Resume r = inv.getArgument(0);
            r.setId(101L);
            return 1;
        }).when(resumeMapper).insert(any(Resume.class));

        resumeService.upload(dto);

        ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);
        verify(resumeMapper).insert(captor.capture());
        assertEquals(1, captor.getValue().getIsDefault().intValue());
    }

    @Test
    void uploadNoCandidateThrowsException() {
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> resumeService.upload(new ResumeUploadDTO()));
    }

    // ==================== listMyResumes() ====================

    @Test
    void listMyResumesSuccess() {
        Candidate candidate = candidate(10L, 1L);
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(candidate);

        Resume resume = resume(100L, 10L, "简历.pdf", 1);
        when(resumeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(resume));

        List<ResumeSummaryVO> result = resumeService.listMyResumes();

        assertEquals(1, result.size());
        assertEquals("简历.pdf", result.get(0).getResumeName());
        assertEquals(1, result.get(0).getIsDefault().intValue());
    }

    // ==================== getDetail() ====================

    @Test
    void getDetailSuccess() {
        Candidate candidate = candidate(10L, 1L);
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(candidate);

        Resume resume = resume(100L, 10L, "简历.pdf", 1);
        resume.setParsedContent("解析内容");
        when(resumeMapper.selectById(100L)).thenReturn(resume);

        ResumeDetailVO vo = resumeService.getDetail(100L);

        assertNotNull(vo);
        assertEquals("简历.pdf", vo.getResumeName());
        assertEquals("解析内容", vo.getParsedContent());
    }

    @Test
    void getDetailNotFoundThrowsException() {
        when(resumeMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class,
                () -> resumeService.getDetail(99L));
    }

    // ==================== getDetail() Admin ====================

    @Test
    void getDetailAsAdminSuccess() {
        UserContext.set(new CurrentUser(1L, "admin", "ADMIN"));
        Resume resume = resume(100L, 10L, "简历.pdf", 1);
        resume.setParsedContent("内容");
        when(resumeMapper.selectById(100L)).thenReturn(resume);

        ResumeDetailVO vo = resumeService.getDetail(100L);
        assertNotNull(vo);
    }

    // ==================== rename() ====================

    @Test
    void renameSuccess() {
        Candidate candidate = candidate(10L, 1L);
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(candidate);

        Resume resume = resume(100L, 10L, "旧名.pdf", 1);
        when(resumeMapper.selectById(100L)).thenReturn(resume);
        when(resumeMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);

        ResumeRenameDTO dto = new ResumeRenameDTO();
        dto.setResumeName("新名字");
        resumeService.rename(100L, dto);

        verify(resumeMapper, times(1)).update(eq(null), any(LambdaUpdateWrapper.class));
    }

    @Test
    void renameSameNameSkipsUpdate() {
        Candidate candidate = candidate(10L, 1L);
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(candidate);

        Resume resume = resume(100L, 10L, "相同.pdf", 1);
        when(resumeMapper.selectById(100L)).thenReturn(resume);

        ResumeRenameDTO dto = new ResumeRenameDTO();
        dto.setResumeName("相同.pdf");
        resumeService.rename(100L, dto);

        verify(resumeMapper, never()).updateById(any());
    }

    // ==================== delete() ====================

    @Test
    void deleteSuccess() {
        Candidate candidate = candidate(10L, 1L);
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(candidate);

        Resume resume = resume(100L, 10L, "简历.pdf", 1);
        when(resumeMapper.selectById(100L)).thenReturn(resume);
        when(jobApplicationMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L);
        when(resumeMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        doNothing().when(resumeFileStorage).delete("resumes/10/uuid.pdf");

        resumeService.delete(100L);

        verify(resumeMapper).delete(any(LambdaQueryWrapper.class));
        verify(resumeFileStorage).delete("resumes/10/uuid.pdf");
    }

    @Test
    void deleteUsedInApplicationThrowsException() {
        Candidate candidate = candidate(10L, 1L);
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(candidate);

        Resume resume = resume(100L, 10L, "简历.pdf", 1);
        when(resumeMapper.selectById(100L)).thenReturn(resume);
        when(jobApplicationMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(1L);

        assertThrows(BusinessException.class,
                () -> resumeService.delete(100L));
    }

    // ==================== Factory Methods ====================

    private Candidate candidate(Long id, Long userId) {
        Candidate c = new Candidate();
        c.setId(id);
        c.setUserId(userId);
        c.setName("测试候选人");
        return c;
    }

    private Resume resume(Long id, Long candidateId, String name, int isDefault) {
        Resume r = new Resume();
        r.setId(id);
        r.setCandidateId(candidateId);
        r.setResumeName(name);
        r.setFileUrl("resumes/10/uuid.pdf");
        r.setFileType("PDF");
        r.setIsDefault(isDefault);
        r.setParseStatus("PENDING");
        return r;
    }
}
