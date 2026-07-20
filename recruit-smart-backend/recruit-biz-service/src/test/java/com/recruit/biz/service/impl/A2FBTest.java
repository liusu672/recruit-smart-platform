package com.recruit.biz.service.impl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.dto.InterviewFeedbackDraftDTO;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class A2FBTest {
 @BeforeAll static void i(){TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),InterviewFeedback.class);}
 @Mock InterviewMapper im;@Mock InterviewFeedbackMapper fm;@Mock com.recruit.biz.mapper.SysUserMapper sm;
 @Mock com.recruit.biz.support.InterviewScorecardCodec sc;
 @Mock ApplicationProcessEventService p;
 @InjectMocks InterviewFeedbackServiceImpl s;
 @BeforeEach void s1(){UserContext.set(new CurrentUser(2L,"i","INTERVIEWER"));}
 @AfterEach void s2(){UserContext.clear();}
 @Test void saveDraftOnAssigned(){
  Interview i=new Interview();i.setId(10L);i.setInterviewerId(2L);i.setStatus("SCHEDULED");
  when(im.selectById(10L)).thenReturn(i);
  lenient().when(sc.write(any())).thenReturn("[]");
  when(fm.selectOne(any())).thenReturn(null);
  when(fm.insert(any())).thenReturn(1);
  var d=new InterviewFeedbackDraftDTO();d.setComment("ok");
  s.saveDraft(10L,d);
  verify(fm).insert(any());
 }
}
