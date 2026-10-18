import { request } from './client.js'

export const api = {
  register: (name, email, password, captchaToken) => request(
    'POST',
    '/api/auth/register',
    { name, email, password },
    captchaToken ? { 'X-Captcha-Token': captchaToken } : {},
  ),
  login: (email, password) => request('POST', '/api/auth/login', { email, password }),

  changePassword: (currentPassword, newPassword) =>
    request('PUT', '/api/users/me/password', { currentPassword, newPassword }),
  deleteAccount: (password) => request('DELETE', '/api/users/me', { password }),

  listGroups: () => request('GET', '/api/groups'),
  createGroup: (group) => request('POST', '/api/groups', group),
  getGroup: (groupId) => request('GET', `/api/groups/${groupId}`),
  updateGroup: (groupId, group) => request('PUT', `/api/groups/${groupId}`, group),
  deleteGroup: (groupId) => request('DELETE', `/api/groups/${groupId}`),
  addMember: (groupId, email) => request('POST', `/api/groups/${groupId}/members`, { email }),
  removeMember: (groupId, memberId) => request('DELETE', `/api/groups/${groupId}/members/${memberId}`),

  listExpenses: (groupId) => request('GET', `/api/groups/${groupId}/expenses`),
  createExpense: (groupId, expense) => request('POST', `/api/groups/${groupId}/expenses`, expense),
  updateExpense: (groupId, expenseId, expense) =>
    request('PUT', `/api/groups/${groupId}/expenses/${expenseId}`, expense),
  deleteExpense: (groupId, expenseId) => request('DELETE', `/api/groups/${groupId}/expenses/${expenseId}`),

  getBalances: (groupId) => request('GET', `/api/groups/${groupId}/balances`),
  getSettlement: (groupId) => request('GET', `/api/groups/${groupId}/settlement`),
  listPayments: (groupId) => request('GET', `/api/groups/${groupId}/payments`),
  registerPayment: (groupId, payment) => request('POST', `/api/groups/${groupId}/payments`, payment),
  deletePayment: (groupId, paymentId) => request('DELETE', `/api/groups/${groupId}/payments/${paymentId}`),
}
