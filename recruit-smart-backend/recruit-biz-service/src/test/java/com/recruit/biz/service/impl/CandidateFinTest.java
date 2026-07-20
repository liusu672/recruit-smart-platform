package com.recruit.biz.service.impl;

import com.recruit.biz.dto.CandidateUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateFinTest {
    @Mock CandidateMapper cm; @Mock ResumeMapper rm; @Mock JobApplicationMapper jam;
    @Mock JobPositionMapper jpm; @Mock AiMatchResultMapper aim;
    @InjectMocks CandidateServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"hr","HR")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test void updatePhoneDuplicateThrows() {
        Candidate existing = new Candidate(); existing.setId(1L); existing.setPhone("13800138000"); existing.setName("张三");
        when(cm.selectById(1L)).thenReturn(existing);
        when(cm.selectCount(any())).thenReturn(1L);
        var dto = new CandidateUpdateDTO(); dto.setPhone("13900139000");
        assertThrows(BusinessException.class, () -> s.updateCandidate(1L, dto));
    }
    @Test void updateWithOnlyGender() {
        Candidate existing = new Candidate(); existing.setId(1L); existing.setName("张三"); existing.setPhone("13800138000");
        when(cm.selectById(1L)).thenReturn(existing);
        var dto = new CandidateUpdateDTO(); dto.setGender("女");
        s.updateCandidate(1L, dto);
        verify(cm).updateById(any());
    }
}
