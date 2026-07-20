package com.recruit.biz.service.impl;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.enums.ResumeFileType;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.storage.ResumeFileResource;
import com.recruit.biz.storage.ResumeFileStorage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class T01ResumeTest {
    @Mock ResumeMapper rm; @Mock CandidateMapper cm; @Mock JobApplicationMapper jam;
    @Mock InterviewMapper im; @Mock ResumeFileStorage rfs;
    @InjectMocks ResumeServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"candidate","CANDIDATE")); }
    @AfterEach void clear() { UserContext.clear(); }
    @Test void previewPdf() {
        Candidate c = new Candidate(); c.setId(10L); c.setUserId(1L);
        when(cm.selectOne(any())).thenReturn(c);
        Resume r = new Resume(); r.setId(1L); r.setCandidateId(10L);
        r.setFileUrl("test.pdf"); r.setFileType("PDF"); r.setIsDefault(0);
        when(rm.selectById(1L)).thenReturn(r);
        when(rfs.load(any(),any(),eq(ResumeFileType.PDF)))
            .thenReturn(new ResumeFileResource(new FileSystemResource(new File(".")),"t.pdf","application/pdf",1));
        assertNotNull(s.preview(1L));
    }
}
