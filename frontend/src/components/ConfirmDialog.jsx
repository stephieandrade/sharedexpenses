import { useEffect, useRef } from 'react'

export default function ConfirmDialog({
  title,
  message,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  danger = false,
  onClose,
}) {
  const dialogRef = useRef(null)

  useEffect(() => {
    const dialog = dialogRef.current
    dialog.showModal()
    return () => {
      if (dialog.open) {
        dialog.close()
      }
    }
  }, [])

  const cancel = (event) => {
    event.preventDefault()
    onClose(false)
  }

  const closeOnBackdrop = (event) => {
    if (event.target === dialogRef.current) {
      onClose(false)
    }
  }

  return (
    <dialog ref={dialogRef} className="dialog" aria-labelledby="confirm-dialog-title"
            onCancel={cancel} onClick={closeOnBackdrop}>
      <div className="dialog__body">
        <h2 id="confirm-dialog-title">{title}</h2>
        {message && <p className="muted">{message}</p>}
        <div className="form__actions">
          <button type="button" className="btn btn-ghost" autoFocus onClick={() => onClose(false)}>
            {cancelLabel}
          </button>
          <button type="button" className={`btn ${danger ? 'btn-danger' : 'btn-primary'}`}
                  onClick={() => onClose(true)}>
            {confirmLabel}
          </button>
        </div>
      </div>
    </dialog>
  )
}
