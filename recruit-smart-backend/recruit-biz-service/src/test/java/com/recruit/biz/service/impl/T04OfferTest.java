package com.recruit.biz.service.impl;
import com.recruit.biz.dto.OfferQueryDTO;
import com.recruit.biz.entity.*;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.biz.vo.OfferSummaryVO;
import com.recruit.common.result.PageResult;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class T04OfferTest {
    @Mock OfferMapper om; @Mock JobApplicationMapper jam; @Mock JobPositionMapper jpm;
    @Mock CandidateMapper cm; @Mock InterviewMapper im; @Mock InterviewFeedbackMapper ifm;
    @Mock OnboardingMapper obm; @Mock ApplicationProcessEventService pes;
    @InjectMocks OfferServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"hr","HR")); }
    @AfterEach void clear() { UserContext.clear(); }
    @Test void listOffersWithStatus() {
        com.recruit.biz.dto.OfferHRQueryDTO dto = new com.recruit.biz.dto.OfferHRQueryDTO();
        dto.setStatus("SENT");
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Offer>(1,10,0);
        page.setRecords(List.of());
        when(om.selectPage(any(),any())).thenReturn(page);
        assertEquals(0L, s.listOffers(dto).getTotal());
    }
}
