import { useState } from 'react'
import { api } from '../../api/api.js'
import { formatDate, formatMoney } from '../../utils/format.js'
import { splitTypeLabel } from '../../utils/splitTypes.js'
import ErrorMessage from '../ErrorMessage.jsx'
import ExpenseForm from './ExpenseForm.jsx'

export default function ExpensesTab({ group, expenses, currency, currentUserId, nameOf, onChanged }) {
  const [showForm, setShowForm] = useState(false)
  const [error, setError] = useState(null)
  const total = expenses.reduce((sum, expense) => sum + Number(expense.amount), 0)

  const create = async (expense) => {
    await api.createExpense(group.id, expense)
    setShowForm(false)
    await onChanged()
  }

  const remove = async (expense) => {
    if (!window.confirm(`¿Eliminar el gasto "${expense.description}"?`)) {
      return
    }
    setError(null)
    try {
      await api.deleteExpense(group.id, expense.id)
      await onChanged()
    } catch (e) {
      setError(e)
    }
  }

  return (
    <section className="stack">
      <div className="section-header">
        <p className="muted">
          {expenses.length} {expenses.length === 1 ? 'gasto' : 'gastos'} · Total {formatMoney(total, currency)}
        </p>
        {!showForm && (
          <button type="button" className="btn btn-primary" onClick={() => setShowForm(true)}>
            Añadir gasto
          </button>
        )}
      </div>

      {showForm && (
        <ExpenseForm members={group.members} currentUserId={currentUserId} currency={currency}
                     onSubmit={create} onCancel={() => setShowForm(false)} />
      )}
      <ErrorMessage error={error} />

      {expenses.length === 0 && !showForm && (
        <div className="card empty">
          <h2>Sin gastos todavía</h2>
          <p className="muted">Añade el primero: quién pagó, cuánto y entre quién se reparte.</p>
        </div>
      )}

      <ul className="list">
        {expenses.map((expense) => (
          <li key={expense.id} className="card expense">
            <div className="expense__main">
              <div>
                <h3>{expense.description}</h3>
                <p className="muted">
                  {formatDate(expense.date)} · Pagó {nameOf(expense.paidById)} · {splitTypeLabel(expense.splitType)}
                </p>
              </div>
              <strong className="expense__amount">{formatMoney(expense.amount, currency)}</strong>
            </div>
            <div className="expense__footer">
              <details>
                <summary>Ver reparto ({expense.shares.length})</summary>
                <ul className="shares">
                  {expense.shares.map((share) => (
                    <li key={share.userId}>
                      <span>{nameOf(share.userId)}</span>
                      <span>{formatMoney(share.amount, currency)}</span>
                    </li>
                  ))}
                </ul>
              </details>
              <button type="button" className="btn btn-ghost btn-small btn-danger-text"
                      onClick={() => remove(expense)}>
                Eliminar
              </button>
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}
