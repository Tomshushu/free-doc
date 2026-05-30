import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export type FontSize = 'small' | 'medium' | 'large'
export type FontFamily = 'system' | 'serif' | 'kai' | 'hei' | 'song'

export const useThemeStore = defineStore('theme', () => {
  // 字体大小
  const fontSize = ref<FontSize>('medium')

  // 字体家族
  const fontFamily = ref<FontFamily>('system')

  // 应用浅色主题到DOM
  function applyLightTheme() {
    const root = document.documentElement
    root.classList.remove('dark')
    root.style.setProperty('--bg-primary', '#ffffff')
    root.style.setProperty('--bg-secondary', '#f9fafb')
    root.style.setProperty('--bg-tertiary', '#f3f4f6')
    root.style.setProperty('--text-primary', '#111827')
    root.style.setProperty('--text-secondary', '#374151')
    root.style.setProperty('--text-tertiary', '#6b7280')
    root.style.setProperty('--border-color', '#e5e7eb')
    root.style.setProperty('--shadow-color', 'rgba(0, 0, 0, 0.1)')
  }

  // 应用字体大小到DOM
  function applyFontSize(size: FontSize) {
    const root = document.documentElement

    // 移除之前的字体大小类
    root.classList.remove('font-size-small', 'font-size-medium', 'font-size-large')

    // 添加新的字体大小类
    root.classList.add(`font-size-${size}`)

    // 设置CSS变量
    switch (size) {
      case 'small':
        root.style.setProperty('--font-size-base', '13px')
        root.style.setProperty('--font-size-sm', '11px')
        root.style.setProperty('--font-size-lg', '15px')
        root.style.setProperty('--font-size-xl', '17px')
        root.style.setProperty('--font-size-2xl', '20px')
        break
      case 'large':
        root.style.setProperty('--font-size-base', '16px')
        root.style.setProperty('--font-size-sm', '14px')
        root.style.setProperty('--font-size-lg', '18px')
        root.style.setProperty('--font-size-xl', '20px')
        root.style.setProperty('--font-size-2xl', '24px')
        break
      default: // medium
        root.style.setProperty('--font-size-base', '14px')
        root.style.setProperty('--font-size-sm', '12px')
        root.style.setProperty('--font-size-lg', '16px')
        root.style.setProperty('--font-size-xl', '18px')
        root.style.setProperty('--font-size-2xl', '22px')
        break
    }
  }

  // 应用字体家族到DOM
  function applyFontFamily(family: FontFamily) {
    const root = document.documentElement

    // 移除之前的字体家族类
    root.classList.remove('font-family-system', 'font-family-serif', 'font-family-kai', 'font-family-hei', 'font-family-song')

    // 添加新的字体家族类
    root.classList.add(`font-family-${family}`)

    // 设置CSS变量
    switch (family) {
      case 'serif':
        root.style.setProperty('--font-family-base', '"Times New Roman", "宋体", serif')
        break
      case 'kai':
        root.style.setProperty('--font-family-base', '"楷体", "KaiTi", "STKaiti", serif')
        break
      case 'hei':
        root.style.setProperty('--font-family-base', '"黑体", "SimHei", "Microsoft YaHei", sans-serif')
        break
      case 'song':
        root.style.setProperty('--font-family-base', '"宋体", "SimSun", "NSimSun", serif')
        break
      default: // system
        root.style.setProperty('--font-family-base', '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji"')
        break
    }
  }

  // 设置字体大小
  function setFontSize(newSize: FontSize) {
    fontSize.value = newSize
    applyFontSize(newSize)
    localStorage.setItem('font-size', newSize)
  }

  // 设置字体家族
  function setFontFamily(newFamily: FontFamily) {
    fontFamily.value = newFamily
    applyFontFamily(newFamily)
    localStorage.setItem('font-family', newFamily)
  }

  // 从本地存储加载主题设置
  function loadTheme() {
    const savedFontSize = localStorage.getItem('font-size') as FontSize
    if (savedFontSize && ['small', 'medium', 'large'].includes(savedFontSize)) {
      fontSize.value = savedFontSize
    }

    const savedFontFamily = localStorage.getItem('font-family') as FontFamily
    if (savedFontFamily && ['system', 'serif', 'kai', 'hei', 'song'].includes(savedFontFamily)) {
      fontFamily.value = savedFontFamily
    }

    applyFontSize(fontSize.value)
    applyFontFamily(fontFamily.value)
  }

  // 获取字体大小显示名称
  function getFontSizeName(fontSizeMode: FontSize): string {
    switch (fontSizeMode) {
      case 'small':
        return '小号'
      case 'large':
        return '大号'
      case 'medium':
      default:
        return '中号'
    }
  }

  // 获取字体家族显示名称
  function getFontFamilyName(fontFamilyMode: FontFamily): string {
    switch (fontFamilyMode) {
      case 'serif':
        return '衬线字体'
      case 'kai':
        return '楷体'
      case 'hei':
        return '黑体'
      case 'song':
        return '宋体'
      case 'system':
      default:
        return '系统字体'
    }
  }

  // 初始化
  function init() {
    applyLightTheme()
    loadTheme()
  }

  return {
    fontSize,
    fontFamily,
    setFontSize,
    setFontFamily,
    init,
    getFontSizeName,
    getFontFamilyName
  }
})
