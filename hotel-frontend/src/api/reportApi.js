import apiClient from './axios'

export const reportApi = {
  getDashboard: (params) => apiClient.get('/reports/dashboard', { params }),
}
