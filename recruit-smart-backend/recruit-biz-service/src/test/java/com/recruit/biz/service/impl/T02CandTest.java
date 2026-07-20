package com.recruit.biz.service.impl;
import com.recruit.biz.dto.CandidateQueryDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
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
class T02CandTest {
    @Mock CandidateMapper cm; @Mock ResumeMapper rm; @Mock JobApplicationMapper jam;
    @Mock JobPositionMapper jpm; @Mock AiMatchResultMapper aim;
    @InjectMocks CandidateServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"hr","HR")); }
    @AfterEach void clear() { UserContext.clear(); }
    @Test void pageNullKeywordEmptyResult() {
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Candidate>(1,10,0);
        page.setRecords(List.of());
        when(cm.selectPage(any(),any())).thenReturn(page);
        assertEquals(0L, s.pageCandidate(new CandidateQueryDTO()).getTotal());
    }
}
