export default function Logo({ size = 28 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 64 64" aria-hidden="true">
      <rect width="64" height="64" rx="14" fill="var(--color-primary)" />
      <circle cx="25" cy="32" r="13" fill="none" stroke="#ffffff" strokeWidth="5" />
      <circle cx="39" cy="32" r="13" fill="none" stroke="#99f6e4" strokeWidth="5" />
    </svg>
  )
}
