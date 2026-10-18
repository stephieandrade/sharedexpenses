import { Link, Outlet } from 'react-router'
import { useAuth } from '../auth/useAuth.js'
import Logo from './Logo.jsx'

export default function Layout() {
  const { user, logout } = useAuth()

  return (
    <>
      <header className="app-header">
        <div className="container app-header__inner">
          <Link to="/groups" className="brand">
            <Logo />
            <span>SharedExpenses</span>
          </Link>
          <div className="app-header__user">
            <Link to="/account" className="app-header__name">{user.name}</Link>
            <button type="button" className="btn btn-ghost btn-small" onClick={logout}>
              Cerrar sesión
            </button>
          </div>
        </div>
      </header>
      <main className="container page">
        <Outlet />
      </main>
    </>
  )
}
