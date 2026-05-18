import apiClient from './axios'

export const reservationApi = {
  getAll:      (params)        => apiClient.get('/reservations', { params }),
  getById:     (id)            => apiClient.get(`/reservations/${id}`),
  getArrivals: (hotelId, date) => apiClient.get('/reservations/arrivals', { params: { hotelId, date } }),
  getDepartures:(hotelId, date)=> apiClient.get('/reservations/departures', { params: { hotelId, date } }),
  create:      (data)          => apiClient.post('/reservations', data),
  confirm:     (id)            => apiClient.patch(`/reservations/${id}/confirm`),
  checkIn:     (id)            => apiClient.patch(`/reservations/${id}/check-in`),
  checkOut:    (id)            => apiClient.patch(`/reservations/${id}/check-out`),
  cancel:      (id, reason)    => apiClient.patch(`/reservations/${id}/cancel`, { reason }),
}
