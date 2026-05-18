import apiClient from './axios'

export const housekeepingApi = {
  getTasks:   (params)    => apiClient.get('/housekeeping/tasks', { params }),
  createTask: (data)      => apiClient.post('/housekeeping/tasks', data),
  updateTask: (id, data)  => apiClient.patch(`/housekeeping/tasks/${id}`, data),
}
