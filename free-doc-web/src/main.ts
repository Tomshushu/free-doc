import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import i18n from './i18n'
import './index.css'
import { useThemeStore } from './stores/theme'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(i18n)

const themeStore = useThemeStore()
themeStore.init()

app.mount('#app')
