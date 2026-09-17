import { ref, watch } from 'vue'

const THEME_KEY = 'campus-theme'
const theme = ref(localStorage.getItem(THEME_KEY) || 'light')

/**
 * 主题切换 composable
 */
export function useTheme() {
  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
  }

  const setTheme = (newTheme) => {
    theme.value = newTheme
  }

  // 同步到 DOM 和 localStorage
  watch(theme, (val) => {
    document.documentElement.setAttribute('data-theme', val)
    localStorage.setItem(THEME_KEY, val)
  }, { immediate: true })

  return {
    theme,
    toggleTheme,
    setTheme,
    isDark: () => theme.value === 'dark'
  }
}
