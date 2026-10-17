import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { api } from '../api/api.js'
import ErrorMessage from '../components/ErrorMessage.jsx'
import Loading from '../components/Loading.jsx'

const CURRENCIES = ['EUR', 'USD', 'GBP', 'CHF', 'MXN']

export default function GroupsPage() {
  const [groups, setGroups] = useState(null)
  const [error, setError] = useState(null)
  const [creating, setCreating] = useState(false)

  useEffect(() => {
    api.listGroups().then(setGroups).catch(setError)
  }, [])

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Mis grupos</h1>
          <p className="muted">Viajes, pisos compartidos, cenas… cada grupo lleva sus cuentas.</p>
        </div>
        {!creating && (
          <button type="button" className="btn btn-primary" onClick={() => setCreating(true)}>
            Nuevo grupo
          </button>
        )}
      </div>

      {creating && <CreateGroupForm onCancel={() => setCreating(false)} />}
      <ErrorMessage error={error} />
      {!groups && !error && <Loading />}

      {groups?.length === 0 && !creating && (
        <div className="card empty">
          <h2>Todavía no tienes grupos</h2>
          <p className="muted">Crea uno e invita a tus amigos por su email.</p>
          <button type="button" className="btn btn-primary" onClick={() => setCreating(true)}>
            Crear mi primer grupo
          </button>
        </div>
      )}

      {groups?.length > 0 && (
        <ul className="group-grid">
          {groups.map((group) => (
            <li key={group.id}>
              <Link to={`/groups/${group.id}`} className="card group-card">
                <h2>{group.name}</h2>
                {group.description && <p className="muted">{group.description}</p>}
                <p className="group-card__meta">
                  <span>{group.memberCount} {group.memberCount === 1 ? 'miembro' : 'miembros'}</span>
                  <span className="badge">{group.currency}</span>
                </p>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}

function CreateGroupForm({ onCancel }) {
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [currency, setCurrency] = useState('EUR')
  const [pending, setPending] = useState(false)
  const [error, setError] = useState(null)

  const onSubmit = async (event) => {
    event.preventDefault()
    setPending(true)
    setError(null)
    try {
      const group = await api.createGroup({ name, description: description || null, currency })
      navigate(`/groups/${group.id}`)
    } catch (e) {
      setError(e)
      setPending(false)
    }
  }

  return (
    <form className="card form" onSubmit={onSubmit}>
      <h2>Nuevo grupo</h2>
      <label className="field">
        <span>Nombre</span>
        <input required maxLength={100} placeholder="Viaje a Roma" value={name}
               onChange={(e) => setName(e.target.value)} />
      </label>
      <label className="field">
        <span>Descripción <small>(opcional)</small></span>
        <input maxLength={500} placeholder="Puente de mayo" value={description}
               onChange={(e) => setDescription(e.target.value)} />
      </label>
      <label className="field">
        <span>Moneda</span>
        <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
          {CURRENCIES.map((code) => <option key={code} value={code}>{code}</option>)}
        </select>
      </label>
      <ErrorMessage error={error} />
      <div className="form__actions">
        <button type="button" className="btn btn-ghost" onClick={onCancel}>Cancelar</button>
        <button type="submit" className="btn btn-primary" disabled={pending}>
          {pending ? 'Creando…' : 'Crear grupo'}
        </button>
      </div>
    </form>
  )
}
