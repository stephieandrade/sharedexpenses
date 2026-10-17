export function formatMoney(amount, currency = 'EUR') {
  return new Intl.NumberFormat('es-ES', { style: 'currency', currency }).format(Number(amount))
}

export function formatDate(isoDate) {
  return new Intl.DateTimeFormat('es-ES', { day: 'numeric', month: 'short', year: 'numeric' })
    .format(new Date(`${isoDate}T00:00:00`))
}

export function balanceClass(amount) {
  const value = Number(amount)
  if (value > 0) {
    return 'positive'
  }
  return value < 0 ? 'negative' : 'neutral'
}

export function todayIso() {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

export function parseDecimal(text) {
  const normalized = String(text ?? '').trim().replace(',', '.')
  if (!/^\d+(\.\d+)?$/.test(normalized)) {
    return NaN
  }
  return Number(normalized)
}

export function toCents(value) {
  return Math.round(value * 100)
}

export function hasAtMostTwoDecimals(text) {
  return /^\d+([.,]\d{1,2})?$/.test(String(text ?? '').trim())
}
