package com.recruit.biz.service.impl;
import com.recruit.biz.dto.CandidateCreateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class T15CandLast {
    @Mock CandidateMapper cm; @Mock ResumeMapper rm; @Mock JobApplicationMapper jam;
    @Mock JobPositionMapper jpm; @Mock AiMatchResultMapper aim;
    @InjectMocks CandidateServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"hr","HR")); }
    @AfterEach void clear() { UserContext.clear(); }
    @Test void createWithSourceNullUsesDefault() {
        when(cm.selectCount(any())).thenReturn(0L);
        doAnswer(inv->{((Candidate)inv.getArgument(0)).setId(100L);return 1;}).when(cm).insert(any());
        var dto = new CandidateCreateDTO();
        dto.setName("张三"); dto.setPhone("13900000001");
        assertEquals(100L, s.createCandidate(dto));
    }
    @Test void createWithZeroExperienceDefaultZero() {
        when(cm.selectCount(any())).thenReturn(0L);
        doAnswer(inv->{
            Candidate c = inv.getArgument(0);
            c.setId(200L);
            assertEquals(Integer.valueOf(0), c.getYearsOfExperience());
            return 1;
        }).when(cm).insert(any());
        var dto = new CandidateCreateDTO();
        dto.setName("李四"); dto.setPhone("13900000002"); dto.setYearsOfExperience(null);
        assertEquals(200L, s.createCandidate(dto));
    }
}
