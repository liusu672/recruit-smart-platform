import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, reactive, ref } from 'vue'

import {
  createOffer,
  getOfferById,
  getOffers,
  revokeOffer,
  sendOffer,
  updateOffer,
} from '@/api/offers'
import {
  createDemoOffer,
  getDemoOfferPage,
  initialDemoOffers,
  revokeDemoOffer,
  sendDemoOffer,
  updateDemoOffer,
} from '@/config/demoOffers'
import type { OfferCreateRequest, OfferQuery, OfferRecord, OfferUpdateRequest } from '@/types/offer'

function cloneDemoOffers() {
  return initialDemoOffers.map(cloneOffer)
}

function cloneOffer(offer: OfferRecord): OfferRecord {
  // Vue 会把演示记录转为响应式代理，显式复制可避免 structuredClone 的 DataCloneError。
  return {
    ...offer,
    timeline: offer.timeline.map((event) => ({ ...event })),
  }
}

export function useOfferManagement() {
  const queryClient = useQueryClient()
  const demoMode = ref(false)
  const demoRecords = ref<OfferRecord[]>(cloneDemoOffers())
  const selectedOfferId = ref<number | null>(null)
  const query = reactive<OfferQuery>({
    keyword: '',
    status: '',
    page: 1,
    pageSize: 10,
  })

  const offersQuery = useQuery({
    queryKey: computed(() => [
      'offers',
      demoMode.value ? 'demo' : 'api',
      query.keyword,
      query.status,
      query.page,
      query.pageSize,
    ]),
    queryFn: () =>
      demoMode.value
        ? Promise.resolve(getDemoOfferPage(demoRecords.value, { ...query }))
        : getOffers({ ...query }),
  })

  const detailQuery = useQuery({
    queryKey: computed(() => [
      'offer-detail',
      demoMode.value ? 'demo' : 'api',
      selectedOfferId.value,
    ]),
    enabled: computed(() => selectedOfferId.value !== null),
    queryFn: async () => {
      const id = selectedOfferId.value
      if (id === null) throw new Error('尚未选择 Offer')
      if (!demoMode.value) return getOfferById(id)
      const offer = demoRecords.value.find((item) => item.id === id)
      if (!offer) throw new Error('演示 Offer 不存在')
      return cloneOffer(offer)
    },
  })

  async function refreshOffers() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['offers'] }),
      queryClient.invalidateQueries({ queryKey: ['offer-detail'] }),
    ])
  }

  function updateDemoRecord(id: number, updater: (offer: OfferRecord) => OfferRecord) {
    const index = demoRecords.value.findIndex((item) => item.id === id)
    if (index < 0) throw new Error('演示 Offer 不存在')
    demoRecords.value[index] = updater(demoRecords.value[index]!)
  }

  const createMutation = useMutation({
    mutationFn: async (data: OfferCreateRequest) => {
      if (!demoMode.value) return createOffer(data)
      const offer = createDemoOffer(demoRecords.value, data)
      demoRecords.value.unshift(offer)
      return offer.id
    },
    onSuccess: refreshOffers,
  })

  const updateMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: OfferUpdateRequest }) => {
      if (!demoMode.value) return updateOffer(id, data)
      updateDemoRecord(id, (offer) => updateDemoOffer(offer, data))
    },
    onSuccess: refreshOffers,
  })

  const sendMutation = useMutation({
    mutationFn: async (id: number) => {
      if (!demoMode.value) return sendOffer(id)
      updateDemoRecord(id, (offer) => sendDemoOffer(offer))
    },
    onSuccess: refreshOffers,
  })

  const revokeMutation = useMutation({
    mutationFn: async (id: number) => {
      if (!demoMode.value) return revokeOffer(id)
      updateDemoRecord(id, (offer) => revokeDemoOffer(offer))
    },
    onSuccess: refreshOffers,
  })

  function applyFilters(filters: Pick<OfferQuery, 'keyword' | 'status'>) {
    Object.assign(query, filters, { page: 1 })
  }

  function resetFilters() {
    Object.assign(query, { keyword: '', status: '', page: 1 })
  }

  function useDemoData() {
    demoMode.value = true
    query.page = 1
    selectedOfferId.value = null
  }

  function useApiData() {
    demoMode.value = false
    query.page = 1
    selectedOfferId.value = null
  }

  return {
    query,
    demoMode,
    selectedOfferId,
    offersQuery,
    detailQuery,
    createMutation,
    updateMutation,
    sendMutation,
    revokeMutation,
    applyFilters,
    resetFilters,
    useDemoData,
    useApiData,
    openDetail: (id: number) => (selectedOfferId.value = id),
    closeDetail: () => (selectedOfferId.value = null),
  }
}
