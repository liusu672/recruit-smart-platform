package com.recruit.biz.service.impl;

import com.recruit.biz.dto.CandidateCreateDTO;
import com.recruit.biz.dto.CandidateQueryDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
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
class CandidateMoreTest {
    @Mock CandidateMapper cm; @Mock ResumeMapper rm; @Mock JobApplicationMapper jam;
    @Mock JobPositionMapper jpm; @Mock AiMatchResultMapper aim;
    @InjectMocks CandidateServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"hr","HR")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test void pageSchoolAndMajor() {
        var dto = new CandidateQueryDTO(); dto.setSchool("武汉大学"); dto.setMajor("软件");
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Candidate>(1,10,1);
        Candidate c = new Candidate(); c.setId(1L); c.setName("张三"); c.setCreatedAt(LocalDateTime.now());
        page.setRecords(List.of(c));
        when(cm.selectPage(any(),any())).thenReturn(page);
        assertEquals(1L, s.pageCandidate(dto).getTotal());
    }
    @Test void createWithCustomSource() {
        when(cm.selectCount(any())).thenReturn(0L);
        doAnswer(inv->{((Candidate)inv.getArgument(0)).setId(99L);return 1;}).when(cm).insert(any());
        var dto = new CandidateCreateDTO();
        dto.setName("测试"); dto.setPhone("13900000001"); dto.setSource("BOSS");
        assertEquals(99L, s.createCandidate(dto));
    }
}
