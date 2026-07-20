package com.recruit.biz.service.impl;
import com.recruit.biz.dto.CandidateUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class A1CandTest {
 @Mock CandidateMapper cm; @Mock ResumeMapper rm; @Mock JobApplicationMapper jam;
 @Mock JobPositionMapper jpm; @Mock AiMatchResultMapper aim;
 @InjectMocks CandidateServiceImpl s;
 @BeforeEach void s1(){UserContext.set(new CurrentUser(1L,"hr","HR"));}
 @AfterEach void s2(){UserContext.clear();}
 @Test void updateEmptyNameThrows(){
  Candidate c=new Candidate();c.setId(1L);c.setPhone("138");
  when(cm.selectById(1L)).thenReturn(c);
  var d=new CandidateUpdateDTO();d.setName("");
  assertThrows(BusinessException.class,()->s.updateCandidate(1L,d));
 }
 @Test void pageClampsPageNum(){
  var d=new com.recruit.biz.dto.CandidateQueryDTO();d.setPageNum(0);
  var p=new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Candidate>(1,10,0);
  p.setRecords(java.util.List.of());
  when(cm.selectPage(any(),any())).thenReturn(p);
  assertEquals(0L,s.pageCandidate(d).getTotal());
 }
}
