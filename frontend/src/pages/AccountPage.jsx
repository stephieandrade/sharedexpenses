import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { api } from '../api/api.js'
import { useAuth } from '../auth/useAuth.js'
import { useConfirm } from '../confirm/useConfirm.js'
import ErrorMessage from '../components/ErrorMessage.jsx'

const MIN_PASSWORD_LENGTH = 8

export default function AccountPage() {
  const { user } = useAuth()

  return (
    <>
      <Link to="/groups" className="back-link">← Mis grupos</Link>
      <div className="page-header">
        <div>
          <h1>Mi cuenta</h1>
          <p className="muted">{user.name} · {user.email}</p>
        </div>
      </div>
      <ChangePasswordForm />
      <DeleteAccountSection />
    </>
  )
}

function ChangePasswordForm() {
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [repeatPassword, setRepeatPassword] = useState('')
  const [pending, setPending] = useState(false)
  const [error, setError] = useState(null)
  const [done, setDone] = useState(false)

  const onSubmit = async (event) => {
    event.preventDefault()
    setDone(false)
    if (newPassword !== repeatPassword) {
      setError({ message: 'Las dos contraseñas nuevas no coinciden' })
      return
    }
    setPending(true)
    setError(null)
    try {
      await api.changePassword(currentPassword, newPassword)
      setCurrentPassword('')
      setNewPassword('')
      setRepeatPassword('')
      setDone(true)
    } catch (e) {
      setError(e)
    } finally {
      setPending(false)
    }
  }

  return (
    <form className="card form" onSubmit={onSubmit}>
      <h2>Cambiar contraseña</h2>
      <label className="field">
        <span>Contraseña actual</span>
        <input type="password" autoComplete="current-password" required value={currentPassword}
               onChange={(e) => setCurrentPassword(e.target.value)} />
      </label>
      <div className="form__row">
        <label className="field field--grow">
          <span>Nueva contraseña</span>
          <input type="password" autoComplete="new-password" required minLength={MIN_PASSWORD_LENGTH} maxLength={72}
                 value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
        </label>
        <label className="field field--grow">
          <span>Repite la nueva contraseña</span>
          <input type="password" autoComplete="new-password" required minLength={MIN_PASSWORD_LENGTH} maxLength={72}
                 value={repeatPassword} onChange={(e) => setRepeatPassword(e.target.value)} />
        </label>
      </div>
      <p className="hint">Mínimo {MIN_PASSWORD_LENGTH} caracteres.</p>
      <ErrorMessage error={error} />
      {done && <p className="alert alert-success" role="status">Contraseña actualizada.</p>}
      <div className="form__actions">
        <button type="submit" className="btn btn-primary" disabled={pending}>
          {pending ? 'Guardando…' : 'Cambiar contraseña'}
        </button>
      </div>
    </form>
  )
}

function DeleteAccountSection() {
  const { logout } = useAuth()
  const confirm = useConfirm()
  const navigate = useNavigate()
  const [password, setPassword] = useState('')
  const [pending, setPending] = useState(false)
  const [error, setError] = useState(null)

  const onSubmit = async (event) => {
    event.preventDefault()
    const accepted = await confirm({
      title: '¿Eliminar tu cuenta?',
      message: 'Saldrás de todos tus grupos y tus datos personales se borrarán. Los gastos que compartiste '
        + 'se conservan para el resto de miembros. Esta acción no se puede deshacer.',
      confirmLabel: 'Eliminar mi cuenta',
      danger: true,
    })
    if (!accepted) {
      return
    }
    setPending(true)
    setError(null)
    try {
      await api.deleteAccount(password)
      logout()
      navigate('/login', { replace: true, state: { notice: 'Tu cuenta se ha eliminado.' } })
    } catch (e) {
      setError(e)
      setPending(false)
    }
  }

  return (
    <form className="card form danger-zone" onSubmit={onSubmit}>
      <h2>Eliminar cuenta</h2>
      <p className="muted">
        Para eliminar tu cuenta tu balance tiene que ser 0 en todos los grupos. Si has creado grupos con otros
        miembros, elimínalos antes.
      </p>
      <label className="field">
        <span>Confirma con tu contraseña</span>
        <input type="password" autoComplete="current-password" required value={password}
               onChange={(e) => setPassword(e.target.value)} />
      </label>
      <ErrorMessage error={error} />
      <div className="form__actions">
        <button type="submit" className="btn btn-danger" disabled={pending}>
          {pending ? 'Eliminando…' : 'Eliminar mi cuenta'}
        </button>
      </div>
    </form>
  )
}
