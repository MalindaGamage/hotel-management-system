import apiClient from './axios'

export const billingApi = {
  getInvoices:    (params)              => apiClient.get('/billing/invoices', { params }),
  getInvoice:     (id)                  => apiClient.get(`/billing/invoices/${id}`),
  createInvoice:  (reservationId)       => apiClient.post('/billing/invoices', { reservationId }),
  issueInvoice:   (id)                  => apiClient.patch(`/billing/invoices/${id}/issue`),
  processPayment: (data, idempotencyKey)=> apiClient.post('/billing/payments', data, {
    headers: { 'X-Idempotency-Key': idempotencyKey }
  }),
}
