package com.recruit.biz.service.impl;

import com.recruit.biz.dto.ResumeRenameDTO;
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
class ResumeExtraTest {
    @Mock ResumeMapper rm; @Mock CandidateMapper cm; @Mock JobApplicationMapper jam;
    @Mock InterviewMapper im; @Mock ResumeFileStorage rfs;
    @InjectMocks ResumeServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"candidate","CANDIDATE")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test void listMyResumesSuccess() {
        Candidate c = new Candidate(); c.setId(10L); c.setUserId(1L);
        when(cm.selectOne(any())).thenReturn(c);
        Resume r = new Resume(); r.setId(1L); r.setResumeName("简历.pdf");
        r.setFileType("PDF"); r.setIsDefault(1); r.setParseStatus("SUCCESS");
        when(rm.selectList(any())).thenReturn(List.of(r));
        var list = s.listMyResumes();
        assertEquals(1, list.size());
        assertEquals("简历.pdf", list.get(0).getResumeName());
    }

    @Test void setDefaultAlreadyDefault() {
        Candidate c = new Candidate(); c.setId(10L); c.setUserId(1L);
        when(cm.selectOne(any())).thenReturn(c);
        Resume r = new Resume(); r.setId(1L); r.setCandidateId(10L); r.setIsDefault(1);
        when(rm.selectById(1L)).thenReturn(r);
        s.setDefault(1L);
        verify(rm, never()).update(any(), any());
    }
}
