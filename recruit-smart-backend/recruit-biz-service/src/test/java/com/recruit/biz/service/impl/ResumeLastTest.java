package com.recruit.biz.service.impl;

import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.storage.ResumeFileStorage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeLastTest {
    @Mock ResumeMapper rm; @Mock CandidateMapper cm; @Mock JobApplicationMapper jam;
    @Mock InterviewMapper im; @Mock ResumeFileStorage rfs;
    @InjectMocks ResumeServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"candidate","CANDIDATE")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test void listResumesEmpty() {
        Candidate cand = new Candidate(); cand.setId(10L); cand.setUserId(1L);
        when(cm.selectOne(any())).thenReturn(cand);
        when(rm.selectList(any())).thenReturn(java.util.List.of());
        assertTrue(s.listMyResumes().isEmpty());
    }
}
