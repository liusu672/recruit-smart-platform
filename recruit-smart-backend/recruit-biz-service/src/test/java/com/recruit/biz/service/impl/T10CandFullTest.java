package com.recruit.biz.service.impl;
import com.recruit.biz.dto.CandidateUpdateDTO;
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
class T10CandFullTest {
    @Mock CandidateMapper cm; @Mock ResumeMapper rm; @Mock JobApplicationMapper jam;
    @Mock JobPositionMapper jpm; @Mock AiMatchResultMapper aim;
    @InjectMocks CandidateServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"hr","HR")); }
    @AfterEach void clear() { UserContext.clear(); }
    @Test void updateAllFields() {
        Candidate c = new Candidate(); c.setId(1L); c.setName("张三"); c.setPhone("13800138000");
        when(cm.selectById(1L)).thenReturn(c);
        when(cm.selectCount(any())).thenReturn(0L);
        var dto = new CandidateUpdateDTO();
        dto.setName("新名"); dto.setGender("女"); dto.setAge(30);
        dto.setPhone("13900000001"); dto.setEmail("new@t.com");
        dto.setEducation("硕士"); dto.setSchool("清华"); dto.setMajor("CS");
        dto.setYearsOfExperience(8); dto.setSource("RECOMMEND");
        s.updateCandidate(1L, dto);
        verify(cm).updateById(any());
    }
    @Test void updateYearsOnly() {
        Candidate c = new Candidate(); c.setId(1L); c.setName("张三"); c.setPhone("13800138000");
        when(cm.selectById(1L)).thenReturn(c);
        var dto = new CandidateUpdateDTO(); dto.setYearsOfExperience(5);
        s.updateCandidate(1L, dto);
        verify(cm).updateById(any());
    }
}
