package com.recruit.biz.service.impl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.biz.vo.InterviewFeedbackVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class T13FeedbackAdminTest {
    @BeforeAll static void init() { TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),InterviewFeedback.class); }
    @Mock InterviewMapper im; @Mock InterviewFeedbackMapper ifm; @Mock com.recruit.biz.mapper.SysUserMapper sum;
    @Mock ApplicationProcessEventService pes;
    @InjectMocks InterviewFeedbackServiceImpl s;
    @Test void getFeedbackAsAdmin() {
        UserContext.set(new CurrentUser(1L,"admin","ADMIN"));
        Interview i = new Interview(); i.setId(10L); i.setInterviewerId(2L);
        when(im.selectById(10L)).thenReturn(i);
        InterviewFeedback fb = new InterviewFeedback();
        fb.setId(1L); fb.setInterviewId(10L); fb.setInterviewerId(2L);
        fb.setState("SUBMITTED"); fb.setScore(90); fb.setSuggestion("PASS");
        when(ifm.selectOne(any())).thenReturn(fb);
        SysUser u = new SysUser(); u.setId(2L); u.setRealName("张三");
        when(sum.selectById(2L)).thenReturn(u);
        InterviewFeedbackVO vo = s.getFeedback(10L);
        assertNotNull(vo); assertEquals(90, vo.getScore());
    }
}
