import { useState } from 'react'
import { api } from '../../api/api.js'
import { useConfirm } from '../../confirm/useConfirm.js'
import { balanceClass, formatMoney } from '../../utils/format.js'
import ErrorMessage from '../ErrorMessage.jsx'

export default function MembersTab({ group, balances, currency, currentUserId, onChanged, onLeft }) {
  const confirm = useConfirm()
  const [email, setEmail] = useState('')
  const [adding, setAdding] = useState(false)
  const [error, setError] = useState(null)
  const isCreator = group.creatorId === currentUserId
  const balanceOf = (userId) => balances.find((b) => b.userId === userId)?.amount ?? 0

  const add = async (event) => {
    event.preventDefault()
    setAdding(true)
    setError(null)
    try {
      await api.addMember(group.id, email.trim())
      setEmail('')
      await onChanged()
    } catch (e) {
      setError(e)
    } finally {
      setAdding(false)
    }
  }

  const remove = async (member) => {
    const leaving = member.id === currentUserId
    const accepted = await confirm(leaving
      ? {
          title: '¿Salir del grupo?',
          message: `Dejarás de ver los gastos y balances de "${group.name}". Solo puedes salir si tu balance es 0.`,
          confirmLabel: 'Salir del grupo',
          danger: true,
        }
      : {
          title: `¿Eliminar a ${member.name} del grupo?`,
          message: 'Solo se puede eliminar a un miembro cuando su balance es 0.',
          confirmLabel: 'Eliminar miembro',
          danger: true,
        })
    if (!accepted) {
      return
    }
    setError(null)
    try {
      await api.removeMember(group.id, member.id)
      if (leaving) {
        onLeft()
      } else {
        await onChanged()
      }
    } catch (e) {
      setError(e)
    }
  }

  return (
    <section className="stack">
      <form className="card form form--inline" onSubmit={add}>
        <label className="field field--grow">
          <span>Añadir miembro por email</span>
          <input type="email" required placeholder="amiga@ejemplo.com" value={email}
                 onChange={(e) => setEmail(e.target.value)} />
        </label>
        <button type="submit" className="btn btn-primary" disabled={adding}>
          {adding ? 'Añadiendo…' : 'Añadir'}
        </button>
      </form>
      <p className="hint">La persona tiene que haberse registrado antes en SharedExpenses.</p>
      <ErrorMessage error={error} />

      <ul className="card list members">
        {group.members.map((member) => {
          const memberIsCreator = member.id === group.creatorId
          const canRemove = !memberIsCreator && (isCreator || member.id === currentUserId)
          const balance = balanceOf(member.id)
          return (
            <li key={member.id} className="member">
              <div>
                <p className="member__name">
                  {member.name}
                  {member.id === currentUserId && <span className="badge">Tú</span>}
                  {memberIsCreator && <span className="badge badge--accent">Creador</span>}
                </p>
                <p className="muted">{member.email}</p>
              </div>
              <div className="member__side">
                <span className={`amount ${balanceClass(balance)}`}>{formatMoney(balance, currency)}</span>
                {canRemove && (
                  <button type="button" className="btn btn-ghost btn-small btn-danger-text"
                          onClick={() => remove(member)}>
                    {member.id === currentUserId ? 'Salir del grupo' : 'Eliminar'}
                  </button>
                )}
              </div>
            </li>
          )
        })}
      </ul>
      <p className="hint">Solo se puede eliminar a alguien cuando su balance es 0. El creador no puede salir del grupo.</p>
    </section>
  )
}
