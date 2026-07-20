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
class A6JATest {
 @BeforeAll static void i(){for(var c:new Class<?>[]{JobApplication.class,JobPosition.class,Candidate.class,Resume.class,Interview.class})TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),c);}
 @Mock JobApplicationMapper jam;@Mock JobPositionMapper jpm;@Mock CandidateMapper cm;
 @Mock ResumeMapper rm;@Mock InterviewMapper im;@Mock ApplicationProcessEventService p;
 @InjectMocks JobApplicationServiceImpl s;
 @BeforeEach void s1(){UserContext.set(new CurrentUser(1L,"candidate","CANDIDATE"));}
 @AfterEach void s2(){UserContext.clear();}
 @Test void withdrawHiredThrows() { Candidate c=new Candidate();c.setId(10L);c.setUserId(1L);when(cm.selectOne(any())).thenReturn(c);JobApplication a=new JobApplication();a.setId(1L);a.setCandidateId(10L);a.setStatus("HIRED");when(jam.selectById(1L)).thenReturn(a);assertThrows(BusinessException.class,()->s.withdraw(1L)); }
}
