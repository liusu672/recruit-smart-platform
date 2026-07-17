package com.recruit.biz.mapper;

import com.recruit.biz.entity.Interview;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DashboardMapper {

    @Select("""
            SELECT COUNT(*)
            FROM job_application
            WHERE status IN ('SUBMITTED', 'SCREENING')
            """)
    Long countPendingScreening();

    @Select("""
            SELECT COUNT(*)
            FROM interview i
            WHERE i.status = 'COMPLETED'
              AND NOT EXISTS (
                SELECT 1
                FROM interview_feedback f
                WHERE f.interview_id = i.id
                  AND COALESCE(f.state, 'SUBMITTED') = 'SUBMITTED'
              )
            """)
    Long countPendingFeedback();

    @Select("""
            SELECT COUNT(*)
            FROM offer
            WHERE status IN ('DRAFT', 'SENT')
            """)
    Long countActiveOffers();

    @Select("""
            SELECT COUNT(*)
            FROM onboarding
            WHERE status = 'REVIEWING'
               OR material_status = 'REVIEWING'
            """)
    Long countReviewingOnboardings();

    @Select("""
            SELECT i.*
            FROM interview i
            WHERE i.status = 'COMPLETED'
              AND NOT EXISTS (
                SELECT 1
                FROM interview_feedback f
                WHERE f.interview_id = i.id
                  AND COALESCE(f.state, 'SUBMITTED') = 'SUBMITTED'
              )
            ORDER BY COALESCE(i.updated_at, i.interview_time, i.created_at) DESC,
                     i.id DESC
            LIMIT #{limit}
            """)
    List<Interview> selectPendingFeedbackInterviews(
            @Param("limit") int limit
    );
}
