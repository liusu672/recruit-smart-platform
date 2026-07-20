package com.recruit.biz.service.impl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.entity.*;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.common.result.PageResult;
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
class T06IntTest {
    @BeforeAll static void init() { for(var c:new Class<?>[]{Interview.class,JobApplication.class,JobPosition.class,Candidate.class,SysUser.class,InterviewFeedback.class,Resume.class}) TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),c); }
    @Mock InterviewMapper im; @Mock InterviewFeedbackMapper ifm; @Mock JobApplicationMapper jam;
    @Mock CandidateMapper cm; @Mock JobPositionMapper jpm; @Mock ResumeMapper rm;
    @Mock SysUserMapper sum; @Mock SysRoleMapper srm; @Mock ApplicationProcessEventService pes;
    @InjectMocks InterviewServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(2L,"interviewer","INTERVIEWER")); }
    @AfterEach void clear() { UserContext.clear(); }
    @Test void listMyInterviewerWithStatus() {
        com.recruit.biz.dto.InterviewQueryDTO dto = new com.recruit.biz.dto.InterviewQueryDTO();
        dto.setStatus("SCHEDULED");
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Interview>(1,10,0);
        page.setRecords(List.of());
        when(im.selectPage(any(),any())).thenReturn(page);
        PageResult<?> r = s.listMyInterviewerInterviews(dto);
        assertEquals(0L, r.getTotal());
    }
}
