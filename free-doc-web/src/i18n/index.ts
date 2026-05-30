import { createI18n } from 'vue-i18n'
import en from './locales/en'
import zhCN from './locales/zh-CN'
import zhTW from './locales/zh-TW'
import ja from './locales/ja'
import de from './locales/de'

export type LocaleKey = 'en' | 'zh-CN' | 'zh-TW' | 'ja' | 'de'

export const localeNames: Record<LocaleKey, string> = {
  'en': 'English',
  'zh-CN': '简体中文',
  'zh-TW': '繁體中文',
  'ja': '日本語',
  'de': 'Deutsch'
}

export const localeOptions: { value: LocaleKey; label: string }[] = [
  { value: 'en', label: 'English' },
  { value: 'zh-CN', label: '简体中文' },
  { value: 'zh-TW', label: '繁體中文' },
  { value: 'ja', label: '日本語' },
  { value: 'de', label: 'Deutsch' }
]

const savedLocale = localStorage.getItem('locale') as LocaleKey || 'en'

const i18n = createI18n({
  legacy: false,
  locale: savedLocale,
  fallbackLocale: 'en',
  messages: {
    'en': en,
    'zh-CN': zhCN,
    'zh-TW': zhTW,
    'ja': ja,
    'de': de
  }
})

export default i18n
