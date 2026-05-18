import apiClient from './axios'

export const roomApi = {
  getAll:           (params)       => apiClient.get('/rooms', { params }),
  getRoomTypes:     (params)       => apiClient.get('/room-types', { params }),
  getById:          (id)           => apiClient.get(`/rooms/${id}`),
  checkAvailability:(params)       => apiClient.get('/rooms/availability', { params }),
  create:           (data)         => apiClient.post('/rooms', data),
  update:           (id, data)     => apiClient.put(`/rooms/${id}`, data),
  updateStatus:     (id, data)     => apiClient.patch(`/rooms/${id}/status`, data),
  delete:           (id)           => apiClient.delete(`/rooms/${id}`),
  createRoomType:   (data)         => apiClient.post('/room-types', data),
}
