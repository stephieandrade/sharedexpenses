export const SPLIT_TYPES = [
  { id: 'EQUAL', label: 'A partes iguales' },
  { id: 'AMOUNTS', label: 'Por importes' },
  { id: 'PERCENTAGES', label: 'Por porcentajes' },
]

export function splitTypeLabel(id) {
  return SPLIT_TYPES.find((type) => type.id === id)?.label ?? id
}
