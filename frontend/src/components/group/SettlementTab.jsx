import { useState } from 'react'
import { api } from '../../api/api.js'
import { useConfirm } from '../../confirm/useConfirm.js'
import { formatDate, formatMoney } from '../../utils/format.js'
import ErrorMessage from '../ErrorMessage.jsx'
import PaymentForm from './PaymentForm.jsx'

export default function SettlementTab({
  group, settlement, payments, balances, currency, currentUserId, nameOf, onChanged,
}) {
  const confirm = useConfirm()
  const [pendingKey, setPendingKey] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [error, setError] = useState(null)

  const markAsPaid = async (payment) => {
    const key = `${payment.fromUserId}-${payment.toUserId}`
    setPendingKey(key)
    setError(null)
    try {
      await api.registerPayment(group.id, payment)
      await onChanged()
    } catch (e) {
      setError(e)
    } finally {
      setPendingKey(null)
    }
  }

  const registerOther = async (payment) => {
    await api.registerPayment(group.id, payment)
    setShowForm(false)
    await onChanged()
  }

  const removePayment = async (payment) => {
    const accepted = await confirm({
      title: '¿Eliminar este pago?',
      message: `${nameOf(payment.fromUserId)} → ${nameOf(payment.toUserId)}, ${formatMoney(payment.amount, currency)}. `
        + 'La deuda que saldaba volverá a aparecer en la liquidación.',
      confirmLabel: 'Eliminar pago',
      danger: true,
    })
    if (!accepted) {
      return
    }
    setError(null)
    try {
      await api.deletePayment(group.id, payment.id)
      await onChanged()
    } catch (e) {
      setError(e)
    }
  }

  return (
    <section className="stack">
      <div className="section-header">
        <p className="muted">El mínimo número de pagos para que todo el mundo quede en paz.</p>
        {!showForm && (
          <button type="button" className="btn btn-secondary" onClick={() => setShowForm(true)}>
            Registrar otro pago
          </button>
        )}
      </div>

      {showForm && (
        <PaymentForm members={group.members} balances={balances} nameOf={nameOf} currentUserId={currentUserId}
                     currency={currency}
                     onSubmit={registerOther} onCancel={() => setShowForm(false)} />
      )}
      <ErrorMessage error={error} />

      {settlement.length === 0 ? (
        <div className="card empty">
          <h2>¡Todo saldado!</h2>
          <p className="muted">No hay deudas pendientes en este grupo.</p>
        </div>
      ) : (
        <ul className="list">
          {settlement.map((payment) => {
            const key = `${payment.fromUserId}-${payment.toUserId}`
            return (
              <li key={key} className="card settlement">
                <p>
                  <strong>{nameOf(payment.fromUserId)}</strong> paga{' '}
                  <strong className="amount">{formatMoney(payment.amount, currency)}</strong> a{' '}
                  <strong>{nameOf(payment.toUserId)}</strong>
                </p>
                <button type="button" className="btn btn-primary btn-small" disabled={pendingKey !== null}
                        onClick={() => markAsPaid(payment)}>
                  {pendingKey === key ? 'Registrando…' : 'Marcar como pagado'}
                </button>
              </li>
            )
          })}
        </ul>
      )}

      <h2 className="section-title">Pagos registrados</h2>
      {payments.length === 0 ? (
        <p className="muted">Todavía no se ha registrado ningún pago.</p>
      ) : (
        <ul className="card list payments">
          {payments.map((payment) => (
            <li key={payment.id} className="payment">
              <div>
                <p>
                  {nameOf(payment.fromUserId)} pagó{' '}
                  <strong className="amount">{formatMoney(payment.amount, currency)}</strong> a{' '}
                  {nameOf(payment.toUserId)}
                </p>
                <p className="muted">{formatDate(payment.date)}</p>
              </div>
              <button type="button" className="btn btn-ghost btn-small btn-danger-text"
                      onClick={() => removePayment(payment)}>
                Eliminar
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
