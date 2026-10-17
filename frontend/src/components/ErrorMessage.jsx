export default function ErrorMessage({ error }) {
  if (!error) {
    return null
  }
  const fieldMessages = Object.values(error.fieldErrors ?? {})

  return (
    <div className="alert alert-error" role="alert">
      <p>{error.message}</p>
      {fieldMessages.length > 0 && (
        <ul>
          {fieldMessages.map((message) => (
            <li key={message}>{message}</li>
          ))}
        </ul>
      )}
    </div>
  )
}
