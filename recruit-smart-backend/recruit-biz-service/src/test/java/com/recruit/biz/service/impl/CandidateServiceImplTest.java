package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.CandidateCreateDTO;
import com.recruit.biz.dto.CandidateQueryDTO;
import com.recruit.biz.dto.CandidateSelfUpdateDTO;
import com.recruit.biz.dto.CandidateUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.AiMatchResultMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.vo.CandidateDetailVO;
import com.recruit.biz.vo.CandidateVO;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateServiceImplTest {

    @Mock
    private CandidateMapper candidateMapper;

    @Mock
    private ResumeMapper resumeMapper;

    @Mock
    private JobApplicationMapper jobApplicationMapper;

    @Mock
    private JobPositionMapper jobPositionMapper;

    @Mock
    private AiMatchResultMapper aiMatchResultMapper;

    @InjectMocks
    private CandidateServiceImpl candidateService;

    @BeforeEach
    void setUp() {
        UserContext.set(new CurrentUser(1L, "hr", "HR"));
    }

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    // ==================== createCandidate() ====================

    @Test
    void createCandidateSuccessReturnsId() {
        CandidateCreateDTO dto = createDTO("张三", "13800138000");
        when(candidateMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        doAnswer(inv -> {
            Candidate c = inv.getArgument(0);
            c.setId(100L);
            return 1;
        }).when(candidateMapper).insert(any(Candidate.class));

        Long id = candidateService.createCandidate(dto);

        assertEquals(100L, id);
        ArgumentCaptor<Candidate> captor = ArgumentCaptor.forClass(Candidate.class);
        verify(candidateMapper).insert(captor.capture());
        Candidate saved = captor.getValue();
        assertEquals("张三", saved.getName());
        assertEquals("13800138000", saved.getPhone());
        assertEquals("AVAILABLE", saved.getCurrentStatus());
        assertEquals("HR_IMPORT", saved.getSource());
        assertEquals(1L, saved.getCreatedBy());
    }

    @Test
    void createCandidateDuplicatePhoneThrowsException() {
        CandidateCreateDTO dto = createDTO("李四", "13800138000");
        when(candidateMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class, () -> candidateService.createCandidate(dto));
    }

    // ==================== updateCandidate() ====================

    @Test
    void updateCandidateSuccessUpdatesFields() {
        Candidate existing = candidate(1L, "旧名", "13800138000");
        when(candidateMapper.selectById(1L)).thenReturn(existing);
        when(candidateMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        CandidateUpdateDTO dto = updateDTO();
        dto.setName("新名");
        dto.setGender("女");
        dto.setAge(28);
        dto.setEducation("硕士");
        dto.setSchool("新学校");
        dto.setMajor("新专业");
        dto.setYearsOfExperience(5);
        dto.setSource("RECOMMEND");

        candidateService.updateCandidate(1L, dto);

        ArgumentCaptor<Candidate> captor = ArgumentCaptor.forClass(Candidate.class);
        verify(candidateMapper).updateById(captor.capture());
        Candidate updated = captor.getValue();
        assertEquals("新名", updated.getName());
        assertEquals("女", updated.getGender());
        assertEquals(Integer.valueOf(28), updated.getAge());
        assertEquals("硕士", updated.getEducation());
        assertEquals("新学校", updated.getSchool());
        assertEquals("新专业", updated.getMajor());
        assertEquals(Integer.valueOf(5), updated.getYearsOfExperience());
        assertEquals("RECOMMEND", updated.getSource());
    }

    @Test
    void updateCandidateNotFoundThrowsException() {
        when(candidateMapper.selectById(99L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> candidateService.updateCandidate(99L, new CandidateUpdateDTO()));
    }

    @Test
    void updateCandidateDuplicatePhoneThrowsException() {
        Candidate existing = candidate(1L, "张三", "13800138000");
        when(candidateMapper.selectById(1L)).thenReturn(existing);
        when(candidateMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        CandidateUpdateDTO dto = updateDTO();
        dto.setPhone("13900139000");

        assertThrows(BusinessException.class,
                () -> candidateService.updateCandidate(1L, dto));
    }

    @Test
    void updateCandidateEmptyNameThrowsException() {
        Candidate existing = candidate(1L, "张三", "13800138000");
        when(candidateMapper.selectById(1L)).thenReturn(existing);

        CandidateUpdateDTO dto = updateDTO();
        dto.setName("");

        assertThrows(BusinessException.class,
                () -> candidateService.updateCandidate(1L, dto));
    }

    // ==================== getCandidateDetail() ====================

    @Test
    void getCandidateDetailReturnsDetailWithResumes() {
        Candidate candidate = candidate(1L, "张三", "13800138000");
        when(candidateMapper.selectById(1L)).thenReturn(candidate);
        when(resumeMapper.selectList(any())).thenReturn(List.of());
        when(jobApplicationMapper.selectList(any())).thenReturn(List.of());

        CandidateDetailVO vo = candidateService.getCandidateDetail(1L);

        assertNotNull(vo);
        assertEquals("张三", vo.getName());
        assertNotNull(vo.getResumes());
        assertNotNull(vo.getApplications());
    }

    @Test
    void getCandidateDetailNotFoundThrowsException() {
        when(candidateMapper.selectById(99L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> candidateService.getCandidateDetail(99L));
    }

    // ==================== getCurrentCandidate() ====================

    @Test
    void getCurrentCandidateSuccess() {
        Candidate candidate = candidate(1L, "张三", "13800138000");
        candidate.setUserId(1L);
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(candidate);
        when(resumeMapper.selectList(any())).thenReturn(List.of());
        when(jobApplicationMapper.selectList(any())).thenReturn(List.of());

        CandidateDetailVO vo = candidateService.getCurrentCandidate();

        assertNotNull(vo);
        assertEquals("张三", vo.getName());
    }

    @Test
    void getCurrentCandidateNotBoundThrowsException() {
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> candidateService.getCurrentCandidate());
    }

    // ==================== updateCurrentCandidate() ====================

    @Test
    void updateCurrentCandidateSuccess() {
        Candidate candidate = candidate(1L, "张三", "13800138000");
        candidate.setUserId(1L);
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(candidate);

        CandidateSelfUpdateDTO dto = new CandidateSelfUpdateDTO();
        dto.setGender("女");
        dto.setAge(30);
        dto.setEducation("博士");
        dto.setSchool("清华");
        dto.setMajor("计算机");
        dto.setYearsOfExperience(10);

        candidateService.updateCurrentCandidate(dto);

        ArgumentCaptor<Candidate> captor = ArgumentCaptor.forClass(Candidate.class);
        verify(candidateMapper).updateById(captor.capture());
        Candidate updated = captor.getValue();
        assertEquals("女", updated.getGender());
        assertEquals(Integer.valueOf(30), updated.getAge());
        assertEquals("博士", updated.getEducation());
        assertEquals("清华", updated.getSchool());
        assertEquals("计算机", updated.getMajor());
        assertEquals(Integer.valueOf(10), updated.getYearsOfExperience());
    }

    @Test
    void updateCurrentCandidateNoFieldsThrowsException() {
        CandidateSelfUpdateDTO dto = new CandidateSelfUpdateDTO();

        assertThrows(BusinessException.class,
                () -> candidateService.updateCurrentCandidate(dto));
    }

    // ==================== pageCandidate() ====================

    @Test
    void pageCandidateReturnsPagedResults() {
        Candidate candidate = candidate(1L, "张三", "13800138000");
        Page<Candidate> pageResult = new Page<>(1, 10, 1);
        pageResult.setRecords(List.of(candidate));
        when(candidateMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        CandidateQueryDTO query = new CandidateQueryDTO();
        query.setKeyword("张三");
        PageResult<CandidateVO> result = candidateService.pageCandidate(query);

        assertNotNull(result);
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("张三", result.getRecords().get(0).getName());
    }

    @Test
    void pageCandidateEmptyResults() {
        Page<Candidate> emptyPage = new Page<>(1, 10, 0);
        emptyPage.setRecords(List.of());
        when(candidateMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(emptyPage);

        PageResult<CandidateVO> result = candidateService.pageCandidate(null);

        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void pageCandidateInvalidRangeThrowsException() {
        CandidateQueryDTO query = new CandidateQueryDTO();
        query.setMinYearsOfExperience(10);
        query.setMaxYearsOfExperience(5);

        assertThrows(BusinessException.class,
                () -> candidateService.pageCandidate(query));
    }

    @Test
    void pageCandidateClampsPageSizeToDefaultWhenExceedsMax() {
        CandidateQueryDTO query = new CandidateQueryDTO();
        query.setPageSize(200);
        Page<Candidate> emptyPage = new Page<>(1, 10, 0);
        emptyPage.setRecords(List.of());
        when(candidateMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(emptyPage);

        candidateService.pageCandidate(query);

        ArgumentCaptor<Page<Candidate>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(candidateMapper).selectPage(pageCaptor.capture(), any());
        assertEquals(10, pageCaptor.getValue().getSize());
    }

    // ==================== Factory Methods ====================

    private CandidateCreateDTO createDTO(String name, String phone) {
        CandidateCreateDTO dto = new CandidateCreateDTO();
        dto.setName(name);
        dto.setPhone(phone);
        dto.setGender("男");
        dto.setAge(25);
        dto.setEducation("本科");
        dto.setSchool("测试大学");
        dto.setMajor("计算机");
        dto.setYearsOfExperience(2);
        return dto;
    }

    private CandidateUpdateDTO updateDTO() {
        CandidateUpdateDTO dto = new CandidateUpdateDTO();
        dto.setName("测试更新");
        dto.setPhone("13900139000");
        return dto;
    }

    private Candidate candidate(Long id, String name, String phone) {
        Candidate c = new Candidate();
        c.setId(id);
        c.setName(name);
        c.setPhone(phone);
        c.setGender("男");
        c.setAge(25);
        c.setEducation("本科");
        c.setSchool("测试大学");
        c.setMajor("计算机科学");
        c.setYearsOfExperience(2);
        c.setCurrentStatus("AVAILABLE");
        c.setSource("HR_IMPORT");
        c.setCreatedBy(1L);
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }
}
