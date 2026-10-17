export default function Loading({ text = 'Cargando…' }) {
  return (
    <p className="loading" role="status">
      <span className="spinner" aria-hidden="true" />
      {text}
    </p>
  )
}
