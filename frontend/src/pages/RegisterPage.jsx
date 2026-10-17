import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router'
import { useAuth } from '../auth/useAuth.js'
import AuthCard from '../components/AuthCard.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import Turnstile from '../components/Turnstile.jsx'
import { useSlowRequestHint } from '../hooks/useSlowRequestHint.js'

const MIN_PASSWORD_LENGTH = 8
const TURNSTILE_SITE_KEY = import.meta.env.VITE_TURNSTILE_SITE_KEY

export default function RegisterPage() {
  const { user, register } = useAuth()
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [captchaToken, setCaptchaToken] = useState(null)
  const [captchaAttempt, setCaptchaAttempt] = useState(0)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState(null)
  const slow = useSlowRequestHint(pending)
  const captchaRequired = Boolean(TURNSTILE_SITE_KEY)

  if (user) {
    return <Navigate to="/groups" replace />
  }

  const onSubmit = async (event) => {
    event.preventDefault()
    setPending(true)
    setError(null)
    try {
      await register(name, email, password, captchaToken)
      navigate('/groups', { replace: true })
    } catch (e) {
      setError(e)
      setPending(false)
      setCaptchaToken(null)
      setCaptchaAttempt((attempt) => attempt + 1)
    }
  }

  const onCaptchaError = () => setError({ message: 'No se ha podido cargar la verificación anti-bots. Recarga la página' })

  return (
    <AuthCard title="Crear cuenta">
      <form className="form" onSubmit={onSubmit}>
        <label className="field">
          <span>Nombre</span>
          <input autoComplete="name" required maxLength={100} value={name}
                 onChange={(e) => setName(e.target.value)} />
        </label>
        <label className="field">
          <span>Email</span>
          <input type="email" autoComplete="email" required value={email}
                 onChange={(e) => setEmail(e.target.value)} />
        </label>
        <label className="field">
          <span>Contraseña</span>
          <input type="password" autoComplete="new-password" required minLength={MIN_PASSWORD_LENGTH}
                 maxLength={72} value={password} onChange={(e) => setPassword(e.target.value)} />
          <small>Mínimo {MIN_PASSWORD_LENGTH} caracteres.</small>
        </label>
        {captchaRequired && (
          <Turnstile key={captchaAttempt} siteKey={TURNSTILE_SITE_KEY} onToken={setCaptchaToken}
                     onError={onCaptchaError} />
        )}
        <ErrorMessage error={error} />
        {slow && <p className="hint">El servidor se está despertando, puede tardar hasta un minuto.</p>}
        <button type="submit" className="btn btn-primary" disabled={pending || (captchaRequired && !captchaToken)}>
          {pending ? 'Creando cuenta…' : 'Crear cuenta'}
        </button>
      </form>
      <p className="auth__switch">
        ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
      </p>
    </AuthCard>
  )
}
