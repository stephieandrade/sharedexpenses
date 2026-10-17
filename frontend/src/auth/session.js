const STORAGE_KEY = 'sharedexpenses.session'

export const SESSION_EXPIRED_EVENT = 'sharedexpenses:session-expired'

export function readSession() {
  try {
    const session = JSON.parse(localStorage.getItem(STORAGE_KEY))
    if (session?.token && new Date(session.expiresAt) > new Date()) {
      return session
    }
  } catch {
    return null
  }
  return null
}

export function saveSession(session) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
}

export function clearSession() {
  localStorage.removeItem(STORAGE_KEY)
}
