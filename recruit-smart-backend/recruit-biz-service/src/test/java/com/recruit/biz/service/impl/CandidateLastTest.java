package com.recruit.biz.service.impl;
import com.recruit.biz.dto.CandidateCreateDTO;
import com.recruit.biz.dto.CandidateUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.security.CurrentUser;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class CandidateLastTest {
    @Mock CandidateMapper cm; @Mock ResumeMapper rm; @Mock JobApplicationMapper jam;
    @Mock JobPositionMapper jpm; @Mock AiMatchResultMapper aim;
    @InjectMocks CandidateServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"hr","HR")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test void updateWithEmptyName() {
        Candidate c = new Candidate(); c.setId(1L); c.setName("旧名"); c.setPhone("13800138000");
        when(cm.selectById(1L)).thenReturn(c);
        var dto = new CandidateUpdateDTO(); dto.setName("");
        assertThrows(BusinessException.class, () -> s.updateCandidate(1L, dto));
    }
    @Test void createWithZeroExperience() {
        when(cm.selectCount(any())).thenReturn(0L);
        doAnswer(inv->{((Candidate)inv.getArgument(0)).setId(99L);return 1;}).when(cm).insert(any());
        var dto = new CandidateCreateDTO();
        dto.setName("测试"); dto.setPhone("13900000001"); dto.setYearsOfExperience(0);
        assertEquals(99L, s.createCandidate(dto));
    }
}
