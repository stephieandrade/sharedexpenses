import { useState } from 'react'
import { api } from '../../api/api.js'
import { formatMoney } from '../../utils/format.js'
import ErrorMessage from '../ErrorMessage.jsx'
import PaymentForm from './PaymentForm.jsx'

export default function SettlementTab({ group, settlement, currency, currentUserId, nameOf, onChanged }) {
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
        <PaymentForm members={group.members} currentUserId={currentUserId} currency={currency}
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
    </section>
  )
}
