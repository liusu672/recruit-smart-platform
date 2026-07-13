import 'element-plus/dist/index.css'
import '@/styles/index.scss'

import { VueQueryPlugin } from '@tanstack/vue-query'
import ElementPlus from 'element-plus'
import { createPinia } from 'pinia'
import { createApp } from 'vue'

import App from './App.vue'
import { queryClientOptions } from './app/queryClient'
import { router } from './router'

const app = createApp(App)

app.use(createPinia())
// Vue Query 负责服务端状态；Pinia 只保留会话和纯前端 UI 状态。
app.use(VueQueryPlugin, queryClientOptions)
app.use(router)
// Element Plus 提供基础交互能力；视觉细节统一在 src/styles 中覆盖。
app.use(ElementPlus)

app.mount('#app')
