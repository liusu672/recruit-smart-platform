import { AxiosHeaders, type AxiosResponse } from 'axios'
import { shallowMount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'

import { getDashboardOverview } from '@/api/dashboard'
import { http } from '@/api/http'
import {
  buildDashboardMetricCards,
  getDashboardTaskMeta,
  getDashboardTaskRoute,
  getDashboardTaskTone,
} from '@/config/dashboard'
import type { Result } from '@/types/api'
import type { DashboardOverview, DashboardTask } from '@/types/dashboard'

const mocks = vi.hoisted(() => ({
  push: vi.fn(),
  refetch: vi.fn(),
  query: null as unknown,
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mocks.push }),
}))

vi.mock('@/composables/useDashboardOverview', () => ({
  useDashboardOverview: () => mocks.query,
}))

function successResponse<T>(data: T): AxiosResponse<Result<T>> {
  return {
    data: { code: 200, message: 'success', data },
    status: 200,
    statusText: 'OK',
    headers: {},
    config: { headers: new AxiosHeaders() },
  }
}

function buildQuery(data: DashboardOverview | undefined, loading = false) {
  return {
    data: ref(data),
    error: ref(null),
    isLoading: ref(loading),
    isFetching: ref(loading),
    refetch: mocks.refetch,
  }
}

describe('dashboard API', () => {
  afterEach(() => vi.restoreAllMocks())

  it('loads the HR dashboard overview endpoint', async () => {
    const overview: DashboardOverview = {
      metrics: {
        pendingScreening: 0,
        pendingFeedback: 0,
        activeOffers: 0,
        reviewingOnboardings: 0,
      },
      tasks: [],
    }
    const get = vi.spyOn(http, 'get').mockResolvedValue(successResponse(overview))

    await expect(getDashboardOverview()).resolves.toEqual(overview)
    expect(get).toHaveBeenCalledWith('/dashboard/overview')
  })
})

describe('dashboard view helpers', () => {
  it('keeps the four metric cards stable when metrics are missing or zero', () => {
    expect(buildDashboardMetricCards(null)).toEqual([
      { key: 'pendingScreening', label: '待筛选投递', value: 0, status: '暂无待处理' },
      { key: 'pendingFeedback', label: '待补充反馈', value: 0, status: '暂无待处理' },
      { key: 'activeOffers', label: '进行中 Offer', value: 0, status: '暂无待处理' },
      { key: 'reviewingOnboardings', label: '入职材料审核', value: 0, status: '暂无待处理' },
    ])
  })

  it('maps task types to HR work routes and visual tones', () => {
    expect(getDashboardTaskRoute('SCREENING')).toBe('/hr/pipeline')
    expect(getDashboardTaskRoute('INTERVIEW_FEEDBACK')).toBe('/hr/interviews')
    expect(getDashboardTaskRoute('OFFER')).toBe('/hr/offers')
    expect(getDashboardTaskRoute('ONBOARDING')).toBe('/hr/onboardings')
    expect(getDashboardTaskTone('INTERVIEW_FEEDBACK')).toBe('danger')
  })

  it('builds task context from backend task fields', () => {
    const task: DashboardTask = {
      type: 'SCREENING',
      relatedId: 10,
      applicationId: 10,
      candidateId: 20,
      candidateName: '林一凡',
      jobId: 30,
      jobTitle: 'Java 后端开发工程师',
      title: '待筛选投递',
      status: 'SUBMITTED',
      statusText: '已投递',
      occurredAt: '2026-07-17T09:30:00',
    }

    expect(getDashboardTaskMeta(task)).toBe(
      '候选人：林一凡 · 职位：Java 后端开发工程师 · 时间：2026-07-17 09:30',
    )
  })
})

describe('DashboardView', () => {
  beforeEach(() => {
    mocks.push.mockReset()
    mocks.refetch.mockReset()
  })

  afterEach(() => vi.resetModules())

  it('shows the empty state instead of static demo tasks', async () => {
    mocks.query = buildQuery({
      metrics: {
        pendingScreening: 0,
        pendingFeedback: 0,
        activeOffers: 0,
        reviewingOnboardings: 0,
      },
      tasks: [],
    })

    const { default: DashboardView } = await import('@/views/dashboard/DashboardView.vue')
    const wrapper = shallowMount(DashboardView, {
      global: {
        stubs: {
          'el-button': { template: '<button><slot /></button>' },
          'el-tooltip': { template: '<span><slot /></span>' },
          RouterLink: { template: '<a><slot /></a>' },
          HrEmptyState: {
            props: ['title', 'description'],
            template: '<section>{{ title }} {{ description }}</section>',
          },
        },
      },
    })

    expect(wrapper.text()).toContain('当前没有待处理任务')
    expect(wrapper.text()).toContain('新的筛选、面试反馈、Offer 或入职审核任务会显示在这里')
    expect(wrapper.text()).not.toContain('高级 Java 工程师候选人需要初筛')
  })
})
