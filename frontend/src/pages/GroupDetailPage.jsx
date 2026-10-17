import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router'
import { api } from '../api/api.js'
import { useAuth } from '../auth/useAuth.js'
import ErrorMessage from '../components/ErrorMessage.jsx'
import Loading from '../components/Loading.jsx'
import BalancesTab from '../components/group/BalancesTab.jsx'
import ExpensesTab from '../components/group/ExpensesTab.jsx'
import MembersTab from '../components/group/MembersTab.jsx'
import SettlementTab from '../components/group/SettlementTab.jsx'
import { balanceClass, formatMoney } from '../utils/format.js'

const TABS = [
  { id: 'expenses', label: 'Gastos' },
  { id: 'balances', label: 'Balances' },
  { id: 'settlement', label: 'Liquidación' },
  { id: 'members', label: 'Miembros' },
]

export default function GroupDetailPage() {
  const { groupId } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)

  const requestedTab = searchParams.get('tab')
  const tab = TABS.some((t) => t.id === requestedTab) ? requestedTab : TABS[0].id

  const fetchGroupData = useCallback(async () => {
    const [group, expenses, balances, settlement] = await Promise.all([
      api.getGroup(groupId),
      api.listExpenses(groupId),
      api.getBalances(groupId),
      api.getSettlement(groupId),
    ])
    return { group, expenses, balances, settlement }
  }, [groupId])

  useEffect(() => {
    let active = true
    fetchGroupData().then(
      (loaded) => {
        if (active) {
          setData(loaded)
          setError(null)
        }
      },
      (e) => active && setError(e),
    )
    return () => {
      active = false
    }
  }, [fetchGroupData])

  const reload = useCallback(async () => {
    try {
      setData(await fetchGroupData())
      setError(null)
    } catch (e) {
      setError(e)
    }
  }, [fetchGroupData])

  const nameOf = useMemo(() => {
    const names = new Map((data?.group.members ?? []).map((m) => [m.id, m.name]))
    return (userId) => {
      const name = names.get(userId) ?? 'Antiguo miembro'
      return userId === user.id ? `${name} (tú)` : name
    }
  }, [data, user.id])

  if (!data) {
    return error ? (
      <div className="empty">
        <ErrorMessage error={error} />
        <Link to="/groups" className="btn btn-primary">Volver a mis grupos</Link>
      </div>
    ) : <Loading />
  }

  const { group, expenses, balances, settlement } = data
  const myBalance = balances.find((b) => b.userId === user.id)
  const props = { group, currency: group.currency, currentUserId: user.id, nameOf, onChanged: reload }

  return (
    <>
      <Link to="/groups" className="back-link">← Mis grupos</Link>
      <div className="page-header">
        <div>
          <h1>{group.name}</h1>
          {group.description && <p className="muted">{group.description}</p>}
        </div>
        {myBalance && (
          <div className={`balance-pill ${balanceClass(myBalance.amount)}`}>
            <span>Tu balance</span>
            <strong>{formatMoney(myBalance.amount, group.currency)}</strong>
          </div>
        )}
      </div>

      <nav className="tabs" aria-label="Secciones del grupo">
        {TABS.map((t) => (
          <button key={t.id} type="button" className={`tab ${t.id === tab ? 'tab--active' : ''}`}
                  aria-current={t.id === tab ? 'page' : undefined}
                  onClick={() => setSearchParams({ tab: t.id }, { replace: true })}>
            {t.label}
            {t.id === 'expenses' && <span className="tab__count">{expenses.length}</span>}
            {t.id === 'settlement' && settlement.length > 0 && <span className="tab__count">{settlement.length}</span>}
          </button>
        ))}
      </nav>

      <ErrorMessage error={error} />
      {tab === 'expenses' && <ExpensesTab {...props} expenses={expenses} />}
      {tab === 'balances' && <BalancesTab {...props} balances={balances} />}
      {tab === 'settlement' && <SettlementTab {...props} settlement={settlement} />}
      {tab === 'members' && (
        <MembersTab {...props} balances={balances} onLeft={() => navigate('/groups', { replace: true })} />
      )}
    </>
  )
}
