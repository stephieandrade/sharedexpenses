import { useEffect, useState } from 'react'

const SLOW_AFTER_MS = 4000

export function useSlowRequestHint(pending) {
  const [slow, setSlow] = useState(false)

  useEffect(() => {
    if (!pending) {
      return undefined
    }
    const timer = setTimeout(() => setSlow(true), SLOW_AFTER_MS)
    return () => {
      clearTimeout(timer)
      setSlow(false)
    }
  }, [pending])

  return slow
}
