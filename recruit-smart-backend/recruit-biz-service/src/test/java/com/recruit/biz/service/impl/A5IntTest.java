package com.recruit.biz.service.impl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.entity.*;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.common.exception.BusinessException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class A5IntTest {
 @BeforeAll static void i(){for(var c:new Class<?>[]{Interview.class,JobApplication.class,JobPosition.class,Candidate.class,SysUser.class,InterviewFeedback.class,Resume.class})TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),c);}
 @Mock InterviewMapper im;@Mock InterviewFeedbackMapper ifm;@Mock JobApplicationMapper jam;
 @Mock CandidateMapper cm;@Mock JobPositionMapper jpm;@Mock ResumeMapper rm;
 @Mock SysUserMapper sum;@Mock SysRoleMapper srm;@Mock ApplicationProcessEventService p;
 @InjectMocks InterviewServiceImpl s;
 @BeforeEach void s1(){UserContext.set(new CurrentUser(1L,"hr","HR"));}
 @AfterEach void s2(){UserContext.clear();}
 @Test void completeSuccess() { Interview i=new Interview();i.setId(1L);i.setStatus("SCHEDULED");when(im.selectById(1L)).thenReturn(i);when(im.update(any(),any())).thenReturn(1);s.completeInterview(1L);verify(im).update(any(),any()); }
}
