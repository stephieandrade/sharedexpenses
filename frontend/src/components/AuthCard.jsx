import Logo from './Logo.jsx'

export default function AuthCard({ title, children }) {
  return (
    <main className="auth">
      <div className="auth__brand">
        <Logo size={44} />
        <h1>SharedExpenses</h1>
        <p>Reparte gastos en grupo y salda las deudas con el mínimo de pagos.</p>
      </div>
      <section className="card auth__card">
        <h2>{title}</h2>
        {children}
      </section>
    </main>
  )
}
