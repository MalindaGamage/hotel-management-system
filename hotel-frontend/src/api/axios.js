import axios from 'axios'
import { refreshAccessToken, logoutUser } from '../store/slices/authSlice'

const apiClient = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
})

// Store injected from main.jsx after Redux store is created — breaks the circular:
// store/index.js → authSlice → authApi → axios → store (would be circular at init time)
let _store = null
export const injectStore = (store) => { _store = store }

// Attach Authorization header from Redux store
apiClient.interceptors.request.use((config) => {
  const token = _store?.getState().auth.accessToken
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// On 401: attempt one token refresh, then retry; on second 401, logout
let isRefreshing = false
let failedQueue  = []

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => error ? prom.reject(error) : prom.resolve(token))
  failedQueue = []
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config

    if (error.response?.status === 401 && !original._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          original.headers.Authorization = `Bearer ${token}`
          return apiClient(original)
        })
      }

      original._retry = true
      isRefreshing = true

      try {
        const result = await _store.dispatch(refreshAccessToken())
        if (refreshAccessToken.fulfilled.match(result)) {
          const newToken = result.payload.accessToken
          processQueue(null, newToken)
          original.headers.Authorization = `Bearer ${newToken}`
          return apiClient(original)
        } else {
          processQueue(error)
          _store.dispatch(logoutUser())
          window.location.href = '/login'
        }
      } catch (refreshError) {
        processQueue(refreshError)
        _store?.dispatch(logoutUser())
        window.location.href = '/login'
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default apiClient
