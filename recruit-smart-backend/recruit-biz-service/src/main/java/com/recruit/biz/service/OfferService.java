package com.recruit.biz.service;

import com.recruit.biz.dto.OfferCreateDTO;
import com.recruit.biz.dto.OfferUpdateDTO;
import com.recruit.biz.dto.OfferQueryDTO;
import com.recruit.biz.dto.OfferHRQueryDTO;
import com.recruit.biz.vo.OfferDetailVO;
import com.recruit.biz.vo.OfferSummaryVO;
import com.recruit.biz.vo.OfferHRSummaryVO;
import com.recruit.common.result.PageResult;

public interface OfferService {
    Long createOffer(OfferCreateDTO dto);
    void updateOffer(Long id, OfferUpdateDTO dto);
    OfferDetailVO getDetail(Long id);
    void sendOffer(Long id);
    PageResult<OfferSummaryVO> listMyOffers(OfferQueryDTO dto);
    void acceptOffer(Long id);
    void rejectOffer(Long id);
    PageResult<OfferHRSummaryVO> listOffers(OfferHRQueryDTO dto);
    void revokeOffer(Long id);
}
