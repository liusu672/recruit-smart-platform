package com.recruit.biz.service.impl;

import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.storage.ResumeFileStorage;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeFinTest {
    @Mock ResumeMapper rm; @Mock CandidateMapper cm; @Mock JobApplicationMapper jam;
    @Mock InterviewMapper im; @Mock ResumeFileStorage rfs;
    @InjectMocks ResumeServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"candidate","CANDIDATE")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test void setDefaultNotDefault() {
        Candidate cand = new Candidate(); cand.setId(10L); cand.setUserId(1L);
        when(cm.selectOne(any())).thenReturn(cand);
        Resume r = new Resume(); r.setId(1L); r.setCandidateId(10L); r.setIsDefault(0);
        when(rm.selectById(1L)).thenReturn(r);
        lenient().when(rm.update(any(),any())).thenReturn(1);
        s.setDefault(1L);
        verify(rm, atLeast(1)).update(any(), any());
    }
    @Test void deleteUsedInInterviewThrows() {
        Candidate cand = new Candidate(); cand.setId(10L); cand.setUserId(1L);
        when(cm.selectOne(any())).thenReturn(cand);
        Resume r = new Resume(); r.setId(1L); r.setCandidateId(10L); r.setFileUrl("test.pdf");
        when(rm.selectById(1L)).thenReturn(r);
        when(jam.selectCount(any())).thenReturn(1L);
        assertThrows(BusinessException.class, () -> s.delete(1L));
    }
}
