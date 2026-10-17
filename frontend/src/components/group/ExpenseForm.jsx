import { useState } from 'react'
import { formatMoney, hasAtMostTwoDecimals, parseDecimal, toCents, todayIso } from '../../utils/format.js'
import { SPLIT_TYPES } from '../../utils/splitTypes.js'
import ErrorMessage from '../ErrorMessage.jsx'

export default function ExpenseForm({ members, currentUserId, currency, onSubmit, onCancel }) {
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [date, setDate] = useState(todayIso)
  const [paidById, setPaidById] = useState(currentUserId)
  const [splitType, setSplitType] = useState('EQUAL')
  const [selected, setSelected] = useState(() => new Set(members.map((m) => m.id)))
  const [values, setValues] = useState({})
  const [pending, setPending] = useState(false)
  const [error, setError] = useState(null)

  const participants = members.filter((m) => selected.has(m.id))
  const total = parseDecimal(amount)
  const needsValues = splitType !== 'EQUAL'

  const toggle = (userId) => {
    setSelected((current) => {
      const next = new Set(current)
      if (next.has(userId)) {
        next.delete(userId)
      } else {
        next.add(userId)
      }
      return next
    })
  }

  const validate = () => {
    if (!description.trim()) {
      return 'Escribe una descripción'
    }
    if (!hasAtMostTwoDecimals(amount) || !(total > 0)) {
      return 'El importe debe ser mayor que 0 y tener como mucho 2 decimales'
    }
    if (participants.length === 0) {
      return 'Elige al menos un participante'
    }
    if (splitType === 'AMOUNTS') {
      if (participants.some((m) => !hasAtMostTwoDecimals(values[m.id]) || !(parseDecimal(values[m.id]) > 0))) {
        return 'Indica un importe mayor que 0 para cada participante'
      }
      const assigned = sumOf(participants, values)
      if (toCents(assigned) !== toCents(total)) {
        return `Las partes suman ${formatMoney(assigned, currency)} y el gasto es de ${formatMoney(total, currency)}`
      }
    }
    if (splitType === 'PERCENTAGES') {
      if (participants.some((m) => !(parseDecimal(values[m.id]) > 0))) {
        return 'Indica un porcentaje mayor que 0 para cada participante'
      }
      if (toCents(sumOf(participants, values)) !== 10000) {
        return 'Los porcentajes deben sumar 100'
      }
    }
    return null
  }

  const submit = async (event) => {
    event.preventDefault()
    const problem = validate()
    if (problem) {
      setError({ message: problem })
      return
    }
    setPending(true)
    setError(null)
    try {
      await onSubmit({
        description: description.trim(),
        amount: total,
        paidById,
        date,
        splitType,
        participants: participants.map((m) => (
          needsValues ? { userId: m.id, value: parseDecimal(values[m.id]) } : { userId: m.id }
        )),
      })
    } catch (e) {
      setError(e)
      setPending(false)
    }
  }

  return (
    <form className="card form" onSubmit={submit} noValidate>
      <h2>Nuevo gasto</h2>
      <div className="form__row">
        <label className="field field--grow">
          <span>Descripción</span>
          <input required maxLength={200} placeholder="Cena, gasolina, alquiler…" value={description}
                 onChange={(e) => setDescription(e.target.value)} />
        </label>
        <label className="field">
          <span>Importe ({currency})</span>
          <input inputMode="decimal" placeholder="0,00" value={amount} onChange={(e) => setAmount(e.target.value)} />
        </label>
      </div>
      <div className="form__row">
        <label className="field field--grow">
          <span>Pagado por</span>
          <select value={paidById} onChange={(e) => setPaidById(Number(e.target.value))}>
            {members.map((m) => <option key={m.id} value={m.id}>{m.name}</option>)}
          </select>
        </label>
        <label className="field">
          <span>Fecha</span>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        </label>
      </div>

      <fieldset className="field">
        <legend>Cómo se reparte</legend>
        <div className="segmented">
          {SPLIT_TYPES.map((type) => (
            <label key={type.id} className={`segmented__option ${splitType === type.id ? 'is-active' : ''}`}>
              <input type="radio" name="splitType" value={type.id} checked={splitType === type.id}
                     onChange={() => setSplitType(type.id)} />
              {type.label}
            </label>
          ))}
        </div>
      </fieldset>

      <fieldset className="field">
        <legend>Participantes</legend>
        <ul className="participants">
          {members.map((m) => (
            <li key={m.id}>
              <label className="checkbox">
                <input type="checkbox" checked={selected.has(m.id)} onChange={() => toggle(m.id)} />
                {m.name}
              </label>
              {needsValues && selected.has(m.id) && (
                <span className="participants__value">
                  <input inputMode="decimal" aria-label={`Valor de ${m.name}`}
                         placeholder={splitType === 'PERCENTAGES' ? '%' : '0,00'} value={values[m.id] ?? ''}
                         onChange={(e) => setValues((current) => ({ ...current, [m.id]: e.target.value }))} />
                  <span>{splitType === 'PERCENTAGES' ? '%' : currency}</span>
                </span>
              )}
            </li>
          ))}
        </ul>
        <p className="hint">{splitHint(splitType, participants, values, total, currency)}</p>
      </fieldset>

      <ErrorMessage error={error} />
      <div className="form__actions">
        <button type="button" className="btn btn-ghost" onClick={onCancel}>Cancelar</button>
        <button type="submit" className="btn btn-primary" disabled={pending}>
          {pending ? 'Guardando…' : 'Guardar gasto'}
        </button>
      </div>
    </form>
  )
}

function sumOf(participants, values) {
  return participants.reduce((sum, m) => sum + (parseDecimal(values[m.id]) || 0), 0)
}

function splitHint(splitType, participants, values, total, currency) {
  const validTotal = total > 0 ? total : 0
  if (splitType === 'EQUAL') {
    return participants.length > 0 && validTotal > 0
      ? `Unos ${formatMoney(validTotal / participants.length, currency)} por persona. Los céntimos sobrantes se asignan a los primeros.`
      : 'Se reparte a partes iguales entre los participantes marcados.'
  }
  const sum = sumOf(participants, values)
  if (splitType === 'AMOUNTS') {
    return `Asignado ${formatMoney(sum, currency)} de ${formatMoney(validTotal, currency)}`
  }
  return `Total: ${Number(sum.toFixed(2))} % de 100 %`
}
