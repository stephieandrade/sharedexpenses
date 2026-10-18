import { useState } from 'react'
import { api } from '../../api/api.js'
import { useConfirm } from '../../confirm/useConfirm.js'
import { formatDate, formatMoney } from '../../utils/format.js'
import { splitTypeLabel } from '../../utils/splitTypes.js'
import ErrorMessage from '../ErrorMessage.jsx'
import ExpenseForm from './ExpenseForm.jsx'

function deleteMessage(expense, expenseCount, paymentCount, currency) {
  const base = `El gasto de ${formatMoney(expense.amount, currency)} desaparecerá y los balances se recalcularán.`
  if (paymentCount === 0) {
    return base
  }
  const payments = paymentCount === 1 ? 'el pago registrado' : `los ${paymentCount} pagos registrados`
  if (expenseCount === 1) {
    return `${base} Como es el último gasto del grupo, también se eliminará ${payments}.`
  }
  const others = paymentCount === 1 ? 'Hay 1 pago registrado' : `Hay ${paymentCount} pagos registrados`
  return `${base} ${others} en el grupo: si alguno correspondía a este gasto, elimínalo en Liquidación.`
}

export default function ExpensesTab({ group, expenses, payments, currency, currentUserId, nameOf, onChanged }) {
  const confirm = useConfirm()
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [error, setError] = useState(null)
  const total = expenses.reduce((sum, expense) => sum + Number(expense.amount), 0)
  const editNotice = payments.length > 0
    ? 'Los pagos registrados no cambian al editar un gasto. Revísalos en Liquidación si hace falta.'
    : null

  const create = async (expense) => {
    await api.createExpense(group.id, expense)
    setShowForm(false)
    await onChanged()
  }

  const update = async (expenseId, changes) => {
    await api.updateExpense(group.id, expenseId, changes)
    setEditingId(null)
    await onChanged()
  }

  const remove = async (expense) => {
    const accepted = await confirm({
      title: `¿Eliminar "${expense.description}"?`,
      message: deleteMessage(expense, expenses.length, payments.length, currency),
      confirmLabel: 'Eliminar gasto',
      danger: true,
    })
    if (!accepted) {
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
        {expenses.map((expense) => expense.id === editingId ? (
          <li key={expense.id}>
            <ExpenseForm expense={expense} members={group.members} currentUserId={currentUserId}
                         currency={currency} notice={editNotice}
                         onSubmit={(changes) => update(expense.id, changes)} onCancel={() => setEditingId(null)} />
          </li>
        ) : (
          <li key={expense.id} className="card expense">
            <div className="expense__main">
              <div>
                <h3>{expense.description}</h3>
                <p className="muted">
                  {formatDate(expense.date)} · Pagó {nameOf(expense.paidById)} · {splitTypeLabel(expense.splitType)}
                  {expense.createdById && expense.createdById !== expense.paidById && (
                    <> · Añadido por {nameOf(expense.createdById)}</>
                  )}
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
              <div className="expense__actions">
                <button type="button" className="btn btn-ghost btn-small" onClick={() => setEditingId(expense.id)}>
                  Editar
                </button>
                <button type="button" className="btn btn-ghost btn-small btn-danger-text"
                        onClick={() => remove(expense)}>
                  Eliminar
                </button>
              </div>
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}
