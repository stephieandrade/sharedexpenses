import { useState } from 'react'
import { hasAtMostTwoDecimals, parseDecimal, todayIso } from '../../utils/format.js'
import ErrorMessage from '../ErrorMessage.jsx'

export default function PaymentForm({ members, currentUserId, currency, onSubmit, onCancel }) {
  const [fromUserId, setFromUserId] = useState(currentUserId)
  const [toUserId, setToUserId] = useState(() => members.find((m) => m.id !== currentUserId)?.id ?? currentUserId)
  const [amount, setAmount] = useState('')
  const [date, setDate] = useState(todayIso)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState(null)

  const submit = async (event) => {
    event.preventDefault()
    const value = parseDecimal(amount)
    if (fromUserId === toUserId) {
      setError({ message: 'Quien paga y quien recibe tienen que ser personas distintas' })
      return
    }
    if (!hasAtMostTwoDecimals(amount) || !(value > 0)) {
      setError({ message: 'El importe debe ser mayor que 0 y tener como mucho 2 decimales' })
      return
    }
    setPending(true)
    setError(null)
    try {
      await onSubmit({ fromUserId, toUserId, amount: value, date })
    } catch (e) {
      setError(e)
      setPending(false)
    }
  }

  return (
    <form className="card form" onSubmit={submit} noValidate>
      <h2>Registrar un pago</h2>
      <div className="form__row">
        <label className="field field--grow">
          <span>Paga</span>
          <select value={fromUserId} onChange={(e) => setFromUserId(Number(e.target.value))}>
            {members.map((m) => <option key={m.id} value={m.id}>{m.name}</option>)}
          </select>
        </label>
        <label className="field field--grow">
          <span>Recibe</span>
          <select value={toUserId} onChange={(e) => setToUserId(Number(e.target.value))}>
            {members.map((m) => <option key={m.id} value={m.id}>{m.name}</option>)}
          </select>
        </label>
      </div>
      <div className="form__row">
        <label className="field field--grow">
          <span>Importe ({currency})</span>
          <input inputMode="decimal" placeholder="0,00" value={amount} onChange={(e) => setAmount(e.target.value)} />
        </label>
        <label className="field">
          <span>Fecha</span>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        </label>
      </div>
      <ErrorMessage error={error} />
      <div className="form__actions">
        <button type="button" className="btn btn-ghost" onClick={onCancel}>Cancelar</button>
        <button type="submit" className="btn btn-primary" disabled={pending}>
          {pending ? 'Guardando…' : 'Registrar pago'}
        </button>
      </div>
    </form>
  )
}
