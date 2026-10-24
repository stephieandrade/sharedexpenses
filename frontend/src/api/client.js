import { SESSION_EXPIRED_EVENT, clearSession, readSession } from '../auth/session.js'

const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')

export class ApiError extends Error {
  constructor(status, message, fieldErrors = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

const PUBLIC_PATHS = ['/api/auth/']

export async function request(method, path, body, extraHeaders = {}) {
  const headers = { ...extraHeaders }
  const isPublic = PUBLIC_PATHS.some((prefix) => path.startsWith(prefix))
  const token = isPublic ? null : readSession()?.token
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  let response
  try {
    response = await fetch(`${API_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new ApiError(0, 'No se puede conectar con el servidor. Inténtalo de nuevo en unos segundos')
  }

  if (response.status === 204) {
    return null
  }
  const data = await response.json().catch(() => null)
  if (!response.ok) {
    if (response.status === 401 && token) {
      clearSession()
      window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT))
    }
    throw new ApiError(response.status, data?.detail ?? 'Ha ocurrido un error inesperado', data?.errors ?? {})
  }
  return data
}
