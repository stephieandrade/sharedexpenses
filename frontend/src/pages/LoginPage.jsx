import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import { useAuth } from '../auth/useAuth.js'
import AuthCard from '../components/AuthCard.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import { useSlowRequestHint } from '../hooks/useSlowRequestHint.js'

export default function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [pending, setPending] = useState(false)
  const [error, setError] = useState(null)
  const slow = useSlowRequestHint(pending)

  if (user) {
    return <Navigate to="/groups" replace />
  }

  const onSubmit = async (event) => {
    event.preventDefault()
    setPending(true)
    setError(null)
    try {
      await login(email, password)
      navigate(location.state?.from ?? '/groups', { replace: true })
    } catch (e) {
      setError(e)
      setPending(false)
    }
  }

  return (
    <AuthCard title="Iniciar sesión">
      <form className="form" onSubmit={onSubmit}>
        <label className="field">
          <span>Email</span>
          <input type="email" autoComplete="email" required value={email}
                 onChange={(e) => setEmail(e.target.value)} />
        </label>
        <label className="field">
          <span>Contraseña</span>
          <input type="password" autoComplete="current-password" required value={password}
                 onChange={(e) => setPassword(e.target.value)} />
        </label>
        <ErrorMessage error={error} />
        {slow && (
          <p className="hint">
            El servidor gratuito se está despertando. La primera petición puede tardar hasta un minuto.
          </p>
        )}
        <button type="submit" className="btn btn-primary" disabled={pending}>
          {pending ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
      <p className="auth__switch">
        ¿No tienes cuenta? <Link to="/register">Regístrate</Link>
      </p>
    </AuthCard>
  )
}
