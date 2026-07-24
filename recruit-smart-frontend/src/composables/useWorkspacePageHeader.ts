import {
  inject,
  onBeforeUnmount,
  provide,
  readonly,
  ref,
  watchEffect,
  type InjectionKey,
  type Ref,
} from 'vue'

type WorkspacePageHeader = {
  title: string
  description: string
}

type WorkspacePageHeaderContext = {
  title: Readonly<Ref<string>>
  description: Readonly<Ref<string>>
  setHeader: (id: symbol, header: WorkspacePageHeader) => void
  clearHeader: (id: symbol) => void
}

const workspacePageHeaderKey: InjectionKey<WorkspacePageHeaderContext> =
  Symbol('workspacePageHeader')

export function provideWorkspacePageHeader() {
  const title = ref('')
  const description = ref('')
  const activeHeaderId = ref<symbol | null>(null)

  function setHeader(id: symbol, header: WorkspacePageHeader) {
    activeHeaderId.value = id
    title.value = header.title
    description.value = header.description
  }

  function clearHeader(id: symbol) {
    if (activeHeaderId.value !== id) return
    activeHeaderId.value = null
    title.value = ''
    description.value = ''
  }

  const context: WorkspacePageHeaderContext = {
    title: readonly(title),
    description: readonly(description),
    setHeader,
    clearHeader,
  }

  provide(workspacePageHeaderKey, context)
  return context
}

export function useWorkspacePageHeader(getHeader: () => WorkspacePageHeader) {
  const context = inject(workspacePageHeaderKey, null)
  if (!context) return false

  const headerId = Symbol('workspacePageHeaderInstance')

  watchEffect(() => {
    context.setHeader(headerId, getHeader())
  })

  onBeforeUnmount(() => {
    context.clearHeader(headerId)
  })

  return true
}
