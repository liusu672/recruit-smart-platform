package com.recruit.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruit.ai.entity.AiMatchResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiMatchResultMapper extends BaseMapper<AiMatchResult> {

    @Insert("""
            INSERT INTO ai_match_result (
                task_id, application_id, job_id, candidate_id, resume_id,
                match_score, recommend_level, recommend_reason,
                highlight_summary, matched_points, risk_summary, risk_points,
                suggestion, source, model_name, prompt_version, generated_at
            ) VALUES (
                #{taskId}, #{applicationId}, #{jobId}, #{candidateId}, #{resumeId},
                #{matchScore}, #{recommendLevel}, #{recommendReason},
                #{highlightSummary}, #{matchedPoints}, #{riskSummary}, #{riskPoints},
                #{suggestion}, #{source}, #{modelName}, #{promptVersion}, #{generatedAt}
            )
            ON DUPLICATE KEY UPDATE
                task_id = VALUES(task_id),
                job_id = VALUES(job_id),
                candidate_id = VALUES(candidate_id),
                resume_id = VALUES(resume_id),
                match_score = VALUES(match_score),
                recommend_level = VALUES(recommend_level),
                recommend_reason = VALUES(recommend_reason),
                highlight_summary = VALUES(highlight_summary),
                matched_points = VALUES(matched_points),
                risk_summary = VALUES(risk_summary),
                risk_points = VALUES(risk_points),
                suggestion = VALUES(suggestion),
                source = VALUES(source),
                model_name = VALUES(model_name),
                prompt_version = VALUES(prompt_version),
                generated_at = VALUES(generated_at)
            """)
    int upsert(AiMatchResult result);
}
