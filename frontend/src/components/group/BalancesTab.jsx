import { balanceClass, formatMoney } from '../../utils/format.js'

export default function BalancesTab({ balances, currency, nameOf }) {
  const largest = Math.max(...balances.map((b) => Math.abs(Number(b.amount))), 0)

  return (
    <section className="stack">
      <p className="muted">
        Positivo: el grupo le debe dinero. Negativo: debe dinero al grupo. La suma de todos los balances es siempre 0.
      </p>
      <ul className="card list balances">
        {balances.map((balance) => {
          const tone = balanceClass(balance.amount)
          const width = largest > 0 ? (Math.abs(Number(balance.amount)) / largest) * 100 : 0
          return (
            <li key={balance.userId} className="balance">
              <div className="balance__row">
                <span>{nameOf(balance.userId)}</span>
                <strong className={`amount ${tone}`}>{formatMoney(balance.amount, currency)}</strong>
              </div>
              <div className="balance__track" aria-hidden="true">
                <div className={`balance__bar ${tone}`} style={{ width: `${width}%` }} />
              </div>
              <small className="muted">{balanceText(tone)}</small>
            </li>
          )
        })}
      </ul>
    </section>
  )
}

function balanceText(tone) {
  if (tone === 'positive') {
    return 'Le deben dinero'
  }
  return tone === 'negative' ? 'Debe dinero' : 'Está en paz'
}
