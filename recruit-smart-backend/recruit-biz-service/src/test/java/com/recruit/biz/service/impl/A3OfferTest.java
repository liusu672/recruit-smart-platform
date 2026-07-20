package com.recruit.biz.service.impl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.dto.OfferCreateDTO;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class A3OfferTest {
 @BeforeAll static void i(){for(var c:new Class<?>[]{Offer.class,JobApplication.class,JobPosition.class,Interview.class,InterviewFeedback.class})TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),c);}
 @Mock OfferMapper om;@Mock JobApplicationMapper jam;@Mock JobPositionMapper jpm;
 @Mock InterviewMapper im;@Mock InterviewFeedbackMapper ifm;@Mock CandidateMapper cm;
 @Mock OnboardingMapper obm;@Mock ApplicationProcessEventService p;
 @InjectMocks OfferServiceImpl s;
 @BeforeEach void s1(){UserContext.set(new CurrentUser(1L,"hr","HR"));}
 @AfterEach void s2(){UserContext.clear();}
 @Test void createOfferAppNotFound(){
  when(jam.selectById(99L)).thenReturn(null);
  var d=offerDTO(99L);
  assertThrows(BusinessException.class,()->s.createOffer(d));
 }
 private OfferCreateDTO offerDTO(Long appId){
  var d=new OfferCreateDTO();d.setApplicationId(appId);d.setSalary(new BigDecimal("10000"));
  d.setEntryDate(LocalDate.now().plusDays(30));d.setProbationMonths(3);d.setWorkLocation("武汉");
  return d;
 }
}
