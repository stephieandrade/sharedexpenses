import { Link } from 'react-router'

export default function NotFoundPage() {
  return (
    <main className="container page empty">
      <h1>Página no encontrada</h1>
      <p>La dirección que buscas no existe.</p>
      <Link to="/groups" className="btn btn-primary">Ir a mis grupos</Link>
    </main>
  )
}
