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
class T12CandFinTest {
    @Mock CandidateMapper cm; @Mock ResumeMapper rm; @Mock JobApplicationMapper jam;
    @Mock JobPositionMapper jpm; @Mock AiMatchResultMapper aim;
    @InjectMocks CandidateServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"hr","HR")); }
    @AfterEach void clear() { UserContext.clear(); }
    @Test void updateWithNewPhoneOK() {
        Candidate c = new Candidate(); c.setId(1L); c.setName("张三"); c.setPhone("13800138000");
        when(cm.selectById(1L)).thenReturn(c);
        when(cm.selectCount(any())).thenReturn(0L);
        var dto = new CandidateUpdateDTO(); dto.setPhone("13900139000");
        s.updateCandidate(1L, dto);
        verify(cm).updateById(any());
    }
    @Test void updateWithSamePhoneOK() {
        Candidate c = new Candidate(); c.setId(1L); c.setName("张三"); c.setPhone("13800138000");
        when(cm.selectById(1L)).thenReturn(c);
        var dto = new CandidateUpdateDTO(); dto.setEducation("博士");
        s.updateCandidate(1L, dto);
        verify(cm).updateById(any());
    }
    @Test void updateNotFoundThrows() {
        when(cm.selectById(99L)).thenReturn(null);
        var dto = new CandidateUpdateDTO(); dto.setName("新");
        assertThrows(BusinessException.class, () -> s.updateCandidate(99L, dto));
    }
}
