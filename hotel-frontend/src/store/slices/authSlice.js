import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { authApi } from '../../api/authApi'

// ─── Thunks ──────────────────────────────────────────────────────────────────

export const loginUser = createAsyncThunk('auth/login', async (credentials, { rejectWithValue }) => {
  try {
    const { data } = await authApi.login(credentials)
    localStorage.setItem('accessToken',  data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    return data
  } catch (err) {
    return rejectWithValue(err.response?.data?.detail || 'Login failed')
  }
})

export const refreshAccessToken = createAsyncThunk('auth/refresh', async (_, { getState, rejectWithValue }) => {
  try {
    const refreshToken = localStorage.getItem('refreshToken')
    if (!refreshToken) return rejectWithValue('No refresh token')
    const { data } = await authApi.refresh(refreshToken)
    localStorage.setItem('accessToken',  data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    return data
  } catch (err) {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    return rejectWithValue('Session expired')
  }
})

export const logoutUser = createAsyncThunk('auth/logout', async (_, { getState }) => {
  try {
    const refreshToken = localStorage.getItem('refreshToken')
    if (refreshToken) await authApi.logout(refreshToken)
  } finally {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }
})

// ─── Slice ───────────────────────────────────────────────────────────────────

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user:            null,
    accessToken:     null,
    refreshToken:    null,
    isAuthenticated: false,
    loading:         false,
    error:           null,
  },
  reducers: {
    rehydrateAuth(state) {
      const token   = localStorage.getItem('accessToken')
      const refresh = localStorage.getItem('refreshToken')
      if (token) {
        state.accessToken     = token
        state.refreshToken    = refresh
        state.isAuthenticated = true
        // Decode user from token payload (no verification needed here — server validates)
        try {
          const payload = JSON.parse(atob(token.split('.')[1]))
          state.user = { email: payload.sub, roles: payload.roles }
        } catch {}
      }
    },
    clearError(state) { state.error = null },
  },
  extraReducers: (builder) => {
    builder
      // Login
      .addCase(loginUser.pending,   (s) => { s.loading = true; s.error = null })
      .addCase(loginUser.fulfilled, (s, { payload }) => {
        s.loading         = false
        s.isAuthenticated = true
        s.accessToken     = payload.accessToken
        s.refreshToken    = payload.refreshToken
        s.user            = payload.user
      })
      .addCase(loginUser.rejected,  (s, { payload }) => { s.loading = false; s.error = payload })
      // Refresh
      .addCase(refreshAccessToken.fulfilled, (s, { payload }) => {
        s.accessToken  = payload.accessToken
        s.refreshToken = payload.refreshToken
      })
      .addCase(refreshAccessToken.rejected, (s) => {
        s.isAuthenticated = false; s.user = null; s.accessToken = null; s.refreshToken = null
      })
      // Logout
      .addCase(logoutUser.fulfilled, (s) => {
        s.isAuthenticated = false; s.user = null; s.accessToken = null; s.refreshToken = null
      })
  },
})

export const { rehydrateAuth, clearError } = authSlice.actions
export default authSlice.reducer

// Selectors
export const selectCurrentUser      = (state) => state.auth.user
export const selectIsAuthenticated  = (state) => state.auth.isAuthenticated
export const selectUserRoles        = (state) => state.auth.user?.roles ?? []
export const selectAuthLoading      = (state) => state.auth.loading
export const selectAuthError        = (state) => state.auth.error
export const selectAccessToken      = (state) => state.auth.accessToken
