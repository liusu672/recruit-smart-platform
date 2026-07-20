package com.recruit.biz.service.impl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.dto.InterviewFeedbackCreateDTO;
import com.recruit.biz.dto.InterviewScoreItemDTO;
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
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class T11FeedbackFinTest {
    @BeforeAll static void init() { TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),InterviewFeedback.class); }
    @Mock InterviewMapper im; @Mock InterviewFeedbackMapper ifm; @Mock com.recruit.biz.mapper.SysUserMapper sum;
    @Mock com.recruit.biz.support.InterviewScorecardCodec sc;
    @Mock ApplicationProcessEventService pes;
    @InjectMocks InterviewFeedbackServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(2L,"interviewer","INTERVIEWER")); }
    @AfterEach void clear() { UserContext.clear(); }
    @Test void submitFeedbackFromDraft() {
        Interview i = new Interview(); i.setId(10L); i.setInterviewerId(2L); i.setStatus("COMPLETED");
        when(im.selectById(10L)).thenReturn(i);
        InterviewFeedback draft = new InterviewFeedback();
        draft.setId(1L); draft.setInterviewId(10L); draft.setState("DRAFT");
        when(ifm.selectOne(any())).thenReturn(draft);
        when(sc.write(any())).thenReturn("[]");
        when(ifm.update(any(),any())).thenReturn(1);
        var dto = new InterviewFeedbackCreateDTO();
        dto.setScore(85); dto.setComment("good"); dto.setSuggestion("PASS");
        var item = new InterviewScoreItemDTO(); item.setScore(4); item.setEvidence("e");
        dto.setScorecard(List.of(item));
        assertNotNull(s.submitFeedback(10L, dto));
    }
}
