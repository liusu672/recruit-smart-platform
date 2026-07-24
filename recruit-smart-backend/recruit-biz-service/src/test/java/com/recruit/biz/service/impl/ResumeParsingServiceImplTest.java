package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.enums.ResumeFileType;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.storage.ResumeFileResource;
import com.recruit.biz.storage.ResumeFileStorage;
import com.recruit.common.exception.BusinessException;
import com.recruit.feign.client.AiServiceClient;
import com.recruit.feign.dto.response.ResumeParseResponse;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeParsingServiceImplTest {

    @BeforeAll
    static void initializeTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                Resume.class
        );
    }

    @Mock
    private ResumeMapper resumeMapper;
    @Mock
    private CandidateMapper candidateMapper;
    @Mock
    private ResumeFileStorage resumeFileStorage;
    @Mock
    private AiServiceClient aiServiceClient;
    @InjectMocks
    private ResumeParsingServiceImpl resumeParsingService;

    @BeforeEach
    void setUp() {
        UserContext.set(new CurrentUser(1L, "candidate", "CANDIDATE"));
    }

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    @Test
    void parsesOwnedResumeAndPersistsStructuredResult()
            throws Exception {
        Resume resume = resume("PENDING");
        Candidate candidate = candidate();
        when(resumeMapper.selectById(100L)).thenReturn(resume);
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(candidate);
        when(resumeMapper.update(
                eq(null),
                any(LambdaUpdateWrapper.class)
        )).thenReturn(1);

        ByteArrayResource resource = resource("resume.pdf", "resume");
        when(resumeFileStorage.load(
                "10/resume.pdf",
                "resume-100.pdf",
                ResumeFileType.PDF
        )).thenReturn(new ResumeFileResource(
                resource,
                "resume-100.pdf",
                "application/pdf",
                resource.contentLength()
        ));

        ResumeParseResponse response = new ResumeParseResponse();
        response.setParsedContent("两年Java开发经验");
        response.setSkills(List.of("Java", "Spring Boot"));
        response.setProjectExperience("招聘平台项目");
        response.setWorkExperience("两年后端开发");
        when(aiServiceClient.parseResume(any(MultipartFile.class)))
                .thenReturn(response);

        resumeParsingService.parse(100L);

        ArgumentCaptor<MultipartFile> fileCaptor =
                ArgumentCaptor.forClass(MultipartFile.class);
        verify(aiServiceClient).parseResume(fileCaptor.capture());
        assertEquals(
                "resume-100.pdf",
                fileCaptor.getValue().getOriginalFilename()
        );
        ArgumentCaptor<LambdaUpdateWrapper<Resume>> captor =
                ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(resumeMapper, times(2)).update(
                eq(null),
                captor.capture()
        );
        String successSql = captor.getAllValues().get(1).getSqlSet();
        assertTrue(successSql.contains("parse_status"));
        assertTrue(successSql.contains("parsed_content"));
        assertTrue(successSql.contains("skills"));
    }

    @Test
    void rejectsResumeAlreadyBeingParsed() {
        when(resumeMapper.selectById(100L))
                .thenReturn(resume("PROCESSING"));
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(candidate());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resumeParsingService.parse(100L)
        );

        assertEquals(400, exception.getCode());
        verify(aiServiceClient, never()).parseResume(any());
    }

    @Test
    void marksResumeFailedWhenAiCallFails() throws Exception {
        UserContext.set(new CurrentUser(2L, "hr", "HR"));
        when(resumeMapper.selectById(100L))
                .thenReturn(resume("FAILED"));
        when(resumeMapper.update(
                eq(null),
                any(LambdaUpdateWrapper.class)
        )).thenReturn(1);

        ByteArrayResource resource = resource("resume.pdf", "resume");
        when(resumeFileStorage.load(any(), any(), any()))
                .thenReturn(new ResumeFileResource(
                        resource,
                        "resume-100.pdf",
                        "application/pdf",
                        resource.contentLength()
                ));
        when(aiServiceClient.parseResume(any(MultipartFile.class)))
                .thenThrow(new IllegalStateException("AI unavailable"));

        assertThrows(
                BusinessException.class,
                () -> resumeParsingService.parse(100L)
        );

        verify(resumeMapper, times(2)).update(
                eq(null),
                any(LambdaUpdateWrapper.class)
        );
    }

    @Test
    void rejectsCandidateWhoDoesNotOwnResume() {
        when(resumeMapper.selectById(100L))
                .thenReturn(resume("PENDING"));
        Candidate otherCandidate = candidate();
        otherCandidate.setId(11L);
        when(candidateMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(otherCandidate);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resumeParsingService.parse(100L)
        );

        assertEquals(403, exception.getCode());
        verify(aiServiceClient, never()).parseResume(any());
    }

    @Test
    void automaticParsingDoesNotRequireUserContext() throws Exception {
        UserContext.clear();
        when(resumeMapper.selectById(100L))
                .thenReturn(resume("PENDING"));
        when(resumeMapper.update(
                eq(null),
                any(LambdaUpdateWrapper.class)
        )).thenReturn(1);

        ByteArrayResource resource = resource("resume.pdf", "resume");
        when(resumeFileStorage.load(any(), any(), any()))
                .thenReturn(new ResumeFileResource(
                        resource,
                        "resume-100.pdf",
                        "application/pdf",
                        resource.contentLength()
                ));
        ResumeParseResponse response = new ResumeParseResponse();
        response.setParsedContent("Java开发经验");
        when(aiServiceClient.parseResume(any(MultipartFile.class)))
                .thenReturn(response);

        resumeParsingService.parseAutomatically(100L);

        verify(aiServiceClient).parseResume(any(MultipartFile.class));
        verifyNoInteractions(candidateMapper);
    }

    @Test
    void backfillsMissingCandidateEducationWithoutOverwritingExistingData()
            throws Exception {
        UserContext.set(new CurrentUser(2L, "hr", "HR"));
        Resume resume = resume("PENDING");
        when(resumeMapper.selectById(100L)).thenReturn(resume);
        when(resumeMapper.update(
                eq(null),
                any(LambdaUpdateWrapper.class)
        )).thenReturn(1);

        ByteArrayResource resource = resource("resume.pdf", "resume");
        when(resumeFileStorage.load(any(), any(), any()))
                .thenReturn(new ResumeFileResource(
                        resource,
                        "resume-100.pdf",
                        "application/pdf",
                        resource.contentLength()
                ));

        ResumeParseResponse response = new ResumeParseResponse();
        response.setParsedContent("武汉理工大学软件工程本科");
        response.setEducation(" 本科 ");
        response.setSchool(" 武汉理工大学 ");
        response.setMajor(" 软件工程 ");
        when(aiServiceClient.parseResume(any(MultipartFile.class)))
                .thenReturn(response);

        Candidate candidate = candidate();
        candidate.setEducation("研究生");
        when(candidateMapper.selectById(10L)).thenReturn(candidate);
        when(candidateMapper.updateById(any(Candidate.class))).thenReturn(1);

        resumeParsingService.parse(100L);

        ArgumentCaptor<Candidate> candidateCaptor =
                ArgumentCaptor.forClass(Candidate.class);
        verify(candidateMapper).updateById(candidateCaptor.capture());
        Candidate update = candidateCaptor.getValue();
        assertEquals(10L, update.getId());
        assertEquals(null, update.getEducation());
        assertEquals("武汉理工大学", update.getSchool());
        assertEquals("软件工程", update.getMajor());
    }

    @Test
    void automaticParsingSkipsSuccessfulResume() {
        UserContext.clear();
        when(resumeMapper.selectById(100L))
                .thenReturn(resume("SUCCESS"));

        resumeParsingService.parseAutomatically(100L);

        verify(resumeMapper, never()).update(
                eq(null),
                any(LambdaUpdateWrapper.class)
        );
        verifyNoInteractions(candidateMapper, aiServiceClient);
    }

    private Resume resume(String status) {
        Resume resume = new Resume();
        resume.setId(100L);
        resume.setCandidateId(10L);
        resume.setFileUrl("10/resume.pdf");
        resume.setFileType("PDF");
        resume.setParseStatus(status);
        return resume;
    }

    private Candidate candidate() {
        Candidate candidate = new Candidate();
        candidate.setId(10L);
        candidate.setUserId(1L);
        return candidate;
    }

    private ByteArrayResource resource(String fileName, String content) {
        return new ByteArrayResource(
                content.getBytes(StandardCharsets.UTF_8)
        ) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
    }
}
