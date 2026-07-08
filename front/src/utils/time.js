export const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const s = String(timeStr).replace('T', ' ')
  return s.length >= 16 ? s.substring(0, 16) : s
}
