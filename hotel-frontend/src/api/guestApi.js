import apiClient from './axios'

export const guestApi = {
  getAll:          (params)    => apiClient.get('/guests', { params }),
  getById:         (id)        => apiClient.get(`/guests/${id}`),
  create:          (data)      => apiClient.post('/guests', data),
  update:          (id, data)  => apiClient.put(`/guests/${id}`, data),
  delete:          (id)        => apiClient.delete(`/guests/${id}`),
  addLoyaltyPoints:(id, points)=> apiClient.patch(`/guests/${id}/loyalty-points`, { points }),
}
