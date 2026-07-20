package com.recruit.biz.service.impl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.entity.*;
import com.recruit.biz.mapper.*;
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
class A4OnbTest {
 @BeforeAll static void i(){for(var c:new Class<?>[]{Onboarding.class,Offer.class,JobApplication.class,JobPosition.class,Candidate.class})TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),c);}
 @Mock OnboardingMapper obm;@Mock CandidateMapper cm;@Mock OfferMapper om;
 @Mock JobApplicationMapper jam;@Mock JobPositionMapper jpm;
 @Mock EmployeeProfileMapper epm;@Mock ApplicationProcessEventService p;
 @InjectMocks OnboardingServiceImpl s;
 @BeforeEach void s1(){UserContext.set(new CurrentUser(1L,"hr","HR"));}
 @AfterEach void s2(){UserContext.clear();}
 @Test void getDetailNotFound(){
  when(obm.selectById(99L)).thenReturn(null);
  assertThrows(com.recruit.common.exception.BusinessException.class,()->s.getDetail(99L));
 }
 @Test void listOnboardingWithStatus(){
  var q=new com.recruit.biz.dto.OnboardingQueryDTO();q.setStatus("REVIEWING");
  var page=new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Onboarding>(1,10,0);
  page.setRecords(List.of());
  when(obm.selectPage(any(),any())).thenReturn(page);
  assertEquals(0L,s.listOnboarding(q).getTotal());
 }
}
