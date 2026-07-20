package com.recruit.biz.service.impl;

import com.recruit.biz.dto.CandidateQueryDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateExtraTest {
    @Mock CandidateMapper cm; @Mock ResumeMapper rm; @Mock JobApplicationMapper jam;
    @Mock JobPositionMapper jpm; @Mock AiMatchResultMapper aim;
    @InjectMocks CandidateServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"hr","HR")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test void pageWithAllFilters() {
        var dto = new CandidateQueryDTO(); dto.setGender("男"); dto.setEducation("本科");
        dto.setSchool("武汉"); dto.setMajor("计算机"); dto.setSource("HR_IMPORT");
        dto.setMinYearsOfExperience(0); dto.setMaxYearsOfExperience(10);
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Candidate>(1,10,1);
        Candidate c = new Candidate(); c.setId(1L); c.setName("张三");
        c.setCreatedAt(LocalDateTime.now());
        page.setRecords(List.of(c));
        when(cm.selectPage(any(), any())).thenReturn(page);
        var r = s.pageCandidate(dto);
        assertEquals(1L, r.getTotal());
    }

    @Test void pageYearsInvalid() {
        var dto = new CandidateQueryDTO(); dto.setMinYearsOfExperience(10); dto.setMaxYearsOfExperience(5);
        assertThrows(BusinessException.class, () -> s.pageCandidate(dto));
    }
}
