/**
 * Format a number as Indian Rupees (₹) with Indian comma formatting.
 * e.g. 123456 → ₹1,23,456
 * @param {number} amount
 * @param {number} [decimals=0]
 * @returns {string}
 */
export function formatCurrency(amount, decimals = 0) {
  if (amount === null || amount === undefined || isNaN(amount)) return '₹0'
  const num = Number(amount)
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(num)
}

/**
 * Format a date as "DD MMM YYYY".
 * e.g. "2024-01-15" → "15 Jan 2024"
 * @param {string|Date} date
 * @returns {string}
 */
export function formatDate(date) {
  if (!date) return '—'
  const d = new Date(date)
  if (isNaN(d.getTime())) return '—'
  return d.toLocaleDateString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

/**
 * Format a date-time as "DD MMM YYYY, HH:MM".
 * e.g. "2024-01-15T14:30:00" → "15 Jan 2024, 14:30"
 * @param {string|Date} date
 * @returns {string}
 */
export function formatDateTime(date) {
  if (!date) return '—'
  const d = new Date(date)
  if (isNaN(d.getTime())) return '—'
  const datePart = d.toLocaleDateString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${datePart}, ${hours}:${minutes}`
}

/**
 * Get initials from a full name (first letter of first + last word).
 * e.g. "Arjun Kumar Sharma" → "AS"
 * @param {string} name
 * @returns {string}
 */
export function getInitials(name) {
  if (!name || typeof name !== 'string') return '?'
  const parts = name.trim().split(/\s+/).filter(Boolean)
  if (parts.length === 0) return '?'
  if (parts.length === 1) return parts[0][0].toUpperCase()
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}

/**
 * Truncate a string to `len` characters, appending "…" if truncated.
 * @param {string} str
 * @param {number} len
 * @returns {string}
 */
export function truncate(str, len = 50) {
  if (!str || typeof str !== 'string') return ''
  if (str.length <= len) return str
  return str.slice(0, len).trimEnd() + '…'
}

/**
 * Format a number with K / M / B suffix.
 * e.g. 1200 → "1.2K", 2500000 → "2.5M"
 * @param {number} n
 * @param {number} [decimals=1]
 * @returns {string}
 */
export function formatNumber(n, decimals = 1) {
  if (n === null || n === undefined || isNaN(n)) return '0'
  const num = Number(n)
  const abs = Math.abs(num)
  const sign = num < 0 ? '-' : ''
  if (abs >= 1_000_000_000) return `${sign}${(abs / 1_000_000_000).toFixed(decimals)}B`
  if (abs >= 1_000_000) return `${sign}${(abs / 1_000_000).toFixed(decimals)}M`
  if (abs >= 1_000) return `${sign}${(abs / 1_000).toFixed(decimals)}K`
  return `${sign}${abs}`
}

/**
 * Format a percentage value.
 * e.g. 12.5 → "12.5%"
 * @param {number} value
 * @param {number} [decimals=1]
 * @returns {string}
 */
export function formatPercent(value, decimals = 1) {
  if (value === null || value === undefined || isNaN(value)) return '0%'
  return `${Number(value).toFixed(decimals)}%`
}

/**
 * Return a relative time string ("2 days ago", "just now", etc.)
 * @param {string|Date} date
 * @returns {string}
 */
export function timeAgo(date) {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  const seconds = Math.floor((Date.now() - d.getTime()) / 1000)
  if (seconds < 60) return 'just now'
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}d ago`
  const months = Math.floor(days / 30)
  if (months < 12) return `${months}mo ago`
  return `${Math.floor(months / 12)}y ago`
}

/**
 * Capitalize first letter of each word.
 * @param {string} str
 * @returns {string}
 */
export function titleCase(str) {
  if (!str) return ''
  return str.replace(/\w\S*/g, (w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
}

/**
 * Convert a status string to a display label (SNAKE_CASE → Title Case).
 * e.g. "PAST_DUE" → "Past Due"
 * @param {string} status
 * @returns {string}
 */
export function formatStatus(status) {
  if (!status) return ''
  return status
    .split('_')
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
    .join(' ')
}

/**
 * Compute MRR from annual amount.
 * @param {number} annual
 * @returns {number}
 */
export function annualToMRR(annual) {
  return Math.round(annual / 12)
}

/**
 * Compute ARR from monthly amount.
 * @param {number} monthly
 * @returns {number}
 */
export function mrrToARR(monthly) {
  return monthly * 12
}
