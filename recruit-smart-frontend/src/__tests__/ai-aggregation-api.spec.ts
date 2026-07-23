import { beforeEach, describe, expect, it, vi } from 'vitest'

import { assessTurnoverRisk, generateFeedbackSummary, listTurnoverRiskHistory } from '@/api/ai'
import { http } from '@/api/http'
import { generateInterviewQuestions } from '@/api/interviews'
import { generateApplicationAiMatch } from '@/api/pipeline'

describe('Biz AI aggregation API contracts', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('requests resume matching through the application aggregate endpoint', async () => {
    const post = vi.spyOn(http, 'post').mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          matchScore: 88,
          recommendLevel: 'HIGH',
          recommendReason: '匹配度较高',
          highlightSummary: 'Java经验',
          riskSummary: '大型系统经验待核实',
          modelName: 'deepseek-chat',
          generatedAt: '2026-07-20T20:00:00',
        },
      },
    })

    const result = await generateApplicationAiMatch(10)

    expect(post).toHaveBeenCalledWith('/applications/10/ai-match')
    expect(result.matchScore).toBe(88)
  })

  it('only sends focus when requesting interview questions', async () => {
    const post = vi.spyOn(http, 'post').mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          category: 'Java后端',
          questions: ['请说明事务传播机制'],
        },
      },
    })

    const result = await generateInterviewQuestions(20, {
      focus: '事务与并发',
    })

    expect(post).toHaveBeenCalledWith('/interviews/20/ai-questions', {
      focus: '事务与并发',
    })
    expect(result[0]?.question).toBe('请说明事务传播机制')
  })

  it('requests feedback summary through the interview aggregate endpoint', async () => {
    const post = vi.spyOn(http, 'post').mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          summary: '候选人技术基础扎实',
          advantages: ['基础扎实'],
          risks: [],
          suggestion: '建议进入下一轮',
        },
      },
    })

    const result = await generateFeedbackSummary(20)

    expect(post).toHaveBeenCalledWith('/interviews/20/ai-summary')
    expect(result.summary).toBe('候选人技术基础扎实')
  })

  it('requests turnover risk through the employee aggregate endpoint', async () => {
    const post = vi.spyOn(http, 'post').mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          riskLevel: 'LOW',
          riskScore: 20,
          summary: '风险较低',
          riskReasons: [],
          suggestions: ['保持沟通'],
          sentimentLabel: '稳定',
          sentimentRiskScore: 12,
          sentimentSummary: '反馈稳定',
        },
      },
    })

    const result = await assessTurnoverRisk(30)

    expect(post).toHaveBeenCalledWith('/employees/30/turnover-risk')
    expect(result.riskLevel).toBe('LOW')
    expect(result.sentimentLabel).toBe('稳定')
  })

  it('requests turnover risk history through the employee aggregate endpoint', async () => {
    const get = vi.spyOn(http, 'get').mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: [
          {
            id: 1,
            employeeId: 30,
            riskLevel: 'MEDIUM',
            riskScore: 48,
            summary: '需关注',
            riskReasons: ['满意度下降'],
            suggestions: ['安排访谈'],
            sentimentLabel: '压力上升',
            sentimentRiskScore: 56,
            periodStart: '2026-04-01',
            periodEnd: '2026-06-30',
            behaviorRecordIds: [11, 12, 13],
            modelName: 'deepseek-chat',
            generatedAt: '2026-07-20T18:00:00',
          },
        ],
      },
    })

    const result = await listTurnoverRiskHistory(30)

    expect(get).toHaveBeenCalledWith('/employees/30/turnover-risks')
    expect(result[0]).toMatchObject({
      riskLevel: 'MEDIUM',
      behaviorRecordIds: [11, 12, 13],
      sentimentRiskScore: 56,
    })
  })
})
