import { useState } from 'react'
import { api } from '../../api/api.js'
import { useConfirm } from '../../confirm/useConfirm.js'
import ErrorMessage from '../ErrorMessage.jsx'

export default function SettingsTab({ group, currentUserId, nameOf, onChanged, onDeleted }) {
  const confirm = useConfirm()
  const [name, setName] = useState(group.name)
  const [description, setDescription] = useState(group.description ?? '')
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)
  const [error, setError] = useState(null)
  const [deleteError, setDeleteError] = useState(null)
  const isCreator = group.creatorId === currentUserId

  const save = async (event) => {
    event.preventDefault()
    setSaving(true)
    setSaved(false)
    setError(null)
    try {
      await api.updateGroup(group.id, { name: name.trim(), description: description.trim() || null })
      await onChanged()
      setSaved(true)
    } catch (e) {
      setError(e)
    } finally {
      setSaving(false)
    }
  }

  const remove = async () => {
    const accepted = await confirm({
      title: `¿Eliminar "${group.name}"?`,
      message: 'Se borrarán todos sus gastos, pagos y miembros. Las cuentas de los miembros no se tocan. '
        + 'Esta acción no se puede deshacer.',
      confirmLabel: 'Eliminar grupo',
      danger: true,
    })
    if (!accepted) {
      return
    }
    setDeleteError(null)
    try {
      await api.deleteGroup(group.id)
      onDeleted()
    } catch (e) {
      setDeleteError(e)
    }
  }

  return (
    <section className="stack">
      <form className="card form" onSubmit={save}>
        <h2>Datos del grupo</h2>
        <label className="field">
          <span>Nombre</span>
          <input required maxLength={100} value={name} onChange={(e) => setName(e.target.value)} />
        </label>
        <label className="field">
          <span>Descripción <small>(opcional)</small></span>
          <input maxLength={500} value={description} onChange={(e) => setDescription(e.target.value)} />
        </label>
        <p className="hint">Moneda: {group.currency}. No se puede cambiar porque los importes ya están en esta moneda.</p>
        <ErrorMessage error={error} />
        {saved && <p className="alert alert-success" role="status">Cambios guardados.</p>}
        <div className="form__actions">
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Guardando…' : 'Guardar cambios'}
          </button>
        </div>
      </form>

      <div className="card form danger-zone">
        <h2>Eliminar grupo</h2>
        {isCreator ? (
          <>
            <p className="muted">Borra el grupo con todos sus gastos y pagos para todos los miembros.</p>
            <ErrorMessage error={deleteError} />
            <div className="form__actions">
              <button type="button" className="btn btn-danger" onClick={remove}>Eliminar grupo</button>
            </div>
          </>
        ) : (
          <p className="muted">Solo {nameOf(group.creatorId)}, que creó el grupo, puede eliminarlo.</p>
        )}
      </div>
    </section>
  )
}
