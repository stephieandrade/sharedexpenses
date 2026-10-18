import { useContext } from 'react'
import { ConfirmContext } from './confirmContext.js'

export function useConfirm() {
  return useContext(ConfirmContext)
}
