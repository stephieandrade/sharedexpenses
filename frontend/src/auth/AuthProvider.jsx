import { useCallback, useEffect, useMemo, useState } from 'react'
import { api } from '../api/api.js'
import { AuthContext } from './authContext.js'
import { SESSION_EXPIRED_EVENT, clearSession, readSession, saveSession } from './session.js'

export default function AuthProvider({ children }) {
  const [session, setSession] = useState(readSession)

  useEffect(() => {
    const onExpired = () => setSession(null)
    window.addEventListener(SESSION_EXPIRED_EVENT, onExpired)
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, onExpired)
  }, [])

  const start = useCallback((newSession) => {
    saveSession(newSession)
    setSession(newSession)
  }, [])

  const login = useCallback(async (email, password) => start(await api.login(email, password)), [start])

  const register = useCallback(
    async (name, email, password, captchaToken) => start(await api.register(name, email, password, captchaToken)),
    [start],
  )

  const logout = useCallback(() => {
    clearSession()
    setSession(null)
  }, [])

  const value = useMemo(
    () => ({ user: session?.user ?? null, login, register, logout }),
    [session, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
