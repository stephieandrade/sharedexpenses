import { useCallback, useState } from 'react'
import ConfirmDialog from '../components/ConfirmDialog.jsx'
import { ConfirmContext } from './confirmContext.js'

export default function ConfirmProvider({ children }) {
  const [request, setRequest] = useState(null)

  const confirm = useCallback(
    (options) => new Promise((resolve) => setRequest({ ...options, resolve })),
    [],
  )

  const close = (accepted) => {
    request.resolve(accepted)
    setRequest(null)
  }

  return (
    <ConfirmContext.Provider value={confirm}>
      {children}
      {request && (
        <ConfirmDialog title={request.title} message={request.message} confirmLabel={request.confirmLabel}
                       danger={request.danger} onClose={close} />
      )}
    </ConfirmContext.Provider>
  )
}
