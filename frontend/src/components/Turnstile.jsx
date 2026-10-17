import { useEffect, useRef } from 'react'

const SCRIPT_URL = 'https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit'

let scriptPromise = null

function loadTurnstile() {
  if (window.turnstile) {
    return Promise.resolve(window.turnstile)
  }
  if (!scriptPromise) {
    scriptPromise = new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.src = SCRIPT_URL
      script.async = true
      script.onload = () => resolve(window.turnstile)
      script.onerror = () => {
        scriptPromise = null
        reject(new Error('No se ha podido cargar la verificación anti-bots'))
      }
      document.head.appendChild(script)
    })
  }
  return scriptPromise
}

export default function Turnstile({ siteKey, onToken, onError }) {
  const containerRef = useRef(null)
  const callbacks = useRef({ onToken, onError })

  useEffect(() => {
    callbacks.current = { onToken, onError }
  })

  useEffect(() => {
    let widgetId = null
    let cancelled = false

    loadTurnstile()
      .then((turnstile) => {
        if (cancelled || !containerRef.current) {
          return
        }
        widgetId = turnstile.render(containerRef.current, {
          sitekey: siteKey,
          language: 'es',
          callback: (token) => callbacks.current.onToken(token),
          'expired-callback': () => callbacks.current.onToken(null),
          'error-callback': () => {
            callbacks.current.onToken(null)
            callbacks.current.onError?.()
          },
        })
      })
      .catch(() => callbacks.current.onError?.())

    return () => {
      cancelled = true
      if (widgetId !== null) {
        window.turnstile?.remove(widgetId)
      }
    }
  }, [siteKey])

  return <div ref={containerRef} className="turnstile" />
}
